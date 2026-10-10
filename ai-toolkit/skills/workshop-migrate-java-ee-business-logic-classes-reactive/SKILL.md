---
name: workshop-migrate-java-ee-business-logic-classes-reactive
description: >-
  Migrate Business Logic classes from Java EE (7/8) to reactive Use Cases on Scaffold Clean Architecture.
  Transforms legacy @Stateless EJBs into Use Cases (domain/usecase) with Mono/Flux reactive flows,
  Gateway interfaces (domain/model/gateways), and WebClient-based Driven Adapters (infrastructure/driven-adapters/rest-consumer).
  Replaces ServiceBroker with WebClient, GenericLogger with @Slf4j, CommonUtil error handling with
  reactive onErrorMap, PublishAsynchronousSQSBean with reactive SQS publishers.
  All code fully non-blocking end-to-end.
license: MIT
compatibility: Java 25, Spring Boot 4.x, Spring WebFlux, Project Reactor
metadata:
  author: criscast
  version: 1.0.0
  based-on: migrate-java-ee-business-logic-classes
---

# Migrate Java EE Business Logic Classes — Reactive

## When to Use This Skill

Use this skill when migrating Business Logic classes from a Java EE project (versions 7/8) to a **reactive** Spring Boot project that follows the **Scaffold Clean Architecture** plugin structure.

This skill transforms:
- Legacy `@Stateless` EJBs → **Use Cases** (`domain/usecase`) with `Mono<T>`/`Flux<T>` return types
- Legacy `ServiceBroker` / `ApiGatewayUtil` → **WebClient** driven adapters (`infrastructure/driven-adapters/rest-consumer`)
- Legacy `CommonUtil.responseErrorMessage()` → **Reactive error handling** with `onErrorMap`/`onErrorResume` + domain exceptions
- Legacy `PublishAsynchronousSQSBean` → **Reactive SQS publisher** adapters
- Legacy `GenericLogger` → **`@Slf4j`** with structured logging

## Target

- **Spring Boot:** 4.x (Spring Framework 7)
- **JDK:** 25
- **Reactive Stack:** Spring WebFlux + Project Reactor (`Mono<T>`, `Flux<T>`)
- **HTTP Client:** `WebClient` (non-blocking) — NOT `RestClient`
- **Jackson:** 3.x (`tools.jackson.*` package)
- **Build Tool:** Gradle 9.2.1+
- **Architecture:** Scaffold Clean Architecture (reactive type)

## Context

You are an expert in migrating Java EE (7/8) applications to reactive Spring Boot microservices using the Scaffold Clean Architecture. Your approach decomposes legacy `@Stateless` EJB business logic into:

1. **Use Cases** (`domain/usecase/`) — Orchestrate business flows reactively, inject gateway interfaces
2. **Gateway Interfaces** (`domain/model/gateways/`) — Define reactive contracts for external operations
3. **Driven Adapters** (`infrastructure/driven-adapters/`) — Implement gateways with WebClient, SQS, DynamoDB, etc.

**Critical paradigm shift from Java EE to Reactive:**
- No `@Service` on use cases — DI is wired in `@Configuration` classes
- No `@Stateless`, `@EJB` — Use `@RequiredArgsConstructor` with `private final` gateway fields
- No blocking I/O — All external calls return `Mono<T>` or `Flux<T>`
- No `RestClient` — Use `WebClient` exclusively (non-blocking HTTP)
- No `try/catch` for error responses — Use `onErrorMap`/`onErrorResume`
- No `@Async` — Native reactive composition handles concurrency
- No `CommonUtil.responseErrorMessage()` — Throw domain exceptions, handled by entry-point error handler

## Requirements

### Pre-conditions

1. The **legacy source code** (Java EE project with `@Stateless` EJBs) must be provided or accessible.
2. The **analysis report** identifying Business Logic classes and their dependencies must be available.
3. The **destination project** must be a reactive Scaffold Clean Architecture project.
4. The use case module must exist or be generated:
   ```bash
   gradle generateUseCase --name=[useCaseName]
   # or
   gradle guc --name [useCaseName]
   ```

If any of these are missing, request them from the user. Do NOT use or create `.sh` or `.py` scripts for the migration. Can use subagents for parallel processing.

## Target Project Structure

