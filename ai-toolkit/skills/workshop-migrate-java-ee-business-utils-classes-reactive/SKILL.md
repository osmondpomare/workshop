---
name: workshop-migrate-java-ee-business-utils-classes-reactive
description: >-
  Migrate usages of the MDW.CMM.BusinessUtils dependency from Java EE projects (versions 7 and 8)
  to reactive Spring Boot equivalents on Scaffold Clean Architecture. Replaces JDBC with R2DBC
  (DatabaseClient), RestClient with WebClient, @Service with Use Cases, and static utilities with
  Helpers modules. All replacements return Mono<T>/Flux<T> for fully non-blocking reactive flows.
license: MIT
compatibility: Java 25, Spring Boot 4.x, Spring WebFlux, R2DBC Oracle, Project Reactor
metadata:
  author: criscast
  version: 1.0.0
  based-on: migrate-java-ee-business-utils-classes
---

# Migrate Java EE BusinessUtils Dependency — Reactive

## When to Use This Skill

Use this skill when a Java EE project uses the `MDW.CMM.BusinessUtils` dependency and each of its utility classes (`BackoutServiceUtil`, `DataServiceUtil`, `PaymentServiceUtil`, `SecurityServiceUtil`, `ApiGatewayUtil`, `BusinessUtil`) must be replaced with **reactive** Spring Boot equivalents on Scaffold Clean Architecture.

```xml
<dependency>
    <groupId>com.company.mdw.common</groupId>
    <artifactId>MDW.CMM.BusinessUtils</artifactId>
    <version>1.1.6</version>
</dependency>
```

This skill transforms:
- Legacy `DataServiceUtil` JDBC calls → **R2DBC Adapters** (`DatabaseClient.sql()`) returning `Mono<T>`/`Flux<T>`
- Legacy `ApiGatewayUtil` / `getCustomer` REST calls → **WebClient Adapters** returning `Mono<T>`
- Legacy `PaymentServiceUtil.executePaymentV2` → **PaymentUseCase** with WebClient adapter returning `Mono<T>`
- Legacy `SecurityServiceUtil.validateChannel` → **R2DBC Adapter** returning `Mono<Boolean>`
- Legacy static helpers (`isBrokerTimeout`, `isFinacleTechnicalError`) → **Helper modules** (`@Component`)
- Legacy `BackoutServiceUtil` → **Private methods** in use case (no I/O involved)
- Legacy `BusinessUtil` → **Inline replacements** or reactive compositions

## Target

- **Spring Boot:** 4.x (Spring Framework 7)
- **JDK:** 25
- **Reactive Stack:** Spring WebFlux + Project Reactor (`Mono<T>`, `Flux<T>`)
- **HTTP Client:** `WebClient` (non-blocking) — NOT `RestClient`
- **Persistence:** Spring Data R2DBC + Oracle R2DBC driver (`DatabaseClient`)
- **Serialization:** Jackson 3.x (`tools.jackson.*` package — NOT `com.fasterxml.jackson.*`)
- **Cache:** `CacheMono` from `reactor-extra` + Caffeine
- **Build Tool:** Gradle 9.2.1+
- **Architecture:** Scaffold Clean Architecture (reactive type)

## Context

You are an expert in migrating applications written with the Java EE programming language in versions 7 and 8. Your approach is to locate every call site that uses a method from `MDW.CMM.BusinessUtils`, then replace each one following the step-by-step instructions for that method below.

**Critical paradigm shift from Java EE to Reactive:**
- No `EntityManager` — use `DatabaseClient.sql()` with bind parameters returning `Mono<T>`/`Flux<T>`
- No `RestClient` / `RestTemplate` — use `WebClient` exclusively (non-blocking HTTP)
- No `@Service` on use cases — DI is wired via `@RequiredArgsConstructor` with `private final` fields
- No blocking I/O — ALL external calls return `Mono<T>` or `Flux<T>`
- No `try/catch` for error responses — Use `onErrorMap`/`onErrorResume`/`switchIfEmpty`
- No `spring-boot-starter-data-jdbc` — use `spring-boot-starter-data-r2dbc` + `com.oracle.database.r2dbc:oracle-r2dbc`
- No `spring-boot-starter-restclient` — use `spring-boot-starter-webflux`
- Static helpers that involve NO I/O remain static (wrapped in `@Component` helper modules)
- Methods that involve I/O become gateway interfaces returning `Mono<T>`/`Flux<T>`

Do NOT use or create `.sh` or `.py` scripts to perform the migration. You may use subagents to process multiple classes in parallel.

## Requirements

### Pre-conditions

1. The **legacy source code** (Java EE project with `MDW.CMM.BusinessUtils`) must be provided or accessible.
2. The **analysis report** identifying BusinessUtils call sites must be available.
3. The **destination project** must be a reactive Scaffold Clean Architecture project.
4. Required modules must exist or be generated via scaffold commands (see Scaffolding section).

If any of these are missing, request them from the user.

### General Rules

- For each method found, apply **only** the changes described for that method. Do not refactor unrelated code.
- After replacing a call site, remove the now-unused static import of the BusinessUtils class if no other method from it remains in the file.
- If the legacy source code uses `EntityManager` (`em`) as a parameter, that parameter must be dropped — it is never carried over to the reactive replacement.
- Do not implement Spring Security for security utilities.
- All database operations use `DatabaseClient.sql()` with native SQL (no `R2dbcEntityTemplate`).
- All HTTP operations use `WebClient` (never `RestClient` or `RestTemplate`).
- **Error handling contract** (only two exception types exist: `BusinessException` and `TechnicalException`, both carrying a `TechnicalMessage` — guided by `transfer_service_MS`):
  - **Driven Adapters**: map a known/expected business outcome from the external system or DB (not found, rejected, validation error, specific status code) to `BusinessException`. Map every other failure (timeouts, connection errors, unmapped exceptions) to `TechnicalException`, as the LAST operator: `.onErrorMap(Predicate.not(BusinessException.class::isInstance), ex -> new TechnicalException(ex, TechnicalMessage.XXX))`.
  - Never let a use case re-wrap or downgrade an exception that already came classified from a gateway — a use case only ever constructs `BusinessException` for its own domain validation and otherwise propagates the gateway's error unchanged.

## Architecture Overview

```
📦project-root
┣ 📂domain
┃ ┣ 📂model/src/main/java/[package]/model
┃ ┃ ┣ 📂customer/
┃ ┃ ┃ ┣ 📜Customer.java
┃ ┃ ┃ ┗ 📂gateways/
┃ ┃ ┃   ┗ 📜CustomerGateway.java
┃ ┃ ┣ 📂channel/
┃ ┃ ┃ ┗ 📂gateways/
┃ ┃ ┃   ┗ 📜ChannelGateway.java
┃ ┃ ┣ 📂homologation/
┃ ┃ ┃ ┗ 📂gateways/
┃ ┃ ┃   ┗ 📜HomologationTypeGateway.java
┃ ┃ ┣ 📂transactioncontrol/
┃ ┃ ┃ ┗ 📂gateways/
┃ ┃ ┃   ┗ 📜TransactionControlGateway.java
┃ ┃ ┣ 📂apigateway/
┃ ┃ ┃ ┗ 📂gateways/
┃ ┃ ┃   ┗ 📜ApiGatewayGateway.java
┃ ┃ ┣ 📂payment/
┃ ┃ ┃ ┗ 📂gateways/
┃ ┃ ┃   ┗ 📜PaymentGateway.java
┃ ┃ ┗ 📂exceptions/
┃ ┃   ┣ 📜BusinessException.java
┃ ┃   ┗ 📜TechnicalException.java
┃ ┗ 📂usecase/src/main/java/[package]/usecase
┃   ┗ 📂payment/
┃     ┗ 📜PaymentUseCase.java               ← Use Case (no @Service)
┣ 📂infrastructure
┃ ┣ 📂driven-adapters
┃ ┃ ┣ 📂r2dbc-oracle/src/main/java/[package]/r2dbc
┃ ┃ ┃ ┣ 📂channel/
┃ ┃ ┃ ┃ ┗ 📜ChannelAdapter.java
┃ ┃ ┃ ┣ 📂homologation/
┃ ┃ ┃ ┃ ┗ 📜HomologationAdapter.java
┃ ┃ ┃ ┗ 📂transactioncontrol/
┃ ┃ ┃   ┗ 📜TransactionControlAdapter.java
┃ ┃ ┗ 📂rest-consumer/src/main/java/[package]/consumer
┃ ┃   ┣ 📂customer/
┃ ┃   ┃ ┣ 📜CustomerAdapter.java            ← WebClient adapter
┃ ┃   ┃ ┣ 📂dto/
┃ ┃   ┃ ┣ 📂config/
┃ ┃   ┃ ┗ 📂mapper/
┃ ┃   ┣ 📂apigateway/
┃ ┃   ┃ ┣ 📜ApiGatewayAdapter.java
┃ ┃   ┃ ┣ 📂dto/
┃ ┃   ┃ ┣ 📂config/
┃ ┃   ┃ ┗ 📂mapper/
┃ ┃   ┗ 📂payment/
┃ ┃     ┣ 📜PaymentAdapter.java
┃ ┃     ┣ 📂dto/
┃ ┃     ┗ 📂config/
┃ ┗ 📂helpers
┃   ┣ 📂broker-code/src/main/java/[package]/helpers/brokercode
┃   ┃ ┗ 📜BrokerErrorCode.java              ← @Component helper
┃   ┗ 📂finacle-retry-code/src/main/java/[package]/helpers/finacleretrycode
┃     ┗ 📜FinacleRetryCode.java
┗ 📂applications/app-service/src/main/java/[package]/config
  ┗ 📜BusinessParametersProperties.java     ← @ConfigurationProperties (env vars, no DB/gateway)
```

