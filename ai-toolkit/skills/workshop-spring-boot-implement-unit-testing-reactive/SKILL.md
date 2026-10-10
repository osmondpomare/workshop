---
name: workshop-spring-boot-implement-unit-testing-reactive
description: >-
  Expert Spring Boot 4 reactive testing specialist that selects the best testing techniques
  for each Scaffold Clean Architecture sub-module. Provides comprehensive testing patterns
  for reactive Spring Boot applications: domain/model (plain JUnit), domain/usecase
  (Mockito + StepVerifier), infrastructure/driven-adapters/r2dbc (DatabaseClient mocks +
  StepVerifier), infrastructure/driven-adapters/rest-consumer (MockWebServer + StepVerifier),
  infrastructure/entry-points/reactive-web (WebTestClient bound directly to the router function —
  no Spring context), and applications/app-service (circuit-breaker fallback logic tested directly,
  with no Spring context; only the circuit-breaker state machine itself needs a tagged, separate
  integration test with full context).
license: MIT
compatibility: Java 25, Spring Boot 4.x, Spring WebFlux, Project Reactor, JUnit 5
metadata:
  author: criscast
  version: 1.0.0
  based-on: spring-boot-implement-unit-testing
---

# Spring Boot 4 Reactive Unit Testing Specialist

## 1. When to Use

Use this skill when:
- Writing tests for a Spring Boot 4.x reactive application using the Scaffold Clean Architecture
- Testing reactive pipelines (Mono/Flux) with StepVerifier
- Testing WebFlux entry points (routers/handlers) with `WebTestClient` bound directly to the router function — **no Spring context**
- Testing R2DBC adapters with DatabaseClient mocks
- Testing REST consumers with MockWebServer
- Testing circuit-breaker fallback logic directly (no Spring context); reserving `@SpringBootTest` only for the rare, explicitly-tagged integration test that verifies the circuit-breaker state machine itself
- Migrating tests from blocking (WebMvc) to reactive (WebFlux)

## 2. Target Stack

| Component | Version |
|---|---|
| Java | 25 |
| Spring Boot | 4.x (Spring Framework 7) |
| Web Framework | Spring WebFlux |
| Reactive Library | Project Reactor |
| Test Framework | JUnit 5 (Jupiter) |
| Mocking | Mockito 5+ |
| Reactive Testing | reactor-test (StepVerifier) |
| HTTP Testing | MockWebServer (OkHttp3) |
| Build Tool | Gradle 9.2.1+ |
| Architecture | Scaffold Clean Architecture (reactive) |

## 3. Context

You are an expert Spring Boot 4 reactive testing specialist. You understand:
- The Scaffold Clean Architecture sub-module structure and which test style applies to each
- Project Reactor testing patterns (StepVerifier, TestPublisher)
- Testing WebFlux routers/handlers by binding `WebTestClient` directly to the `RouterFunction` (`bindToRouterFunction`), or by invoking the handler method directly with a manually built `ServerRequest` — **without** `@WebFluxTest`, `@ContextConfiguration`, or any other Spring context
- R2DBC DatabaseClient mock chain patterns
- MockWebServer for real HTTP testing of WebClient adapters
- Testing circuit-breaker fallback logic directly via Resilience4j's `FallbackMethod` (no Spring context), and knowing when a real circuit-breaker state-machine test genuinely requires `@SpringBootTest` — always tagged as an integration test, never the default
- The distinction between blocking and reactive test approaches

## 4. Quick Reference Table

| Sub-Module | Test Style | Key Tools | Spring Context |
|---|---|---|---|
| `domain/model` | Plain JUnit 5 | assertEquals, assertNotNull | ❌ None |
| `domain/usecase` | Mockito + StepVerifier | @Mock, StepVerifier, @ParameterizedTest | ❌ None |
| `driven-adapters/r2dbc` | Mockito + StepVerifier | Mock DatabaseClient chain, BiFunction holder | ❌ None |
| `driven-adapters/rest-consumer` | MockWebServer + StepVerifier | MockWebServer, real WebClient | ❌ None |
| `entry-points/reactive-web` | `WebTestClient.bindToRouterFunction(...)` | @Mock, jsonPath, no `@MockitoBean` needed | ❌ None |
| `applications/app-service` (fallback logic) | Direct handler call + `FallbackMethod` | @Mock, StepVerifier | ❌ None |
| `applications/app-service` (circuit-breaker state machine) | `@SpringBootTest` + WebTestClient | Real `CircuitBreakerRegistry`, tagged `integration-test` | ✅ Full (rare, tagged, run separately) |

## 5. Quick Decision Tree (Reactive)

```
What sub-module are you testing?
│
├─ domain/model?
│  → Plain JUnit 5, no reactive, no Spring, no Mockito
│
├─ domain/usecase?
│  → @ExtendWith(MockitoExtension.class) + @Mock gateways + StepVerifier
│
├─ infrastructure/driven-adapters/r2dbc?
│  → Mock DatabaseClient chain + BiFunction holder + StepVerifier
│
├─ infrastructure/driven-adapters/rest-consumer?
│  → MockWebServer + real WebClient + StepVerifier
│
├─ infrastructure/entry-points/reactive-web?
│  → `WebTestClient.bindToRouterFunction(routerFunction)` + `@Mock` use case — NO Spring context
│
└─ applications/app-service?
   ├─ Testing what the fallback method returns for a given exception?
   │  → Instantiate the handler directly + build `ServerRequest` via `MockServerHttpRequest`/
   │    `MockServerWebExchange` + invoke via Resilience4j's `FallbackMethod` — NO Spring context
   │
   └─ Testing that the circuit breaker actually opens/half-opens/closes after N real calls?
      → `@SpringBootTest(RANDOM_PORT)` + real `CircuitBreakerRegistry` — full context, but
        tag the test `@Tag("integration-test")` and keep it out of the default unit-test run
```

## 6. Core Principles

1. **Match test style to sub-module** — Domain tests are plain JUnit; infrastructure tests mock collaborators; entry-point tests bind `WebTestClient` directly to the router function, with no Spring context at all.
2. **StepVerifier for ALL reactive assertions** — Never call `.block()` in tests. Always verify reactive streams with StepVerifier.
3. **No Spring context by default, anywhere** — `domain`, `driven-adapters`, and `entry-points` unit tests never load Spring context. `app-service` doesn't either, **except** for the rare, explicitly-tagged integration test that verifies the circuit breaker's actual state machine (opens/half-opens/closes over real calls) — that one test class needs `@SpringBootTest`, everything else does not.
4. **Real HTTP for REST consumers** — Use MockWebServer to test the actual HTTP serialization/deserialization, not just mock the WebClient.
5. **Test the reactive pipeline, not just the result** — Verify error signals, empty signals, completion, and backpressure behavior.
6. **Use @MockitoBean (NOT @MockBean)** — `@MockBean` is removed in Spring Boot 4.0. Use `@MockitoBean` from `org.springframework.test.context.bean.override.mockito`.
7. **Isolate concerns** — Each test class tests ONE class. Mock all external dependencies.