```
📦project-root
┣ 📂domain
┃ ┣ 📂model
┃ ┃ ┗ 📂src/main/java/[package]/model
┃ ┃   ┣ 📂[entity-name]/
┃ ┃   ┃ ┣ 📜EntityName.java                ← Domain model
┃ ┃   ┃ ┗ 📂gateways/
┃ ┃   ┃   ┗ 📜EntityNameRepository.java     ← Gateway interface (Mono/Flux)
┃ ┃   ┣ 📂enums/
┃ ┃   ┃ ┗ 📜TechnicalMessage.java
┃ ┃   ┣ 📂exceptions/
┃ ┃   ┃ ┣ 📜BusinessException.java
┃ ┃   ┃ ┗ 📜TechnicalException.java
┃ ┃   ┗ 📂utils/
┃ ┃     ┗ 📜Constants.java
┃ ┗ 📂usecase
┃   ┣ 📂src/main/java/[package]/usecase/
┃   ┃ ┗ 📂[business-name]/
┃   ┃   ┗ 📜BusinessNameUseCase.java        ← Reactive business logic; injects SecurityGateway
┃   ┃                                          directly if it needs encrypt/decrypt (no SecurityUseCase)
┃   ┗ 📂src/test/java/[package]/usecase/
┃     ┗ 📂[business-name]/
┃       ┗ 📜BusinessNameUseCaseTest.java
┣ 📂infrastructure
┃ ┗ 📂driven-adapters
┃   ┣ 📂rest-consumer/                       ← WebClient-based HTTP adapters
┃   ┃ ┣ 📂src/main/java/[package]/consumer/
┃   ┃ ┃ ┣ 📂[service-name]/
┃   ┃ ┃ ┃ ┣ 📜ServiceNameAdapter.java       ← Implements gateway
┃   ┃ ┃ ┃ ┣ 📂dto/
┃   ┃ ┃ ┃ ┃ ┣ 📂request/
┃   ┃ ┃ ┃ ┃ ┗ 📂response/
┃   ┃ ┃ ┃ ┣ 📂config/
┃   ┃ ┃ ┃ ┃ ┗ 📜ServiceNameProperties.java
┃   ┃ ┃ ┃ ┗ 📂mapper/
┃   ┃ ┃ ┃   ┗ 📜ServiceNameMapper.java
┃   ┃ ┃ ┗ 📂config/
┃   ┃ ┃   ┗ 📜RestConsumerConfig.java       ← WebClient bean wiring
┃   ┃ ┗ 📂src/test/java/...
┃   ┗ 📂sqs-publisher/                       ← Reactive SQS adapters
┃     ┗ 📂src/main/java/[package]/sqs/
┃       ┗ 📜EventPublisherAdapter.java
┗ 📂applications
  ┗ 📂app-service
```

## Migration Rules

### Rule 1: Use Case Layer (`domain/usecase/`)

Transform each `@Stateless` EJB into a Use Case class:

| Legacy (Java EE) | Reactive (Use Case) |
|-------------------|---------------------|
| `@Stateless` | Remove — no annotation needed |
| `@EJB ServiceBroker` | `private final ServiceGateway serviceGateway` |
| `@EJB PublishAsynchronousSQSBean` | `private final EventPublisherGateway eventGateway` |
| `GenericLogger` + `@PostConstruct` | `@Slf4j` |
| `String execute(String json, Map headers)` | `Mono<ResponseModel> execute(RequestModel request)` |
| `try { ... } catch { return errorMsg }` | Reactive error operators (`onErrorMap`, `onErrorResume`) |
| Sequential blocking calls | `flatMap`, `zipWith`, `then` composition |
| `@Async` fire-and-forget | `.subscribe()` or `then()` for fire-and-forget within stream |
| `CommonUtil.responseErrorMessage()` | `Mono.error(new BusinessException(...))` |
| `if (x == null) return error` | `switchIfEmpty(Mono.error(...))` |
| Mutable instance fields | Local variables or method parameters |

**Use Case Pattern:**
```java
@RequiredArgsConstructor
public class GetBalanceUseCase {

    private final BalanceGateway balanceGateway;

    public Mono<Balance> execute(BalanceRequest request) {
        return balanceGateway.getBalance(request.getAccountId())
            .doOnSuccess(balance -> log.info("Balance retrieved successfully"))
            .doOnError(error -> log.error("Error retrieving balance", error));
    }
}
```
If this use case also needs to encrypt its result (Cifrado/encryption in the legacy code), inject `SecurityGateway` alongside `BalanceGateway` directly — no `SecurityUseCase` wrapper. See Rule 5 for the full composition.

### Rule 2: Gateway Interfaces (`domain/model/gateways/`)

Define reactive contracts for every external operation:

```java
public interface BalanceGateway {
    Mono<Balance> getBalance(String accountId);
}

public interface EventPublisherGateway {
    Mono<Void> publishEvent(AuditEvent event);
}
```
If the legacy code has Cifrado/Descifrado logic, also define `SecurityGateway` here — see Rule 5 for its exact shape (`encrypt`/`decrypt`) and why it is injected directly into the use case instead of behind a `SecurityUseCase` wrapper.

### Rule 3: Driven Adapters (`infrastructure/driven-adapters/`)

#### WebClient Adapter (replaces ServiceBroker/RestClient)

```java
@RequiredArgsConstructor
@Slf4j
public class BalanceAdapter implements BalanceGateway {

    private final WebClient balanceWebClient;
    private final BalanceAdapterMapper mapper;

    @Override
    public Mono<Balance> getBalance(String accountId) {
        return balanceWebClient.post()
            .uri("/api/balance")
            .bodyValue(mapper.toRequest(accountId))
            .retrieve()
            .bodyToMono(BalanceResponse.class)
            .doOnError(error -> log.error("Error calling balance service", error))
            .map(mapper::toDomain)
            .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.BALANCE_NOT_FOUND)))
            .onErrorMap(Predicate.not(BusinessException.class::isInstance),
                exception -> new TechnicalException(exception, TechnicalMessage.MIDDLEWARE_ERROR));
    }
}
```

> **Template rule for every driven adapter**: map a known/expected business outcome (not found, rejected, validation error, specific status code from the external system) to `BusinessException`; map everything else (timeouts, connection errors, unmapped exceptions) to `TechnicalException`, always as the LAST operator, guarded with `Predicate.not(BusinessException.class::isInstance)` so a `BusinessException` already raised earlier in the chain is never overwritten. If a specific adapter has no known business outcome to detect (e.g. a low-level generic gateway proxy), it is still correct to keep only the guarded `TechnicalException` catch-all.

#### WebClient Configuration

