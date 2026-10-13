# Java EE Project Analysis — BCO.WorkshopServices.901 (ReversalServices)

## Description

BCO.WorkshopServices.901 is a multi-module Maven Java EE 7 application deployed on Open Liberty
(full-java8). It implements a **transaction reversal (backout)** domain called `ReversalServices`.
It exposes a JAX-RS JSON facade to register and query reversals and a JMS Message-Driven Bean that
asynchronously confirms reversal notifications. On registration it validates the request, prevents
duplicates, resolves a target currency from a DB parameter, obtains an FX rate from an external
Exchange Rate service, persists the reversal in PostgreSQL (JPA), writes an audit event to DynamoDB,
and emits a JMS notification. The MDB consumer updates the reversal state and the DynamoDB event
status, with bounded JMS redelivery. Modules: **MEL** (DTOs/JAXB), **JPA** (entities + persistence
services), **SEC** (EJB business beans, utilities, JMS), **SEF** (JAX-RS facade WAR), **EAR**
(assembly). Cross-cutting middleware (`org.bintec.mdw.common`) provides logging (TracerManager) and a
service consumer broker (ServiceConsumer). The target of migration is Spring Boot 4.1.x reactive
(WebFlux) on Java 25 using Clean Architecture.

## Business Logic

### `BCO.WKS.901.SEC/.../sec/bean/reversal/RegisterReversalBean.java`
- **Cross-cutting libraries**: `MDW.CMM.TracerManager` (`GenericLogger`, `LoggerFactory`, `ServiceTypeEnum.BACKOUT`); `MDW.CMM.ServiceConsumer` (`UtilJSON`, `CommonServiceConsumerUtil`).
- **Common utilities**: `ResponseBuilderUtil`, `ExchangeRateUtil`, `DynamoDBUtil`, `ConstantWKS901`.
- **External provider libraries**: indirectly AWS DynamoDB (via `DynamoDBUtil`) and the Exchange Rate REST service (via `ExchangeRateUtil` → `ServiceBroker`).
- **Error handling**: throws/handles `WKS901Exception` (codes `WKS901-001` invalid, `WKS901-002` duplicated, `WKS901-003` exchange, `WKS901-006` JMS), `JPAException` → `WKS901-004` database, generic `RuntimeException` → `WKS901-999`. On any error it calls `markAsFailed` to set state `FAILED`.
- **Active vs Dead methods**: `registerReversal(MessageRQ)` — ACTIVE (called by `ReversalService.registerReversal`). Private helpers all ACTIVE.
- **Orchestration flow (`registerReversal`)**:
  1. Extract request header.
  2. `validateRequest` — null checks + currency regex `^[A-Z]{3}$` + positive amount.
  3. Duplicate check: `reversoPersistenceBean.findByMessageId(messageId)` → `WKS901-002` if found.
  4. Resolve target currency: `getParametroValor(BCO_WKS901_MONEDA_DESTINO, COP)`.
  5. Compute amount (scale 2) and FX rate via `ExchangeRateUtil.getExchangeRate` (scale 6).
  6. Build `Reverso` (UUID id, state `REGISTERED`, valorCop = amount × rate).
  7. Persist via `reversoPersistenceBean.saveReverso`.
  8. `DynamoDBUtil.putReversalEvent(reverso)` (conditional put, no overwrite).
  9. `sendNotification` → JMS to `jms/BCO-NotifyReversal.Q`.
  10. Build success response `RegisterReversalRSType`.

### `BCO.WKS.901.SEC/.../sec/bean/reversal/QueryReversalBean.java`
- Cross-cutting/util same as above (`ResponseBuilderUtil`, `DynamoDBUtil`, `ConstantWKS901`).
- **Error handling**: `WKS901-007` not found, `WKS901-004` database.
- **Active vs Dead**: `getReversal(String, String)` — ACTIVE (called by `ReversalService.getReversal`).
- **Flow**: find by messageId → map to `GetReversalRSType` → enrich `eventStatus` from `DynamoDBUtil.getEventStatus` → build response.

