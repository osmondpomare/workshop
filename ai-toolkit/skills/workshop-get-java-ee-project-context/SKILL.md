---
name: workshop-get-java-ee-project-context
description: Analyze applications written using the Java EE programming language in versions 7 and 8
---
# Get Project Context

## When to use this skill
Use this skill when analyzing applications written in Java EE.

## Context
You are an expert in analyzing applications written using the Java EE programming language in versions 7 and 8. Your approach to performing the analysis starts by identifying the README.md file, which typically contains project-related information such as a brief description and data related to the functionalities it exposes.

## Requirements
- Analyze the application structure
    - Identify Business Logic
    - Identify Entities that map Database Objects
    - Identify DTOs
    - Identify Cross-cutting Libraries
    - Identify AWS and external provider Libraries
    - Identify common utilities within the code
    - Identify the application's Entry Points
    - Identify the Deployment process (DevOps Pipelines Config)
    - Identify Dead Code vs Active Code (for each Business Logic class and utility, trace call-sites; mark methods/classes without active callers as DEAD_CODE)
    - Identify AWS Infrastructure Dependencies (DynamoDB tables, SQS queues, SNS topics, S3 buckets — with operations: GetItem/PutItem/SendMessage/etc.)
    - Identify Security Contracts (payload encryption/decryption, authentication mechanisms, credential sources, header propagation requirements)
    - Identify Critical Business Rules that block the flow or have compliance impact (e.g., PEP checks, client existence validation, bureau blocking rules)
    - Identify External Service Registry or Configuration systems (dynamic endpoint resolution, credential stores, timeout configurations)
    - Identify Resiliency Patterns (circuit breakers, retry mechanisms, timeouts, fallbacks)
    - Identify Potential Domains (bounded contexts): use the identified DTOs — and the integration/domain groupings already assigned to them — as the primary signal to infer the business domains/modules present in the application (e.g., 'credit-bureau', 'control-list', 'user', 'order'). Cross-reference each candidate domain against the Business Logic classes and Entry Points that consume those DTOs to confirm the grouping is coherent, and merge or split groupings where the DTO-based signal alone is ambiguous.
    - Identify Database-Driven Configuration Parameters (`PARAMETRO`, `PARAMETRO_ATRIBUTO`, and `ATTRIBUTE` tables/entities): locate every JPA entity mapped to these tables (e.g. `Parametro.java`, `ParametroAtributo.java`, `Atributo.java`), every DAO/repository/service method that reads from them, and every call site where a parameter code/name is passed as a literal (e.g., `parametroService.findByCodigo("MAX_RETRY_COUNT")`, `atributoService.getValor("CHANNEL_TIMEOUT")`). Extract and de-duplicate every parameter name/code found this way — each one is a candidate environment variable for the Spring Boot migration target. Flag any lookup built from a non-literal/dynamic value as `DYNAMIC_LOOKUP` since it cannot be statically resolved to a fixed env var name.