```java
@Configuration
public class RestConsumerConfig {

    @Bean
    public BalanceAdapter balanceAdapter(
            @Qualifier("balanceWebClient") WebClient webClient,
            BalanceAdapterMapper mapper) {
        return new BalanceAdapter(webClient, mapper);
    }

    @Bean(name = "balanceWebClient")
    public WebClient balanceWebClient(BalanceProperties properties) {
        ConnectionProvider provider = ConnectionProvider.builder("balance")
            .pendingAcquireTimeout(Duration.ofMillis(properties.getPendingAcquireTimeout()))
            .maxConnections(properties.getMaxConnections())
            .maxIdleTime(Duration.ofSeconds(properties.getMaxIdleTime()))
            .maxLifeTime(Duration.ofSeconds(properties.getMaxLifeTime()))
            .build();

        ClientHttpConnector connector = new ReactorClientHttpConnector(
            HttpClient.create(provider)
                .compress(true)
                .option(CONNECT_TIMEOUT_MILLIS, properties.getConnectionTimeout())
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeout(), MILLISECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(properties.getReadTimeout(), MILLISECONDS));
                }));

        // Use WebClient.builder() directly — do NOT inject a WebClient.Builder bean. In a Clean
        // Architecture app-service the auto-configured WebClient.Builder is often absent, which
        // fails startup with "required a bean of type 'WebClient$Builder' that could not be found".
        return WebClient.builder()
            .baseUrl(properties.getHost())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(connector)
            .build();
    }
}
```

### Rule 4: Error Handling (Reactive)

> **Only two exception types exist in the target project: `BusinessException` and `TechnicalException`** (both carrying a `TechnicalMessage`). Never invent additional subtypes (`ResourceNotFoundException`, `NotFoundException`, `BusinessRuleException`, etc.) unless the legacy code has a genuinely distinct retry/timeout concern that the reference project itself models separately (e.g., a dedicated timeout exception) — and even then it must still expose a `TechnicalMessage` and be treated as a `BusinessException`/`TechnicalException` by callers. Guide yourself by `transfer_service_MS`, which follows this contract consistently across use cases, adapters, and entry-points:
>
> - **Use Cases**: throw `BusinessException` for every domain/business-rule violation (validation failures, "not found", "unavailable", "unauthorized" outcomes, 404/409-style legacy responses). This is the ONLY exception type a use case should construct directly.
> - **Driven Adapters**: map a response that represents a *known, expected business outcome* from the external system/DB (not found, rejected, validation error, specific status code) to `BusinessException`. Map every other failure (timeouts, connection errors, unmapped/unexpected exceptions) to `TechnicalException`, always as the LAST operator in the chain: `.onErrorMap(Predicate.not(BusinessException.class::isInstance), ex -> new TechnicalException(ex, TechnicalMessage.INTERNAL_ERROR))`.
> - **Entry-Points**: explicitly catch `BusinessException` and translate it to the corresponding client response (400/404/409/503 depending on the specific `TechnicalMessage`); catch `TechnicalException` — plus any unmapped/generic exception and `CallNotPermittedException` from circuit-breaker fallbacks — and translate to a 500/503 default response.

| Legacy Pattern | Reactive Replacement |
|---------------|---------------------|
| `CommonUtil.responseErrorMessage("404", msg)` | `Mono.error(new BusinessException(TechnicalMessage.XXX_NOT_FOUND))` |
| `CommonUtil.responseErrorMessage("500", msg)` | `onErrorMap(ex -> new TechnicalException(ex, TechnicalMessage.INTERNAL_ERROR))` |
| `CommonUtil.responseCustomMessage("409", msg)` | `Mono.error(new BusinessException(TechnicalMessage.XXX_CONFLICT))` |
| `try/catch` around external call | `.onErrorMap(Predicate.not(BusinessException.class::isInstance), ex -> new TechnicalException(ex, ...))` |
| `if (result == null) return error` | `.switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.XXX_NOT_FOUND)))` |
| General catch-all (adapters/entry-points) | `.onErrorMap(Predicate.not(BusinessException.class::isInstance), ex -> new TechnicalException(ex, ...))` |

**Exception classes live in `domain/model/exceptions/`:**
```java
public class BusinessException extends RuntimeException {
    private final TechnicalMessage technicalMessage;

    public BusinessException(TechnicalMessage technicalMessage) {
        super(technicalMessage.getMessage());
        this.technicalMessage = technicalMessage;
    }

    // Use when the external system returns a dynamic message/code that should be preserved
    // alongside a generic TechnicalMessage category (e.g. SecurityAdapter — see Rule 5)
    public BusinessException(String message, TechnicalMessage technicalMessage) {
        super(message);
        this.technicalMessage = technicalMessage;
    }
}

public class TechnicalException extends RuntimeException {
    private final TechnicalMessage technicalMessage;

    public TechnicalException(TechnicalMessage technicalMessage) {
        super(technicalMessage.getMessage());
        this.technicalMessage = technicalMessage;
    }

    public TechnicalException(Throwable cause, TechnicalMessage technicalMessage) {
        super(technicalMessage.getMessage(), cause);
        this.technicalMessage = technicalMessage;
    }
}
```

### Rule 5: Security — Encryption/Decryption (Cifrado, Descifrado)