### `BCO.WKS.901.SEC/.../sec/bean/notify/NotifyReversalBean.java`
- Cross-cutting: TracerManager, `UtilJSON`.
- **Error handling**: on `JPAException`/`WKS901Exception` throws `RedeliveryAttemptedRuntimeException` to trigger JMS redelivery.
- **Active vs Dead**: `notifyReversal(ReversalNotificationType)` — ACTIVE (called by `MdbNotifyReversalBean.onMessage`).
- **Flow**: `updateEstado(reversalId, NOTIFIED)` → `DynamoDBUtil.updateEventStatus(messageId, NOTIFIED)` → trace.

### `BCO.WKS.901.SEC/.../sec/bean/persistence/ReversoPersistenceBean.java`
- Persistence facade (`@PersistenceContext unitName=JPAManager`) delegating to JPA service singletons.
- **Active methods**: `saveReverso`, `findByMessageId`, `updateEstado`, `getParametroValor`, static `currentUser` — all ACTIVE.

### `BCO.WKS.901.SEC/.../sec/bean/GenericSendJMSMesssageBean.java`
- JNDI-based JMS sender (`java:comp/env` → `jms/WKS-CF`, target queue). `sendJmsStringMessage` — ACTIVE (called by `RegisterReversalBean.sendNotification`).

## Migration (Business Logic) Key Points

- **Critical compliance rules to preserve**:
  - Idempotency / duplicate prevention on `messageId` (DB unique + DynamoDB conditional put `attribute_not_exists(messageId)`).
  - Monetary math: amount scale 2, rate scale 6, `valorCop = amount × rate` HALF_UP — must not change rounding.
  - Status lifecycle `REGISTERED → NOTIFIED | FAILED`.
  - JMS redelivery cap (`BCO_WKS901_MAX_REINTENTOS`, default 3) with discard beyond max.
- **Infrastructure dependencies needing Spring Boot equivalents**:
  - DynamoDB AWS SDK **v1** → **AWS SDK v2** (`DynamoDbAsyncClient` for reactive).
  - JAX-RS facade → **Spring WebFlux** `@RestController` returning `Mono`.
  - JPA/`EntityManager` (singleton services) → **Spring Data R2DBC** reactive repositories (PostgreSQL r2dbc), or `@Transactional` reactive; mapping to Clean Architecture use cases.
  - JNDI JMS (`wasJmsServer`) → messaging adapter (e.g., reactive messaging / `JmsTemplate` on a bridge, or SQS). Target broker to be confirmed; abstract behind a port.
  - `ServiceBroker` external call → **WebClient** with the registry endpoint/timeouts moved to `application.yml`.
  - ServiceRegistry (`service-registry.properties`) → `application.yml` config properties.
- **Security mechanisms**: endpoint secured by Liberty basicRegistry + container roles (`wasappusers`) and TLS keystore. External Exchange Rate call is anonymous HTTPS. AWS credentials from env/instance profile. Decide Spring Security posture (likely network isolation + actuator security).
- **Resiliency patterns**: external call timeouts (connect 5000ms / read 10000ms) → WebClient timeouts; JMS redelivery cap → consumer retry policy. No circuit breaker today (candidate for Resilience4j — optional).
- **Dead code**: none detected — all public methods have active callers. `DestinationType` DTO fields are parsed but not consumed by logic (see DTOs).
- **Risk assessment**: `RegisterReversalBean` **HIGH** (orchestration + FX + DynamoDB + JMS + money); `NotifyReversalBean`/MDB **MEDIUM** (async + redelivery); `QueryReversalBean` **MEDIUM**; utilities/DTOs/entities **LOW**.

## Database Object Entities

- `BCO.WKS.901.JPA/.../jpa/model/Reverso.java` — table `REVERSO`, PK `ID_REVERSO` (String UUID, len 36), unique `ID_MENSAJE`, `@NamedQuery Reverso.findByMessageId`.
- `BCO.WKS.901.JPA/.../jpa/model/Parametro.java` — table `PARAMETRO`, PK `ID_PARAMETRO`, `@Cacheable(true)`.

## DTOs

All under `BCO.WKS.901.MEL/.../mel/services/reversal/` (JAXB `@XmlType`, Jackson-serializable). Domain group: **reversal**.

