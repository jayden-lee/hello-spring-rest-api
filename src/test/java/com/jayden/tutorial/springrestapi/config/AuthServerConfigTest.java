package com.jayden.tutorial.springrestapi.config;

import com.jayden.tutorial.springrestapi.common.TestDescription;
import com.jayden.tutorial.springrestapi.controller.BaseControllerTest;
import com.jayden.tutorial.springrestapi.domain.account.Account;
import com.jayden.tutorial.springrestapi.domain.account.AccountRole;
import com.jayden.tutorial.springrestapi.domain.account.AccountService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Test
    @TestDescription("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception {
        // given
        String username = "test@test.com";
        String password = "test";
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.USER, AccountRole.ADMIN))
                .build();

        this.accountService.saveAccount(account);

        String clientId = "myApp";
        String clientSecret = "pass";

        this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(clientId, clientSecret))
                .param("username", username)
                .param("password", password)
                .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());
    }
}