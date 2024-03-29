package com.jayden.tutorial.springrestapi.controller;

import com.jayden.tutorial.springrestapi.common.AppProperties;
import com.jayden.tutorial.springrestapi.common.TestDescription;
import com.jayden.tutorial.springrestapi.domain.account.Account;
import com.jayden.tutorial.springrestapi.domain.account.AccountRole;
import com.jayden.tutorial.springrestapi.domain.account.AccountService;
import com.jayden.tutorial.springrestapi.domain.account.infra.AccountRepository;
import com.jayden.tutorial.springrestapi.domain.event.Event;
import com.jayden.tutorial.springrestapi.domain.event.EventDto;
import com.jayden.tutorial.springrestapi.domain.event.EventStatus;
import com.jayden.tutorial.springrestapi.domain.event.infra.EventRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTest extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;

    @Before
    public void setUp() {
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto eventDto = EventDto.builder()
            .name("Spring")
            .description("REST API Development with Spring")
            .beginEnrollmentDateTime(LocalDateTime.of(2019, 10, 1, 0, 0, 0))
            .closeEnrollmentDateTime(LocalDateTime.of(2019, 10, 2, 23, 59, 59))
            .beginEventDateTime(LocalDateTime.of(2019, 11, 1, 10, 0, 0))
            .endEventDateTime(LocalDateTime.of(2019, 11, 2, 18, 0, 0))
            .basePrice(100)
            .maxPrice(200)
            .limitOfEnrollment(100)
            .location("마곡역 이매너스")
            .build();

        mockMvc.perform(post("/api/events/")
            .header(HttpHeaders.AUTHORIZATION, getBearerToken())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaTypes.HAL_JSON)
            .content(objectMapper.writeValueAsString(eventDto)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("id").exists())
            .andExpect(header().exists(HttpHeaders.LOCATION))
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath("free").value(false))
            .andExpect(jsonPath("offline").value(true))
            .andDo(document("create-event",
                links(
                    linkWithRel("self").description("link to self"),
                    linkWithRel("query-events").description("link to query events"),
                    linkWithRel("update-event").description("link to update a existing event"),
                    linkWithRel("profile").description("link to profile")
                ),
                requestHeaders(
                    headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                    headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                ),
                requestFields(
                    fieldWithPath("name").description("Name of new event"),
                    fieldWithPath("description").description("description of new event"),
                    fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                    fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                    fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                    fieldWithPath("endEventDateTime").description("date time of end of new event"),
                    fieldWithPath("location").description("location of new event"),
                    fieldWithPath("basePrice").description("base price of new event"),
                    fieldWithPath("maxPrice").description("max price of new event"),
                    fieldWithPath("limitOfEnrollment").description("limit of enrollment")
                ),
                responseHeaders(
                    headerWithName(HttpHeaders.LOCATION).description("Location header"),
                    headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                ),
                relaxedResponseFields(
                    fieldWithPath("id").description("id of new event"),
                    fieldWithPath("name").description("Name of new event"),
                    fieldWithPath("description").description("description of new event"),
                    fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                    fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                    fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                    fieldWithPath("endEventDateTime").description("date time of end of new event"),
                    fieldWithPath("location").description("location of new event"),
                    fieldWithPath("basePrice").description("base price of new event"),
                    fieldWithPath("maxPrice").description("max price of new event"),
                    fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
                    fieldWithPath("free").description("it tells if this event is free or not"),
                    fieldWithPath("offline").description("it tells if this event is offline event or not"),
                    fieldWithPath("eventStatus").description("event status"),
                    fieldWithPath("_links.self.href").description("link to self"),
                    fieldWithPath("_links.query-events.href").description("link to query events"),
                    fieldWithPath("_links.update-event.href").description("link to update a existing event"),
                    fieldWithPath("_links.profile.href").description("link to profile")
                )
            ));
    }

    private String getBearerToken() throws Exception {
        return getBearerToken(true);
    }

    private String getBearerToken(boolean needToCreateAccount) throws Exception {
        return "Bearer " + getAccessToken(needToCreateAccount);
    }

    private String getAccessToken(boolean needToCreateAccount) throws Exception {
        if (needToCreateAccount) {
            this.createAccount();
        }

        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
            .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
            .param("username", appProperties.getUserUsername())
            .param("password", appProperties.getUserPassword())
            .param("grant_type", "password"))
            .andExpect(status().isOk());

        MockHttpServletResponse response = perform.andReturn().getResponse();
        String resultString = response.getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();

        return parser.parseMap(resultString).get("access_token").toString();
    }

    private Account createAccount() {
        Account account = Account.builder()
            .email(appProperties.getUserUsername())
            .password(appProperties.getUserPassword())
            .roles(Set.of(AccountRole.USER))
            .build();

        return this.accountService.saveAccount(account);
    }

    @Test
    @TestDescription("입력 값이 비어있는 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        mockMvc.perform(post("/api/events")
            .header(HttpHeaders.AUTHORIZATION, getBearerToken())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(objectMapper.writeValueAsString(eventDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
            .name("Spring")
            .description("REST API Development with Spring")
            .beginEnrollmentDateTime(LocalDateTime.of(2019, 10, 1, 0, 0, 0))
            .closeEnrollmentDateTime(LocalDateTime.of(2019, 10, 2, 23, 59, 59))
            .beginEventDateTime(LocalDateTime.of(2019, 11, 1, 10, 0, 0))
            .endEventDateTime(LocalDateTime.of(2019, 10, 2, 18, 0, 0))
            .basePrice(200)
            .maxPrice(100)
            .limitOfEnrollment(100)
            .location("마곡역 이매너스")
            .build();

        mockMvc.perform(post("/api/events")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .header(HttpHeaders.AUTHORIZATION, getBearerToken())
            .content(objectMapper.writeValueAsString(eventDto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("content[0].objectName").exists())
            .andExpect(jsonPath("content[0].defaultMessage").exists())
            .andExpect(jsonPath("content[0].code").exists())
            .andExpect(jsonPath("_links.index").exists());
    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩 두 번째 페이지 조회하기")
    public void queryEvents() throws Exception {
        // given
        IntStream.range(0, 30).forEach(this::generateEvents);

        // when & then
        this.mockMvc.perform(get("/api/events")
            .param("page", "1")
            .param("size", "10")
            .param("sort", "name,DESC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("page").exists())
            .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
            .andExpect(jsonPath("_links.self").exists())
            .andExpect(jsonPath("_links.profile").exists())
            .andDo(document("get-events"));
        // TODO 문서화
    }

    @Test
    @TestDescription("인증 정보를 가지고 30개의 이벤트를 10개씩 첫 번째 페이지 조회하기")
    public void queryEventsWithAuthentication() throws Exception {
        // given
        IntStream.range(0, 30).forEach(this::generateEvents);

        // when & then
        this.mockMvc.perform(get("/api/events")
            .param("page", "0")
            .param("size", "10")
            .param("sort", "name,DESC")
            .header(HttpHeaders.AUTHORIZATION, getBearerToken()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("page").exists())
            .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
            .andExpect(jsonPath("_links.self").exists())
            .andExpect(jsonPath("_links.profile").exists())
            .andExpect(jsonPath("_links.create-event").exists())
            .andDo(document("get-events"));
        // TODO 문서화
    }

    private Event generateEvents(int index) {
        Event event = buildEvent(index);
        return this.eventRepository.save(event);
    }

    private Event generateEvents(int index, Account account) {
        Event event = buildEvent(index);
        event.setManager(account);
        return this.eventRepository.save(event);
    }

    private Event buildEvent(int index) {
        return Event.builder()
            .name("event " + index)
            .description("test event")
            .beginEnrollmentDateTime(LocalDateTime.of(2019, 10, 1, 0, 0, 0))
            .closeEnrollmentDateTime(LocalDateTime.of(2019, 10, 2, 23, 59, 59))
            .beginEventDateTime(LocalDateTime.of(2019, 11, 1, 10, 0, 0))
            .endEventDateTime(LocalDateTime.of(2019, 11, 2, 18, 0, 0))
            .basePrice(100)
            .maxPrice(200)
            .limitOfEnrollment(100)
            .location("마곡역 이매너스")
            .free(false)
            .offline(true)
            .eventStatus(EventStatus.DRAFT)
            .build();
    }

    @Test
    @TestDescription("기존의 이벤트 단건 조회하기")
    public void getEvent() throws Exception {
        // given
        Account account = this.createAccount();
        Event event = this.generateEvents(100, account);

        // when & then
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").exists())
            .andExpect(jsonPath("name").exists())
            .andExpect(jsonPath("_links.self").exists())
            .andExpect(jsonPath("_links.profile").exists())
            .andDo(document("get-event"));
        // TODO 문서화
    }

    @Test
    @TestDescription("없는 이벤트 단건 조회할 때 404 응답받기")
    public void getEvent404() throws Exception {
        // when & then
        this.mockMvc.perform(get("/api/events/11231"))
            .andExpect(status().isNotFound());
    }

    @Test
    @TestDescription("이벤트를 정상적으로 수정하기")
    public void updateEvent() throws Exception {
        // given
        Account account = this.createAccount();
        Event event = this.generateEvents(100, account);

        EventDto eventDto = modelMapper.map(event, EventDto.class);
        String eventName = "Updated Event Name";
        eventDto.setName(eventName);

        // when & then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
            .header(HttpHeaders.AUTHORIZATION, getBearerToken(false))
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(this.objectMapper.writeValueAsString(eventDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("name").value(eventName))
            .andExpect(jsonPath("_links.self").exists())
            .andDo(document("update-event"));
    }

    @Test
    @TestDescription("입력값이 비어있는 경우에 이벤트 수정 실패 테스트")
    public void updateEvent400Empty() throws Exception {
        // given
        Event event = this.generateEvents(100);

        EventDto eventDto = new EventDto();

        // when & then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
            .header(HttpHeaders.AUTHORIZATION, getBearerToken())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(this.objectMapper.writeValueAsString(eventDto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 잘못된 경우에 이벤트 수정 실패 테스트")
    public void updateEvent400Wrong() throws Exception {
        // given
        Event event = this.generateEvents(100);

        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        // when & then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
            .header(HttpHeaders.AUTHORIZATION, getBearerToken())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(this.objectMapper.writeValueAsString(eventDto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("존재하지 않는 이벤트 수정 실패 테스트")
    public void updateEvent404() throws Exception {
        // given
        Event event = this.generateEvents(100);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);

        // when & then
        this.mockMvc.perform(put("/api/events/123123")
            .header(HttpHeaders.AUTHORIZATION, getBearerToken())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(this.objectMapper.writeValueAsString(eventDto)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}