- `MessageRQ.java` — IN_USE (facade register entry).
- `MessageRS.java` — IN_USE (all responses).
- `RequestMessageType.java` — IN_USE.
- `RequestHeaderType.java` — IN_USE (channel, messageId, clientId, destination).
- `RequestBodyType.java` — IN_USE.
- `RegisterReversalRQType.java` — IN_USE (transactionId, phoneNumber, amount, currency, reason).
- `RegisterReversalRSType.java` — IN_USE.
- `GetReversalRSType.java` — IN_USE.
- `ReversalNotificationType.java` — IN_USE (JMS payload).
- `ResponseMessageType.java` — IN_USE.
- `ResponseHeaderType.java` — IN_USE.
- `ResponseBodyType.java` — IN_USE.
- `StatusType.java` — IN_USE.
- `DestinationType.java` — IN_USE as a field of `RequestHeaderType` (deserialized) but its values are **not read by business logic** → migrate as part of the contract, logic consumption UNUSED.

## Potential Domains

- **reversal** (`reverso` / backout)
  - Description: register, query and asynchronously confirm transaction reversals with FX conversion and event auditing.
  - DTOs: all MEL `services/reversal/*` listed above.
  - Business Logic: `RegisterReversalBean`, `QueryReversalBean`, `NotifyReversalBean`, `ReversoPersistenceBean`, `GenericSendJMSMesssageBean`.
  - Entry Points: `ReversalService` (JAX-RS), `MdbNotifyReversalBean` (JMS).
  - Suggested target package: `org.bintec.bco.wks901.reversal` (Clean Architecture modules: `domain/model`, `domain/usecase`, `infrastructure/entry-points`, `infrastructure/driven-adapters`).
- **shared/common**: `ConstantWKS901`, `ConstantJPA`, `ResponseBuilderUtil`, `MessageTracerUtil`, exceptions.

## Cross-cutting and External Libraries

```xml
<dependency>
  <groupId>org.bintec.mdw.common</groupId>
  <artifactId>MDW.CMM.TracerManager</artifactId>
  <version>1.0.0</version>
</dependency>
<dependency>
  <groupId>org.bintec.mdw.common</groupId>
  <artifactId>MDW.CMM.ServiceConsumer</artifactId>
  <version>1.0.0</version>
</dependency>
<dependency>
  <groupId>com.amazonaws</groupId>
  <artifactId>aws-java-sdk-dynamodb</artifactId>
  <version>1.12.780</version>
</dependency>
<dependency>
  <groupId>com.fasterxml.jackson</groupId>
  <artifactId>jackson-bom</artifactId>
  <version>2.17.2</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
<dependency>
  <groupId>javax</groupId>
  <artifactId>javaee-api</artifactId>
  <version>7.0</version>
  <scope>provided</scope>
</dependency>
```
- `MDW.CMM.TracerManager`: `org.bintec.mdw.common.tracer.service.{GenericLogger, LoggerFactory, ServiceTypeEnum}`, `tracer.util.TypeEnum`.
- `MDW.CMM.ServiceConsumer`: `consumer.broker.ServiceBroker`, `consumer.util.{UtilJSON, CommonServiceConsumerUtil}`, `consumer.exception.{CommonUtilException, ServiceConsumerException}`.
- PostgreSQL JDBC `org.postgresql:postgresql:42.7.4` (copied in Dockerfile).

## Utilities

- `BCO.WKS.901.SEC/.../sec/util/ExchangeRateUtil.java`
- `BCO.WKS.901.SEC/.../sec/util/DynamoDBUtil.java`
- `BCO.WKS.901.SEC/.../sec/util/ResponseBuilderUtil.java`
- `BCO.WKS.901.SEC/.../sec/util/MessageTracerUtil.java`
- `BCO.WKS.901.SEC/.../sec/util/ConstantWKS901.java`
- `BCO.WKS.901.JPA/.../jpa/util/ConstantJPA.java`

## Entry Points

### HTTP (JAX-RS) — `BCO.WKS.901.SEF/.../sef/service/ReversalService.java`
Base path `@Path("/services/backout/ReversalServices")`, JSON in/out.
- **POST** `/registerReversal` — Request: `MessageRQ` (body `RegisterReversalRQType`). Response: `MessageRS` (body `RegisterReversalRSType`). Calls `RegisterReversalBean.registerReversal`.
- **GET** `/getReversal/{messageId}` (header `channel`) — Response: `MessageRS` (body `GetReversalRSType`). Calls `QueryReversalBean.getReversal`.
- JAX-RS `Application` registration: `BCO.WKS.901.SEF/.../sef/config/ConfigServices.java`.