> There is **no dedicated `SecurityUseCase` wrapper class.** Any legacy Cifrado/Descifrado call — a `SecurityServiceUtil`-style helper, an in-process AES encryption/decryption utility, or any equivalent key-exchange-based cipher routine — is replaced by a call to a domain `SecurityGateway`, injected **directly** into whichever business use case needs it, exactly like any other gateway (`CustomerGateway`, `BalanceGateway`, etc.). The cipher math and the call to the external security microservice live ONLY in the driven adapter (`SecurityAdapter`) — never in the use case, and never re-implemented locally. Full adapter, request/response envelope, and an AWS KMS alternative (for server-side secret/value decryption, as opposed to client-payload encryption) are described in Rule 3 and below.

**Domain Gateway** (`domain/model/security/gateways/SecurityGateway.java`):
```java
public interface SecurityGateway {
    Mono<String> encrypt(SecurityParam param, String clearMessage);
    Mono<String> decrypt(SecurityParam param, String protectedMessage);
}
```
Only declare the direction(s) the legacy code actually uses — a service that only ever encrypts an outgoing response should declare just `encrypt(...)`.

**Domain Param** (`domain/model/security/SecurityParam.java`):
```java
@Value
@Builder(toBuilder = true)
public class SecurityParam {
    String phoneNumber;
    String clientPublicKey;
    String messageId;
}
```

**Usage — directly in the business use case**, not through a security wrapper:
```java
@RequiredArgsConstructor
public class GetBalanceSecureUseCase {

    private final BalanceGateway balanceGateway;
    private final SecurityGateway securityGateway;
    private final ObjectMapper objectMapper;

    public Mono<BalanceInformation> execute(BalanceParam param) {
        return balanceGateway.getBalance(param.getAccountNumber())
            .flatMap(balance -> encrypt(param, balance));
    }

    private Mono<BalanceInformation> encrypt(BalanceParam param, Balance balance) {
        return Mono.defer(() -> param.getClientPublicKey() == null || param.getClientPublicKey().isBlank()
            ? Mono.error(new BusinessException(TechnicalMessage.INVALID_PUBLIC_KEY))
            : securityGateway.encrypt(toSecurityParam(param), toJson(balance))
                .map(protectedMessage -> BalanceInformation.builder().protectedMessage(protectedMessage).build()));
    }

    private SecurityParam toSecurityParam(BalanceParam param) {
        return SecurityParam.builder()
            .phoneNumber(param.getPhoneNumber())
            .clientPublicKey(param.getClientPublicKey())
            .messageId(param.getMessageId())
            .build();
    }

    @SneakyThrows
    private String toJson(Balance balance) {
        return objectMapper.writeValueAsString(balance);
    }
}
```
The missing/blank public key check stays a `BusinessException` raised **in the use case** (it is domain validation); everything past that point — building the request envelope, calling the security microservice, translating its response/HTTP errors — is the adapter's job (Rule 3 contract: business outcome → `BusinessException`, everything else → `TechnicalException`).

#### Entry-Point: Encrypted vs. Unencrypted Channel Branching

If the legacy code decides whether to encrypt based on the calling channel, replicate that branch in the **entry-point handler** — never inside the use case or the adapter:

```java
@CircuitBreaker(name = "balances", fallbackMethod = "fallback")
public Mono<ServerResponse> getBalance(BalanceRequest request, ServerRequest.Headers headers) {
    boolean isUnencryptedChannel = unencryptedChannels.contains(
        request.getRequestMessage().getRequestHeader().getChannel());
    return isUnencryptedChannel
        ? handleUnencrypted(request)
        : handleEncrypted(request, headers.firstHeader("x-mfp-device-id"));
}
```
- The set of unencrypted channels comes from configuration (`@Value("#{'${balance.unencrypted-channels}'.split(',')}")`), never hardcoded.
- Validate any extra header/field the encrypted path requires (e.g. `x-mfp-device-id`) only on that branch.
- Each branch calls a different use-case method (`getBalance(...)` vs `getBalanceSecure(...)`) — the handler never performs cipher logic itself.

#### AWS KMS as an Alternative Decryption Mechanism

If the legacy code decrypts a **server-side secret or stored value** (e.g., a registered device id, a symmetric key) rather than a client-supplied encrypted payload, that is a different concern: implement a `KeyManagerGateway`/`KmsAdapter` backed by the AWS KMS SDK directly (no WebClient, no external security microservice).

### Rule 6: Async Migration (SQS, Fire-and-Forget)

In a reactive stack, `@Async` is NOT needed. Use reactive composition:

| Legacy Pattern | Reactive Replacement |
|---------------|---------------------|
| `@Async` fire-and-forget REST | `webClient.post().retrieve().bodyToMono(Void.class).subscribe()` |
| `PublishAsynchronousSQSBean.publishMessage()` | `sqsGateway.publishEvent(event)` (returns `Mono<Void>`) |
| `CompletableFuture<Void>` | `Mono<Void>` |
| Parallel independent calls | `Mono.zip(call1, call2, call3)` |
| Sequential dependent calls | `call1.flatMap(r1 -> call2(r1))` |

**SQS Publisher Gateway:**
```java
public interface EventPublisherGateway {
    Mono<Void> publishAuditEvent(AuditEvent event);
}
```

**SQS Publisher Adapter:**
```java
@RequiredArgsConstructor
@Slf4j
public class SqsPublisherAdapter implements EventPublisherGateway {

    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    @Override
    public Mono<Void> publishAuditEvent(AuditEvent event) {
        return Mono.fromFuture(() -> sqsAsyncClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(objectMapper.writeValueAsString(event))
                .build()))
            .doOnSuccess(response -> log.info("SQS message sent: {}", response.messageId()))
            .doOnError(error -> log.error("SQS publish failed", error))
            .onErrorMap(ex -> new TechnicalException(ex, TechnicalMessage.SQS_PUBLISH_ERROR))
            .then();
    }
}
```