## 7. Testing Patterns Per Sub-Module

---

### 7.1 domain/model — Plain JUnit 5

**Purpose:** Test domain models, exceptions, enums, value objects, and business rules with zero dependencies.

**Gradle dependencies (`domain/model/build.gradle`):**
```groovy
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter'
}
```

**Pattern: Domain Exception Test**
```java
package co.com.company.model.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TechnicalExceptionTest {

    @Test
    void shouldCreateExceptionWithTechnicalMessage() {
        TechnicalException exception = new TechnicalException(TechnicalMessage.INTERNAL_ERROR);

        assertNotNull(exception);
        assertEquals(TechnicalMessage.INTERNAL_ERROR, exception.getTechnicalMessage());
        assertEquals("BAL-500", exception.getTechnicalMessage().getCode());
        assertEquals("Internal server error", exception.getTechnicalMessage().getMessage());
    }

    @Test
    void shouldCreateExceptionWithCustomMessage() {
        TechnicalException exception = new TechnicalException(
            TechnicalMessage.SERVICE_UNAVAILABLE, "Authentication service timeout"
        );

        assertNotNull(exception);
        assertEquals("Authentication service timeout", exception.getDetail());
        assertEquals("BAL-503", exception.getTechnicalMessage().getCode());
    }

    @Test
    void shouldCreateBusinessException() {
        BusinessException exception = new BusinessException(BusinessMessage.INVALID_PHONE_NUMBER);

        assertNotNull(exception);
        assertEquals("11-62L", exception.getBusinessMessage().getCode());
    }
}
```

**Pattern: Domain Enum Test**
```java
package co.com.company.model.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

class TransactionStatusTest {

    @Test
    void shouldHaveExpectedValues() {
        assertEquals(4, TransactionStatus.values().length);
        assertNotNull(TransactionStatus.PENDING);
        assertNotNull(TransactionStatus.COMPLETED);
        assertNotNull(TransactionStatus.FAILED);
        assertNotNull(TransactionStatus.REVERSED);
    }

    @ParameterizedTest
    @EnumSource(TransactionStatus.class)
    void shouldHaveNonNullCode(TransactionStatus status) {
        assertNotNull(status.getCode());
        assertFalse(status.getCode().isBlank());
    }
}
```

**Pattern: Value Object Test**
```java
package co.com.company.model.balance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BalanceTest {

    @Test
    void shouldBuildBalanceWithAllFields() {
        Balance balance = Balance.builder()
            .balanceAmount("6276834.77")
            .availableAmount("5000000.00")
            .numPockets("0")
            .build();

        assertEquals("6276834.77", balance.getBalanceAmount());
        assertEquals("5000000.00", balance.getAvailableAmount());
        assertEquals("0", balance.getNumPockets());
    }

    @Test
    void shouldHaveDefaultValuesWhenNotSet() {
        Balance balance = Balance.builder().build();

        assertNull(balance.getBalanceAmount());
        assertNull(balance.getNumPockets());
    }
}
```

---

### 7.2 domain/usecase — Mockito + StepVerifier

**Purpose:** Test use case business logic by mocking gateway interfaces and verifying reactive streams.

**Gradle dependencies (`domain/usecase/build.gradle`):**
```groovy
dependencies {
    implementation project(':domain:model')
    testImplementation 'io.projectreactor:reactor-test'
    testImplementation 'org.mockito:mockito-junit-jupiter'
}
```

**Pattern: Use Case Success and Error Paths**
```java
package co.com.company.usecase.authentication;

import co.com.company.model.authentication.*;
import co.com.company.model.exceptions.AuthenticationException;
import co.com.company.model.gateway.AuthenticationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationUseCaseTest {

    @Mock
    private AuthenticationRepository authenticationRepository;

    @InjectMocks
    private AuthenticationUseCase authenticationUseCase;

    private AuthenticationUser authUser;

    @BeforeEach
    void setUp() {
        authUser = AuthenticationUser.builder()
            .phoneNumber("3998703022")
            .password("encryptedPass")
            .channel("APP")
            .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "11-62L"})
    void shouldAuthenticateUserSuccessfully(String successCode) {
        AuthenticationResult expectedResult = AuthenticationResult.builder()
            .code(successCode)
            .info("auth-info")
            .build();

        when(authenticationRepository.authenticateUser(any(AuthenticationUser.class)))
            .thenReturn(Mono.just(expectedResult));

        StepVerifier.create(authenticationUseCase.authenticateUser(authUser))
            .expectNextMatches(result -> {
                assertEquals(successCode, result.getCode());
                assertEquals("auth-info", result.getInfo());
                return true;
            })
            .verifyComplete();

        verify(authenticationRepository).authenticateUser(any(AuthenticationUser.class));
    }

    @Test
    void shouldFailAuthenticationWithInvalidCode() {
        AuthenticationResult failedResult = AuthenticationResult.builder()
            .code("99")
            .message("Invalid credentials")
            .build();

        when(authenticationRepository.authenticateUser(any()))
            .thenReturn(Mono.just(failedResult));

        StepVerifier.create(authenticationUseCase.authenticateUser(authUser))
            .expectErrorMatches(throwable -> {
                assertInstanceOf(AuthenticationException.class, throwable);
                AuthenticationException ex = (AuthenticationException) throwable;
                assertEquals("99", ex.getCode());
                return true;
            })
            .verify();
    }

    @Test
    void shouldHandleRepositoryError() {
        when(authenticationRepository.authenticateUser(any()))
            .thenReturn(Mono.error(new RuntimeException("Connection timeout")));

        StepVerifier.create(authenticationUseCase.authenticateUser(authUser))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    void shouldHandleEmptyResponse() {
        when(authenticationRepository.authenticateUser(any()))
            .thenReturn(Mono.empty());

        StepVerifier.create(authenticationUseCase.authenticateUser(authUser))
            .verifyComplete();
    }
}
```

**Pattern: Use Case with Flux**
```java
@ExtendWith(MockitoExtension.class)
class GetTransactionsUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GetTransactionsUseCase getTransactionsUseCase;

    @Test
    void shouldReturnMultipleTransactions() {
        var tx1 = Transaction.builder().id("T001").amount("50000").build();
        var tx2 = Transaction.builder().id("T002").amount("120000").build();

        when(transactionRepository.findByPhoneNumber(anyString()))
            .thenReturn(Flux.just(tx1, tx2));

        StepVerifier.create(getTransactionsUseCase.execute("3998703022"))
            .expectNext(tx1)
            .expectNext(tx2)
            .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenNoTransactions() {
        when(transactionRepository.findByPhoneNumber(anyString()))
            .thenReturn(Flux.empty());

        StepVerifier.create(getTransactionsUseCase.execute("3998703022"))
            .expectNextCount(0)
            .verifyComplete();
    }
}
```