### JMS — `BCO.WKS.901.SEC/.../sec/bean/notify/MdbNotifyReversalBean.java`
- MDB listener on queue `jms/BCO-NotifyReversal.Q` (`BCO.NotifyReversal.Q`). Payload `ReversalNotificationType`. Delegates to `NotifyReversalBean.notifyReversal`. Enforces redelivery cap via `JMSXDeliveryCount` vs `BCO_WKS901_MAX_REINTENTOS`.

## Dead Code Analysis

- `RegisterReversalBean.registerReversal()` — ACTIVE (called by: `ReversalService.registerReversal`)
- `QueryReversalBean.getReversal()` — ACTIVE (called by: `ReversalService.getReversal`)
- `NotifyReversalBean.notifyReversal()` — ACTIVE (called by: `MdbNotifyReversalBean.onMessage`)
- `GenericSendJMSMesssageBean.sendJmsStringMessage()` — ACTIVE (called by: `RegisterReversalBean.sendNotification`)
- `ReversoPersistenceBean.{saveReverso, findByMessageId, updateEstado, getParametroValor, currentUser}()` — ACTIVE
- `ExchangeRateUtil.getExchangeRate()` — ACTIVE; `DynamoDBUtil.{putReversalEvent, updateEventStatus, getEventStatus}()` — ACTIVE
- `ReversoJPAServiceIMPL.*` / `ParameterJPAServiceIMPL.getParametro` — ACTIVE
- No DEAD_CODE methods found.

## AWS Infrastructure Inventory

- **DynamoDB**:
  - Table: env `DYNAMO_REVERSAL_TABLE` (default `BCO_REVERSAL_EVENTS`).
  - Partition key: `messageId` (String). No sort key.
  - Attributes: `messageId, reversalId, transactionId, phoneNumber, amount(N), currency, amountCop(N), eventStatus, eventDate, notifiedDate`.
  - Operations: `PutItem` (conditional `attribute_not_exists(messageId)`), `UpdateItem` (`SET eventStatus=:status, notifiedDate=:notifiedDate`), `GetItem` (consistent read).
  - SDK: **v1** `aws-java-sdk-dynamodb 1.12.780` (migrate to v2).
- **SQS/SNS/S3**: none.
- **Credentials**: `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY` static if present, else `DefaultAWSCredentialsProviderChain`; region `AWS_REGION` (default `us-east-1`); optional `DYNAMO_ENDPOINT` for local.

## Security Contracts

- **Endpoint protection**: Open Liberty `basicRegistry` (`APP_USER`/`APP_PASSWORD`) + security role `wasappusers`; TLS via `defaultKeyStore` (`KEYSTORE_PASSWORD`).
- **Payload encryption**: none (plain JSON).
- **External service auth**: Exchange Rate service — anonymous HTTPS GET.
- **Header requirements** (register): `channel`, `messageId`, `clientId`, `destination` in request header; `getReversal` requires `channel` HTTP header + `messageId` path param.
- **Credential sources**: AWS from env/instance profile; DB from Liberty datasource vars (`DB_USER`/`DB_PASSWORD`); JMS via JNDI.

## Critical Business Rules

- RN-001: Request must be structurally valid; currency must match `^[A-Z]{3}$` and amount > 0 → else `WKS901-001` and halt. Code: `RegisterReversalBean.validateRequest`.
- RN-002: A reversal must be unique per `messageId` → `WKS901-002` and halt. Code: `RegisterReversalBean.registerReversal` (dup check) + DynamoDB conditional put.
- RN-003: FX rate must be obtainable for source→target currency → else `WKS901-003` and halt. Code: `ExchangeRateUtil.getExchangeRate`.
- RN-004: On any failure during registration, reversal is marked `FAILED`. Code: `RegisterReversalBean.markAsFailed`.
- RN-005: JMS notification processed at most `BCO_WKS901_MAX_REINTENTOS` (default 3) deliveries; beyond that the message is discarded. Code: `MdbNotifyReversalBean.onMessage` + `maxRedelivery`.
- RN-006: Query not found → `WKS901-007`. Code: `QueryReversalBean.getReversal`.

