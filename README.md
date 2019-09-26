# Spring REST API Study Repo
> 인프런 스프링 기반 REST API 개발 강좌를 학습하고 정리한 내용입니다

## Prerequisites
- Installing Java 11 and MySQL 5.7

## Dependencies List
1. Web
2. JPA
3. HATEOAS
4. REST Docs
5. H2
6. MySQL
7. Lombok

## REST (REpresentational State Transfer)
> REST API 학습 관련하여 참고하면 좋은 영상 [그런 REST API로 괜찮은가](https://www.youtube.com/watch?v=RP_f5dMoHFc)

- 인터넷에서 서로 다른 시스템 간의 상호 운용성을 제공하는 방법 중 하나
- 시스템 제각각의 독립적인 진화를 보장하기 위한 방법
- REST 아키텍처 스타일을 따르는 API
- REST는 분산 하이퍼미디어 시스템(예:웹)을 위한 아키텍처 스타일

### REST를 구성하는 스타일
HTTP 프로토콜을 사용하기 때문에 REST를 구성하는 스타일을 대부분 지키고 있다. 그중에서 <code>uniform interface</code>를
잘 지키지 못하고 있다.

- client-server
- stateless
- cache
- **uniform interface**
- layered system
- code-on-demand (optional)

### Uniform Interface 제약 조건
- identification of resources
- manipulation of resources through representations
- **self-descriptive messages**
- **hypermedia as the engine of application state (HATEOAS)**

#### self-descriptive messages
- 메시지는 스스로를 설명해야한다
- 서버가 전송하는 메시지를 클라이언트는 메시지만 보고 해석이 가능해야 한다
- profile 링크 헤더를 추가한다 (대안으로 HAL의 링크 데이터에 profile 링크를 추가)

```
HTTP/1.1 200 OK
Content-Type: application/json-path+json

[{ "op": "remove", "path": "/a/b/c" }]
```

#### HATEOAS
- 애플리케이션의 상태는 Hyperlink를 이용해 전이되어야한다
- 링크 정보를 동적으로 변경할 수 있다

```
HTTP/1.1 200 OK
Content-Type: text/html

<html>
<head></head>
<body><a href="/test">test</a></body>
</html>
```

### 왜 Uniform Interfaces를 지켜야 하는가?
- 클라이언트와 서버가 각각 독립적으로 진화한다
- **서버의 기능이 변경되어도 클라이언트를 업데이트할 필요가 없다**
- REST를 만들게 된 계기