---

### 7.3 infrastructure/driven-adapters/r2dbc — DatabaseClient Mocks + StepVerifier

**Purpose:** Test R2DBC adapters by mocking the DatabaseClient chain without a real database.

**Gradle dependencies (`infrastructure/driven-adapters/r2dbc-oracle/build.gradle`):**
```groovy
dependencies {
    implementation project(':domain:model')
    implementation 'org.springframework:spring-r2dbc'
    testImplementation 'io.projectreactor:reactor-test'
    testImplementation 'org.mockito:mockito-junit-jupiter'
}
```

**Pattern: DatabaseClient Mock Chain (Complete)**
```java
package co.com.company.r2dbc.adapter;

import co.com.company.model.balance.Balance;
import tools.jackson.databind.ObjectMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceAdapterTest {

    private BalanceAdapter balanceAdapter;

    @Mock
    private DatabaseClient databaseClient;

    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;

    @Mock
    private FetchSpec<Balance> fetchSpec;

    @Mock
    private Row row;

    @Mock
    private RowMetadata rowMetadata;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        balanceAdapter = new BalanceAdapter(databaseClient, objectMapper);
    }

    @Test
    void shouldGetBalanceSuccessfully() {
        buildMockDBCall();
        String jsonResponse = """
            {
                "numPockets": "0",
                "balanceAmount": "6276834.77",
                "availableAmount": "5000000.00"
            }
            """;
        when(row.get("balance_data", String.class)).thenReturn(jsonResponse);

        StepVerifier.create(balanceAdapter.getBalance("87000", "N"))
            .assertNext(balance -> {
                assertEquals("0", balance.getNumPockets());
                assertEquals("6276834.77", balance.getBalanceAmount());
                assertEquals("5000000.00", balance.getAvailableAmount());
            })
            .verifyComplete();

        verify(databaseClient).sql(anyString());
        verify(executeSpec).bindValues(anyMap());
    }

    @Test
    void shouldFailWithDatabaseError() {
        when(databaseClient.sql(anyString())).thenThrow(new RuntimeException("DB connection failed"));

        StepVerifier.create(balanceAdapter.getBalance("87000", "N"))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    void shouldFailWithMalformedJson() {
        buildMockDBCall();
        when(row.get("balance_data", String.class)).thenReturn("invalid-json");

        StepVerifier.create(balanceAdapter.getBalance("87000", "N"))
            .expectError()
            .verify();
    }

    @Test
    void shouldHandleEmptyResult() {
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bindValues(anyMap())).thenReturn(executeSpec);
        when(executeSpec.map(any(BiFunction.class))).thenReturn(fetchSpec);
        when(fetchSpec.one()).thenReturn(Mono.empty());

        StepVerifier.create(balanceAdapter.getBalance("87000", "N"))
            .verifyComplete();
    }

    /**
     * Builds the mock chain: DatabaseClient → GenericExecuteSpec → FetchSpec → Row
     * Uses BiFunction holder pattern to capture and invoke the row mapper.
     */
    @SuppressWarnings("unchecked")
    private void buildMockDBCall() {
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bindValues(anyMap())).thenReturn(executeSpec);

        final BiFunction<Row, RowMetadata, Balance>[] mapperHolder = new BiFunction[1];

        when(executeSpec.map(any(BiFunction.class))).thenAnswer(invocation -> {
            mapperHolder[0] = invocation.getArgument(0);
            return fetchSpec;
        });

        when(fetchSpec.one()).thenAnswer(invocation ->
            Mono.just(mapperHolder[0].apply(row, rowMetadata))
        );
    }
}
```

**Pattern: DatabaseClient with bind() instead of bindValues()**
```java
@SuppressWarnings("unchecked")
private void buildMockDBCallWithBind() {
    when(databaseClient.sql(anyString())).thenReturn(executeSpec);
    when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
    when(executeSpec.bind(anyInt(), any())).thenReturn(executeSpec);

    final BiFunction<Row, RowMetadata, Object>[] mapperHolder = new BiFunction[1];

    when(executeSpec.map(any(BiFunction.class))).thenAnswer(invocation -> {
        mapperHolder[0] = invocation.getArgument(0);
        return fetchSpec;
    });

    when(fetchSpec.all()).thenAnswer(invocation ->
        Flux.just(mapperHolder[0].apply(row, rowMetadata))
    );
}
```

---

### 7.4 infrastructure/driven-adapters/rest-consumer — MockWebServer + StepVerifier

**Purpose:** Test WebClient-based REST adapters with real HTTP using MockWebServer.

**Gradle dependencies (`infrastructure/driven-adapters/rest-consumer/build.gradle`):**
```groovy
dependencies {
    implementation project(':domain:model')
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    testImplementation 'io.projectreactor:reactor-test'
    testImplementation 'com.squareup.okhttp3:mockwebserver'
}
```

