---
name: workshop-spring-boot-implement-logback-reactive
description: >-
  Implement SLF4J+Logback structured JSON logging for a reactive Spring Boot project using
  Scaffold Clean Architecture. Configures logback.xml with LogstashEncoder and
  MaskingJsonGeneratorDecorator for PII masking, customFields for observability metadata,
  reactive WebFilter for correlation header propagation via Reactor Context + MDC,
  and structured logging with kv() arguments from logstash-logback-encoder.
license: MIT
compatibility: Java 25, Spring Boot 4.x, Spring WebFlux, Project Reactor
metadata:
  author: criscast
  version: 1.0.0
  based-on: spring-boot-implement-logback
---

# Reactive Structured JSON Logging with SLF4J + Logback

## When to Use

Use this skill when implementing structured logging for a **reactive** Spring Boot application that:
- Uses Spring WebFlux + Project Reactor (`Mono<T>`, `Flux<T>`)
- Follows Scaffold Clean Architecture (reactive type)
- Requires JSON-structured log output for centralized log aggregation (ELK, Splunk, CloudWatch)
- Needs correlation header propagation across async reactive pipelines
- Must mask PII fields in log output (compliance requirement)
- Deploys to containerized environments where JSON stdout is captured by infrastructure

## Target

| Attribute       | Value                                      |
|-----------------|--------------------------------------------|
| Java            | 25                                         |
| Spring Boot     | 4.x (Spring Framework 7)                  |
| Web Framework   | Spring WebFlux + Project Reactor           |
| Architecture    | Scaffold Clean Architecture (reactive)     |
| Build Tool      | Gradle 9.2.1+                              |
| Serialization   | Jackson 3.x (`tools.jackson.*` package)    |
| Log Format      | Structured JSON (LogstashEncoder)          |
| Log Location    | `applications/app-service/src/main/resources/` |

## Context

You are a reactive logging expert specializing in structured JSON logging for Spring WebFlux
applications. You understand that reactive applications have unique logging challenges:

- Threads are shared across multiple requests (event loop model)
- MDC (Mapped Diagnostic Context) is thread-local and breaks in async contexts
- Reactor Context must be used to propagate correlation data across operator chains
- A single JSON appender strategy eliminates environment-specific configuration drift

Your implementation produces production-grade structured logs from day one, with PII masking,
correlation propagation, and observability metadata baked in.

## Requirements

Before starting, ensure the project:
1. Uses Spring Boot 4.x with `spring-boot-starter-webflux` dependency
2. Follows Scaffold Clean Architecture with `applications/app-service/` module
3. Uses Gradle as the build tool
4. Has Lombok configured (`@Slf4j` annotation support)

## Procedure

### Step 1: Add Gradle Dependencies

**File:** `build.gradle` (root project — `ext` block for version variables)

```groovy
ext {
    // ... other versions
    logbackVersion = '1.5.32'
    logstashLogbackVersion = '9.0'
}
```

**File:** `main.gradle` (shared subprojects configuration)

The logging dependencies are added to ALL infrastructure modules (entry-points, driven-adapters, helpers, app-service) but **excluded from domain modules** (model, usecase) to keep the domain layer free of infrastructure concerns.

```groovy
subprojects {
    dependencies {
        // ... other shared dependencies

        if (!project.name.equalsIgnoreCase(modelModule) && !project.name.equalsIgnoreCase(useCaseModule)) {
            dependencies.add("implementation", "ch.qos.logback:logback-classic:${logbackVersion}")
            dependencies.add("implementation", "net.logstash.logback:logstash-logback-encoder:${logstashLogbackVersion}")
        }
    }
}
```

This ensures:
- `logback-classic` (1.5.32) — Logback implementation for SLF4J
- `logstash-logback-encoder` (9.0) — Structured JSON logging with LogstashEncoder

Are available in all modules that perform I/O or need structured logging, while domain modules (`model` and `usecase`) remain clean with only `@Slf4j` from Lombok (which uses the SLF4J API without binding to a specific implementation).

> **Important:** These dependencies go in `main.gradle` (the shared subprojects block), NOT in individual module `build.gradle` files. This pattern ensures consistency across all infrastructure modules.

> **Why `logback-classic` is explicit:** Although `spring-boot-starter-webflux` includes logback transitively, the explicit version in `main.gradle` ensures all non-domain modules share the exact same version, preventing classpath conflicts in multi-module Gradle projects.

