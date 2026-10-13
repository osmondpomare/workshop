# Workshop Bintec – Legacy Java EE on Open Liberty

A deliberately "legacy-style" Java EE 7 / Java 8 application, deployed on **Open Liberty** with Docker. It is the starting point for demonstrating how AI agents can migrate a WAS-era multi-module EAR to a modern stack.

The code is original and generic. It follows the module conventions of the WAS projects (`SEF` / `SEC` / `MEL` / `JPA` / `EAR`) and uses two slimmed-down common libraries (`MDW.CMM.TracerManager`, `MDW.CMM.ServiceConsumer`) that are consumed as **pre-compiled jars**.

## What the service does

`BCO-WorkshopServices.901` is a BCO (backout) service that registers the **reversal of a failed transaction**:

```
 client ──POST registerReversal──▶ SEF  ReversalService (JAX-RS, @Stateless)
                                     │
                                     ▼
                                   SEC  RegisterReversalBean
                                     ├─ 1. REST  → ServiceBroker (MDW.CMM.ServiceConsumer) → open.er-api.com (exchange rate)
                                     ├─ 2. DB    → ReversoPersistenceBean → JPA → PostgreSQL (tables REVERSO / PARAMETRO)
                                     ├─ 3. NoSQL → DynamoDBUtil (AWS SDK v1) → LocalStack table BCO_REVERSAL_EVENTS
                                     └─ 4. JMS   → GenericSendJMSMesssageBean → queue jms/BCO-NotifyReversal.Q
                                                                       │
                                                                       ▼
                                   SEC  MdbNotifyReversalBean (MDB) → NotifyReversalBean
                                          └─ marks the reversal NOTIFIED in PostgreSQL and DynamoDB

 client ──GET getReversal/{messageId}──▶ QueryReversalBean → PostgreSQL + DynamoDB
```

Every layer traces JSON lines through `MDW.CMM.TracerManager` (`LoggerFactory.getLogger(ServiceTypeEnum.X, Clazz.class)`).

## Repository layout

```
workshop/
├── docker-compose.yml                 PostgreSQL + LocalStack + Open Liberty app (pulls osmondpomare/bco-workshop-services-901:1.0.0)
├── docker-compose.build.yml           Override to build the app image from source instead of pulling it
├── libs/repository/                   File-based Maven repo with the compiled common jars
├── MDW-COMMON.WKS/                    Sources of the common libraries (to rebuild the jars)
│   ├── MDW.CMM.TracerManager          LoggerFactory, GenericLogger, ServiceTypeEnum, TypeEnum (java.util.logging)
│   ├── MDW.CMM.ServiceConsumer        ServiceBroker, GenericRestClient, ResourceLocatorSingleton, UtilJSON
│   └── build-libs.sh
├── BCO-WorkshopServices.901/
│   ├── BCO.WKS.901                    Parent POM (modules declared with ../)
│   ├── BCO.WKS.901.MEL                Message entities (JAXB-style request/response DTOs)
│   ├── BCO.WKS.901.JPA                Entities, persistence.xml, singleton *JPAServiceIMPL classes
│   ├── BCO.WKS.901.SEC                EJB module: stateless beans, MDB, JMS sender, DynamoDB/REST utils
│   ├── BCO.WKS.901.SEF                WAR: JAX-RS facade, web.xml with BASIC auth, ibm-web-*.xml
│   ├── BCO.WKS.901.EAR                EAR packaging (BCO.WorkshopServices.901.ear)
│   ├── liberty/server.xml             Datasource, embedded JMS engine, activation spec, security
│   ├── liberty/service-registry.properties   Endpoints used by ServiceConsumer
│   └── Dockerfile                     Multi-stage: Maven (JDK 8) → Open Liberty (Java 8)
├── infra/postgres/init.sql            Schema and parameters
├── infra/localstack/init-dynamodb.sh  Creates the DynamoDB table
├── infra/localstack/init-sqs.sh       Creates the SQS queue BCO-NotifyReversal (migrated messaging)
├── requests/registerReversal.json     Sample request
├── scripts/test-flow.sh               End-to-end smoke test
├── ai-toolkit/                        Migration kit: skills, steering, playbook, agents, MCP configs
├── workshop_services_901_ms/          Migrated reference project (reactive Spring Boot, read-only)
└── workshop_services_901_us_ms/       Empty target folder — migrated here during the workshop
```