**Pattern: REST Consumer with MockWebServer (Complete)**
```java
package co.com.company.consumer.adapter;

import co.com.company.model.authentication.*;
import co.com.company.model.exceptions.TechnicalException;
import tools.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationAdapterTest {

    private static AuthenticationAdapter authenticationAdapter;
    private static MockWebServer mockBackEnd;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        mapper = new ObjectMapper();

        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(2000));

        WebClient webClient = WebClient.builder()
            .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient))
            .baseUrl(mockBackEnd.url("/").toString())
            .build();

        authenticationAdapter = new AuthenticationAdapter(
            webClient, webClient, mapper, "/api/auth", "/api/validate"
        );
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void shouldAuthenticateUserSuccessfully() throws Exception {
        String responseJson = """
            {
                "code": "0",
                "info": "auth-info",
                "token": "eyJhbGciOiJIUzI1NiJ9..."
            }
            """;

        mockBackEnd.enqueue(new MockResponse()
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setResponseCode(HttpStatus.OK.value())
            .setBody(responseJson));

        AuthenticationUser authUser = AuthenticationUser.builder()
            .phoneNumber("3998703022")
            .password("encrypted")
            .channel("APP")
            .build();

        StepVerifier.create(authenticationAdapter.authenticateUser(authUser))
            .expectNextMatches(result -> {
                assertEquals("0", result.getCode());
                assertEquals("auth-info", result.getInfo());
                return true;
            })
            .verifyComplete();

        RecordedRequest request = mockBackEnd.takeRequest();
        assertEquals("/api/auth", request.getPath());
        assertEquals("POST", request.getMethod());
    }

    @Test
    void shouldHandleBadRequestError() {
        mockBackEnd.enqueue(new MockResponse()
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setResponseCode(HttpStatus.BAD_REQUEST.value())
            .setBody("""
                {"error": "invalid_request", "message": "Missing phone number"}
                """));

        AuthenticationUser authUser = AuthenticationUser.builder()
            .phoneNumber("")
            .build();

        StepVerifier.create(authenticationAdapter.authenticateUser(authUser))
            .expectError(TechnicalException.class)
            .verify();
    }

    @Test
    void shouldHandleServerError() {
        mockBackEnd.enqueue(new MockResponse()
            .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .setBody("Internal Server Error"));

        AuthenticationUser authUser = AuthenticationUser.builder()
            .phoneNumber("3998703022")
            .build();

        StepVerifier.create(authenticationAdapter.authenticateUser(authUser))
            .expectErrorMatches(throwable -> {
                assertInstanceOf(TechnicalException.class, throwable);
                TechnicalException ex = (TechnicalException) throwable;
                assertEquals("BAL-500", ex.getTechnicalMessage().getCode());
                return true;
            })
            .verify();
    }

    @Test
    void shouldHandleTimeout() {
        mockBackEnd.enqueue(new MockResponse()
            .setBodyDelay(5, java.util.concurrent.TimeUnit.SECONDS)
            .setResponseCode(HttpStatus.OK.value()));

        AuthenticationUser authUser = AuthenticationUser.builder()
            .phoneNumber("3998703022")
            .build();

        StepVerifier.create(authenticationAdapter.authenticateUser(authUser))
            .expectError()
            .verify(Duration.ofSeconds(10));
    }

    @Test
    void shouldHandleEmptyResponse() {
        mockBackEnd.enqueue(new MockResponse()
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setResponseCode(HttpStatus.OK.value())
            .setBody(""));

        AuthenticationUser authUser = AuthenticationUser.builder()
            .phoneNumber("3998703022")
            .build();

        StepVerifier.create(authenticationAdapter.authenticateUser(authUser))
            .expectError()
            .verify();
    }

    @Test
    void shouldSendCorrectHeaders() throws Exception {
        mockBackEnd.enqueue(new MockResponse()
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setResponseCode(HttpStatus.OK.value())
            .setBody("""
                {"code": "0", "info": "success"}
                """));

        AuthenticationUser authUser = AuthenticationUser.builder()
            .phoneNumber("3998703022")
            .messageId("msg-001")
            .build();

        StepVerifier.create(authenticationAdapter.authenticateUser(authUser))
            .expectNextCount(1)
            .verifyComplete();

        RecordedRequest request = mockBackEnd.takeRequest();
        assertEquals("application/json", request.getHeader("Content-Type"));
    }
}
```

---

### 7.5 infrastructure/entry-points/reactive-web — WebTestClient bound directly to the RouterFunction (No Spring Context)

**Purpose:** Test WebFlux routers and handlers through the real HTTP contract (routing, JSON (de)serialization, status codes, headers) **without loading any Spring context**. `WebTestClient` can bind directly to a `RouterFunction`, a controller instance, or a `WebHandler` — only `bindToApplicationContext(...)` needs an actual `ApplicationContext`. Since the router in Scaffold Clean Architecture is a plain method that takes the handler as a parameter (`@Bean public RouterFunction<ServerResponse> routerFunction(AuthHandler authHandler) { ... }`), it can be called directly in a test with a mocked handler/use case — no `@WebFluxTest`, no `@ContextConfiguration`, no `@Import`, no `@MockitoBean`.

**Gradle dependencies (`infrastructure/entry-points/reactive-web/build.gradle`):**
```groovy
dependencies {
    implementation project(':domain:model')
    implementation project(':domain:usecase')
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    testImplementation 'org.springframework.boot:spring-boot-starter-test' // WebTestClient, MockServerHttpRequest
    testImplementation 'io.projectreactor:reactor-test'
}
```

**Pattern A (preferred): `bindToRouterFunction` — full HTTP contract, zero Spring context**
```java
package co.com.company.api;

import co.com.company.api.handler.AuthHandler;
import co.com.company.model.authentication.AuthenticationResult;
import co.com.company.model.exceptions.AuthenticationException;
import co.com.company.model.exceptions.BusinessMessage;
import co.com.company.usecase.authentication.AuthenticationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthRouterRestTest {

    @Mock
    private AuthenticationUseCase authenticationUseCase;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        AuthHandler authHandler = new AuthHandler(authenticationUseCase);
        // routerFunction(...) is a plain method on the @Configuration class — call it directly,
        // no @Bean/@Configuration processing, no ApplicationContext involved.
        webTestClient = WebTestClient
                .bindToRouterFunction(new AuthRouterRest().routerFunction(authHandler))
                .build();
    }

    @Test
    void shouldRouteToAuthenticateHandler() {
        AuthenticationResult result = AuthenticationResult.builder()
                .code("0")
                .info("auth-info")
                .build();

        given(authenticationUseCase.authenticateUser(any()))
                .willReturn(Mono.just(result));

        String requestBody = """
            {
                "password": "encryptedPass",
                "channel": "APP"
            }
            """;

        webTestClient.post()
                .uri("/api/v1/security/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-phone-number", "3998703022")
                .header("x-message-id", "msg-001")
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data").exists()
                .jsonPath("$.data.info").isEqualTo("auth-info")
                .jsonPath("$.data.code").isEqualTo("0");
    }

    @Test
    void shouldHandleAuthenticationError() {
        given(authenticationUseCase.authenticateUser(any()))
                .willReturn(Mono.error(new AuthenticationException(BusinessMessage.INVALID_CREDENTIALS, "11-62L")));

        String requestBody = """
            {
                "password": "wrongPass",
                "channel": "APP"
            }
            """;

        webTestClient.post()
                .uri("/api/v1/security/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-phone-number", "3998703022")
                .header("x-message-id", "msg-002")
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0].code").isEqualTo("11-62L")
                .jsonPath("$.errors[0].message").exists();
    }

    @Test
    void shouldHandleMissingRequiredHeaders() {
        String requestBody = """
            {
                "password": "encryptedPass",
                "channel": "APP"
            }
            """;

        webTestClient.post()
                .uri("/api/v1/security/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldHandleInvalidRequestBody() {
        webTestClient.post()
                .uri("/api/v1/security/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-phone-number", "3998703022")
                .header("x-message-id", "msg-003")
                .bodyValue("invalid-json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldHandleTechnicalException() {
        given(authenticationUseCase.authenticateUser(any()))
                .willReturn(Mono.error(new RuntimeException("Unexpected error")));

        String requestBody = """
            {
                "password": "encryptedPass",
                "channel": "APP"
            }
            """;

        webTestClient.post()
                .uri("/api/v1/security/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-phone-number", "3998703022")
                .header("x-message-id", "msg-004")
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
```

> `new AuthRouterRest().routerFunction(authHandler)` and `new AuthHandler(authenticationUseCase)` are plain object construction — `@Configuration`/`@Component` annotations on those classes are only consumed by Spring at runtime; a test that instantiates them directly never triggers component scanning, bean post-processing, or context startup. This is why the whole class needs no `@SpringBootTest`, no `@WebFluxTest`, no `@ContextConfiguration` at all.