### Method → Layer Mapping

| BusinessUtils Method | Target Layer | Module | Return Type |
|---------------------|-------------|--------|-------------|
| `BackoutServiceUtil.buildBackoutRequest(...)` | Use Case (private) | `domain/usecase` | `BackoutRQ` (sync) |
| `BackoutServiceUtil.getBackoutRequestJson(...)` | Use Case (private) | `domain/usecase` | `String` (sync) |
| `DataServiceUtil.getParameterValue(...)` | Environment Variable | `applications/app-service/config` | `String` (sync, no `Mono`) |
| `DataServiceUtil.getParameterAttribute(...)` | Environment Variable | `applications/app-service/config` | `String` (sync, no `Mono`) |
| `DataServiceUtil.getParameterByValue(...)` | Environment Variable | `applications/app-service/config` | `boolean` (sync, no `Mono`) |
| `DataServiceUtil.getParameters(...)` | Environment Variables | `applications/app-service/config` | `Map<String, String>` (sync, no `Mono`) |
| `DataServiceUtil.getCustomer(...)` | WebClient Adapter | `driven-adapters/rest-consumer` | `Mono<Customer>` |
| `DataServiceUtil.homologateType(...)` | R2DBC Adapter | `driven-adapters/r2dbc-oracle` | `Mono<String>` |
| `DataServiceUtil.manageControlTransaction(...)` | R2DBC Adapter | `driven-adapters/r2dbc-oracle` | `Mono<Integer>` |
| `DataServiceUtil.clearMap()` | Delete | — | — |
| `SecurityServiceUtil.validateChannel(...)` | R2DBC Adapter | `driven-adapters/r2dbc-oracle` | `Mono<Boolean>` |
| `PaymentServiceUtil.executePaymentV2(...)` | Use Case + WebClient | `domain/usecase` + `rest-consumer` | `Mono<ResponseHeaderOutMessageType>` |
| `PaymentServiceUtil.isBrokerTimeout(...)` | Helper | `helpers/broker-code` | `boolean` (static) |
| `PaymentServiceUtil.isFinacleTechnicalError(...)` | Helper | `helpers/finacle-retry-code` | `boolean` (static) |
| `PaymentServiceUtil.homologateGuardaditoPocket(...)` | Use Case (private) | `domain/usecase` | `String` (sync) |
| `ApiGatewayUtil.createRequestApiGateway406(...)` | WebClient Adapter | `driven-adapters/rest-consumer` | `Mono<T>` |
| `BusinessUtil.findParameter(...)` | Inline (env var access) | `applications/app-service/config` | `properties.get(name)` (sync) |
| `BusinessUtil.successResponse(...)` | Use Case (private/static) | `domain/usecase` | `boolean` (sync) |
| `BusinessUtil.homologatePocket(...)` | Use Case (private) | `domain/usecase` | `Mono<List<PocketType>>` |

## Scaffolding Commands

Generate required modules using Scaffold Clean Architecture plugin:

```bash
# Use Cases
gradle generateUseCase --name=payment
# or shorthand
gradle guc --name payment

# R2DBC Driven Adapter
gradle generateDrivenAdapter --type=r2dbc
# or shorthand
gradle gda --type r2dbc

# WebClient Driven Adapter (rest-consumer)
gradle generateDrivenAdapter --type=restconsumer
# or shorthand
gradle gda --type restconsumer

# Helpers
gradle generateHelper --name=broker-code
gradle generateHelper --name=finacle-retry-code
# or shorthand
gradle gh --name=broker-code
gradle gh --name=finacle-retry-code
```

## Dependencies

### R2DBC Oracle Module (`infrastructure/driven-adapters/r2dbc-oracle/build.gradle`)