---

### Step 2: Create logback.xml

**File:** `applications/app-service/src/main/resources/logback.xml`

Create a single logback configuration with a JSON console appender. This file uses `logback.xml`
(NOT `logback-spring.xml`) — no Spring profile switching is used. All environments output
structured JSON.

```xml
<configuration>

    <appender name="default" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p --- [%15.15t] %-30.30logger{39} :  %m%n</pattern>
        </encoder>
    </appender>

    <appender name="jsonConsoleAppender" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"application":"${APP_NAME:-my-service}",
                "env": "${SERVICE_ENV:-local}",
                "capacity": "${CAPACITY:-my-capacity}",
                "bia": "${BIA:-false}",
                "country": "${COUNTRY:-co}",
                "serviceid": "${SERVICE_ID:-NQ000}"}</customFields>

            <decorator class="net.logstash.logback.mask.MaskingJsonGeneratorDecorator">
                <defaultMask>**********</defaultMask>
                <path>password</path>
                <path>otp</path>
                <path>token</path>
                <path>idNumber</path>
                <path>documentNumber</path>
                <path>phoneNumber</path>
                <path>name1</path>
                <path>lastName1</path>
                <path>birthDate</path>
                <path>expeditionDate</path>
            </decorator>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="jsonConsoleAppender"/>
    </root>
</configuration>
```

**Key elements:**
- `customFields`: Injects observability metadata into every log line using environment variables with defaults
- `decorator` (NOT `jsonGeneratorDecorator`): Configures PII masking via `MaskingJsonGeneratorDecorator`
- `defaultMask`: The replacement value for masked fields (`**********`)
- `<path>`: Each JSON field name to mask at any nesting depth
- `default` appender: Retained for fallback/local debugging but NOT referenced by root logger

**Environment variables to configure in deployment:**

| Variable      | Description                     | Default        |
|---------------|---------------------------------|----------------|
| `APP_NAME`    | Application/service name        | `my-service`   |
| `SERVICE_ENV` | Deployment environment          | `local`        |
| `CAPACITY`    | Infrastructure capacity label   | `my-capacity`  |
| `BIA`         | Business Impact Analysis flag   | `false`        |
| `COUNTRY`     | Country code                    | `co`           |
| `SERVICE_ID`  | the company service identifier        | `NQ000`        |

---

### Step 3: Create CorrelationWebFilter

**File:** `applications/app-service/src/main/java/com/company/config/CorrelationWebFilter.java`

This reactive `WebFilter` extracts correlation headers from incoming requests and propagates them
via both MDC (for the current thread) and Reactor Context (for async propagation).

```java
package com.company.config;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationWebFilter implements WebFilter {

    private static final String MESSAGE_ID = "messageId";
    private static final String CONSUMER_ID = "consumerId";
    private static final String PHONE_NUMBER = "phoneNumber";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String messageId = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst("x-message-id"))
            .orElse(UUID.randomUUID().toString());
        String consumerId = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst("x-consumer-id"))
            .orElse("unknown");
        String phoneNumber = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst("x-phone-number"))
            .orElse("");

        // Populate MDC for the current thread
        MDC.put(MESSAGE_ID, messageId);
        MDC.put(CONSUMER_ID, consumerId);
        MDC.put(PHONE_NUMBER, phoneNumber);

        return chain.filter(exchange)
            .contextWrite(Context.of(
                MESSAGE_ID, messageId,
                CONSUMER_ID, consumerId,
                PHONE_NUMBER, phoneNumber
            ))
            .doFinally(signalType -> MDC.clear());
    }
}
```

**Why WebFilter (not OncePerRequestFilter):**
- `OncePerRequestFilter` is from Spring MVC (servlet-based) and does NOT work with WebFlux
- `WebFilter` is the reactive equivalent, operating on `ServerWebExchange`
- `@Order(HIGHEST_PRECEDENCE)` ensures correlation is established before any other filter

---

### Step 4: Configure Reactor Context → MDC Propagation

Reactive applications execute operators across different threads. MDC is thread-local, so
correlation data stored in MDC is lost when operators switch threads. Two approaches exist:

#### Approach A: Custom Hooks (Manual Propagation)

Use this when `io.micrometer:context-propagation` is NOT on the classpath or you need full
control over propagation behavior.