**Pattern B: Testing GET endpoints with query parameters — same binding, no context**
```java
@Test
void shouldGetBalanceSuccessfully() {
    given(balanceUseCase.getBalance(anyString(), anyString()))
            .willReturn(Mono.just(balance));

    webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/api/v1/balance")
                    .queryParam("type", "N")
                    .build())
            .header("x-phone-number", "3998703022")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.data.balanceAmount").isEqualTo("6276834.77")
            .jsonPath("$.data.numPockets").isEqualTo("0");
}
```

**Pattern C: Direct handler invocation with a manually built `ServerRequest`** — use this only when you need to call the handler method itself (bypassing routing entirely), e.g. to feed a `ServerRequest` into Resilience4j's `FallbackMethod` for fallback testing (see §7.6):
```java
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;

private ServerRequest buildServerRequest(String jsonBody) {
    MockServerHttpRequest httpRequest = MockServerHttpRequest
            .post("/api/v1/security/authenticate")
            .header("Content-Type", "application/json")
            .header("x-phone-number", "3998703022")
            .body(jsonBody);

    MockServerWebExchange exchange = MockServerWebExchange.from(httpRequest);
    return ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
}
```
`MockServerHttpRequest`/`MockServerWebExchange` are plain WebFlux test utility classes (from `spring-test`) — building one does **not** start Spring, scan components, or create an `ApplicationContext`. They only simulate the HTTP request/exchange objects the handler expects as method parameters.

---

### 7.6 applications/app-service — Circuit-Breaker Fallback Logic (No Context) vs. State-Machine Integration Test (Full Context, Tagged Separately)

**Purpose:** Most of what needs testing about `@CircuitBreaker` is **what the fallback method returns for a given exception** — that is pure logic, testable with no Spring context at all via Resilience4j's own `FallbackMethod` utility. Only the circuit breaker's actual state machine (does it open after N failures? does it half-open and recover?) requires the real AOP interception wired by Spring, and that one scenario should be a separate, explicitly tagged integration test — not the default way to test fallback behavior, and not run as part of the regular unit-test suite.

**Gradle dependencies (`applications/app-service/build.gradle`):**
```groovy
dependencies {
    implementation project(':domain:model')
    implementation project(':domain:usecase')
    implementation project(':infrastructure:driven-adapters:rest-consumer')
    implementation project(':infrastructure:entry-points:reactive-web')
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation "io.github.resilience4j:resilience4j-spring-boot4:${resilience4jVersion}"
    implementation "io.github.resilience4j:resilience4j-reactor:${resilience4jVersion}"

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'io.projectreactor:reactor-test'
}
```

**Pattern A (default, no Spring context): unit-test the fallback method directly with `FallbackMethod`**

The handler's `fallback(...)` overloads are plain methods — invoke them directly via reflection using Resilience4j's `FallbackMethod.create(...)`, exactly the way the `@CircuitBreaker` AOP proxy would, but with a manually-built `ServerRequest`/`ServerResponse` and no Spring context whatsoever:
```java
package co.com.company.api;

import co.com.company.model.enums.TechnicalMessage;
import co.com.company.model.exceptions.TechnicalException;
import co.com.company.usecase.authentication.AuthenticationUseCase;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.fallback.FallbackMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AuthHandlerFallbackTest {

    @Mock
    private AuthenticationUseCase authenticationUseCase;

    private FallbackMethod fallbackMethodFor(AuthHandler target, ServerRequest.Headers headers) throws Exception {
        Method handlerMethod = target.getClass().getMethod("authenticate", ServerRequest.class);
        return FallbackMethod.create("fallback", handlerMethod, new Object[]{headers}, target, target);
    }

    @Test
    void shouldReturnServiceUnavailable_When_CircuitBreakerOpen() throws Throwable {
        AuthHandler target = new AuthHandler(authenticationUseCase);
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("authenticate");

        Mono<ServerResponse> response = (Mono<ServerResponse>) fallbackMethodFor(target, buildRequest().headers())
                .fallback(CallNotPermittedException.createCallNotPermittedException(circuitBreaker));

        StepVerifier.create(response)
                .expectNextMatches(r -> {
                    assertThat(r.statusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnInternalServerError_When_TechnicalException() throws Throwable {
        AuthHandler target = new AuthHandler(authenticationUseCase);

        Mono<ServerResponse> response = (Mono<ServerResponse>) fallbackMethodFor(target, buildRequest().headers())
                .fallback(new TechnicalException(TechnicalMessage.INTERNAL_ERROR));

        StepVerifier.create(response)
                .expectNextMatches(r -> {
                    assertThat(r.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    return true;
                })
                .verifyComplete();
    }

    private ServerRequest buildRequest() {
        MockServerHttpRequest httpRequest = MockServerHttpRequest
                .post("/api/v1/security/authenticate")
                .header("x-phone-number", "3998703022")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(httpRequest);
        return ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
    }
}
```
This covers "what does the fallback return for exception X" with the exact same reflection-based invocation Resilience4j's AOP proxy uses at runtime — no `@SpringBootTest`, no real circuit breaker registry, no waiting for sliding windows or half-open transitions.

**Pattern B (rare, tagged integration test): the circuit breaker's real state machine**

Reserve `@SpringBootTest` for the one scenario Pattern A cannot cover: verifying that the circuit breaker actually transitions OPEN after N real failing calls through the real `CircuitBreakerRegistry`. Tag it so it never runs as part of the default unit-test task, and never use `Thread.sleep(...)` to wait for a half-open transition — reset the registered circuit breaker between tests instead of relying on wall-clock timing:
```java
package co.com.company;

import co.com.company.usecase.authentication.AuthenticationUseCase;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration-test") // excluded from the default unit-test run; execute in its own CI step
class AuthHandlerCircuitBreakerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockitoBean
    private AuthenticationUseCase authenticationUseCase;

    @BeforeEach
    void resetCircuitBreaker() {
        circuitBreakerRegistry.circuitBreaker("authenticate").reset();
    }

    @Test
    void shouldOpenCircuit_When_SlidingWindowFailsCompletely() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("authenticate");
        given(authenticationUseCase.authenticateUser(any()))
                .willReturn(Mono.error(new RuntimeException("Service down")));

        int slidingWindowSize = circuitBreaker.getCircuitBreakerConfig().getSlidingWindowSize();
        for (int i = 0; i < slidingWindowSize; i++) {
            webTestClient.post()
                    .uri("/api/v1/security/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-phone-number", "3998703022")
                    .header("x-message-id", "msg-" + i)
                    .bodyValue("""{"password": "pass", "channel": "APP"}""")
                    .exchange();
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
```
Keep this class alone in its module/build step (e.g. a dedicated Gradle task filtering `useJUnitPlatform { includeTags 'integration-test' }` for `test`, and `excludeTags 'integration-test'` for the default `test` task) so the fast unit-test suite never pays for booting the full application context.

