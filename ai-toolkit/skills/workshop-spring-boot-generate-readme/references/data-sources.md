# Where to Find Each Fact in the Repo

Map every template placeholder to a concrete file/command. Prefer values found in the repo over
guessing. If a fact cannot be found anywhere listed below, mark it per the rules in
`readme-template.md` (`*Solicitar a the company*` for secrets, `_Pendiente por definir_` otherwise) —
never invent hostnames, ports, table names, or credentials.

## Project identity
- **`{{PROJECT_TITLE}}` / `{{PROJECT_NAME}}`**
  - Gradle: `rootProject.name` in `settings.gradle` / `settings.gradle.kts`.
  - Maven: `<artifactId>` in `pom.xml`.
  - Fallback: repository directory name.
- **`{{ONE_TO_TWO_PARAGRAPH_DESCRIPTION}}`**
  - `metadata.description` in `.metadata/catalog-info.yaml`.
  - `<description>` in `pom.xml`.
  - If missing, infer from the entry-point/controller package (what domain operations it exposes) —
    keep it factual, do not invent business claims.

## Architecture
- **Module layout** — detect the architecture style from the folder tree:
  - Gradle Clean Architecture plugin (`co.com.bancolombia.cleanArchitecture` in root `build.gradle`):
    modules under `domain/model`, `domain/usecase`, `infrastructure/entry-points/*`,
    `infrastructure/driven-adapters/*`, `infrastructure/helpers/*`, `applications/app-service`.
  - Plain layered Spring Boot: single module with `controller`/`service`/`repository`/`domain`
    packages (per this repo's own Java project structure convention).
  - List the actual subprojects found in `settings.gradle` (`include ':x'` lines) or Maven modules
    (`<modules>` in the parent `pom.xml`) as the "Componentes principales".
- **Architecture diagram / wiki link** — search for an existing `docs/architecture*` image or a
  link already present in an old README; otherwise leave as `_Pendiente por definir_` (this cannot
  be generated from code).

## Technology stack
- **Java version** — `sourceCompatibility` / `toolchain.languageVersion` in `build.gradle`, or
  `<java.version>` / `<maven.compiler.release>` in `pom.xml`.
- **Framework version** — `springBootVersion` in root `build.gradle` `ext {}` block, or the
  `spring-boot-starter-parent` version in `pom.xml`.
- **Clean Architecture plugin version** — `cleanArchitectureVersion` in root `build.gradle`, if present.
- **Build tool version** — `gradle/wrapper/gradle-wrapper.properties` (`distributionUrl`) or
  `.mvn/wrapper/maven-wrapper.properties`.
- **Databases** — grep `build.gradle`/`pom.xml` for `r2dbc-`, `postgresql`, `oracle`, `mongodb`,
  `mysql`, `jpa`; cross-check against `infrastructure.r2dbc.*` / `spring.datasource.*` keys in
  `application.yml`.
- **ORM/data access** — presence of `r2dbc-*`, `spring-boot-starter-data-jpa`, `mybatis-spring`.
- **AWS/cloud services** — dependencies like `software.amazon.awssdk:*`, `spring-cloud-aws-*`; grep
  for `sqs`, `sns`, `s3`, `dynamodb` across config and code.
- **Resilience/observability libs** — `resilience4j`, `micrometer`, `logstash-logback-encoder`
  versions from the `ext {}` block.

## Important links
- `.metadata/catalog-info.yaml` → `metadata.links[]` (IaC repo, MS repo, test/perf repos, pipelines,
  Grafana dashboard) and `metadata.annotations` (`grafana/overview-dashboard`, `github.com/project-slug`).
- `.metadata/api-info.yaml` → API spec link, API repo link.
- If a previous README already had SonarCloud/FluidAttacks links, preserve them (they use org/project
  slugs not derivable from code).

## Environment variables
- Grep every `application*.yml` / `application*.properties` (across ALL modules, especially
  `applications/app-service/src/main/resources/`) for the `${VAR_NAME:default}` pattern.
- Build one row per distinct `VAR_NAME`:
  - **Descripción**: infer from the YAML key path it configures (e.g. `infrastructure.r2dbc.postgresql.host`
    → "Host de conexión a la base de datos PostgreSQL").
  - **Valor por defecto**: the text after `:` in `${VAR:default}`. If the variable has no default
    (`${VAR}`) and its name matches `PASSWORD|SECRET|TOKEN|KEY|CREDENTIAL`, use `*Solicitar a the company*`
    — never fabricate a plausible-looking secret value.
  - **Requerido**: `Sí` if no default is provided, otherwise still `Sí` unless the feature it guards
    is optional (e.g. metrics toggles default `false` and are optional → mark `Sí` only if the app
    fails to start without it; otherwise `No` is acceptable for pure feature flags).
- Group rows by concern (database, business rules, external services, observability) using blank
  rows or sub-tables if there are more than ~15 variables, mirroring the reference example.

## Build / run / test commands
- Gradle: `./gradlew build`, `./gradlew bootRun`, `./gradlew test`, `./gradlew jacocoTestReport`.
- Maven: `./mvnw clean install`, `./mvnw spring-boot:run`, `./mvnw test`, `./mvnw verify` (jacoco
  goal if the `jacoco-maven-plugin` is configured).
- Docker: check for a root `Dockerfile` or `deployment/Dockerfile` and a `docker-compose.yml` to
  fill the exact `docker build`/`docker run`/`docker-compose up` invocations (image name, exposed
  port from `server.port` in `application.yml`).

## Local database setup
- Look for `docker-compose.yml`, `deployment/`, or a `*_DB` sibling repo reference in
  `catalog-info.yaml` links.
- Extract actual `CREATE TABLE` statements from migration scripts under `src/main/resources/db/migration`
  (Flyway) or `src/main/resources/liquibase` if present; otherwise link to the external DB repo
  instead of inventing schema.

## API endpoints
- Primary source: `.metadata/openapi.yaml` — walk `paths:` and emit one subsection per operation
  (method + path + summary), request/response examples from the `schemas`.
- If no OpenAPI file exists, grep controllers (`@RestController`, `@GetMapping`/`@PostMapping`/...,
  or WebFlux `RouterFunction` route definitions) under `infrastructure/entry-points/*` for the
  actual routes.
- Swagger UI / OpenAPI JSON URLs come from `springdoc.swagger-ui.path` / `springdoc.api-docs.path`
  in `application.yml` (defaults: `/swagger-ui.html`, `/v3/api-docs`), combined with `server.port`.

## Deployment
- `azure-pipelines.yml` (or `.github/workflows/*.yml`) — summarize stages only if it materially
  helps (build → test → sonar → deploy); don't paste the full pipeline YAML.
- IaC repo link from `catalog-info.yaml`.

## Contact
- Never name individuals (project convention). Use the owning team/group from
  `spec.owner` in `catalog-info.yaml`, translated to a human-readable team name if the value is a
  group slug (e.g. `group:default/a_company_platform_marketplace_developer` → "Equipo Platform Marketplace").
  If unclear, leave the generic template line as-is for the user to fill in.