```groovy
dependencies {
    implementation project(':domain:model')
    implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
    implementation 'com.oracle.database.r2dbc:oracle-r2dbc'
    implementation 'io.projectreactor.addons:reactor-extra'
    implementation 'com.github.ben-manes.caffeine:caffeine'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

### WebClient Module (`infrastructure/driven-adapters/rest-consumer/build.gradle`)

```groovy
dependencies {
    implementation project(':domain:model')
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'io.netty:netty-handler'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

### Helper Module (`infrastructure/helpers/broker-code/build.gradle`)

```groovy
dependencies {
    implementation 'org.springframework:spring-context'
}
```

### Use Case Module (`domain/usecase/build.gradle`)

```groovy
dependencies {
    implementation project(':domain:model')
    implementation 'io.projectreactor:reactor-core'
    implementation 'tools.jackson.core:jackson-databind'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

### Application Configuration (`applications/app-service/src/main/resources/application.yaml`)

```yaml
spring:
  r2dbc:
    url: "${R2DBC_URL:r2dbc:oracle://localhost:1521/XEPDB1}"
    username: "${DB_USERNAME:}"
    password: "${DB_PASSWORD:}"
    pool:
      initial-size: 5
      max-size: 20
      max-idle-time: 30m

business:
  parameters:
    max-amount: "${MAX_AMOUNT:}"
    max-retries: "${MAX_RETRIES:}"
    timeout: "${TIMEOUT:}"
    retry-attribute: "${RETRY_ATTRIBUTE:}"
    allowed-status-codes: "${ALLOWED_STATUS_CODES:}"
    # One entry per parameter/attribute found in the "Database Parameters (Environment
    # Variable Candidates)" section of the get-java-ee-project-context analysis report —
    # never a PARAMETRO/PARAMETRO_ATRIBUTO/ATRIBUTO query.

adapter:
  webclient:
    customer:
      host: "${CUSTOMER_HOST:}"
      path: "${CUSTOMER_PATH:/api/v1/customers/details/{phoneNumber}}"
      connection-timeout: "${CUSTOMER_CONNECTION_TIMEOUT:3000}"
      read-timeout: "${CUSTOMER_READ_TIMEOUT:30000}"
    api-gateway:
      host: "${API_GATEWAY_HOST:}"
      path: "${API_GATEWAY_PATH:/services}"
      connection-timeout: "${API_GATEWAY_CONNECTION_TIMEOUT:3000}"
      read-timeout: "${API_GATEWAY_READ_TIMEOUT:30000}"
    payment:
      host: "${PAYMENT_HOST:}"
      path: "${PAYMENT_PATH:/api/v1/payments}"
      connection-timeout: "${PAYMENT_CONNECTION_TIMEOUT:3000}"
      read-timeout: "${PAYMENT_READ_TIMEOUT:30000}"
```

---

## Migration Instructions

Apply the changes specified for each utility class and method signature found in the project.

---

### BackoutServiceUtil

**Package:** `com.company.mdw.business.cmm`

#### `buildBackoutRequest(...)`

**Signature:**
```java
public static BackoutRQ buildBackoutRequest(
    String messageId, String name, String region,
    String consumerTypeId, String consumerTypeName,
    String containerTypeId, String containerTypeName,
    Boolean sendSNS, Boolean sendSQS
) throws CommonUtilException
```

**Do these changes when found:**

1. If there is no Lombok dependency, add it to the use case module `build.gradle`.
2. If `BackoutRQ` and related classes are not present in the project, include the ones provided in `./assets/backout`.
3. Before the next step, verify whether the calling class already has a utility class in use. If it does, declare the new method there.
4. Create this private method in the use case and use it instead:

```java
private BackoutRQ buildBackoutRequest(String messageId, String channel, String region,
                                      String consumerId, String consumerName,
                                      String containerTypeId, String containerTypeName,
                                      Boolean sns, Boolean sqs) {
    var formattedDate = ZonedDateTime.now(ZoneId.of("America/Bogota"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    var backout = new BackoutRQ();
    var backoutRequest = new BackoutRequestType();
    var header = new BcoHeaderType();
    var consumer = new ConsumerType();
    var container = new ContainerType();

    consumer.setId(consumerId);
    consumer.setName(consumerName);

    container.setId(containerTypeId);
    container.setName(containerTypeName);

    header.setConsumerDate(formattedDate);
    header.setName(channel);
    header.setRegion(region);
    header.setConsumer(consumer);
    header.setMessageID(messageId);
    header.setContainer(container);
    if (null == sqs || !sqs) {
        header.setSendSNS(sns);
    }
    if (null != sqs && sqs) {
        header.setSendSQS(true);
    }

    backoutRequest.setHeader(header);
    backout.setBackoutRequest(backoutRequest);
    return backout;
}
```

> **Note:** This method involves NO I/O, so it remains synchronous. It is invoked within the reactive chain via `Mono.fromCallable()` or directly in a `map`/`flatMap` operator.

---

#### `getBackoutRequestJson(...)`

**Signature:**
```java
public static String getBackoutRequestJson(
    BackoutRQ backoutRQ, Object backoutBodyObjectType
) throws CommonUtilException
```

**Do these changes when found:**

1. If `BackoutRQ` and related classes are not present in the project, include the ones provided in `./assets/backout`.
2. Add the Jackson 3 dependency to `domain/usecase/build.gradle`:
   ```groovy
   implementation 'tools.jackson.core:jackson-databind'
   ```
3. Create this private method in the use case:

```java
import tools.jackson.databind.json.JsonMapper;

private String getBackoutRequestJson(BackoutRQ backoutRQ, Object body) {
    backoutRQ.getBackoutRequest().setBody(body);
    return JsonMapper.builder().build().writeValueAsString(backoutRQ);
}
```

> **Note (Spring Boot 4.x):** Boot 4.x uses Jackson 3 by default (`tools.jackson.*` package). Do NOT use `com.fasterxml.jackson.databind.ObjectMapper` — that is Jackson 2 and is not included in Boot 4.x unless explicitly added as a legacy dependency.

> **Reactive usage:** Since Jackson serialization is CPU-bound (no I/O), call this directly within `map()`:
> ```java
> .map(backoutRQ -> getBackoutRequestJson(backoutRQ, bodyObject))
> ```

---

### DataServiceUtil

**Package:** `com.company.mdw.business.cmm`

#### `getParameterValue(...)`, `getParameterAttribute(...)`, `getParameterByValue(...)`, `getParameters(...)`

> **Detection scope:** These rules apply to ANY legacy implementation whose business purpose is reading configuration from the `PARAMETRO`, `PARAMETRO_ATRIBUTO`, or `ATRIBUTO` tables — not only the literal `DataServiceUtil.getParameter*(...)` calls. This includes direct JPA/`EntityManager` queries, DAO/ORM service calls (`ParametroDAO`, `ParametroJPAService`, `ParameterAttributeJPAService`), named queries, or native SQL/stored procedures against those tables. Regardless of how the legacy code reads the parameter, the migration output MUST be a Spring Boot **environment variable** exposed through `@ConfigurationProperties`. **Never** create a `ParameterGateway`, an R2DBC/reactive-JPA adapter, a cache (`CacheMono`), or any other database query implementation for these tables — parameter values are always sourced from configuration/env vars in the target project, not the database.
>
> Use the "Database Parameters (Environment Variable Candidates)" section of the project analysis report (produced by the `get-java-ee-project-context` skill) to get the exact parameter name/code and its suggested `UPPER_SNAKE_CASE` environment variable name for each call site.

**Signatures:**
```java
public static String getParameterValue(String parameterTypeId, String parameterName, String defaultValue, String region, EntityManager em) throws DataException
public static String getParameterAttribute(String parameterTypeId, String parameterName, String attributeName, String region, EntityManager em) throws DataException
public static String getParameterByValue(String parameterTypeId, String parameterValue, String region, EntityManager em) throws DataException
public static List<Parametro> getParameters(String parameterTypeId, String region, EntityManager em)
```

**Steps:**

1. For every distinct parameter name/code (`PARAMETRO`), attribute (`PARAMETRO_ATRIBUTO`/`ATRIBUTO`), or value-existence check found in the legacy code, add one field to a single `@ConfigurationProperties` class in `applications/app-service/src/main/java/[package]/config/`:

```java
package com.company.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "business.parameters")
public class BusinessParametersProperties {
    private String maxAmount;          // was PARAMETRO: typeId=10, name=MAX_AMOUNT
    private String maxRetries;         // was PARAMETRO: typeId=5,  name=MAX_RETRIES
    private String timeout;            // was PARAMETRO: typeId=5,  name=TIMEOUT
    private String retryAttribute;     // was PARAMETRO_ATRIBUTO/ATRIBUTO: name=RETRY_ATTRIBUTE
    private List<String> allowedStatusCodes; // was getParameterByValue(...) existence check
}
```

2. Bind each field to an environment variable in `application.yaml`:

```yaml
business:
  parameters:
    max-amount: "${MAX_AMOUNT:}"
    max-retries: "${MAX_RETRIES:}"
    timeout: "${TIMEOUT:}"
    retry-attribute: "${RETRY_ATTRIBUTE:}"
    allowed-status-codes: "${ALLOWED_STATUS_CODES:}" # comma-separated → bound to List<String>
```

3. **Replace call sites.** No `Mono`/reactive wrapping, gateway, or adapter is needed — the value is resolved once at startup from the environment, not fetched per request:

| Legacy call | Replacement |
|---|---|
| `getParameterValue(typeId, "MAX_AMOUNT", null, region, em)` | `properties.getMaxAmount()` |
| `getParameterValue(typeId, "MAX_AMOUNT", "10", region, em)` (with default) | `properties.getMaxAmount()` — set the default on the env var itself (`"${MAX_AMOUNT:10}"`) instead of in code |
| `getParameterAttribute(typeId, name, attributeName, region, em)` used only to echo back `parameterName` | `parameterName` directly (unchanged — it was never a real lookup) |
| `getParameterAttribute(typeId, name, attributeName, region, em)` used to fetch an attribute value | `properties.getRetryAttribute()` |
| `getParameterByValue(typeId, value, region, em)` (existence check) | `properties.getAllowedStatusCodes().contains(value)` |
| `getParameters(typeId, region, em)` (bulk map) | Inject `BusinessParametersProperties` and read individual fields directly — do not rebuild a `Map<String, String>` |
| `BusinessUtil.findParameter(parameters, name)` | `properties.get<Name>()` (the matching typed field) |

4. If the call site is inside a reactive chain (`flatMap`), reference the property field directly — since it requires no I/O, do not wrap it in `Mono.just(...)` unless strictly necessary to keep the chain's types aligned.

---

#### `getCustomer(...)`

> **Detection scope:** This rule applies to ANY legacy implementation whose business purpose is retrieving the customer/client by phone number — not only the literal `DataServiceUtil.getCustomer(...)` call. This includes direct `Cliente` JPA/`EntityManager` queries, DAO/ORM service calls (`ClienteDAO`, `ClienteJPAService`, `ParameterAttributeJPAService`-style services), named queries, or native SQL/stored procedures against the `CLIENTE`/`CLIENTE_*` tables. Regardless of how the legacy code fetches the customer, the migration output MUST always be the canonical reactive `customerGateway.getCustomer(phoneNumber, messageId)` WebClient call below. **Never** migrate this to a new database repository/adapter (e.g., an R2DBC or reactive JPA query) — customer data is always sourced from the customer service, not the database, in the target project.

**Signature:**
```java
public static Cliente getCustomer(
    String phoneNumber, EntityManager em
) throws DataException, JPAException
```

1. Generate the WebClient driven adapter if not present:
   ```bash
   gradle generateDrivenAdapter --type=restconsumer
   ```

2. Create the **Domain Model** in `domain/model/src/main/java/[package]/model/customer/`:

```java
package com.company.model.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private Long clientId;
    private String idType;
    private String idNumber;
    private String firstName;
    private String secondName;
    private String lastName;
    private String secondLastName;
    private String email;
    private String isEmailActivated;
    private String phoneNumber;
    private String accountNumber;
    private String cifId;
    private String clientType;
    private Integer statusId;
    private String expeditionDate;
    private String birthDate;
    private String region;
    private String biometry;
    private String userCreation;
    private String creationDate;
}
```

3. Create the **Gateway Interface** in `domain/model/src/main/java/[package]/model/customer/gateways/`:

```java
package com.company.model.customer.gateways;

import com.company.model.customer.Customer;
import reactor.core.publisher.Mono;

public interface CustomerGateway {
    Mono<Customer> getCustomer(String phoneNumber, String messageId);
}
```

4. Create the **WebClient Adapter** in `infrastructure/driven-adapters/rest-consumer/src/main/java/[package]/consumer/customer/`:

```java
package com.company.consumer.customer;

import com.company.consumer.customer.dto.CustomerDTO;
import com.company.consumer.customer.mapper.CustomerAdapterMapper;
import com.company.model.customer.Customer;
import com.company.model.customer.gateways.CustomerGateway;
import com.company.model.enums.TechnicalMessage;
import com.company.model.exceptions.BusinessException;
import com.company.model.exceptions.TechnicalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@RequiredArgsConstructor
@Slf4j
public class CustomerAdapter implements CustomerGateway {

    private final WebClient customerWebClient;
    private final CustomerAdapterMapper mapper;

    @Override
    public Mono<Customer> getCustomer(String phoneNumber, String messageId) {
        return customerWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/v1/customers/details/{phoneNumber}")
                .build(phoneNumber))
            .header("x-message-id", messageId)
            .retrieve()
            .bodyToMono(CustomerDTO.class)
            .doOnError(error -> log.error("Error calling customer service for phone={}", phoneNumber, error))
            .map(mapper::toDomain)
            .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.CUSTOMER_NOT_FOUND)))
            .onErrorMap(Predicate.not(BusinessException.class::isInstance),
                ex -> new TechnicalException(ex, TechnicalMessage.CUSTOMER_SERVICE_ERROR));
    }
}
```

> **Business vs. Technical:** "Customer not found" is a known, expected business outcome — it MUST be a `BusinessException`, never `TechnicalException`. Only truly unexpected failures (timeouts, connection errors, unmapped responses) become `TechnicalException`, via the guarded catch-all as the LAST operator so an already-thrown `BusinessException` is never re-wrapped. See `CustomerAdapter` in `transfer_service_MS` for the reference implementation of this split.

5. Create the **WebClient Configuration**:

```java
package com.company.consumer.customer.config;