### Rule 7: Singleton State Warning

A Java EE `@Stateless` EJB has per-invocation isolation. In the reactive model, use cases are singletons. **Never store mutable per-request state in instance fields.** Pass all request-scoped data through the reactive chain as method parameters or via Reactor `Context`.

## Migration Examples

### Example 1: Full Business Class Migration

#### Before (Java EE)
```java
@Stateless
public class GetBalanceServiceBusiness {

    @EJB
    private ServiceBroker serviceBroker;

    @EJB
    private PublishAsynchronousSQSBean sqsBean;

    private GenericLogger logger;

    @PostConstruct
    public void init() {
        logger = LoggerFactory.getLogger(ServiceTypeEnum.BUSSINES, this.getClass());
    }

    public String execute(String messageRqJson, Map<String, String> headers) {
        try {
            MessageRQ messageRQ = JsonUtil.fromJson(messageRqJson, MessageRQ.class);
            logger.info("Processing balance request");

            BrokerRequest request = ServiceBroker.createBrokerRequest("getBalance", headers);
            request.setBody(messageRQ.getAccountId());
            BrokerResponse response = serviceBroker.invoke(request);

            sqsBean.publishMessage("audit-queue", messageRQ.getAccountId());

            return response.getBody();
        } catch (Exception e) {
            return CommonUtil.responseErrorMessage("500", "Error getting balance");
        }
    }
}
```

#### After (Reactive Use Case)
```java
package com.company.usecase.balance;

import com.company.model.balance.Balance;
import com.company.model.balance.gateways.BalanceGateway;
import com.company.model.audit.AuditEvent;
import com.company.model.audit.gateways.EventPublisherGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class GetBalanceUseCase {

    private final BalanceGateway balanceGateway;
    private final EventPublisherGateway eventPublisherGateway;

    public Mono<Balance> execute(String accountId) {
        return balanceGateway.getBalance(accountId)
            .doOnSuccess(balance -> log.info("Balance retrieved for account"))
            .flatMap(balance -> publishAuditEvent(accountId).thenReturn(balance));
    }

    private Mono<Void> publishAuditEvent(String accountId) {
        return eventPublisherGateway.publishAuditEvent(
                AuditEvent.builder().accountId(accountId).action("GET_BALANCE").build())
            .onErrorResume(ex -> {
                log.warn("Failed to publish audit event, continuing", ex);
                return Mono.empty();
            });
    }
}
```

### Example 2: ServiceBroker → WebClient Driven Adapter

#### Before (Java EE)
```java
BrokerRequest request = ServiceBroker.createBrokerRequest("transferFunds", headers);
request.setBody(transferPayload);
BrokerResponse response = serviceBroker.invoke(request);
```

#### After (WebClient Adapter)
```java
package com.company.consumer.transfer;

import com.company.consumer.transfer.dto.request.TransferRequest;
import com.company.consumer.transfer.dto.response.TransferResponse;
import com.company.consumer.transfer.mapper.TransferAdapterMapper;
import com.company.model.enums.TechnicalMessage;
import com.company.model.exceptions.BusinessException;
import com.company.model.exceptions.TechnicalException;
import com.company.model.transfer.Transfer;
import com.company.model.transfer.gateways.TransferGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@RequiredArgsConstructor
@Slf4j
public class TransferAdapter implements TransferGateway {

    private final WebClient transferWebClient;
    private final TransferAdapterMapper mapper;
    private final String path;

    @Override
    public Mono<Transfer> transferFunds(Transfer transfer) {
        TransferRequest request = mapper.toRequest(transfer);

        return transferWebClient.post()
            .uri(path)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(TransferResponse.class)
            .doOnError(error -> log.error("Error calling transfer service", error))
            .flatMap(this::mapTransferResponse)
            .onErrorMap(Predicate.not(BusinessException.class::isInstance),
                ex -> new TechnicalException(ex, TechnicalMessage.MIDDLEWARE_ERROR));
    }

    private Mono<Transfer> mapTransferResponse(TransferResponse response) {
        if (!response.getStatus().equals(TechnicalMessage.SUCCESS)) {
            return Mono.error(new BusinessException(response.getStatus()));
        }
        return Mono.just(mapper.toDomain(response));
    }
}
```

> Guided by `TransferAdapter` in `transfer_service_MS`: a non-success status returned by the external system is a known business outcome (`BusinessException`), never `TechnicalException`. Only a transport-level failure (timeout, connection error, unmapped response) reaches the guarded catch-all.

### Example 3: Cifrado/Encryption → Direct SecurityGateway Injection

#### Before (Java EE)
```java
// Ciframos la respuesta antes de enviarla al cliente
String balanceJson = mapper.writeValueAsString(balanceDTO);
String protectedMessage = sch.getJSONObjectEncryptAES(
    balanceJson, request.getClientPublicKey(),
    Constant.COMMON_STRING_PHONENUMBER, em);

if (StringUtils.isEmpty(request.getClientPublicKey())) {
    return CommonUtil.responseErrorMessage("405", "La clave pública no puede ser nula o vacía");
}
```

