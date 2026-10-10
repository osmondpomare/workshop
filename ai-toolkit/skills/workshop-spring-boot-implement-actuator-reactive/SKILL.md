---
name: workshop-spring-boot-implement-actuator-reactive
description: >-
  Configure Spring Boot Actuator for production-grade observability in reactive WebFlux microservices
  on Scaffold Clean Architecture. Implements OTLP metrics export, OpenTelemetry distributed tracing,
  granular metric toggles via environment variables, Resilience4j circuit breaker health indicators,
  R2DBC health checks, baggage correlation fields, and custom business metrics via MeterRegistry.
  All configurations follow the production patterns from the company reference microservices.
license: MIT
compatibility: Java 25, Spring Boot 4.x, Spring WebFlux, OpenTelemetry, OTLP, Micrometer
metadata:
  author: criscast
  version: 1.0.0
  based-on: spring-boot-implement-actuator
---

# Spring Boot Actuator – Reactive WebFlux (OTLP + OpenTelemetry)

Production-grade observability configuration for reactive Spring Boot microservices deployed on
Scaffold Clean Architecture. Based on verified patterns from the company reference microservices:
`cocorebankint_balance_ms`, `cocorebankint_authorize_ms`, `cointgateway_auth_ms`, `cospibreb_status_ms`.

---

## 1. When to Use

- Setting up observability (metrics + tracing) in a reactive Spring Boot 4.x microservice
- Configuring OTLP metrics export to an OpenTelemetry Collector
- Enabling distributed tracing with OpenTelemetry and OTLP endpoints
- Adding Resilience4j circuit breaker health indicators
- Implementing custom business metrics via MeterRegistry
- Configuring R2DBC health checks for reactive database connections
- Deploying to ECS (not Kubernetes) with network-level security

---

## 2. Target

| Aspect              | Value                                          |
|---------------------|------------------------------------------------|
| Java                | 25                                             |
| Spring Boot         | 4.x (Spring Framework 7)                      |
| Web Stack           | Spring WebFlux + Project Reactor               |
| Architecture        | Scaffold Clean Architecture (reactive)         |
| Build Tool          | Gradle 9.2.1+                                  |
| Metrics Export      | OTLP (OpenTelemetry Collector via HTTP)        |
| Tracing             | OpenTelemetry with OTLP endpoint               |
| Deployment          | ECS (NOT Kubernetes)                           |
| Security            | Network-level (API Gateway/ECS, NOT Spring Security) |

---

## 3. Context

You are a reactive observability expert specializing in Spring Boot 4.x microservices with
OpenTelemetry instrumentation. You implement production-grade monitoring following the company's
standardized patterns for ECS-deployed services. You configure OTLP-based metrics and tracing,
granular metric toggles, circuit breaker health indicators, and custom business counters.

---

## 4. Requirements

Before implementing, ensure the project has:

- [ ] Scaffold Clean Architecture (reactive type) already generated
- [ ] Spring Boot 4.x with WebFlux starter configured
- [ ] Gradle 9.2.1+ as the build tool
- [ ] R2DBC driver configured (for R2DBC health checks)
- [ ] Resilience4j dependency (if circuit breaker health is needed)
- [ ] Access to an OpenTelemetry Collector endpoint (for metrics/tracing export)

---

## 5. Procedure

### Step 1: Add Gradle Dependencies