import com.company.consumer.customer.CustomerAdapter;
import com.company.consumer.customer.mapper.CustomerAdapterMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class CustomerWebClientConfig {

    @Bean(name = "customerWebClient")
    public WebClient customerWebClient(WebClient.Builder builder,
                                        @Value("${adapter.webclient.customer.host}") String host,
                                        @Value("${adapter.webclient.customer.connection-timeout}") int connectionTimeout,
                                        @Value("${adapter.webclient.customer.read-timeout}") int readTimeout) {
        ConnectionProvider provider = ConnectionProvider.builder("customer")
            .maxConnections(50)
            .pendingAcquireTimeout(Duration.ofMillis(connectionTimeout))
            .maxIdleTime(Duration.ofSeconds(30))
            .maxLifeTime(Duration.ofSeconds(60))
            .build();

        HttpClient httpClient = HttpClient.create(provider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
            .doOnConnected(conn -> {
                conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS));
                conn.addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS));
            });

        return builder
            .baseUrl(host)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    @Bean
    public CustomerAdapter customerAdapter(@Qualifier("customerWebClient") WebClient webClient,
                                            CustomerAdapterMapper mapper) {
        return new CustomerAdapter(webClient, mapper);
    }
}
```

6. Replace: `DataServiceUtil.getCustomer(phoneNumber, em)`
   With: `customerGateway.getCustomer(phoneNumber, messageId)`

   > Note: Return type changes from `Cliente` to `Mono<Customer>`. `DataException`/`JPAException` are replaced by reactive error signals. See field mapping table in base skill.

---

#### `homologateType(...)`

**Signature:**
```java
public static String homologateType(
    String sourceSystem, String destinationSystem,
    String keyCode, String sourceValue, EntityManager em
) throws JPAException
```

1. Create the **Gateway Interface** in `domain/model/src/main/java/[package]/model/homologation/gateways/`:

```java
package com.company.model.homologation.gateways;

import reactor.core.publisher.Mono;

public interface HomologationTypeGateway {
    Mono<String> getDestinationValue(String originSystem, String destinationSystem, String type, String originValue);
}
```

2. Create the **R2DBC Adapter**:

```java
package com.company.r2dbc.homologation;

import com.company.model.homologation.gateways.HomologationTypeGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class HomologationAdapter implements HomologationTypeGateway {

    private final DatabaseClient databaseClient;

    private static final String QUERY_HOMOLOGATION = """
            SELECT h.VALOR_DESTINO
            FROM MY_SCHEMA.HOMOLOGACION_TIPO h
            WHERE UPPER(h.SISTEMA_ORIGEN) = UPPER(:originSystem)
              AND UPPER(h.SISTEMA_DESTINO) = UPPER(:destinationSystem)
              AND UPPER(h.TIPO) = UPPER(:type)
              AND UPPER(h.VALOR_ORIGEN) = UPPER(:originValue)
            """;

    @Override
    public Mono<String> getDestinationValue(String originSystem, String destinationSystem,
                                             String type, String originValue) {
        return databaseClient
            .sql(QUERY_HOMOLOGATION)
            .bind("originSystem", originSystem)
            .bind("destinationSystem", destinationSystem)
            .bind("type", type)
            .bind("originValue", originValue)
            .map((row, metadata) -> row.get("VALOR_DESTINO", String.class))
            .one()
            .doOnSubscribe(s -> log.debug("Homologating: {} -> {} [{}:{}]", originSystem, destinationSystem, type, originValue));
    }
}
```

3. Replace: `homologateType(originSystem, destinationSystem, type, originValue, em)`
   With: `homologationTypeGateway.getDestinationValue(originSystem, destinationSystem, type, originValue)`

   > Note: Returns `Mono.empty()` if no match is found (equivalent to returning `null` in the legacy code). Use `.defaultIfEmpty("")` or `.switchIfEmpty(...)` as needed at the call site.

---

#### `manageControlTransaction(...)`

**Signature:**
```java
public static void manageControlTransaction(
    ControlTransaccion controlTransaccion,
    String status, EntityManager em
) throws JPAException
```

1. Create the **Gateway Interface** in `domain/model/src/main/java/[package]/model/transactioncontrol/gateways/`:

```java
package com.company.model.transactioncontrol.gateways;