#### After (Direct gateway injection — no SecurityUseCase)
```java
// In the business use case (BalanceUseCase-style):
private Mono<BalanceInformation> encryptBalance(BalanceParam param, Balance balance) {
    return Mono.defer(() -> param.getClientPublicKey() == null || param.getClientPublicKey().isBlank()
        ? Mono.error(new BusinessException(TechnicalMessage.INVALID_PUBLIC_KEY))
        : securityGateway.encrypt(toSecurityParam(param), toJson(balance))
            .map(protectedMessage -> BalanceInformation.builder().protectedMessage(protectedMessage).build()));
}
```

> Note: no `onErrorMap` here — the use case only adds the `BusinessException` for its own precondition (missing public key). `securityGateway.encrypt(...)` (Rule 3/5, adapter calling the external security microservice) already returns a properly classified `BusinessException`/`TechnicalException`, so the use case just propagates it. A use case never adds a blanket catch-all to `TechnicalException`; only driven adapters do that.

### Example 4: Parallel Operations with Mono.zip

#### Before (Java EE — Sequential)
```java
Account account = accountService.getAccount(accountId);
List<Transaction> transactions = transactionService.getRecent(accountId);
CustomerProfile profile = customerService.getProfile(customerId);
```

#### After (Reactive — Parallel)
```java
public Mono<AccountSummary> getAccountSummary(String accountId, String customerId) {
    return Mono.zip(
        accountGateway.getAccount(accountId),
        transactionGateway.getRecent(accountId),
        customerGateway.getProfile(customerId)
    ).map(tuple -> AccountSummary.builder()
        .account(tuple.getT1())
        .transactions(tuple.getT2())
        .profile(tuple.getT3())
        .build());
}
```

## Use Case Test Pattern

```java
package com.company.usecase.balance;

import com.company.model.balance.Balance;
import com.company.model.balance.gateways.BalanceGateway;
import com.company.model.audit.gateways.EventPublisherGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetBalanceUseCaseTest {

    @Mock
    private BalanceGateway balanceGateway;

    @Mock
    private EventPublisherGateway eventPublisherGateway;

    GetBalanceUseCase getBalanceUseCase;

    @BeforeEach
    void setUp() {
        getBalanceUseCase = new GetBalanceUseCase(balanceGateway, eventPublisherGateway);
    }

    @Test
    void shouldGetBalanceSuccessfully() {
        given(balanceGateway.getBalance("87000"))
            .willReturn(Mono.just(Balance.builder()
                .balanceAmount("1000.00")
                .build()));
        given(eventPublisherGateway.publishAuditEvent(any()))
            .willReturn(Mono.empty());

        StepVerifier.create(getBalanceUseCase.execute("87000"))
            .assertNext(balance -> assertEquals("1000.00", balance.getBalanceAmount()))
            .verifyComplete();
    }

    @Test
    void shouldHandleGatewayError() {
        given(balanceGateway.getBalance("87000"))
            .willReturn(Mono.error(new RuntimeException("Connection timeout")));

        StepVerifier.create(getBalanceUseCase.execute("87000"))
            .expectError()
            .verify();
    }
}
```

## WebClient Adapter Test Pattern

```java
package com.company.consumer.balance;

import com.company.consumer.balance.dto.response.BalanceResponse;
import com.company.consumer.balance.mapper.BalanceAdapterMapper;
import com.company.model.balance.Balance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceAdapterTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private BalanceAdapterMapper mapper;

    BalanceAdapter balanceAdapter;

    @BeforeEach
    void setUp() {
        balanceAdapter = new BalanceAdapter(webClient, mapper, "/api/balance");
    }

    @Test
    void shouldGetBalanceSuccessfully() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(BalanceResponse.class))
            .thenReturn(Mono.just(new BalanceResponse()));
        when(mapper.toDomain(any())).thenReturn(Balance.builder().balanceAmount("1000").build());
        when(mapper.toRequest(anyString())).thenReturn(null);

        StepVerifier.create(balanceAdapter.getBalance("87000"))
            .assertNext(balance -> {})
            .verifyComplete();
    }
}
```

## Adapter Properties Pattern

```java
package com.company.consumer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "adapter.rest-consumer.balance")
public class BalanceProperties {
    private String host;
    private String path;
    private int connectionTimeout = 5000;
    private int readTimeout = 30000;
    private int maxConnections = 50;
    private int maxIdleTime = 30;
    private int maxLifeTime = 300;
    private int pendingAcquireTimeout = 5000;
    private int pendingAcquireMaxCount = 100;
    private boolean metrics = true;
    private int evictInBackground = 60;
}
```

## Configuration (application.yaml)

```yaml
adapter:
  rest-consumer:
    balance:
      host: ${BALANCE_SERVICE_HOST:http://localhost:8081}
      path: /api/balance
      connection-timeout: ${BALANCE_CONNECTION_TIMEOUT:5000}
      read-timeout: ${BALANCE_READ_TIMEOUT:30000}
      max-connections: ${BALANCE_MAX_CONNECTIONS:50}
      max-idle-time: ${BALANCE_MAX_IDLE_TIME:30}
      max-life-time: ${BALANCE_MAX_LIFE_TIME:300}
    transfer:
      host: ${TRANSFER_SERVICE_HOST:http://localhost:8082}
      path: /api/transfers
      connection-timeout: ${TRANSFER_CONNECTION_TIMEOUT:5000}
      read-timeout: ${TRANSFER_READ_TIMEOUT:30000}
```

## Instructions

### Step 1: Validate Target Project & Generate Use Case Module