**File:** `applications/app-service/src/main/java/com/company/config/ReactorMdcContextConfig.java`

```java
package com.company.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;

@Configuration
public class ReactorMdcContextConfig {

    private static final String MDC_CONTEXT_REACTOR_KEY = "reactor.mdc";

    @PostConstruct
    public void configureMdcContextPropagation() {
        Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY,
            Operators.lift((scannable, subscriber) -> new MdcContextLifter<>(subscriber)));
    }

    @PreDestroy
    public void cleanupHook() {
        Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY);
    }
}
```

**File:** `applications/app-service/src/main/java/com/company/config/MdcContextLifter.java`

```java
package com.company.config;

import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.Map;
import java.util.stream.Collectors;

public class MdcContextLifter<T> implements CoreSubscriber<T> {

    private final CoreSubscriber<T> delegate;

    public MdcContextLifter(CoreSubscriber<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Context currentContext() {
        return delegate.currentContext();
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        copyContextToMdc();
        delegate.onSubscribe(subscription);
    }

    @Override
    public void onNext(T t) {
        copyContextToMdc();
        delegate.onNext(t);
    }

    @Override
    public void onError(Throwable throwable) {
        copyContextToMdc();
        delegate.onError(throwable);
    }

    @Override
    public void onComplete() {
        copyContextToMdc();
        delegate.onComplete();
    }

    private void copyContextToMdc() {
        Context context = delegate.currentContext();
        if (!context.isEmpty()) {
            Map<String, String> mdcMap = context.stream()
                .filter(entry -> entry.getKey() instanceof String && entry.getValue() instanceof String)
                .collect(Collectors.toMap(
                    entry -> (String) entry.getKey(),
                    entry -> (String) entry.getValue()
                ));
            MDC.setContextMap(mdcMap);
        } else {
            MDC.clear();
        }
    }
}
```

#### Approach B: Micrometer Context Propagation (Automatic — Preferred for Spring Boot 4.x)

If `io.micrometer:context-propagation` is on the classpath (included transitively by
`spring-boot-starter-actuator` or `micrometer-tracing`), MDC propagation happens automatically.

**File:** `applications/app-service/build.gradle` (additional dependency if not already present)

```groovy
dependencies {
    implementation 'io.micrometer:context-propagation'  // version managed by Spring Boot BOM
}
```

With this library present, Spring Boot 4.x auto-configures Reactor context propagation. The
custom `ReactorMdcContextConfig` and `MdcContextLifter` are **NOT needed**.

**How to choose:**
- If your project already uses `spring-boot-starter-actuator` or Micrometer Tracing → Use Approach B (automatic)
- If your project is minimal without actuator/tracing → Use Approach A (manual hooks)
- If both are present, Approach B takes precedence and Approach A should be removed to avoid conflicts

---

### Step 5: Structured Logging Usage Patterns

Use `@Slf4j` (Lombok) on all classes and `kv()` from `StructuredArguments` for structured fields.

```java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RequiredArgsConstructor
public class BalanceAdapter {

    private final DatabaseClient databaseClient;

    public Mono<Balance> getBalance(String accountNumber) {
        return databaseClient.sql("SELECT balance FROM accounts WHERE account_number = :account")
            .bind("account", accountNumber)
            .map(row -> new Balance(row.get("balance", BigDecimal.class)))
            .one()
            .doOnSubscribe(sub -> log.info("Querying balance", kv("accountNumber", accountNumber)))
            .doOnSuccess(result -> log.info("Balance retrieved", kv("balance", result.amount())))
            .doOnError(error -> log.error("Failed to retrieve balance",
                kv("accountNumber", accountNumber), error));
    }
}
```

**Structured logging patterns for reactive operators:**

```java
@Slf4j
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;

    public Mono<TransferResult> execute(TransferRequest request) {
        log.info("Transfer initiated",
            kv("sourceAccount", request.sourceAccount()),
            kv("targetAccount", request.targetAccount()),
            kv("amount", request.amount()));

        return accountRepository.findById(request.sourceAccount())
            .switchIfEmpty(Mono.defer(() -> {
                log.warn("Source account not found", kv("accountId", request.sourceAccount()));
                return Mono.error(new AccountNotFoundException(request.sourceAccount()));
            }))
            .flatMap(account -> processTransfer(account, request))
            .doOnSuccess(result -> log.info("Transfer completed",
                kv("transferId", result.id()),
                kv("status", result.status())))
            .doOnError(error -> log.error("Transfer failed",
                kv("sourceAccount", request.sourceAccount()),
                kv("errorType", error.getClass().getSimpleName()),
                error));
    }
}
```