import reactor.core.publisher.Mono;

public interface TransactionControlGateway {
    Mono<Integer> updateTransactionControlStatus(String transactionId, String status);
}
```

2. Create the **R2DBC Adapter**:

```java
package com.company.r2dbc.transactioncontrol;

import com.company.model.transactioncontrol.gateways.TransactionControlGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class TransactionControlAdapter implements TransactionControlGateway {

    private final DatabaseClient databaseClient;

    private static final String UPDATE_STATUS = """
            UPDATE MY_SCHEMA.CONTROL_TRANSACCION
            SET ESTADO = :status, FECHA_MODIFICACION = CURRENT_TIMESTAMP
            WHERE TRANSACCION_ID = :transactionId
            """;

    @Override
    public Mono<Integer> updateTransactionControlStatus(String transactionId, String status) {
        return databaseClient
            .sql(UPDATE_STATUS)
            .bind("status", status)
            .bind("transactionId", transactionId)
            .fetch()
            .rowsUpdated()
            .map(Long::intValue)
            .doOnSuccess(rows -> log.debug("Updated transaction control: transactionId={}, status={}, rows={}", transactionId, status, rows))
            .doOnError(error -> log.error("Error updating transaction control: transactionId={}", transactionId, error));
    }
}
```

3. Replace: `manageControlTransaction(controlTransaccion, status, em)`
   With: `transactionControlGateway.updateTransactionControlStatus(transactionId, status)`

   > Note: Returns `Mono<Integer>` (affected rows). The result may be ignored with `.then()` in the reactive chain.

**Reactive usage example:**
```java
return transactionControlGateway.updateTransactionControlStatus(txId, "COMPLETED")
    .then(processNextStep(request));
```

---

#### `clearMap()`

**Signature:**
```java
public static void clearMap()
```

Delete every call to `clearMap()` — no replacement needed. The reactive cache is managed by Caffeine TTL.

---

### SecurityServiceUtil

**Package:** `com.company.mdw.business.cmm`

#### `validateChannel(...)`

**Signature:**
```java
public static boolean validateChannel(
    String channel, EntityManager em
) throws JPAException
```

1. Create the **Gateway Interface** in `domain/model/src/main/java/[package]/model/channel/gateways/`:

```java
package com.company.model.channel.gateways;

import reactor.core.publisher.Mono;

public interface ChannelGateway {
    Mono<Boolean> channelExists(String channelId);
}
```

2. Create the **R2DBC Adapter**:

```java
package com.company.r2dbc.channel;

import com.company.model.channel.gateways.ChannelGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class ChannelAdapter implements ChannelGateway {

    private final DatabaseClient databaseClient;

    private static final String QUERY_CHANNEL_EXISTS = """
            SELECT COUNT(1) AS CNT
            FROM MY_SCHEMA.CANAL c
            WHERE UPPER(c.CANAL_ID) = UPPER(:channelId)
              AND c.ESTADO = 'ACTIVO'
            """;

    @Override
    public Mono<Boolean> channelExists(String channelId) {
        return databaseClient
            .sql(QUERY_CHANNEL_EXISTS)
            .bind("channelId", channelId)
            .map((row, metadata) -> row.get("CNT", Long.class))
            .one()
            .map(count -> count > 0)
            .defaultIfEmpty(false);
    }
}
```

3. For each use case that calls `validateChannel`, inject `ChannelGateway` and declare:

```java
private Mono<Boolean> channelValidation(String id) {
    if (id == null || id.equalsIgnoreCase("MF-001")) {
        return Mono.just(false);
    }
    return channelGateway.channelExists(id);
}
```

4. Replace: `SecurityServiceUtil.validateChannel(channel, em)`
   With: `this.channelValidation(channel)`

**Reactive usage example (with validation gate):**
```java
return channelValidation(request.getChannel())
    .flatMap(isValid -> {
        if (!isValid) {
            return Mono.error(new BusinessException(TechnicalMessage.INVALID_CHANNEL));
        }
        return processRequest(request);
    });
```

---

### PaymentServiceUtil

**Package:** `com.company.mdw.business.cmm`

#### `executePaymentV2(...)`

Find all usages of either overload:

**Overload 3.2 — individual parameters:**
```java
public static ResponseHeaderOutMessageType executePaymentV2(
    HttpHeaders httpHeaders, String channel, String messageId,
    String region, String accountNumber, String accountAccounting,
    String value, String tranInfo, String transactionCode,
    String tranParticulars, String tranParticularsC, String tranPart,
    String paymentSubType, String partTrnRmksC, String partTrnRmksD,
    Object pockets, String phoneNumber,
    String reverseTransactionType, String reverseTranparticulars,
    String reverseTranparticularsD, boolean creditAccountFirst,
    GenericLogger logger, CallAsynchronousRESTBean callAsynchronousRESTBean,
    EntityManager em, ServiceRegistry serviceRegistry
) throws MiddlewareException, CommonUtilException
```

**Overload 3.3 — pre-built request:**
```java
public static ResponseHeaderOutMessageType executePaymentV2(
    HttpHeaders httpHeaders, String channel, String messageId,
    String phoneNumber, String region,
    ExecutePaymentV2ServiceRequestType executePaymentV2,
    String reverseTransactionType, String reverseTranparticulars,
    String reverseTranparticularsD, boolean creditAccountFirst,
    GenericLogger logger, CallAsynchronousRESTBean callAsynchronousRESTBean,
    EntityManager em, ServiceRegistry serviceRegistry
) throws MiddlewareException
```

**Steps:**

1. Create the **Gateway Interface** in `domain/model/src/main/java/[package]/model/payment/gateways/`:

```java
package com.company.model.payment.gateways;

import com.company.model.payment.PaymentRequest;
import com.company.model.payment.PaymentResponse;
import reactor.core.publisher.Mono;

public interface PaymentGateway {
    Mono<PaymentResponse> executePayment(PaymentRequest request);
}
```

2. Create the **PaymentUseCase** in `domain/usecase/src/main/java/[package]/usecase/payment/`:

```java
package com.company.usecase.payment;

import com.company.model.homologation.gateways.HomologationTypeGateway;
import com.company.model.payment.PaymentRequest;
import com.company.model.payment.PaymentResponse;
import com.company.model.payment.gateways.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class PaymentUseCase {

    private final PaymentGateway paymentGateway;
    private final HomologationTypeGateway homologationTypeGateway;

    public Mono<PaymentResponse> executePaymentV2(PaymentRequest request) {
        return homologatePocketTypes(request)
            .flatMap(paymentGateway::executePayment)
            .doOnSuccess(response -> log.info("Payment executed: messageId={}", request.getMessageId()));
    }

    private Mono<PaymentRequest> homologatePocketTypes(PaymentRequest request) {
        if (request.getPockets() == null || request.getPockets().isEmpty()) {
            return Mono.just(request);
        }
        return Flux.fromIterable(request.getPockets())
            .flatMap(pocket -> homologationTypeGateway
                .getDestinationValue("SHERPA", "FINACLE", "TIPOBOL", pocket.getPocketType())
                .defaultIfEmpty(pocket.getPocketType())
                .map(homologated -> {
                    pocket.setPocketType(homologated);
                    return pocket;
                }))
            .collectList()
            .map(pockets -> {
                request.setPockets(pockets);
                return request;
            });
    }
}
```

> Note: The old blanket `onErrorMap(... -> new TechnicalException(...))` at the end of `executePaymentV2` was removed. A use case must never downgrade or re-wrap an error — by the time `paymentGateway.executePayment(...)` completes, `PaymentAdapter` (below) has already classified the failure as `BusinessException` or `TechnicalException`; the use case just propagates it unchanged. Only construct a `BusinessException` here if the use case itself performs additional domain validation (as `CustomerUseCase.validateCustomer` does in `transfer_service_MS`).

3. Create the **WebClient Adapter** in `infrastructure/driven-adapters/rest-consumer/src/main/java/[package]/consumer/payment/`:

```java
package com.company.consumer.payment;