1. Confirm the target project uses Scaffold Clean Architecture with reactive type.
2. Identify the base package from `gradle.properties`.
3. Generate use cases as needed:
   ```bash
   gradle guc --name [useCaseName]
   ```

### Step 2: Identify Business Logic to Migrate

From the analysis report:
1. List all `@Stateless` EJB classes (Business Logic).
2. Map each EJB's dependencies: `ServiceBroker` calls, SQS publishers, ORM services, security utilities.
3. Identify the main orchestration method (`execute`, `executeOperation`, etc.).
4. Document the sequential flow of operations.
5. **Flag Customer/Client lookups specially**: if any dependency's business purpose is retrieving the customer/client (by phone number, id, etc.) — whether implemented as `DataServiceUtil.getCustomer(...)`, a direct `Cliente` JPA/`EntityManager` query, a DAO/ORM service, or a named/native query — do **not** treat it as a generic "ORM service" to be migrated into a new database adapter. Reuse the canonical `CustomerGateway.getCustomer(phoneNumber, messageId)` from `migrate-java-ee-business-utils-classes-reactive` instead.
6. **Flag Parameter/Attribute lookups specially**: if any dependency's business purpose is reading configuration from the `PARAMETRO`, `PARAMETRO_ATRIBUTO`, or `ATRIBUTO` tables — whether implemented as `DataServiceUtil.getParameterValue(...)`/`getParameterAttribute(...)`/`getParameterByValue(...)`/`getParameters(...)`, a direct JPA/`EntityManager` query, a DAO/ORM service, or a named/native query — do **not** treat it as a generic "ORM service" to be migrated into a database adapter. Cross-reference the parameter name/code against the "Database Parameters (Environment Variable Candidates)" section of the `get-java-ee-project-context` analysis report and replace it with the corresponding environment variable, per `migrate-java-ee-business-utils-classes-reactive`.

### Step 3: Create Domain Models and Gateway Interfaces

For each external dependency identified:
1. Create/reuse domain model in `domain/model/`.
2. Create gateway interface in `domain/model/[entity]/gateways/` with `Mono<T>`/`Flux<T>` methods.

### Step 4: Create Use Cases

For each `@Stateless` EJB:
1. Create use case in `domain/usecase/src/main/java/[package]/usecase/[name]/`.
2. Inject gateway interfaces via constructor (`@RequiredArgsConstructor`).
3. Implement business logic using reactive composition (`flatMap`, `zipWith`, `onErrorMap`).
4. Create test class with `MockitoExtension` + `StepVerifier`.
5. **No `@Service`/`@Component`** annotations on the use case class.

### Step 5: Create Driven Adapters

For each gateway that needs implementation:
1. **HTTP calls** → Create WebClient adapter in `infrastructure/driven-adapters/rest-consumer/`.
2. **SQS** → Create reactive SQS adapter in `infrastructure/driven-adapters/sqs-publisher/`.
3. **DynamoDB** → Create reactive DynamoDB adapter.
4. **Customer/Client lookup** → Reuse the existing `CustomerGateway`/`CustomerAdapter` (WebClient-based, see `migrate-java-ee-business-utils-classes-reactive`). Do **not** create a database repository/adapter to query customer data directly, even if the legacy code fetched it via a DB query/DAO/ORM service.
5. **Parameter/Attribute lookup** (`PARAMETRO`, `PARAMETRO_ATRIBUTO`, `ATRIBUTO`) → Do **not** create a gateway or database adapter. Add the parameter as a field on `BusinessParametersProperties` (`@ConfigurationProperties`, see `migrate-java-ee-business-utils-classes-reactive`) bound to an environment variable, and reference that field directly at the call site.
6. Each adapter has: Adapter class, DTOs, Mapper (MapStruct), Properties, Config.

### Step 6: Wire Encryption/Decryption (Cifrado, Descifrado)

If the legacy has Cifrado/Descifrado logic (in-process AES encryption/decryption math, `SecurityServiceUtil`-style helpers):
1. Do **not** create a `SecurityUseCase` wrapper. Create `SecurityGateway` (domain interface) and inject it directly into whichever use case(s) already need encryption/decryption.
2. Implement `SecurityAdapter` (WebClient) calling the external security microservice — never re-implement the cipher math locally.
3. If the legacy code instead decrypts a server-side secret or stored value (not a client payload), implement `KeyManagerGateway`/`KmsAdapter` using the AWS KMS SDK directly.
4. If the legacy code branches encryption on the calling channel, replicate that branch in the entry-point handler (Rule 5), not in the use case or adapter.
5. Reference: Rule 5 for the full gateway, adapter, envelope DTOs, and configuration.

### Step 7: Wire Configuration

Create `@Configuration` classes in adapter modules to wire beans:
- Use case beans wired in `applications/app-service` module.
- Adapter beans wired in `infrastructure/driven-adapters/` config classes.

### Step 8: Verify Compilation

```bash
./gradlew build -x test
```

### Step 9: Functional Equivalence Verification (MANDATORY)

Generate a comparison table for each orchestration method:

| # | Legacy Step | Migrated Step | Status |
|---|---|---|---|
| 1 | Read headers | Extracted by entry-point, passed as model | ✅ |
| 2 | Decrypt request | `securityUseCase.decryptPayload()` | ✅ |
| 3 | Call external service | `serviceGateway.operation()` via WebClient | ✅ |
| 4 | Publish SQS event | `eventGateway.publishEvent()` | ✅ |

Status: ✅ Equivalent | ⚠️ Partial | ❌ Not migrated | 🆕 New