## Tech stack

| Concern | Technology |
|---|---|
| Language | Java 8 (source/target 1.8) |
| Platform | Java EE 7 (`javaee-api:7.0`), EJB 3.2, JAX-RS 2.0, JPA 2.1, JMS 2.0 |
| Runtime | Open Liberty `full-java8-openj9-ubi-minimal`, feature `javaee-7.0` + `wasJmsServer-1.0` |
| Database | PostgreSQL 15 (JTA datasource `jdbc/WorkshopDS`) |
| NoSQL | DynamoDB on LocalStack, AWS SDK for Java **v1** (`aws-java-sdk-dynamodb:1.12.780`) |
| Messaging | Liberty embedded messaging engine, queue `BCO.NotifyReversal.Q`, MDB consumer |
| Outbound REST | `HttpURLConnection` via `MDW.CMM.ServiceConsumer` → `https://open.er-api.com/v6/latest/{currency}` |
| Build | Maven multi-module, EAR packaging |

## Prerequisites

- Docker with Docker Compose v2
- Internet access to pull images from Docker Hub, plus at runtime (the public exchange-rate API)
- Optional, only needed to build the app image from source instead of pulling it: JDK 8, Maven 3.8+, and internet access to Maven Central and `icr.io` (see [Building from source instead of pulling](#building-from-source-instead-of-pulling))

## Run it

```bash
cd workshop
docker compose up
```

By default `bco-wks-901` pulls the pre-built image `osmondpomare/bco-workshop-services-901:1.0.0` from Docker Hub — no JDK or Maven required. The first run downloads the image plus the Postgres/LocalStack images, so it takes a few minutes. The app is ready when the Liberty log shows:

```
CWWKZ0001I: Application BCO.WorkshopServices.901 started
CWWKF0011I: The defaultServer server is ready to run a smarter planet.
```

To stop everything and remove the data:

```bash
docker compose down -v
```

### Building from source instead of pulling

If you change the Java code under `BCO-WorkshopServices.901` or the common libraries, rebuild the image locally with the build override instead of pulling the published one:

```bash
docker compose -f docker-compose.yml -f docker-compose.build.yml build bco-wks-901
docker compose -f docker-compose.yml -f docker-compose.build.yml up
```

This requires JDK 8 and Maven, plus internet access to Maven Central, `icr.io` (Open Liberty base image) and Docker Hub.

## Try it

Credentials (BASIC auth, role `wasappusers`): `wsuser` / `wspassword`.

Register a reversal:

```bash
curl -s -u wsuser:wspassword \
  -H "Content-Type: application/json" \
  -X POST \
  -d @requests/registerReversal.json \
  http://localhost:9080/BCO.WKS.901.SEF/rest/services/backout/ReversalServices/registerReversal
```

Expected response:

```json
{"responseMessage":{"header":{"channel":"MF-001","messageId":"MSG-0001","clientId":"3001234567","responseDate":"...","status":{"statusCode":"0","statusDesc":"SUCCESS"}},"body":{"registerReversalRS":{"reversalId":"...","status":"REGISTERED","exchangeRate":"...","amountCop":"..."}}}}
```

Query it (after the MDB has run, `status` and `eventStatus` are `NOTIFIED`):

```bash
curl -s -u wsuser:wspassword -H "channel: MF-001" \
  http://localhost:9080/BCO.WKS.901.SEF/rest/services/backout/ReversalServices/getReversal/MSG-0001
```

Or run the whole flow, which also prints the PostgreSQL row and the DynamoDB item, with a fresh `messageId` each time:

```bash
./scripts/test-flow.sh
```

Inspect the stores directly:

```bash
docker exec -it wks-postgres psql -U workshop -d workshop -c "SELECT * FROM reverso;"
docker exec -it wks-localstack awslocal dynamodb scan --table-name BCO_REVERSAL_EVENTS --region us-east-1
docker logs -f wks-bco-901
```

### Status codes

| Code | Meaning |
|---|---|
| `0` | Success |
| `WKS901-001` | Invalid request (missing fields, amount ≤ 0, currency not ISO-4217 format) |
| `WKS901-002` | Duplicated `messageId` |
| `WKS901-003` | Exchange-rate service failed |
| `WKS901-004` | Database error |
| `WKS901-005` | DynamoDB error |
| `WKS901-006` | JMS error |
| `WKS901-007` | Reversal not found |
| `WKS901-999` | Unexpected error |

When steps 3 or 4 fail after the row is saved, the reversal is marked `FAILED` in PostgreSQL.

## Configuration

All configuration is injected through environment variables (see `docker-compose.yml`):

| Variable | Used by | Default |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Liberty datasource | `localhost`, `5432`, `workshop`, `workshop`, `workshop` |
| `DYNAMO_ENDPOINT` | `DynamoDBUtil` (leave empty for real AWS) | – |
| `DYNAMO_REVERSAL_TABLE` | `DynamoDBUtil` | `BCO_REVERSAL_EVENTS` |
| `AWS_REGION`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` | AWS SDK v1 | `us-east-1`, default credential chain |
| `SERVICE_REGISTRY_PATH` | `ResourceLocatorSingleton` | `/config/service-registry.properties` (set in the image) |
| `EXCHANGE_RATE_BASE_URL` | placeholder in `service-registry.properties` | `https://open.er-api.com` |
| `APP_USER`, `APP_PASSWORD` | Liberty `basicRegistry` | `wsuser`, `wspassword` |
| `CURRENT_USER` | audit columns `USUARIO_CREACION` / `USUARIO_MODIFICACION` | `Nequi` |
| `TRACER_DEBUG_ENABLED`, `TRACER_TRACEABLE` | `MDW.CMM.TracerManager` | `false`, `true` |

Business parameters live in the `PARAMETRO` table: `BCO_WKS901_MONEDA_DESTINO` (target currency, `COP`) and `BCO_WKS901_MAX_REINTENTOS` (max MDB redeliveries, `3`).

## Common libraries (compiled jars)

The application resolves `MDW.CMM.TracerManager:1.0.0` and `MDW.CMM.ServiceConsumer:1.0.0` (groupId `org.bintec.mdw.common`) from the file repository `libs/repository`, declared in the SEC, SEF and EAR POMs. The jars are already compiled for Java 8 (class file version 52), so no build step is needed.

To change and republish them (JDK 8+ and Maven required):

```bash
cd MDW-COMMON.WKS
./build-libs.sh
```

`ServiceConsumer` reads endpoints as `<service>.<operation>.endpoint|method|connectTimeout|readTimeout|header.<name>`, supports `${ENV_VAR:default}` placeholders and `{pathParam}` substitution.

## Building without Docker

```bash
mvn -f BCO-WorkshopServices.901/BCO.WKS.901/pom.xml clean package
```

The EAR is written to `BCO-WorkshopServices.901/BCO.WKS.901.EAR/target/BCO.WorkshopServices.901.ear`. To run it on a local Open Liberty, copy `liberty/server.xml` and `liberty/service-registry.properties` into the server's config directory, put the PostgreSQL JDBC driver in `usr/shared/resources/jdbc/`, drop the EAR into `apps/`, and export the variables above.

## Troubleshooting

- **LocalStack asks for an auth token.** Since March 2026 the `latest` LocalStack image requires an account. The compose file pins `localstack/localstack:4.4.0`. If your environment still asks for a token, export `LOCALSTACK_AUTH_TOKEN` before `docker compose up`.
- **The exchange-rate API is unreachable.** Point `EXCHANGE_RATE_BASE_URL` at any service that answers `GET /v6/latest/{currency}` with `{"result":"success","rates":{"COP":...}}`, or send `"currency":"COP"` (rate 1, no outbound call).
- **SSL handshake failure calling the exchange-rate API.** When Liberty's SSL features are enabled, Liberty replaces the JVM default trust store with `defaultKeyStore`. `server.xml` sets `trustDefaultCerts="true"` on `defaultSSLConfig` so outbound calls also trust the JDK `cacerts`. If your network inspects TLS with a corporate certificate, add that CA to the trust store or point `EXCHANGE_RATE_BASE_URL` to a reachable mock.
- **Port already in use.** Change the published ports in `docker-compose.yml` (`5432`, `4566`, `9080`, `9443`).
- **The MDB is not consuming.** The `jmsActivationSpec` id in `server.xml` must match `<application>/<ejb-module>/<bean>`: `BCO.WorkshopServices.901/BCO.WKS.901.SEC/MdbNotifyReversalBean`.

## Migration hotspots (for the workshop)

These are intentional legacy traits for the agents to work on:

- EAR with separate WAR/EJB/library modules, parent POM using `../` modules, and no `<parent>` inheritance
- `@Stateless`/`@LocalBean` EJBs, a container-managed JTA `EntityManager`, and `@TransactionAttribute` choreography
- JNDI lookups through `java:comp/env` plus `ejb-jar.xml` resource references for JMS
- A message-driven bean bound through a Liberty `jmsActivationSpec`, and redelivery handled by hand
- Singleton `*JPAServiceIMPL` classes that receive the `EntityManager` as a parameter
- AWS SDK for Java v1 (`AmazonDynamoDBClientBuilder`, `AttributeValue`) and configuration read with `System.getenv`
- In-house libraries delivered only as jars: a custom tracer (`java.util.logging`) and an `HttpURLConnection` REST client with a properties-based service registry
- `javax.*` namespaces, JAXB-style DTOs, BASIC auth defined in `web.xml`, and `ibm-web-bnd.xml`/`ibm-web-ext.xml` descriptors

## Migrating to Spring Boot (the workshop)

The workshop drives this legacy EAR through an AI-assisted migration to a modern **reactive Spring Boot** microservice (Spring WebFlux + Project Reactor) built on the Scaffold Clean Architecture. Three pieces support that flow:

| Folder | Role | Notes |
|---|---|---|
| `ai-toolkit/` | Migration kit | Skills, steering rules, the 14-step playbook, pre-built agents, and MCP configs that an AI agent (Kiro CLI, Claude Code, or AWS Transform CLI) uses to perform the migration. See [`ai-toolkit/README.md`](ai-toolkit/README.md). |
| `workshop_services_901_ms/` | Migrated reference project | The **already-migrated** result, provided **read-only** as a reference of the expected target. Reactive Spring Boot 4.x, Java 25, Gradle, Scaffold Clean Architecture. See [`workshop_services_901_ms/README.md`](workshop_services_901_ms/README.md). |
| `workshop_services_901_us_ms/` | Workshop target | **Empty on purpose.** During the workshop the agent scaffolds and migrates the implementation into this folder. Compare your output against the reference project above. |

### How it maps

The legacy Java EE building blocks are migrated to reactive Spring Boot equivalents:

| Legacy (Java EE) | Migrated (reactive Spring Boot) |
|---|---|
| `ReversalService` (JAX-RS) | `ReversalController` (WebFlux `@RestController`) |
| `MdbNotifyReversalBean` (JMS MDB) | `SQSProcessor` (SQS listener) |
| `RegisterReversalBean` / `QueryReversalBean` / `NotifyReversalBean` | `RegisterReversalUseCase` / `QueryReversalUseCase` / `NotifyReversalUseCase` |
| `*JPAServiceIMPL` + `EntityManager` | R2DBC adapters (`ReversoAdapter`, `ParametroAdapter`) |
| `ExchangeRateUtil` + `ServiceBroker` (`HttpURLConnection`) | `ExchangeRateAdapter` (`WebClient`) |
| `DynamoDBUtil` (AWS SDK v1) | `ReversalEventAdapter` (AWS SDK v2 async) |
| `GenericSendJMSMesssageBean` (JMS) | `SQSSender` (SQS async) |

The messaging hop also changes transport: the legacy Liberty queue `BCO.NotifyReversal.Q` becomes the SQS queue `BCO-NotifyReversal` (provisioned on LocalStack by `infra/localstack/init-sqs.sh`).

To run the migration playbook, follow [`ai-toolkit/README.md`](ai-toolkit/README.md); the end-to-end command sequence for the session is in [`execute-workshop.md`](execute-workshop.md).