import com.company.consumer.payment.dto.PaymentRequestDTO;
import com.company.consumer.payment.dto.PaymentResponseDTO;
import com.company.consumer.payment.mapper.PaymentAdapterMapper;
import com.company.model.enums.TechnicalMessage;
import com.company.model.exceptions.BusinessException;
import com.company.model.exceptions.TechnicalException;
import com.company.model.payment.PaymentRequest;
import com.company.model.payment.PaymentResponse;
import com.company.model.payment.gateways.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@RequiredArgsConstructor
@Slf4j
public class PaymentAdapter implements PaymentGateway {

    private final WebClient paymentWebClient;
    private final PaymentAdapterMapper mapper;

    @Override
    public Mono<PaymentResponse> executePayment(PaymentRequest request) {
        PaymentRequestDTO dto = mapper.toDto(request);
        return paymentWebClient.post()
            .uri("/api/v1/payments/execute")
            .bodyValue(dto)
            .retrieve()
            .bodyToMono(PaymentResponseDTO.class)
            .doOnError(error -> log.error("Error calling payment service: messageId={}", request.getMessageId(), error))
            .onErrorMap(WebClientResponseException.BadRequest.class,
                ex -> new BusinessException(TechnicalMessage.PAYMENT_REJECTED))
            .onErrorMap(Predicate.not(BusinessException.class::isInstance),
                ex -> new TechnicalException(ex, TechnicalMessage.PAYMENT_SERVICE_ERROR))
            .map(mapper::toDomain);
    }
}
```

4. **Parameter mapping — overload 3.2 → new:**

| Old parameter | Maps to `PaymentRequest` field | Notes |
|---|---|---|
| `httpHeaders` | — | Dropped |
| `channel` | `channel` | 1:1 |
| `messageId` | `messageId` | 1:1 |
| `region` | `region` | 1:1 |
| `accountNumber` | `accountNumber` | 1:1 |
| `accountAccounting` | `accountAccounting` | 1:1 |
| `value` | `value` | 1:1 |
| `tranInfo` | `tranInfo` | 1:1 |
| `transactionCode` | `transactionCode` | 1:1 |
| `tranParticulars` | `tranParticulars` | 1:1 |
| `tranParticularsC` | `tranParticularsC` | 1:1 |
| `tranPart` | `tranPart` | 1:1 |
| `paymentSubType` | `paymentSubType` | 1:1 |
| `partTrnRmksC` | `partTrnRmksC` | 1:1 |
| `partTrnRmksD` | `partTrnRmksD` | 1:1 |
| `pockets` (Object) | `pockets` (List&lt;PocketType&gt;) | Cast if raw Object |
| `phoneNumber` | `phoneNumber` | 1:1 |
| `reverseTransactionType` | `reverseTransactionType` | 1:1 |
| `reverseTranparticulars` | `reverseTranparticulars` | 1:1 |
| `reverseTranparticularsD` | `reverseTranparticularsD` | 1:1 |
| `creditAccountFirst` | `creditAccountFirst` | 1:1 |
| `logger` | — | Dropped (use `@Slf4j`) |
| `callAsynchronousRESTBean` | — | Dropped (replaced by WebClient) |
| `em` | — | Dropped |
| `serviceRegistry` | — | Dropped |

5. Replace overload 3.3 by extracting fields from `ExecutePaymentV2ServiceRequestType`:

```java
ExecutePaymentV2RQType rq = executePaymentV2ServiceRequestType.getExecutePaymentV2RQ();
var paymentRequest = PaymentRequest.builder()
    .channel(channel)
    .messageId(messageId)
    .region(region)
    .accountNumber(rq.getAccountNumber())
    .accountAccounting(rq.getAccountAccounting())
    .value(rq.getValue())
    .tranInfo(rq.getTranInfo())
    .transactionCode(rq.getTransactionCode())
    .tranParticulars(rq.getNameCommerce())
    .tranParticularsC(rq.getTrnParticularsC())
    .tranPart(rq.getTranPart())
    .paymentSubType(rq.getPaymentSubType())
    .partTrnRmksC(rq.getPartTrnRmksD())       // SWAPPED (known bug)
    .partTrnRmksD(rq.getPartTrnRmksC())       // SWAPPED (known bug)
    .pockets(rq.getPockets())
    .phoneNumber(phoneNumber)
    .reverseTransactionType(reverseTransactionType)
    .reverseTranparticulars(reverseTranparticulars)
    .reverseTranparticularsD(reverseTranparticularsD)
    .creditAccountFirst(creditAccountFirst)
    .build();