#### `applications/app-service/build.gradle`

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-opentelemetry'
    implementation "io.opentelemetry.instrumentation:opentelemetry-resources:2.25.0-alpha"
}
```

#### `infrastructure/entry-points/reactive-web/build.gradle`

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

#### `infrastructure/helpers/metrics/build.gradle` (if custom metrics needed)

```groovy
dependencies {
    implementation project(':model')
    implementation 'org.springframework:spring-context'
    implementation 'jakarta.annotation:jakarta.annotation-api'
    implementation 'io.micrometer:micrometer-core'
}
```

> **IMPORTANT**: Do NOT add `micrometer-registry-prometheus`. This architecture uses OTLP export
> exclusively. Do NOT add `spring-boot-starter-web` — the project uses WebFlux only.

---

### Step 2: Configure application.yaml

Place this in `applications/app-service/src/main/resources/application.yaml`:

```yaml
management:
  metrics:
    enable:
      all: "${METRICS_ENABLE_ALL:false}"
      system: "${METRICS_ENABLE_SYSTEM:false}"
      process: "${METRICS_ENABLE_PROCESS:false}"
      application: "${METRICS_ENABLE_APPLICATION:false}"
      disk: "${METRICS_ENABLE_DISK:false}"
      jvm: "${METRICS_ENABLE_JVM:true}"
      http.server.requests: "${METRICS_ENABLE_HTTP_SERVER_REQ:false}"
      logback: "${METRICS_ENABLE_LOGBACK:false}"
      resilience4j: "${METRICS_ENABLE_RESILIENCE4J:false}"
    tags:
      instance: "${HOSTNAME:unknown}"
  endpoints:
    web:
      exposure:
        include: "health, metrics"
  endpoint:
    health:
      probes:
        enabled: false
      show-details: always
  observations:
    annotations:
      enabled: true
  otlp:
    metrics:
      export:
        enabled: "${OTEL_COLLECTOR_ENABLED:true}"
        url: "${OTEL_COLLECTOR_HOST:http://localhost:4318}/v1/metrics"
        step: 1m
        headers:
          Authorization: "${OTEL_COLLECTOR_AUTH_HEADER:}"
  opentelemetry:
    tracing:
      export:
        otlp:
          endpoint: "${OTEL_COLLECTOR_HOST:http://localhost:4318}/v1/traces"
          headers:
            Authorization: "${OTEL_COLLECTOR_AUTH_HEADER:}"
          compression: gzip
    resource-attributes:
      service.name: "${SERVICE_NAME:my-service-local}"
      service.namespace: "${SERVICE_NAMESPACE:company}"
      deployment.environment: "${SERVICE_ENV:local}"
      service.capacity: "${CAPACITY:my-capacity}"
      service.serviceid: "${SERVICE_ID:NQ000}"
      service.country: "${COUNTRY:co}"
      service.bia: "${BIA:false}"
      service.region: "${REGION:us-east-1}"
      service.component_type: "${COMPONENT_TYPE:ecs}"
      instance: "${HOSTNAME:unknown}"
  tracing:
    export:
      enabled: "${OTEL_TRACING_ENABLED:true}"
    sampling:
      probability: "${OTEL_TRACING_SAMPLING:0.1}"
    baggage:
      correlation:
        fields:
          - x-message-id
          - x-phone-number
      remote-fields: x-message-id,x-phone-number
  health:
    circuitbreakers:
      enabled: true
    diskspace:
      enabled: false
    ping:
      enabled: false
    r2dbc:
      enabled: true

springdoc:
  swagger-ui:
    enabled: false
```

---

### Step 3: Configure Resilience4j Circuit Breaker Health

Add the following to `application.yaml` (same file, after the `management` block):

```yaml
resilience4j.circuitbreaker:
  circuit-breaker-aspect-order: 1
  configs:
    default:
      register-health-indicator: true
  instances:
    my-service:
      base-config: "default"
      failure-rate-threshold: "${MY_SERVICE_FAILURE_RATE_THRESHOLD:40}"
      slow-call-rate-threshold: "${MY_SERVICE_SLOW_CALL_RATE_THRESHOLD:40}"
      slow-call-duration-threshold: "${MY_SERVICE_SLOW_CALL_DURATION_THRESHOLD:5s}"
      sliding-window-type: "${MY_SERVICE_SLIDING_WINDOW_TYPE:TIME_BASED}"
      sliding-window-size: "${MY_SERVICE_SLIDING_WINDOW_SIZE:30}"
      minimum-number-of-calls: "${MY_SERVICE_MIN_NUMBER_CALLS:20}"
      wait-duration-in-open-state: "${MY_SERVICE_WAIT_DURATION_OPEN_STATE:30s}"
      max-wait-duration-in-half-open-state: "${MY_SERVICE_MAX_WAIT_DURATION_HALF_OPEN_STATE:120s}"
      permitted-number-of-calls-in-half-open-state: "${MY_SERVICE_PERMITTED_CALLS_HALF_OPEN_STATE:10}"
      automatic-transition-from-open-to-half-open-enabled: "${MY_SERVICE_AUTO_TRANS_OPEN_TO_HALF_OPEN_STATE:false}"
      record-exceptions: "${MY_SERVICE_RECORD_EXCEPTIONS:}"
      ignore-exceptions: "${MY_SERVICE_IGNORE_EXCEPTIONS:}"