## Output
- Build a report with the analysis performed. The content must be in English language. The report structure must be as follows:
    - Title
    - Description: Must not exceed 250 words.
    - Business Logic: Must contain the file paths where the identified Business Logic is located. For each of these paths, describe:
        - Cross-cutting libraries used
        - Common utilities referenced
        - External provider libraries
        - Error handling points (which exceptions are thrown, which error codes are used)
        - **Active vs Dead methods**: For each public method, indicate if it has active callers or is dead code
        - **Orchestration flow**: For the main orchestration method, document the step-by-step execution order with what each step does
    - Migration (Business Logic) Key Points: It should contain elements that describe the key points for a possible migration of these services involved in the business logic. It is important that the description is supported by the Steering Committee to eventually propose the creation of @Services using the Spring Boot framework. Think like in the future we will use this for migrate each functionality to Spring Boot Project. MUST include:
        - **Critical compliance rules** that MUST be preserved (PEP checks, client validation, blocking rules)
        - **Infrastructure dependencies** that need Spring Boot equivalents (DynamoDB→AWS SDK v2, SQS→Spring Cloud AWS, ServiceRegistry→application.yml)
        - **Security mechanisms** that need explicit migration decisions (encryption, auth, credential resolution)
        - **Resiliency patterns** that need Spring Boot equivalents (circuit breaker→Resilience4j, timeouts→WebClient config)
        - **Dead code** that should NOT be migrated
        - **Risk assessment** per component (HIGH/MEDIUM/LOW)
    - Database Object Entities: Must contain the file paths where they are mapped.
    - DTOs: Must contain the file paths where they are mapped. Please include all paths for each of the identified files. For each DTO, mark its usage status:
        - IN_USE: Has at least one reference in active Business Logic or Entry Points
        - UNUSED: Has zero references in active code (recommend NOT migrating)
      Group DTOs by their integration/domain (e.g., 'control-list DTOs', 'credit-bureau DTOs', 'internal DTOs').
    - Potential Domains: A list of the business domains/bounded contexts identified in the application, derived primarily from the DTO domain groupings above. For each domain include:
        - Domain name (short, kebab-case, e.g., `credit-bureau`, `control-list`)
        - Description: the business capability this domain covers
        - DTOs belonging to this domain (file paths, cross-referenced with the DTOs section)
        - Business Logic classes associated with this domain
        - Entry Points associated with this domain
        - Suggested target package name for the Spring Boot project, following the domain-driven module structure (e.g., `com.company.project.creditbureau`)
      Group any DTOs/classes that don't fit a clear business domain under a single `shared`/`common` domain rather than forcing them into an unrelated one.
    - Cross-cutting and External Libraries: Must describe the library name. It is very important to include the XML of the identified dependencies.
    - Utilities: Must contain the file paths where they are mapped.
    - Entry Points: Must contain the file paths where they are mapped. For each entry point, describe the Endpoint (if it is an HTTP Entry-Point), the input Request, the output Response, and which DTOs are used. Include the file paths called to execute the Business Logic.
    - Dead Code Analysis: For each Business Logic class, list methods with zero active call-sites. Mark them as DEAD_CODE with a recommendation to NOT migrate them. Format:
        - Class: `ClassName.methodName()` — DEAD_CODE (no callers found)
        - Class: `ClassName.methodName()` — ACTIVE (called by: CallerClass.callerMethod())
    - AWS Infrastructure Inventory: List all AWS services used with their configuration:
        - DynamoDB: table names, partition/sort keys, operations (Get/Put/Update/Delete), SDK version
        - SQS: queue names/URLs, message format, operations (Send/Receive)
        - SNS: topic ARNs, message format
        - S3: bucket names, operations
        - Credentials: how credentials are resolved (instance profile, system properties, env vars)
    - Security Contracts: Document all security mechanisms:
        - Endpoint protection: how the endpoint is secured (container security, EJB roles, network isolation)
        - Payload encryption: algorithms used (AES, RSA), key exchange mechanism
        - External service auth: for each integration, document the authentication method (Basic Auth, API Key, OAuth, mutual TLS)
        - Header requirements: which headers the consumer MUST send (messageId, consumerId, publicKey, region, channel)
        - Credential sources: where credentials come from (ServiceRegistry, system properties, JNDI, database parameters)
    - Critical Business Rules: List rules that have compliance or blocking impact. For each rule:
        - Rule ID (if available)
        - Description
        - Blocking behavior (what happens when the rule is violated — which error code, does it halt the flow?)
        - Code reference (file:line)
        - Example: "RN-003: If client exists with biometry==1, throw ERROR_CODE_USER_ALREADY_EXIST and halt flow. Code: DocumentValidationServiceBean.java:380"
    - Resiliency Patterns: Document existing patterns:
        - Circuit breakers: which services, thresholds, fallback behavior
        - Timeouts: per-service timeout values (from ServiceRegistry, config, or hardcoded)
        - Retry: which operations are retried, how many times, backoff strategy
    - External Service Integration Map: For each external service call:
        - Service name
        - Endpoint resolution method (ServiceRegistry key, hardcoded URL, config property)
        - Authentication method (Basic Auth user/password source, API Key source)
        - Request/Response format
        - Timeout values
        - Error handling (which error codes are expected, how they are translated)
    - Migration Risk Assessment: Based on the analysis, flag:
        - HIGH RISK: Components with complex orchestration, multiple external dependencies, encryption, or compliance rules
        - MEDIUM RISK: Components with external integrations but straightforward logic
        - LOW RISK: Simple CRUD, utility classes, DTOs
    - Database Parameters (Environment Variable Candidates): Analysis of the `PARAMETRO`, `PARAMETRO_ATRIBUTO`, and `ATTRIBUTE` tables/entities. Must contain:
        - Entity mapping: the file path of each Java class that maps to `PARAMETRO`, `PARAMETRO_ATRIBUTO`, or `ATTRIBUTE`
        - Access points: file paths and method names where these entities/tables are queried or read
        - Parameter Inventory: a de-duplicated list of every parameter name/code literal referenced in the code, one row per parameter, with:
            - Parameter name/code (exactly as it appears in code, e.g., `"MAX_RETRY_COUNT"`)
            - Source table (`PARAMETRO`, `PARAMETRO_ATRIBUTO`, or `ATTRIBUTE`)
            - Code reference (file:line)
            - Business Logic class/method that consumes it
            - Suggested Spring Boot environment variable name in `UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_COUNT`), for use with `@Value("${...}")` or `@ConfigurationProperties`
        - Dynamic lookups: any parameter code built at runtime from a variable rather than a literal, marked `DYNAMIC_LOOKUP`, with the code reference and a note that it requires manual review before it can become an env var
    - Project Parameters, must contain the next parameters (Getting from pom.xml configuration files from the project):
        - groupId
        - artifactId
        - packageName
    - DevOps Pipelines Config: Sometimes repositories contains Jenkinsfile or azure-pipelines.yml files; identify how the file is structured based on the assets found in the skill and indicated if a DevOps configurations is not found on project.
    