return paymentUseCase.executePaymentV2(paymentRequest);
```

> **Remarks (overload 3.3 only):** `partTrnRmksC` and `partTrnRmksD` are stored in swapped fields inside `ExecutePaymentV2RQType` due to a known bug. Pass `rq.getPartTrnRmksD()` as `partTrnRmksC` and vice versa. Do NOT apply this swap for overload 3.2.

> Note: `MiddlewareException` and `CommonUtilException` are no longer thrown. Remove them from `throws` declarations and `catch` blocks — errors propagate as reactive error signals.

---

#### `isBrokerTimeout(...)`

**Signature:**
```java
public static boolean isBrokerTimeout(
    ResponseHeaderOutMessageType responseHeaderOutMessage
)
```

1. Generate the helper module:
   ```bash
   gradle generateHelper --name=broker-code
   ```

2. Create `BrokerErrorCode.java` in `infrastructure/helpers/broker-code/src/main/java/[package]/helpers/brokercode/`:

```java
package com.company.helpers.brokercode;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BrokerErrorCode {

    private static final Set<String> ERROR_CODES = Set.of(
        "FIS-9010", "FIS-0999", "FIS-9001", "FIS-9002", "FIS-9003",
        "FIS-9004", "FIS-9005", "FIS-9006", "FIS-9007", "FIS-9008"
    );

    public static boolean isErrorCode(String code) {
        return code != null && ERROR_CODES.contains(code);
    }
}
```

3. Replace: `PaymentServiceUtil.isBrokerTimeout(response)`
   With: `BrokerErrorCode.isErrorCode(response.getResponseHeaderOut().getHeader().getStatusCode())`

   If the response chain can be null, extract defensively:
   ```java
   String code = Optional.ofNullable(response)
           .map(PaymentResponse::getResponseHeaderOut)
           .map(ResponseHeaderOut::getHeader)
           .map(HeaderType::getStatusCode)
           .orElse(null);
   boolean isError = BrokerErrorCode.isErrorCode(code);
   ```

> **Note:** This method is static with NO I/O — it remains synchronous and can be called directly in `map()` or `filter()` operators.

---

#### `isFinacleTechnicalError(...)`

**Signature:**
```java
public static boolean isFinacleTechnicalError(
    ResponseHeaderOutMessageType responseHeaderOutMessage,
    String region, EntityManager em,
    GenericLogger logger, String messageId, String consumerId
)
```

1. Generate the helper module:
   ```bash
   gradle generateHelper --name=finacle-retry-code
   ```

2. Create `FinacleRetryCode.java` in `infrastructure/helpers/finacle-retry-code/src/main/java/[package]/helpers/finacleretrycode/`:

```java
package com.company.helpers.finacleretrycode;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FinacleRetryCode {

    private static final Set<String> RETRY_CODES = Set.of(
        "1001", "1002", "1003", "1004", "1005",
        "2001", "2002", "3001", "9999"
    );

    public static boolean isRetryCode(String code) {
        if (code == null) return false;
        try {
            return RETRY_CODES.contains(code);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
```

3. Replace: `PaymentServiceUtil.isFinacleTechnicalError(response, region, em, logger, messageId, consumerId)`
   With: `FinacleRetryCode.isRetryCode(response.getResponseHeaderOut().getHeader().getStatusCode())`

   > Note: `isRetryCode(String)` safely returns `false` for `null` or non-numeric input. All extra parameters (`region`, `em`, `logger`, etc.) are dropped.

---

#### `homologateGuardaditoPocket(...)`

**Signature:**
```java
public static String homologateGuardaditoPocket(
    String source, String destination, String pocketType,
    String region, String defaultValue, EntityManager em
) throws JPAException
```

Count how many times `homologateGuardaditoPocket` is called in the class:

- **More than once** → declare as `private static`:
```java
private static String homologateGuardaditoPocket(String pocketType) {
    if ("GU".equalsIgnoreCase(pocketType) || "2".equals(pocketType)) {
        return "Guardadito";
    }
    return null;
}
```

- **Only once** → declare as `private`:
```java
private String homologateGuardaditoPocket(String pocketType) {
    if ("GU".equalsIgnoreCase(pocketType) || "2".equals(pocketType)) {
        return "Guardadito";
    }
    return null;
}
```

Replace: `PaymentServiceUtil.homologateGuardaditoPocket(source, destination, pocketType, region, defaultValue, em)`
With: `homologateGuardaditoPocket(pocketType)`

> Note: This method involves NO I/O, so it remains synchronous. `JPAException` is no longer thrown. The `defaultValue` was always `"Guardadito"` at known call sites.

---

### ApiGatewayUtil

**Package:** `com.company.mdw.business.util`

#### `createRequestApiGateway406(...)`

Find all usages of either overload:

**Without AddressType (6.1):**
```java
public static <T> T createRequestApiGateway406(
    HttpHeaders httpHeaders, String messageId, String consumerId, String region,
    ServiceRegistry serviceRegistry, String operation, String serviceName, String version,
    Object requestBodyObjectType, Class<?> classMessage
) throws CommonUtilException, RestClientException
```

**With AddressType (6.2):**
```java
public static <T> T createRequestApiGateway406(
    HttpHeaders httpHeaders, String messageId, String consumerId, String region,
    ServiceRegistry serviceRegistry, String operation, String serviceName, String version,
    Object requestBodyObjectType, Class<?> classMessage, AddressType addressType
) throws CommonUtilException, RestClientException
```

**Steps:**

1. Create the **Gateway Interface** in `domain/model/src/main/java/[package]/model/apigateway/gateways/`:

```java
package com.company.model.apigateway.gateways;

import reactor.core.publisher.Mono;

public interface ApiGatewayGateway {
    <T> Mono<T> callApi(String messageId, String consumerId, String region,
                         String serviceName, String operation, String version,
                         Object body, Class<T> responseType);
}
```

2. Create the **WebClient Adapter** in `infrastructure/driven-adapters/rest-consumer/src/main/java/[package]/consumer/apigateway/`:

```java
package com.company.consumer.apigateway;

import com.company.consumer.apigateway.dto.ApiGatewayRequest;
import com.company.consumer.apigateway.mapper.ApiGatewayMapper;
import com.company.model.apigateway.gateways.ApiGatewayGateway;
import com.company.model.enums.TechnicalMessage;
import com.company.model.exceptions.BusinessException;
import com.company.model.exceptions.TechnicalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@RequiredArgsConstructor
@Slf4j
public class ApiGatewayAdapter implements ApiGatewayGateway {

    private final WebClient gatewayWebClient;
    private final ApiGatewayMapper mapper;

    @Override
    public <T> Mono<T> callApi(String messageId, String consumerId, String region,
                                String serviceName, String operation, String version,
                                Object body, Class<T> responseType) {
        ApiGatewayRequest request = mapper.toRequest(messageId, consumerId, region,
            serviceName, operation, version, body);

        return gatewayWebClient.post()
            .uri("/services")
            .header("x-message-id", messageId)
            .header("x-consumer-id", consumerId)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(responseType)
            .doOnError(error -> log.error("Error calling API Gateway: service={}, operation={}", serviceName, operation, error))
            .onErrorMap(Predicate.not(BusinessException.class::isInstance),
                ex -> new TechnicalException(ex, TechnicalMessage.API_GATEWAY_ERROR));
    }
}
```

> Note: This is a generic proxy shared by every downstream service invoked through the API Gateway. It only classifies the default/unexpected case as `TechnicalException`; a caller wrapping `callApi(...)` for a specific operation is responsible for mapping that operation's known business-rejection responses to `BusinessException` before this generic error mapping runs (as `PaymentAdapter` does above).

3. Create the **WebClient Configuration**:

```java
package com.company.consumer.apigateway.config;

import com.company.consumer.apigateway.ApiGatewayAdapter;
import com.company.consumer.apigateway.mapper.ApiGatewayMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class ApiGatewayWebClientConfig {

    @Bean(name = "gatewayWebClient")
    public WebClient gatewayWebClient(WebClient.Builder builder,
                                       @Value("${adapter.webclient.api-gateway.host}") String host,
                                       @Value("${adapter.webclient.api-gateway.connection-timeout}") int connectionTimeout,
                                       @Value("${adapter.webclient.api-gateway.read-timeout}") int readTimeout) {
        ConnectionProvider provider = ConnectionProvider.builder("api-gateway")
            .maxConnections(100)
            .pendingAcquireTimeout(Duration.ofMillis(connectionTimeout))
            .maxIdleTime(Duration.ofSeconds(30))
            .maxLifeTime(Duration.ofSeconds(60))
            .build();

        HttpClient httpClient = HttpClient.create(provider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
            .doOnConnected(conn -> {
                conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS));
                conn.addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS));
            });

        return builder
            .baseUrl(host)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    @Bean
    public ApiGatewayAdapter apiGatewayAdapter(@Qualifier("gatewayWebClient") WebClient webClient,
                                                ApiGatewayMapper mapper) {
        return new ApiGatewayAdapter(webClient, mapper);
    }
}
```

4. **Parameter mapping — old → new (both overloads):**

| Old parameter | New parameter | Notes |
|---|---|---|
| `httpHeaders` | — | Dropped |
| `messageId` | `messageId` | 1:1 |
| `consumerId` | `consumerId` | 1:1 |
| `region` | `region` | 1:1 |
| `serviceRegistry` | — | Dropped (replaced by `application.yaml`) |
| `operation` | `operation` | 1:1 |
| `serviceName` | `serviceName` | 1:1 |
| `version` | `version` | 1:1 |
| `requestBodyObjectType` | `body` | Generic `Object` |
| `classMessage` | `responseType` | `Class<T>` |
| `addressType` (6.2 only) | — | Dropped |

5. Replace both overloads with:
```java
apiGatewayGateway.callApi(messageId, consumerId, region, serviceName, operation, version,
    requestBody, ResponseType.class)
```

> Note: Exceptions change from checked `CommonUtilException`, `RestClientException` to reactive error signals (`TechnicalException` via `onErrorMap`). Remove `catch (CommonUtilException | RestClientException e)` blocks.

**Reactive usage example:**
```java
return apiGatewayGateway.callApi(messageId, consumerId, region,
        "accountService", "getBalance", "1.0", requestBody, BalanceResponse.class)
    .flatMap(response -> processBalance(response));
```

---

### BusinessUtil

**Package:** `com.company.mdw.business.util`

#### `findParameter(...)`

**Signature:**
```java
public static Parametro findParameter(List<Parametro> parameters, String name)
```

This method is only valid after `getParameters(...)` has been replaced per the `DataServiceUtil` rules above — the legacy `List<Parametro>` no longer exists.

Replace: `BusinessUtil.findParameter(parameters, name)`
With: `businessParametersProperties.get<Name>()` — the typed field on `BusinessParametersProperties` matching `name`.

> Note: Return type changes from `Parametro` to `String`/`List<String>` (whatever the field's type is). Remove any chained `.getValor()` calls. No `Mono`/`flatMap` unwrapping is needed — the value is a plain injected field, not fetched from a database.

---

#### `successResponse(...)`

**Signature:**
```java
public static boolean successResponse(MessageRS messageRS)
```

Count how many times `successResponse` is called in the class:

- **More than once** → declare as `public static` in a utility class:
```java
public static boolean isSuccessResponse(MessageRS messageRS) {
    if (messageRS == null) return false;
    if (messageRS.getResponseMessage() == null) return false;
    if (messageRS.getResponseMessage().getHeader() == null) return false;
    return "0".equals(messageRS.getResponseMessage().getHeader().getStatus().getCode());
}
```

- **Only once** → declare as `private` in the use case:
```java
private boolean isSuccessResponse(MessageRS messageRS) {
    if (messageRS == null) return false;
    if (messageRS.getResponseMessage() == null) return false;
    if (messageRS.getResponseMessage().getHeader() == null) return false;
    return "0".equals(messageRS.getResponseMessage().getHeader().getStatus().getCode());
}
```

> **Note:** This method involves NO I/O — used in `map()` or `filter()` within the reactive chain:
> ```java
> .filter(this::isSuccessResponse)
> .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.PAYMENT_FAILED)))
> ```

---

#### `homologatePocket(...)`

**Signature:**
```java
public static void homologatePocket(
    List<PocketType> pocketList, EntityManager em
) throws JPAException
```

**Reactive replacement** — since `HomologationTypeGateway.getDestinationValue()` returns `Mono<String>`, the pocket homologation must be reactive:

1. Ensure `HomologationTypeGateway` is available (from `homologateType` migration above).
2. In the use case, declare as a private method:

```java
import reactor.core.publisher.Flux;