```

**Key points:**
- `register-health-indicator: true` in `configs.default` exposes circuit breaker state in `/actuator/health`
- `circuit-breaker-aspect-order: 1` ensures circuit breaker runs before other aspects
- Replace `my-service` with the actual downstream service name
- Add additional instances for each downstream dependency

---

### Step 4: Create Custom Business Metrics Helper (Optional)

Create the metrics helper module at `infrastructure/helpers/metrics/`:

#### `infrastructure/helpers/metrics/src/main/java/com/company/metrics/BusinessMetricsRecorder.java`

```java
package com.company.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessMetricsRecorder {

    private final MeterRegistry registry;

    private static final String METRIC_NAME = "company.my_capacity.transactions.total";
    private static final String METRIC_DESCRIPTION = "Total number of transactions";

    public void recordSuccess(String transactionType) {
        Counter.builder(METRIC_NAME)
                .description(METRIC_DESCRIPTION)
                .tag("status", "success")
                .tag("failure_category", "none")
                .tag("transaction_type", transactionType)
                .register(registry)
                .increment();
    }

    public void recordFailure(String transactionType, String failureCategory) {
        Counter.builder(METRIC_NAME)
                .description(METRIC_DESCRIPTION)
                .tag("status", "failed")
                .tag("failure_category", failureCategory)
                .tag("transaction_type", transactionType)
                .register(registry)
                .increment();
    }
}
```

**Naming convention**: `company.<capacity>.transactions.total`

**Required tags**:
- `status`: `success` or `failed`
- `failure_category`: error classification or `none`
- `transaction_type`: business operation name

#### Usage in a Use Case (domain layer adapter):

```java
@Component
@RequiredArgsConstructor
public class ProcessTransactionUseCase {

    private final TransactionGateway transactionGateway;
    private final BusinessMetricsRecorder metricsRecorder;

    public Mono<TransactionResult> execute(TransactionRequest request) {
        return transactionGateway.process(request)
                .doOnSuccess(result -> metricsRecorder.recordSuccess(request.type()))
                .doOnError(error -> metricsRecorder.recordFailure(
                        request.type(), classifyError(error)));
    }

    private String classifyError(Throwable error) {
        if (error instanceof TimeoutException) return "timeout";
        if (error instanceof ConnectionException) return "connection";
        return "unknown";
    }
}
```

---

### Step 5: Verify

1. **Start the application** and confirm actuator endpoints are accessible:
   ```bash
   curl http://localhost:8080/actuator/health
   curl http://localhost:8080/actuator/metrics
   ```

2. **Verify health response** includes circuit breaker and R2DBC indicators:
   ```json
   {
     "status": "UP",
     "components": {
       "circuitBreakers": {
         "status": "UP",
         "details": {
           "my-service": { "status": "UP" }
         }
       },
       "r2dbc": {
         "status": "UP"
       }
     }
   }
   ```

3. **Verify OTLP metrics export** — check collector logs for incoming metrics
4. **Verify tracing** — confirm spans appear in your observability backend
5. **Verify custom metrics** — call `/actuator/metrics/company.my_capacity.transactions.total`

---

## 6. Full application.yaml Template (Production-Ready)

```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  application:
    name: ${SERVICE_NAME:my-service}