**Key rules:**
- Always use `kv("fieldName", value)` — produces `"fieldName": "value"` in JSON output
- Pass the exception as the LAST argument to `log.error()` — logback prints the stack trace
- Use `doOnSubscribe` for "request started" logs, `doOnSuccess` for "completed" logs
- Use `doOnError` for error logging — do NOT catch and rethrow just to log
- NEVER log PII values directly — they are masked by the decorator only if the field name matches

---

### Step 6: PII Masking Configuration Guidelines

The `MaskingJsonGeneratorDecorator` masks fields by JSON path at any nesting depth.

**Adding new fields to mask:**

Edit `logback.xml` and add `<path>` elements:

```xml
<decorator class="net.logstash.logback.mask.MaskingJsonGeneratorDecorator">
    <defaultMask>**********</defaultMask>
    <!-- Authentication -->
    <path>password</path>
    <path>otp</path>
    <path>token</path>
    <!-- Personal identification -->
    <path>idNumber</path>
    <path>documentNumber</path>
    <path>phoneNumber</path>
    <!-- Personal data -->
    <path>name1</path>
    <path>lastName1</path>
    <path>birthDate</path>
    <path>expeditionDate</path>
    <!-- Add new fields here -->
    <path>email</path>
    <path>address</path>
    <path>creditCardNumber</path>
</decorator>
```

**How masking works:**
- Any JSON field whose name matches a `<path>` entry is replaced with `**********`
- Matching is by field name at ANY depth in the JSON structure
- The original value never reaches the log output
- Applies to all log statements including `kv()` structured arguments

**Example output (masked):**

```json
{
  "@timestamp": "2026-08-24T22:01:50.123-05:00",
  "level": "INFO",
  "message": "User authenticated",
  "application": "my-service",
  "env": "production",
  "phoneNumber": "**********",
  "token": "**********",
  "messageId": "abc-123-def"
}
```

---

## Application YAML Configuration

**File:** `applications/app-service/src/main/resources/application.yaml`

Observability section relevant to logging and tracing integration:

```yaml
management:
  opentelemetry:
    resource-attributes:
      service.name: "${SERVICE_NAME:-my-service-local}"
      deployment.environment: "${SERVICE_ENV:-local}"
      service.capacity: "${CAPACITY:-my-capacity}"
      service.serviceid: "${SERVICE_ID:-NQ000}"
      service.country: "${COUNTRY:-co}"
      service.bia: "${BIA:-false}"
  tracing:
    baggage:
      correlation:
        fields:
          - x-message-id
          - x-phone-number
      remote-fields: x-message-id,x-phone-number
```

This configuration ensures:
- OpenTelemetry resource attributes align with `customFields` in logback.xml
- Correlation headers (`x-message-id`, `x-phone-number`) are propagated as tracing baggage
- Remote fields are forwarded to downstream services

---

## Best Practices

1. **One appender, all environments** — JSON output is parseable by humans (with `jq`) and machines alike. No profile switching eliminates config drift.

2. **Structured arguments over string concatenation** — Use `kv()` instead of `"field=" + value`. Structured fields are searchable and indexable.

3. **Log at boundaries** — Log at entry/exit points of adapters and services, not inside utility methods.

4. **Use appropriate log levels:**
   - `ERROR` — Unexpected failures requiring investigation
   - `WARN` — Business rule violations, degraded operation
   - `INFO` — Significant business events (transaction completed, user authenticated)
   - `DEBUG` — Detailed diagnostic information (disabled in production by default)

5. **Exception as last argument** — Always pass `Throwable` as the last argument to `log.error()`. Never call `error.getMessage()` separately.

6. **Never log then rethrow** — Either log OR propagate. Logging then rethrowing creates duplicate log entries.

7. **Correlation in every request** — The `CorrelationWebFilter` ensures every log line within a request contains `messageId`, enabling end-to-end tracing.

8. **Mask first, ask questions later** — When in doubt about whether a field is PII, add it to the masking paths. False positives are far less costly than data leaks.

9. **Environment variables for metadata** — Never hardcode environment-specific values in logback.xml. Use `${VAR:-default}` syntax.