private Mono<List<PocketType>> homologatePocket(List<PocketType> pocketList) {
    if (pocketList == null || pocketList.isEmpty()) {
        return Mono.just(List.of());
    }
    return Flux.fromIterable(pocketList)
        .flatMap(pocket -> homologationTypeGateway
            .getDestinationValue("SHERPA", "FINACLE", "TIPOBOL", pocket.getPocketType())
            .defaultIfEmpty(pocket.getPocketType())
            .map(destinationValue -> {
                var copy = new PocketType();
                copy.setPocketType(destinationValue);
                copy.setNamePocket(pocket.getNamePocket());
                copy.setValuePocket(pocket.getValuePocket());
                copy.setNewValuePocket(pocket.getNewValuePocket());
                return copy;
            }))
        .collectList();
}
```

3. Replace: `BusinessUtil.homologatePocket(pocketList, em)`
   With: `this.homologatePocket(pocketList)` (returns `Mono<List<PocketType>>`)

> Note: Return type changes from `void` to `Mono<List<PocketType>>`. The old method mutated the list in-place; the reactive version creates copies and collects to a new list. `JPAException` is no longer thrown — errors propagate reactively.

**Reactive usage example:**
```java
return homologatePocket(request.getPockets())
    .flatMap(homologatedPockets -> {
        request.setPockets(homologatedPockets);
        return paymentGateway.executePayment(request);
    });
```

---

## Best Practices

### Reactive Composition

- Use `flatMap` for sequential dependent operations (result of A needed by B)
- Use `Mono.zip()` for parallel independent operations
- Use `switchIfEmpty(Mono.error(...))` instead of null-check-then-throw
- Use `onErrorMap` to wrap unexpected exceptions into domain exceptions
- Use `onErrorResume` for fallback/recovery strategies
- Use `.then()` when you need to chain but discard the previous value
- Use `doOnSuccess`/`doOnError` for side effects (logging) — never for business logic

### Error Handling

| Legacy Pattern | Reactive Replacement |
|---------------|---------------------|
| `try { ... } catch (DataException e) { return error }` | `.onErrorMap(ex -> new TechnicalException(...))` |
| `if (result == null) throw new DataException(...)` | `.switchIfEmpty(Mono.error(new BusinessException(...)))` |
| `JPAException` in signature | Remove — errors propagate as reactive signals |
| `CommonUtilException` in signature | Remove — wrapped in `onErrorMap` |
| `MiddlewareException` in signature | Remove — replaced by `TechnicalException` |

### Gateway Design

- Every gateway method returns `Mono<T>` or `Flux<T>` — NEVER blocking types
- Gateway interfaces live in `domain/model/[entity]/gateways/` — the domain layer has NO dependency on infrastructure
- Adapters implement gateways and live in `infrastructure/driven-adapters/`
- Adapter beans are wired via `@Configuration` classes (not `@Service` or `@Repository` annotations)

### Constructor Injection

- Use `@RequiredArgsConstructor` (Lombok) with `private final` fields
- No `@Autowired`, no field injection, no setter injection
- Use cases have NO Spring annotations — they are plain classes wired by configuration

### Caching

- Use `CacheMono` from `reactor-extra` for reactive caching
- Use Caffeine as the underlying cache store (TTL-based eviction)
- Cache keys must be deterministic and include all query parameters
- Do NOT use Spring `@Cacheable` (blocking cache abstraction incompatible with reactive)

### Testing

- Use `StepVerifier` from `reactor-test` for verifying reactive chains
- Mock gateways with `Mockito` + `when(...).thenReturn(Mono.just(...))`
- Test error paths with `StepVerifier.create(...).expectError(ExceptionType.class).verify()`

---

## Post-Migration Validation Checklist

After completing the migration of all BusinessUtils call sites, verify:

- [ ] **No `MDW.CMM.BusinessUtils` imports remain** — search across all Java files
- [ ] **No `EntityManager` parameters** — all `em` references removed
- [ ] **No `DataException`, `JPAException`, `CommonUtilException`, `MiddlewareException`** in throws declarations
- [ ] **No `RestClient` / `RestTemplate`** — all HTTP uses `WebClient`
- [ ] **No `spring-boot-starter-data-jdbc`** — replaced by `spring-boot-starter-data-r2dbc`
- [ ] **No blocking calls** — no `.block()`, `.blockFirst()`, `.blockLast()` in non-test code
- [ ] **All gateway methods return `Mono<T>` or `Flux<T>`** — verify interface signatures
- [ ] **R2DBC connection pool configured** — check `application.yaml` has pool settings
- [ ] **WebClient connection providers configured** — check timeout and pool settings
- [ ] **No `ParameterGateway`/R2DBC adapter for `PARAMETRO`, `PARAMETRO_ATRIBUTO`, `ATRIBUTO`** — verify these are `@ConfigurationProperties` fields, not database queries
- [ ] **`BusinessParametersProperties` bound to env vars** — every parameter/attribute found in the analysis report has a matching field and `application.yaml` entry
- [ ] **Helper modules annotated with `@Component`** — verify they are component-scanned
- [ ] **Use cases have NO Spring annotations** — no `@Service`, `@Component`, `@Repository`
- [ ] **Jackson 3 imports** — verify `tools.jackson.*` (not `com.fasterxml.jackson.*`)
- [ ] **Tests pass with `StepVerifier`** — all reactive chains verified
- [ ] **Compilation succeeds** — run `gradle build` with no errors
- [ ] **`clearMap()` calls deleted** — no replacements needed
- [ ] **`ServiceRegistry` references removed** — replaced by `application.yaml` properties

## Constraints and Warnings

- **Never carry over `EntityManager`** — it must be dropped from all replaced signatures.
- **Do not copy asset classes blindly if they already exist** — merge missing methods and fields instead.
- **`addressType` in `ApiGatewayUtil` overload 6.2 is silently dropped** — if a non-null `AddressType` was being passed, confirm with the team.
- **`partTrnRmksC`/`partTrnRmksD` swap** applies only when replacing `executePaymentV2` overload 3.3.
- **`bankId` and `currencyCode`** in Payment are hard-coded to `"1500"` and `"COP"`. If the project targets a different region, update those literals.
- **Do not implement Spring Security** for security-related utilities.
- **Do not use `@Transactional`** on R2DBC adapters — use `TransactionalOperator` if transactional boundaries are needed.
- **Do not use `R2dbcEntityTemplate`** — use `DatabaseClient.sql()` with native SQL exclusively.

## References

- `./assets/` — adapter, model, config, exception, and helper classes required by each migration step
- Scaffold Clean Architecture: `gradle generateDrivenAdapter --type=r2dbc`, `gradle gda --type restconsumer`
- Reactor Extra: `CacheMono` documentation
- Spring Data R2DBC: `DatabaseClient` reference
- Jackson 3: `tools.jackson.databind.json.JsonMapper`