management:
  metrics:
    enable:
      all: "${METRICS_ENABLE_ALL:false}"
      system: "${METRICS_ENABLE_SYSTEM:false}"
      process: "${METRICS_ENABLE_PROCESS:false}"
      application: "${METRICS_ENABLE_APPLICATION:false}"
      disk: "${METRICS_ENABLE_DISK:false}"
      jvm: "${METRICS_ENABLE_JVM:true}"
      http.server.requests: "${METRICS_ENABLE_HTTP_SERVER_REQ:false}"
      logback: "${METRICS_ENABLE_LOGBACK:false}"
      resilience4j: "${METRICS_ENABLE_RESILIENCE4J:false}"
    tags:
      instance: "${HOSTNAME:unknown}"
  endpoints:
    web:
      exposure:
        include: "health, metrics"
  endpoint:
    health:
      probes:
        enabled: false
      show-details: always
  observations:
    annotations:
      enabled: true
  otlp:
    metrics:
      export:
        enabled: "${OTEL_COLLECTOR_ENABLED:true}"
        url: "${OTEL_COLLECTOR_HOST:http://localhost:4318}/v1/metrics"
        step: 1m
        headers:
          Authorization: "${OTEL_COLLECTOR_AUTH_HEADER:}"
  opentelemetry:
    tracing:
      export:
        otlp:
          endpoint: "${OTEL_COLLECTOR_HOST:http://localhost:4318}/v1/traces"
          headers:
            Authorization: "${OTEL_COLLECTOR_AUTH_HEADER:}"
          compression: gzip
    resource-attributes:
      service.name: "${SERVICE_NAME:my-service-local}"
      service.namespace: "${SERVICE_NAMESPACE:company}"
      deployment.environment: "${SERVICE_ENV:local}"
      service.capacity: "${CAPACITY:my-capacity}"
      service.serviceid: "${SERVICE_ID:NQ000}"
      service.country: "${COUNTRY:co}"
      service.bia: "${BIA:false}"
      service.region: "${REGION:us-east-1}"
      service.component_type: "${COMPONENT_TYPE:ecs}"
      instance: "${HOSTNAME:unknown}"
  tracing:
    export:
      enabled: "${OTEL_TRACING_ENABLED:true}"
    sampling:
      probability: "${OTEL_TRACING_SAMPLING:0.1}"
    baggage:
      correlation:
        fields:
          - x-message-id
          - x-phone-number
      remote-fields: x-message-id,x-phone-number
  health:
    circuitbreakers:
      enabled: true
    diskspace:
      enabled: false
    ping:
      enabled: false
    r2dbc:
      enabled: true

resilience4j.circuitbreaker:
  circuit-breaker-aspect-order: 1
  configs:
    default:
      register-health-indicator: true
  instances:
    my-service:
      base-config: "default"
      failure-rate-threshold: "${MY_SERVICE_FAILURE_RATE_THRESHOLD:40}"
      slow-call-rate-threshold: "${MY_SERVICE_SLOW_CALL_RATE_THRESHOLD:40}"
      slow-call-duration-threshold: "${MY_SERVICE_SLOW_CALL_DURATION_THRESHOLD:5s}"
      sliding-window-type: "${MY_SERVICE_SLIDING_WINDOW_TYPE:TIME_BASED}"
      sliding-window-size: "${MY_SERVICE_SLIDING_WINDOW_SIZE:30}"
      minimum-number-of-calls: "${MY_SERVICE_MIN_NUMBER_CALLS:20}"
      wait-duration-in-open-state: "${MY_SERVICE_WAIT_DURATION_OPEN_STATE:30s}"
      max-wait-duration-in-half-open-state: "${MY_SERVICE_MAX_WAIT_DURATION_HALF_OPEN_STATE:120s}"
      permitted-number-of-calls-in-half-open-state: "${MY_SERVICE_PERMITTED_CALLS_HALF_OPEN_STATE:10}"
      automatic-transition-from-open-to-half-open-enabled: "${MY_SERVICE_AUTO_TRANS_OPEN_TO_HALF_OPEN_STATE:false}"
      record-exceptions: "${MY_SERVICE_RECORD_EXCEPTIONS:}"
      ignore-exceptions: "${MY_SERVICE_IGNORE_EXCEPTIONS:}"