---

## 8. Dependencies Summary (Gradle)

```groovy
// Root build.gradle.kts — common test setup
subprojects {
    tasks.withType(Test) {
        useJUnitPlatform()
    }
}
```

| Sub-Module | Required Test Dependencies |
|---|---|
| `domain/model` | `junit-jupiter` |
| `domain/usecase` | `reactor-test`, `mockito-junit-jupiter` |
| `driven-adapters/r2dbc` | `reactor-test`, `mockito-junit-jupiter` |
| `driven-adapters/rest-consumer` | `reactor-test`, `mockwebserver` |
| `entry-points/reactive-web` | `spring-boot-starter-test`, `reactor-test` |
| `applications/app-service` | `spring-boot-starter-test`, `reactor-test`, `resilience4j-*` |

---

## 9. StepVerifier Patterns Reference

### Basic Assertions
```java
// Expect a single value and complete
StepVerifier.create(mono)
    .expectNext(expectedValue)
    .verifyComplete();

// Expect value matching predicate
StepVerifier.create(mono)
    .expectNextMatches(value -> value.getCode().equals("0"))
    .verifyComplete();

// Assert with full access to value
StepVerifier.create(mono)
    .assertNext(value -> {
        assertEquals("0", value.getCode());
        assertNotNull(value.getInfo());
    })
    .verifyComplete();
```

### Error Assertions
```java
// Expect specific error type
StepVerifier.create(mono)
    .expectError(TechnicalException.class)
    .verify();

// Expect error matching predicate
StepVerifier.create(mono)
    .expectErrorMatches(throwable -> {
        assertInstanceOf(TechnicalException.class, throwable);
        return ((TechnicalException) throwable).getTechnicalMessage().getCode().equals("BAL-500");
    })
    .verify();

// Expect error with message
StepVerifier.create(mono)
    .expectErrorMessage("Connection timeout")
    .verify();

// Expect error satisfying consumer
StepVerifier.create(mono)
    .expectErrorSatisfies(throwable -> {
        assertInstanceOf(AuthenticationException.class, throwable);
        assertEquals("11-62L", ((AuthenticationException) throwable).getCode());
    })
    .verify();
```

### Flux Assertions
```java
// Expect multiple values in order
StepVerifier.create(flux)
    .expectNext(value1, value2, value3)
    .verifyComplete();

// Expect count
StepVerifier.create(flux)
    .expectNextCount(5)
    .verifyComplete();

// Expect sequence matching
StepVerifier.create(flux)
    .expectNextSequence(List.of(value1, value2))
    .verifyComplete();

// Record and assert all values
StepVerifier.create(flux)
    .recordWith(ArrayList::new)
    .thenConsumeWhile(x -> true)
    .consumeRecordedWith(values -> {
        assertEquals(3, values.size());
        assertTrue(values.stream().allMatch(v -> v.getAmount() != null));
    })
    .verifyComplete();
```

### Empty and Timeout
```java
// Expect empty (complete without values)
StepVerifier.create(mono)
    .verifyComplete();

// Verify with timeout
StepVerifier.create(mono)
    .expectNext(value)
    .verifyComplete();

// Expect timeout behavior
StepVerifier.create(mono.timeout(Duration.ofMillis(500)))
    .expectError(java.util.concurrent.TimeoutException.class)
    .verify();
```

### Virtual Time (for delay/interval testing)
```java
StepVerifier.withVirtualTime(() -> Mono.delay(Duration.ofHours(1)).thenReturn("delayed"))
    .expectSubscription()
    .thenAwait(Duration.ofHours(1))
    .expectNext("delayed")
    .verifyComplete();
```

---

## 10. MockWebServer Patterns Reference

### Setup and Teardown
```java
private static MockWebServer mockBackEnd;

@BeforeAll
static void setUp() throws IOException {
    mockBackEnd = new MockWebServer();
    mockBackEnd.start();
}

@AfterAll
static void tearDown() throws IOException {
    mockBackEnd.shutdown();
}
```

### Enqueue Responses
```java
// Success response
mockBackEnd.enqueue(new MockResponse()
    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    .setResponseCode(200)
    .setBody(jsonBody));

// Error response
mockBackEnd.enqueue(new MockResponse()
    .setResponseCode(500)
    .setBody("Internal Server Error"));

// Timeout simulation
mockBackEnd.enqueue(new MockResponse()
    .setBodyDelay(5, TimeUnit.SECONDS)
    .setResponseCode(200));

// Empty response
mockBackEnd.enqueue(new MockResponse()
    .setResponseCode(204));
```

### Verify Requests
```java
RecordedRequest request = mockBackEnd.takeRequest();
assertEquals("POST", request.getMethod());
assertEquals("/api/auth", request.getPath());
assertEquals("application/json", request.getHeader("Content-Type"));
assertTrue(request.getBody().readUtf8().contains("phoneNumber"));
```

### WebClient Configuration for Tests
```java
WebClient webClient = WebClient.builder()
    .clientConnector(new ReactorClientHttpConnector(
        HttpClient.create().responseTimeout(Duration.ofMillis(2000))
    ))
    .baseUrl(mockBackEnd.url("/").toString())
    .build();
```

---

## 11. WebTestClient Patterns Reference

### HTTP Methods
```java
// POST with body
webTestClient.post()
    .uri("/api/v1/resource")
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue(requestBody)
    .exchange()
    .expectStatus().isCreated();

// GET with query params
webTestClient.get()
    .uri(uriBuilder -> uriBuilder
        .path("/api/v1/resource")
        .queryParam("filter", "active")
        .queryParam("page", "1")
        .build())
    .exchange()
    .expectStatus().isOk();

// PUT
webTestClient.put()
    .uri("/api/v1/resource/{id}", resourceId)
    .bodyValue(updateBody)
    .exchange()
    .expectStatus().isOk();

// DELETE
webTestClient.delete()
    .uri("/api/v1/resource/{id}", resourceId)
    .exchange()
    .expectStatus().isNoContent();
```

### Response Body Assertions
```java
// JSON path assertions
.expectBody()
    .jsonPath("$.data").exists()
    .jsonPath("$.data.code").isEqualTo("0")
    .jsonPath("$.data.items").isArray()
    .jsonPath("$.data.items.length()").isEqualTo(3)
    .jsonPath("$.errors").doesNotExist();

// Deserialize response
.expectBody(ResponseDTO.class)
    .value(response -> {
        assertEquals("0", response.getCode());
        assertNotNull(response.getData());
    });

// Raw body assertion
.expectBody(String.class)
    .value(body -> assertTrue(body.contains("success")));
```