10. **Do not log request/response bodies** — They frequently contain PII. Log only identifiers and status codes.

---

## Post-Implementation Validation Checklist

After implementation, verify:

- [ ] Application starts without logback configuration errors
- [ ] Log output is valid JSON (pipe to `jq .` to verify)
- [ ] `customFields` appear in every log line (application, env, capacity, bia, country, serviceid)
- [ ] `messageId` appears in logs when `x-message-id` header is sent
- [ ] `messageId` is auto-generated (UUID) when header is absent
- [ ] PII fields (password, phoneNumber, documentNumber, etc.) show `**********` in output
- [ ] `kv()` arguments appear as top-level JSON fields in log output
- [ ] Correlation propagates across `flatMap`, `zip`, and other async operators
- [ ] Stack traces appear in full for `log.error("msg", exception)` calls
- [ ] No duplicate `slf4j-api` or `logback-classic` in dependency tree (`./gradlew dependencies`)
- [ ] Root logger level is `INFO` (not `DEBUG` in production)

**Quick validation commands:**

```bash
# Start the application and verify JSON output
./gradlew :applications:app-service:bootRun 2>&1 | head -5 | jq .

# Verify no duplicate logging dependencies
./gradlew :applications:app-service:dependencies --configuration runtimeClasspath | grep -i logback

# Test correlation header propagation
curl -s -H "x-message-id: test-123" http://localhost:8080/api/health | jq .
# Then check logs for messageId: "test-123"
```

---

## Constraints and Warnings

| Constraint | Reason |
|------------|--------|
| **No GenericLogger, LoggerFactory, TypeEnum, ServiceTypeEnum** | Legacy patterns. Use `@Slf4j` directly. |
| **File is `logback.xml`** (NOT `logback-spring.xml`) | No Spring profile switching — single JSON appender for all environments. |
| **Gradle only** (NOT Maven) | Project uses Gradle 9.2.1+. |
| **No `<jsonGeneratorDecorator>` element** | Use `<decorator>` element for `MaskingJsonGeneratorDecorator`. |
| **PII masking is mandatory** | All services must mask sensitive fields. Non-negotiable compliance requirement. |
| **Dependencies in `main.gradle`, not module `build.gradle`** | `logback-classic` and `logstash-logback-encoder` are declared in the shared `main.gradle` subprojects block, excluded from domain modules (model, usecase). |
| **Single JSON appender for ALL environments** | Eliminates config drift. Use `jq` for local readability. |
| **`customFields` must include all 6 fields** | `application`, `env`, `capacity`, `bia`, `country`, `serviceid` — required for observability platform indexing. |
| **Use `kv()` for all structured fields** | Do NOT use string concatenation in log messages. |
| **Never log PII unmasked** | Document numbers, phone numbers, names, birth dates must always pass through the masking decorator. |
| **WebFilter, not OncePerRequestFilter** | `OncePerRequestFilter` is servlet-based and incompatible with WebFlux. |
| **Jackson 3.x** | The project uses `tools.jackson.*` packages (NOT `com.fasterxml.jackson.*`). LogstashEncoder uses its own Jackson internally — no conflict. |

---

## Troubleshooting

**Logs are not in JSON format:**
- Verify `logback.xml` is in `applications/app-service/src/main/resources/`
- Check that `logstash-logback-encoder` dependency is resolved: `./gradlew dependencies | grep logstash`
- Ensure no other `logback-spring.xml` or `logback-test.xml` is overriding configuration

**MDC fields (messageId) missing in async operators:**
- Verify Reactor Context propagation is configured (Approach A or B from Step 4)
- Check that `contextWrite()` is called AFTER the chain (it propagates upstream)
- If using Approach B, verify `context-propagation` library is on classpath

**PII fields not being masked:**
- Field names in `<path>` must match the JSON field name exactly (case-sensitive)
- Verify the field is logged via `kv("fieldName", value)` — the field name must match
- Nested fields are matched by field name at any depth — no JsonPath syntax needed

**Duplicate log lines:**
- Check you are not logging AND rethrowing the same exception
- Verify only one of Approach A or B is active (not both)

**ClassNotFoundException for MaskingJsonGeneratorDecorator:**
- Upgrade `logstash-logback-encoder` to 7.4+ (masking was introduced in 7.2)
- Recommended version: `9.0`