springdoc:
  swagger-ui:
    enabled: false
```

---

## 7. Gradle Dependencies Reference

### `applications/app-service/build.gradle`

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-opentelemetry'
    implementation "io.opentelemetry.instrumentation:opentelemetry-resources:2.25.0-alpha"
}
```

### `infrastructure/entry-points/reactive-web/build.gradle`

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

### `infrastructure/helpers/metrics/build.gradle`

```groovy
dependencies {
    implementation project(':model')
    implementation 'org.springframework:spring-context'
    implementation 'jakarta.annotation:jakarta.annotation-api'
    implementation 'io.micrometer:micrometer-core'
}
```

> **DO NOT ADD**: `micrometer-registry-prometheus`, `spring-boot-starter-web`,
> `opentelemetry-exporter-zipkin`, `opentelemetry-exporter-jaeger`.
> These are incompatible with the OTLP-only reactive architecture.

---

## 8. Custom Metrics Helper Pattern

### File Structure

```
infrastructure/helpers/metrics/
├── build.gradle
└── src/main/java/com/company/metrics/
    └── BusinessMetricsRecorder.java
```

### Implementation

```java
package com.company.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessMetricsRecorder {

    private final MeterRegistry registry;

    private static final String METRIC_NAME = "company.my_capacity.transactions.total";
    private static final String METRIC_DESCRIPTION = "Total number of transactions";

    public void recordSuccess(String transactionType) {
        Counter.builder(METRIC_NAME)
                .description(METRIC_DESCRIPTION)
                .tag("status", "success")
                .tag("failure_category", "none")
                .tag("transaction_type", transactionType)
                .register(registry)
                .increment();
    }

    public void recordFailure(String transactionType, String failureCategory) {
        Counter.builder(METRIC_NAME)
                .description(METRIC_DESCRIPTION)
                .tag("status", "failed")
                .tag("failure_category", failureCategory)
                .tag("transaction_type", transactionType)
                .register(registry)
                .increment();
    }
}
```

### Metric Naming Convention

| Pattern | Example |
|---------|---------|
| `company.<capacity>.transactions.total` | `company.balance.transactions.total` |
| `company.<capacity>.operations.<operation>.total` | `company.authorize.operations.validate.total` |

### Required Tags

| Tag | Values | Description |
|-----|--------|-------------|
| `status` | `success`, `failed` | Transaction outcome |
| `failure_category` | `timeout`, `connection`, `validation`, `business`, `none` | Error classification |
| `transaction_type` | Business operation name | Type of operation performed |

---

## 9. Resilience4j Circuit Breaker Configuration

### Key Settings Explained

| Property | Default | Description |
|----------|---------|-------------|
| `failure-rate-threshold` | 40% | Percentage of failures to open circuit |
| `slow-call-rate-threshold` | 40% | Percentage of slow calls to open circuit |
| `slow-call-duration-threshold` | 5s | Calls exceeding this are considered slow |
| `sliding-window-type` | TIME_BASED | Use time window (not count) |
| `sliding-window-size` | 30 | Window size in seconds (TIME_BASED) |
| `minimum-number-of-calls` | 20 | Minimum calls before evaluating thresholds |
| `wait-duration-in-open-state` | 30s | Time in OPEN before transitioning to HALF_OPEN |
| `max-wait-duration-in-half-open-state` | 120s | Max time in HALF_OPEN |
| `permitted-number-of-calls-in-half-open-state` | 10 | Test calls in HALF_OPEN |
| `automatic-transition-from-open-to-half-open-enabled` | false | Manual transition (wait for next call) |

### Adding Multiple Downstream Instances