## Scaffolding Use Cases

Generate use case modules using the Scaffold Clean Architecture plugin:

```bash
gradle generateUseCase --name=GetBalance
# Creates:
# domain/usecase/src/main/java/[package]/usecase/getbalance/GetBalanceUseCase.java
# domain/usecase/src/test/java/[package]/usecase/getbalance/
```

## Dependencies Summary

### domain/model/build.gradle
```groovy
dependencies {
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'io.projectreactor:reactor-core'
}
```

### domain/usecase/build.gradle
```groovy
dependencies {
    implementation project(':model')
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'io.projectreactor:reactor-core'
    testImplementation 'io.projectreactor:reactor-test'
}
```

### infrastructure/driven-adapters/rest-consumer/build.gradle
```groovy
dependencies {
    implementation project(':model')
    implementation 'org.springframework:spring-context'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation "org.mapstruct:mapstruct:${mapstructVersion}"
    annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
    testImplementation 'io.projectreactor:reactor-test'
}
```

## Best Practices

- **No blocking calls** — Never use `.block()` anywhere in use cases or adapters.
- **No `@Service`/`@Component` on use cases** — Wire via `@Configuration` classes.
- **Use `Mono.defer()`** — Wrap adapter logic to ensure lazy subscription.
- **Error boundaries** — Use `onErrorMap` at adapter level to convert exceptions to domain exceptions.
- **Structured logging** — Use `kv()` from logstash for structured log arguments.
- **ConnectionProvider per external service** — Each WebClient gets its own connection pool.
- **MapStruct for mapping** — Never manually map DTO↔Domain.
- **Test with StepVerifier** — All reactive code must be tested with `StepVerifier`.
- **Orchestration order preserved** — Cheap validations before expensive remote calls.
- **No double external calls** — Same service invoked once per request unless intentional.
- **Fire-and-forget with error resilience** — Use `.onErrorResume(e -> Mono.empty())` for non-critical async operations.

## Post-Migration Validation Checklist

- [ ] **Functional equivalence table** generated and reviewed
- [ ] **No `@Stateless`/`@EJB`**: All removed
- [ ] **Use cases return Mono/Flux**: No blocking return types
- [ ] **No `@Service` on use cases**: DI wired via `@Configuration`
- [ ] **WebClient for HTTP calls**: Not RestClient, not WebClient.block()
- [ ] **Gateway interfaces created**: For every external dependency
- [ ] **Adapters implement gateways**: WebClient-based, with ConnectionProvider
- [ ] **Error handling reactive**: `onErrorMap`/`onErrorResume`, no try/catch
- [ ] **No `SecurityUseCase` wrapper**: encryption/decryption uses `SecurityGateway` injected directly into the use case that needs it
- [ ] **`SecurityAdapter` calls the external security microservice**: no local AES cipher math re-implemented; KMS-based `KeyManagerGateway` used only for server-side secret/value decryption
- [ ] **No mutable singleton state**: No instance fields modified per-request
- [ ] **All timeouts configured**: Every WebClient has connection/read timeouts
- [ ] **SQS migrated reactively**: `Mono<Void>` publishers, no blocking
- [ ] **Tests with StepVerifier**: Use case + adapter tests
- [ ] **Orchestration order preserved**: Cheap ops before expensive
- [ ] **No stubs**: Real implementations (not log-only placeholders)
- [ ] **Compilation passes**: `./gradlew build -x test` exits with 0
- [ ] **No circular dependencies**: Domain has no imports from infrastructure

## Known Edge Cases

| Pattern | Correct Handling |
|---------|-----------------|
| Request headers | Extracted by entry-point, passed as domain model fields — not raw maps |
| `MessageRQ` as input | Entry-point deserializes; use case receives typed domain model |
| Sequential dependent calls | Use `flatMap` chain — not parallel `zip` |
| Independent parallel calls | Use `Mono.zip()` for concurrent execution |
| Fire-and-forget with resilience | Use `.onErrorResume(e -> { log.warn(...); return Mono.empty(); })` |
| Conditional flows | Use `filter()` + `switchIfEmpty()` or `flatMap` with conditional logic |
| Multiple ESB operations | Each gets its own WebClient adapter — no generic broker |
| AWS SDK v1 → v2 reactive | Use `Mono.fromFuture(() -> asyncClient.operation())` |
| Retry on transient errors | Use `.retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))` |
| Circuit breaker | Use Resilience4j `@CircuitBreaker` on adapter or `ReactiveCircuitBreaker` |
| Customer/Client lookup (any form: `DataServiceUtil.getCustomer`, direct `Cliente` JPA/`EntityManager` query, DAO/ORM service, named/native query) | Always call `customerGateway.getCustomer(phoneNumber, messageId)` (WebClient-based, see `migrate-java-ee-business-utils-classes-reactive`) — never create a new database repository/adapter for customer data |
| Parameter/Attribute lookup (any form: `DataServiceUtil.getParameterValue`/`getParameterAttribute`/`getParameterByValue`/`getParameters`, direct JPA/`EntityManager` query against `PARAMETRO`/`PARAMETRO_ATRIBUTO`/`ATRIBUTO`, DAO/ORM service, named/native query) | Always resolve via an environment variable field on `BusinessParametersProperties` (`@ConfigurationProperties`, see `migrate-java-ee-business-utils-classes-reactive` and the `get-java-ee-project-context` parameter inventory) — never create a new database repository/adapter for these tables |