### Headers and Status
```java
// Status assertions
.expectStatus().isOk()                    // 200
.expectStatus().isCreated()               // 201
.expectStatus().isNoContent()             // 204
.expectStatus().isBadRequest()            // 400
.expectStatus().isUnauthorized()          // 401
.expectStatus().isForbidden()             // 403
.expectStatus().isNotFound()              // 404
.expectStatus().is5xxServerError()        // 5xx
.expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)  // specific

// Header assertions
.expectHeader().contentType(MediaType.APPLICATION_JSON)
.expectHeader().exists("x-correlation-id")
.expectHeader().valueEquals("x-custom-header", "value")
```

---

## 12. DatabaseClient Mock Chain Pattern

The complete mock chain for R2DBC DatabaseClient testing:

```
DatabaseClient
  └── .sql(anyString())          → returns GenericExecuteSpec (mock)
       └── .bind()/bindValues()  → returns GenericExecuteSpec (self)
            └── .map(BiFunction) → captures mapper, returns FetchSpec (mock)
                 └── .one()      → applies captured mapper to mock Row
                 └── .all()      → applies captured mapper as Flux
                 └── .first()    → applies captured mapper as Mono
```

**Key Insight:** The BiFunction holder pattern allows you to:
1. Capture the row mapper that the adapter passes to `.map()`
2. Invoke it with your mock `Row` and `RowMetadata`
3. Return the mapped result wrapped in `Mono`/`Flux`

```java
@SuppressWarnings("unchecked")
private void buildMockDBCall() {
    when(databaseClient.sql(anyString())).thenReturn(executeSpec);
    when(executeSpec.bindValues(anyMap())).thenReturn(executeSpec);

    final BiFunction<Row, RowMetadata, Object>[] mapperHolder = new BiFunction[1];

    when(executeSpec.map(any(BiFunction.class))).thenAnswer(invocation -> {
        mapperHolder[0] = invocation.getArgument(0);
        return fetchSpec;
    });

    when(fetchSpec.one()).thenAnswer(invocation ->
        Mono.just(mapperHolder[0].apply(row, rowMetadata))
    );
}
```

---

## 13. @ParameterizedTest Patterns

```java
// ValueSource for simple values
@ParameterizedTest
@ValueSource(strings = {"0", "11-62L", "00"})
void shouldAcceptValidCodes(String code) { ... }

// CsvSource for multiple parameters
@ParameterizedTest
@CsvSource({
    "3998703022, APP, 0",
    "3001234567, WEB, 0",
    "invalid, APP, 99"
})
void shouldProcessDifferentScenarios(String phone, String channel, String expectedCode) { ... }

// MethodSource for complex objects
@ParameterizedTest
@MethodSource("provideAuthenticationScenarios")
void shouldHandleMultipleScenarios(AuthenticationUser user, String expectedCode) { ... }

private static Stream<Arguments> provideAuthenticationScenarios() {
    return Stream.of(
        Arguments.of(AuthenticationUser.builder().phoneNumber("300").build(), "0"),
        Arguments.of(AuthenticationUser.builder().phoneNumber("").build(), "99")
    );
}

// EnumSource
@ParameterizedTest
@EnumSource(value = TechnicalMessage.class, names = {"INTERNAL_ERROR", "SERVICE_UNAVAILABLE"})
void shouldMapTechnicalMessages(TechnicalMessage message) { ... }

// NullAndEmptySource
@ParameterizedTest
@NullAndEmptySource
@ValueSource(strings = {" ", "  "})
void shouldRejectInvalidPhoneNumbers(String phone) { ... }
```

---

## 14. Best Practices (Reactive-Specific)