```yaml
resilience4j.circuitbreaker:
  circuit-breaker-aspect-order: 1
  configs:
    default:
      register-health-indicator: true
  instances:
    balance-service:
      base-config: "default"
      failure-rate-threshold: "${BALANCE_FAILURE_RATE_THRESHOLD:40}"
      slow-call-duration-threshold: "${BALANCE_SLOW_CALL_DURATION:5s}"
      sliding-window-size: "${BALANCE_SLIDING_WINDOW_SIZE:30}"
      minimum-number-of-calls: "${BALANCE_MIN_NUMBER_CALLS:20}"
      wait-duration-in-open-state: "${BALANCE_WAIT_DURATION_OPEN_STATE:30s}"
    auth-service:
      base-config: "default"
      failure-rate-threshold: "${AUTH_FAILURE_RATE_THRESHOLD:50}"
      slow-call-duration-threshold: "${AUTH_SLOW_CALL_DURATION:3s}"
      sliding-window-size: "${AUTH_SLIDING_WINDOW_SIZE:20}"
      minimum-number-of-calls: "${AUTH_MIN_NUMBER_CALLS:10}"
      wait-duration-in-open-state: "${AUTH_WAIT_DURATION_OPEN_STATE:60s}"
```

### Health Response with Circuit Breakers

```json
{
  "status": "UP",
  "components": {
    "circuitBreakers": {
      "status": "UP",
      "details": {
        "balance-service": {
          "status": "UP",
          "details": {
            "failureRate": "0.0%",
            "slowCallRate": "0.0%",
            "state": "CLOSED",
            "bufferedCalls": 15,
            "failedCalls": 0,
            "slowCalls": 0,
            "notPermittedCalls": 0
          }
        }
      }
    }
  }
}
```

---

## 10. Environment Variables Reference

### OTLP / OpenTelemetry

| Variable | Default | Description |
|----------|---------|-------------|
| `OTEL_COLLECTOR_ENABLED` | `true` | Enable/disable OTLP metrics export |
| `OTEL_COLLECTOR_HOST` | `http://localhost:4318` | OpenTelemetry Collector base URL |
| `OTEL_COLLECTOR_AUTH_HEADER` | _(empty)_ | Authorization header for collector |
| `OTEL_TRACING_ENABLED` | `true` | Enable/disable distributed tracing |
| `OTEL_TRACING_SAMPLING` | `0.1` | Sampling probability (0.0 to 1.0) |