## Resiliency Patterns

- **Timeouts**: Exchange Rate `connectTimeout=5000ms`, `readTimeout=10000ms` (service-registry.properties).
- **Retry**: JMS redelivery (Liberty `maxRedeliveryCount=5` on engine; app-level cap default 3 via DB param).
- **Circuit breakers**: none.
- **Fallback**: identical source/target currency short-circuits to rate `1`.

## External Service Integration Map

- **ExchangeRateServices.getLatestRates**
  - Endpoint resolution: ServiceRegistry key `ExchangeRateServices.getLatestRates.endpoint` = `${EXCHANGE_RATE_BASE_URL:https://open.er-api.com}/v6/latest/{currency}`.
  - Auth: none.
  - Request: `GET`, path param `currency` = source currency.
  - Response: JSON with `result` (expects `success`) and `rates.{targetCurrency}`.
  - Timeouts: connect 5000ms / read 10000ms.
  - Error handling: non-success / missing rate → `WKS901-003`.

## Migration Risk Assessment

- **HIGH RISK**: `RegisterReversalBean` (orchestration, money math, DynamoDB conditional write, JMS, external FX).
- **MEDIUM RISK**: `MdbNotifyReversalBean`/`NotifyReversalBean` (async + redelivery semantics), `QueryReversalBean` (DynamoDB enrichment), `DynamoDBUtil` (SDK v1→v2), `GenericSendJMSMesssageBean` (JNDI→Spring messaging).
- **LOW RISK**: DTOs (MEL), entities (`Reverso`, `Parametro`), `ResponseBuilderUtil`, `MessageTracerUtil`, constants, exceptions.

## Database Parameters (Environment Variable Candidates)

- **Entity mapping**: `BCO.WKS.901.JPA/.../jpa/model/Parametro.java` (table `PARAMETRO`). No `PARAMETRO_ATRIBUTO` / `ATTRIBUTE` entities present.
- **Access points**: `ParameterJPAServiceIMPL.getParametro` (criteria query by `idParametro`); `ReversoPersistenceBean.getParametroValor`; consumed by `RegisterReversalBean` and `MdbNotifyReversalBean.maxRedelivery`.

| Parameter code | Source table | Code reference | Consumer | Suggested env var |
|----------------|-------------|----------------|----------|-------------------|
| `BCO_WKS901_MONEDA_DESTINO` | `PARAMETRO` | `ConstantWKS901.PARAM_TARGET_CURRENCY`; `RegisterReversalBean.registerReversal` | `RegisterReversalBean` | `BCO_WKS901_MONEDA_DESTINO` (default `COP`) |
| `BCO_WKS901_MAX_REINTENTOS` | `PARAMETRO` | `ConstantWKS901.PARAM_MAX_REDELIVERY`; `MdbNotifyReversalBean.maxRedelivery` | `MdbNotifyReversalBean` | `BCO_WKS901_MAX_REINTENTOS` (default `3`) |

- **Dynamic lookups**: none (all parameter codes are literals).

Other environment variables referenced in code/config: `CURRENT_USER` (default `Nequi`), `AWS_REGION`, `DYNAMO_ENDPOINT`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `DYNAMO_REVERSAL_TABLE`, `EXCHANGE_RATE_BASE_URL`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

## Project Parameters

- **groupId**: `org.bintec`
- **artifactId**: `BCO.WKS.901` (parent/aggregator; modules `BCO.WKS.901.{MEL,JPA,SEC,SEF,EAR}`)
- **packageName**: `org.bintec.bco.wks901`

## DevOps Pipelines Config

- No `Jenkinsfile` and no `azure-pipelines.yml` found in the project.
- A `Dockerfile` exists: Maven (eclipse-temurin-8) multi-stage build producing the EAR, deployed on `icr.io/appcafe/open-liberty:full-java8`; copies PostgreSQL JDBC 42.7.4 and Liberty `server.xml` / `service-registry.properties`. Exposes ports 9080/9443.
- Liberty runtime config: datasource `jdbc/WorkshopDS` (PostgreSQL), JMS `jms/WKS-CF` + queue `BCO.NotifyReversal.Q` (`maxRedeliveryCount=5`), basicRegistry security, TLS keystore.