### DO ✅
- **Always use StepVerifier** — never `.block()` in tests
- **Use `@MockitoBean`** (Boot 4.x) instead of deprecated `@MockBean` — only where a Spring context is genuinely unavoidable (the rare tagged circuit-breaker integration test); everywhere else, use plain `@Mock`
- **Bind `WebTestClient` directly to the router function** (`WebTestClient.bindToRouterFunction(...)`) for entry-point tests — no Spring context needed
- **Use MockWebServer** for REST consumer tests (NOT MockRestServiceServer)
- **Test error signals explicitly** — reactive errors propagate differently than exceptions
- **Verify backpressure behavior** when testing Flux publishers
- **Use `verify()` (not `verifyComplete()`) for error expectations** — `verifyComplete()` after error will fail
- **Use `@BeforeAll`/`@AfterAll` (static)** for MockWebServer lifecycle
- **Use real ObjectMapper** (don't mock serialization)
- **Verify interactions** with `verify(mock).method()` after StepVerifier
- **Use text blocks** for JSON test data
- **Keep domain/model tests free of reactive code** — they test pure Java
- **Test circuit-breaker fallback logic via Resilience4j's `FallbackMethod`**, invoked directly against a plain handler instance — no Spring context needed to verify what a fallback returns for a given exception

### DON'T ❌
- Don't use `.block()` — it defeats the purpose of reactive testing
- Don't use `@MockBean` — removed in Spring Boot 4.0
- Don't use `MockMvc` — it's for WebMvc, not WebFlux
- Don't use `@WebFluxTest`/`@ContextConfiguration`/`@Import` for entry-point unit tests — bind `WebTestClient` directly to the router function instead; reaching for a Spring test slice here is unnecessary Spring context for what should be a plain unit test
- Don't use `@WebMvcTest` — this is a reactive stack, and neither WebMvc slice testing nor `@WebFluxTest` are needed for a plain router/handler unit test
- Don't use `MockRestServiceServer` — it's for RestTemplate/RestClient
- Don't use `@DataJpaTest` — R2DBC uses DatabaseClient mocks
- Don't reach for `@SpringBootTest` to test what a `@CircuitBreaker` fallback method returns — invoke it directly via `FallbackMethod.create(...)`; reserve `@SpringBootTest` only for the one tagged integration test that verifies the real circuit-breaker state machine
- Don't load full Spring context for unit tests — only for the explicitly tagged (`@Tag("integration-test")`) circuit-breaker state-machine test, run separately from the default unit-test task
- Don't use `Thread.sleep(...)` to wait for a circuit breaker to transition half-open — reset the registered `CircuitBreaker` between tests instead
- Don't mock `Mono`/`Flux` — return real reactive types from mocks: `Mono.just()`, `Mono.error()`
- Don't use `Mono.block()` to extract values for assertions
- Don't ignore `.verifyComplete()` — always verify stream completion
- Don't mix blocking and reactive in tests

### Test Data Management
```java
// Use @BeforeEach for test data setup
@BeforeEach
void setUp() {
    authUser = AuthenticationUser.builder()
        .phoneNumber("3998703022")
        .password("encrypted")
        .channel("APP")
        .build();
}

// Use text blocks for JSON
String requestBody = """
    {
        "password": "encryptedPass",
        "channel": "APP"
    }
    """;

// Use builders for domain objects
AuthenticationResult.builder()
    .code("0")
    .info("auth-info")
    .build();
```

---

## 15. Test Coverage Goals

| Sub-Module | Target Coverage | Priority |
|---|---|---|
| `domain/model` | 90%+ | Exceptions, enums, value objects |
| `domain/usecase` | 95%+ | All business logic paths |
| `driven-adapters/r2dbc` | 85%+ | Happy path, DB errors, empty results |
| `driven-adapters/rest-consumer` | 85%+ | Success, 4xx, 5xx, timeouts, empty |
| `entry-points/reactive-web` | 90%+ | Happy path, errors, validation, missing headers |
| `applications/app-service` | 70%+ | Circuit breaker, fallback, health |

### What to Test per Module
- **domain/model**: Exception constructors, enum values, builder patterns, validation logic
- **domain/usecase**: Success path, error path, edge cases, parameterized scenarios
- **driven-adapters/r2dbc**: Successful query, DB error, empty result, malformed data
- **driven-adapters/rest-consumer**: Success response, 4xx errors, 5xx errors, timeouts, empty body
- **entry-points/reactive-web**: Happy path, business errors, technical errors, missing headers, invalid body
- **applications/app-service**: Circuit breaker open/closed, fallback responses, recovery

---

## 16. Post-Implementation Validation Checklist

After writing tests, verify:

- [ ] All tests pass: `./gradlew test`
- [ ] **Spring context starts: `./gradlew :app-service:bootRun` reaches `Started MainApplication`.**
  `./gradlew test` and `build -x test` compile and run slice tests but do NOT boot the full
  `app-service` context, so they never catch bean-wiring errors (duplicate bean names, missing
  `WebClient.Builder`, missing `ObjectMapper`, Spring-Boot-3-only libraries like
  `resilience4j-spring-boot3`). Always run `bootRun` once as the final verification; stop it after
  the `Started MainApplication` log line.
- [ ] No `.block()` calls in test assertions (use StepVerifier)
- [ ] No `@MockBean` usage (replaced with `@MockitoBean`, and only in the one tagged integration test that needs it)
- [ ] No `MockMvc` usage (replaced with WebTestClient)
- [ ] No `@WebMvcTest`/`@WebFluxTest` (entry-point tests bind `WebTestClient` directly to the router function instead)
- [ ] domain/model tests have NO reactive imports
- [ ] domain/usecase tests have NO Spring context annotations
- [ ] driven-adapters tests have NO Spring context annotations
- [ ] entry-points tests have NO Spring context annotations (`WebTestClient.bindToRouterFunction(...)` or direct handler invocation with `MockServerHttpRequest`/`MockServerWebExchange`)
- [ ] circuit-breaker fallback logic is tested via `FallbackMethod.create(...)` against a plain handler instance — NO Spring context
- [ ] the only test class using `@SpringBootTest` is the circuit-breaker state-machine test, and it is tagged `@Tag("integration-test")` and excluded from the default `test` task
- [ ] no `Thread.sleep(...)` anywhere in tests (including the tagged integration test) — reset the `CircuitBreaker` between tests instead
- [ ] StepVerifier chains end with `.verifyComplete()` or `.verify()`
- [ ] MockWebServer is started in `@BeforeAll` and shutdown in `@AfterAll`
- [ ] Error paths are tested (not just happy paths)
- [ ] `reactor-test` dependency is declared where StepVerifier is used
- [ ] Test class names match source class + `Test` suffix
- [ ] Test methods use descriptive names: `shouldDoX_WhenY()`

---

## 17. Constraints and Warnings

### Hard Constraints
| Constraint | Reason |
|---|---|
| `@MockitoBean` only, and only in the tagged integration test | `@MockBean` removed in Spring Boot 4.0; every other test uses plain `@Mock` |
| No `@WebFluxTest`/`@WebMvcTest` for entry-point tests | Bind `WebTestClient` directly to the router function — a Spring test slice is unnecessary context for a router/handler unit test |
| `WebTestClient` only | `MockMvc` is for Spring MVC (blocking) |
| `StepVerifier` always | Never use `.block()` for reactive assertions |
| `MockWebServer` for REST | `MockRestServiceServer` is for RestTemplate/RestClient |
| No `@DataJpaTest` | R2DBC adapters use DatabaseClient mocks |
| `reactor-test` required | Without it, StepVerifier is not available |
| domain/model = plain JUnit | No reactive, no Spring, no Mockito |
| `@SpringBootTest` reserved for one tagged class | Only the circuit-breaker state-machine test needs full context; fallback logic is tested via `FallbackMethod` with none |
| No `Thread.sleep(...)` for circuit-breaker timing | Reset the registered `CircuitBreaker` between tests instead of waiting on wall-clock transitions |

### Common Pitfalls
1. **Forgetting `verifyComplete()`** — StepVerifier won't execute until a terminal operation is called
2. **Using `verifyComplete()` after `expectError()`** — Use `.verify()` for error expectations
3. **Mocking Mono/Flux directly** — Always return `Mono.just()`, `Mono.error()`, `Flux.just()` from mocks
4. **Static MockWebServer methods** — `@BeforeAll`/`@AfterAll` must be static; the adapter and server are static fields
5. **Reaching for `@WebFluxTest`/`@Import` for handler tests** — `WebTestClient.bindToRouterFunction(new XxxRouterRest().routerFunction(handler))` covers the same HTTP contract with zero Spring context and no bean scanning to configure
6. **Not capturing BiFunction in R2DBC mocks** — The mapper holder pattern is essential for DatabaseClient testing
7. **Mixing `when/thenReturn` with reactive types** — Always return `Mono.just(value)` not just `value`
8. **Testing circuit-breaker fallback behavior through a full `@SpringBootTest` + N real HTTP calls** — invoke the fallback method directly via `FallbackMethod.create(...)`; reserve the full-context test for the one scenario that genuinely needs the real state machine

### Migration Notes (from Blocking Tests)
| Blocking (WebMvc) | Reactive (WebFlux) |
|---|---|
| `@WebMvcTest` | `WebTestClient.bindToRouterFunction(...)` (no Spring context) |
| `MockMvc` | `WebTestClient` |
| `@MockBean` | `@Mock` (plain Mockito; `@MockitoBean` only in the tagged integration test) |
| `mockMvc.perform()` | `webTestClient.post().exchange()` |
| `andExpect(status().isOk())` | `.expectStatus().isOk()` |
| `andExpect(jsonPath("$.x"))` | `.expectBody().jsonPath("$.x")` |
| `MockRestServiceServer` | `MockWebServer` |
| `assertEquals(result)` | `StepVerifier.create().expectNext().verifyComplete()` |
| `JdbcTemplate` mocks | `DatabaseClient` mock chain |
| `@DataJpaTest` | Manual DatabaseClient mocking |