### Service Resource Attributes

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVICE_NAME` | `my-service-local` | Service name in traces/metrics |
| `SERVICE_NAMESPACE` | `company` | Namespace grouping |
| `SERVICE_ENV` | `local` | Deployment environment |
| `CAPACITY` | `my-capacity` | Business capacity identifier |
| `SERVICE_ID` | `NQ000` | the company service ID |
| `COUNTRY` | `co` | Country code |
| `BIA` | `false` | Business Impact Analysis flag |
| `REGION` | `us-east-1` | AWS region |
| `COMPONENT_TYPE` | `ecs` | Infrastructure component type |
| `HOSTNAME` | `unknown` | Instance hostname (auto-set by ECS) |

### Metric Toggles

| Variable | Default | Description |
|----------|---------|-------------|
| `METRICS_ENABLE_ALL` | `false` | Master toggle for all metrics |
| `METRICS_ENABLE_SYSTEM` | `false` | OS-level system metrics |
| `METRICS_ENABLE_PROCESS` | `false` | Process CPU/memory metrics |
| `METRICS_ENABLE_APPLICATION` | `false` | Spring application metrics |
| `METRICS_ENABLE_DISK` | `false` | Disk space metrics |
| `METRICS_ENABLE_JVM` | `true` | JVM heap, GC, threads |
| `METRICS_ENABLE_HTTP_SERVER_REQ` | `false` | HTTP request duration/count |
| `METRICS_ENABLE_LOGBACK` | `false` | Logback event counters |
| `METRICS_ENABLE_RESILIENCE4J` | `false` | Resilience4j metrics |

### Circuit Breaker (per instance)

| Variable | Default | Description |
|----------|---------|-------------|
| `MY_SERVICE_FAILURE_RATE_THRESHOLD` | `40` | Failure rate to open (%) |
| `MY_SERVICE_SLOW_CALL_RATE_THRESHOLD` | `40` | Slow call rate to open (%) |
| `MY_SERVICE_SLOW_CALL_DURATION_THRESHOLD` | `5s` | Slow call threshold |
| `MY_SERVICE_SLIDING_WINDOW_TYPE` | `TIME_BASED` | Window type |
| `MY_SERVICE_SLIDING_WINDOW_SIZE` | `30` | Window size |
| `MY_SERVICE_MIN_NUMBER_CALLS` | `20` | Minimum calls for evaluation |
| `MY_SERVICE_WAIT_DURATION_OPEN_STATE` | `30s` | Wait before half-open |
| `MY_SERVICE_MAX_WAIT_DURATION_HALF_OPEN_STATE` | `120s` | Max half-open time |
| `MY_SERVICE_PERMITTED_CALLS_HALF_OPEN_STATE` | `10` | Test calls in half-open |
| `MY_SERVICE_AUTO_TRANS_OPEN_TO_HALF_OPEN_STATE` | `false` | Auto transition |
| `MY_SERVICE_RECORD_EXCEPTIONS` | _(empty)_ | Exceptions to record as failure |
| `MY_SERVICE_IGNORE_EXCEPTIONS` | _(empty)_ | Exceptions to ignore |

---

## 11. Health Indicators Configuration

### Enabled Health Indicators

| Indicator | Enabled | Purpose |
|-----------|---------|---------|
| `circuitbreakers` | ✅ `true` | Resilience4j circuit breaker states |
| `r2dbc` | ✅ `true` | Reactive database connectivity |
| `diskspace` | ❌ `false` | Not relevant for containerized ECS |
| `ping` | ❌ `false` | Redundant with health endpoint itself |

### Key Decisions

- **`probes.enabled: false`** — ECS uses the health endpoint directly, not Kubernetes probes (`/readyz`, `/livez`)
- **`show-details: always`** — Safe because actuator is behind network-level security (API Gateway/VPC)
- **Exposure limited to `health, metrics`** — No `env`, `beans`, `info` exposed
- **No Spring Security on actuator** — Security is delegated to infrastructure (API Gateway, security groups)

### Custom Health Indicator (Optional)

For domain-specific health checks:

```java
package com.company.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DownstreamServiceHealthIndicator implements ReactiveHealthIndicator {

    private final WebClient downstreamClient;

    public DownstreamServiceHealthIndicator(WebClient downstreamClient) {
        this.downstreamClient = downstreamClient;
    }

    @Override
    public Mono<Health> health() {
        return downstreamClient.get()
                .uri("/health")
                .retrieve()
                .toBodilessEntity()
                .map(response -> Health.up().build())
                .onErrorResume(ex -> Mono.just(
                        Health.down().withException(ex).build()))
                .timeout(java.time.Duration.ofSeconds(3),
                        Mono.just(Health.down().withDetail("reason", "timeout").build()));
    }
}
```

---

## 12. Best Practices

### Metrics

- **Keep JVM metrics enabled** (`METRICS_ENABLE_JVM: true`) — minimal cost, high value for GC/heap monitoring
- **Disable `all` by default** — enable specific categories only when needed
- **Use `1m` step** for OTLP export — aligns with standard aggregation intervals
- **Tag with `instance`** — enables per-instance dashboards in multi-replica deployments
- **Custom metrics use `Counter.builder`** — register and increment in one call for thread-safety

### Tracing

- **Set sampling to `0.1` (10%)** in production — reduces overhead while providing representative data
- **Use baggage correlation** for business identifiers (`x-message-id`, `x-phone-number`)
- **Enable `gzip` compression** — reduces network overhead for trace payloads
- **Include all 10 resource attributes** — enables filtering/grouping in observability dashboards

### Circuit Breakers

- **Always set `register-health-indicator: true`** — makes circuit state visible in health endpoint
- **Use TIME_BASED sliding window** — more representative than COUNT_BASED for variable traffic
- **Set `minimum-number-of-calls: 20`** — prevents premature circuit opening during low traffic
- **Disable automatic transition** — prefer wait-for-next-call to avoid unnecessary retries

### Security

- **Never expose `env`, `beans`, `configprops`** — can leak secrets and internal configuration
- **Never add Spring Security for actuator** — network security handles access control
- **Authorization header via env var** — collector auth token injected at runtime, never in code

---

## 13. Post-Implementation Validation Checklist

| # | Check | How to Verify |
|---|-------|---------------|
| 1 | Actuator health endpoint responds | `curl localhost:8080/actuator/health` returns 200 |
| 2 | Health shows circuit breaker details | Response includes `circuitBreakers` component |
| 3 | Health shows R2DBC status | Response includes `r2dbc` component |
| 4 | Diskspace and ping are absent | Not present in health response |
| 5 | Metrics endpoint accessible | `curl localhost:8080/actuator/metrics` returns metric names |
| 6 | JVM metrics present | `/actuator/metrics/jvm.memory.used` returns data |
| 7 | Custom business metrics present | `/actuator/metrics/company.*.transactions.total` returns data |
| 8 | OTLP export sends metrics | Collector logs show incoming metric payloads |
| 9 | Traces exported via OTLP | Traces appear in observability backend |
| 10 | Sampling rate applied | ~10% of requests generate traces |
| 11 | Baggage fields propagated | `x-message-id` appears in trace attributes |
| 12 | Resource attributes in traces | `service.name`, `service.namespace` present |
| 13 | Only `health, metrics` exposed | `/actuator/env` returns 404 |
| 14 | Swagger UI disabled | `/swagger-ui.html` returns 404 |
| 15 | No Prometheus endpoint | `/actuator/prometheus` returns 404 |
| 16 | Circuit breaker opens on failures | Health shows OPEN state after threshold |

---

## 14. Constraints and Warnings

### DO NOT

- ❌ Add `micrometer-registry-prometheus` — incompatible with OTLP-only export
- ❌ Add `spring-boot-starter-web` — conflicts with WebFlux reactive stack
- ❌ Enable `probes.enabled: true` — ECS does not use Kubernetes probes
- ❌ Set `show-details: never` — details are safe behind network security
- ❌ Expose `env`, `beans`, `configprops`, `info` endpoints
- ❌ Add Spring Security for actuator endpoints — network handles security
- ❌ Use `opentelemetry-exporter-zipkin` or `opentelemetry-exporter-jaeger` — use OTLP only
- ❌ Hardcode collector URLs or auth tokens in YAML — always use environment variables
- ❌ Set `METRICS_ENABLE_ALL: true` in production — creates excessive metric cardinality
- ❌ Set tracing sampling to `1.0` in production — causes performance degradation
- ❌ Place metrics helper in `domain/` or `model/` — it belongs in `infrastructure/helpers/`

### CRITICAL NOTES

1. **OTLP endpoint path matters**: Metrics use `/v1/metrics`, traces use `/v1/traces`
2. **Collector host does NOT include the path** — only base URL (e.g., `http://collector:4318`)
3. **Step of `1m`** means metrics are batched and exported every 60 seconds
4. **Compression `gzip`** is set on tracing only — metrics use default (none)
5. **`observations.annotations.enabled: true`** enables `@Observed` annotation support for automatic span creation
6. **Baggage `remote-fields`** must match `correlation.fields` for end-to-end propagation
7. **Circuit breaker instance name** in YAML must match the `@CircuitBreaker(name = "...")` annotation value
8. **Gradle module inclusion** — ensure `infrastructure/helpers/metrics` is included in `settings.gradle`

### settings.gradle inclusion

```groovy
include ':infrastructure:helpers:metrics'
project(':infrastructure:helpers:metrics').projectDir = file('./infrastructure/helpers/metrics')
```

---

## Reference Microservices

These patterns are verified against the following production services:

| Service | Capacity | Notes |
|---------|----------|-------|
| `cocorebankint_balance_ms` | Balance inquiry | OTLP + R2DBC + Circuit Breaker |
| `cocorebankint_authorize_ms` | Transaction auth | OTLP + Custom metrics + CB |
| `cointgateway_auth_ms` | Gateway auth | OTLP + Tracing + Baggage |
| `cospibreb_status_ms` | SPIBREB status | OTLP + R2DBC + CB + Custom metrics |
