---
name: workshop-migrate-java-ee-jms-clases-reactive
description: >-
  Migrate JMS @MessageDriven classes from Java EE (versions 7 and 8) to reactive SQS listeners
  on Scaffold Clean Architecture. Uses SqsAsyncClient with Mono.fromFuture() for fully non-blocking
  message consumption, custom SQSListener with Flux-based polling, Function<Message, Mono<Void>>
  processor pattern, manual message deletion after processing, and delegation to Use Cases
  returning Mono/Flux. Replaces legacy JMS MDBs with a reactive event-driven architecture.
license: MIT
compatibility: Java 25, Spring Boot 4.x, Spring WebFlux, AWS SDK v2 SQS, Project Reactor
metadata:
  author: criscast
  version: 1.0.0
  based-on: migrate-java-ee-jms-clases
---

# Migrate Java EE JMS Classes to Reactive SQS (Scaffold Clean Architecture)

## 1. When to Use

Use this skill when you need to:

- Migrate `@MessageDriven` beans (MDBs) from Java EE 7/8 to reactive SQS listeners
- Replace JMS `MessageListener.onMessage()` with a fully non-blocking `Function<Message, Mono<Void>>` pattern
- Integrate SQS message consumption into a Scaffold Clean Architecture (reactive type) project
- Move from container-managed JMS concurrency to Reactor-based scheduling with `SqsAsyncClient`
- Build event-driven microservices that consume SQS messages reactively

Do NOT use this skill when:
- The target project uses Spring MVC (blocking) — use the non-reactive `migrate-java-ee-jms-clases` skill instead
- The project uses Spring Cloud AWS `@SqsListener` annotation — this skill uses raw `SqsAsyncClient`
- The source is not a JMS `@MessageDriven` bean

---

## 2. Target Stack

| Component | Version/Technology |
|---|---|
| Java | 25 |
| Spring Boot | 4.x (Spring Framework 7) |
| Reactive Stack | Spring WebFlux + Project Reactor |
| AWS SDK | v2 (`software.amazon.awssdk:sqs`) |
| SQS Client | `SqsAsyncClient` (fully async) |
| Architecture | Scaffold Clean Architecture (reactive) |
| Serialization | Jackson 3.x (`tools.jackson.*` package) |
| Build Tool | Gradle 9.2.1+ |
| Entry Point | `infrastructure/entry-points/sqs-listener/` |

---

## 3. Context

You are a reactive JMS migration expert. Your role is to transform legacy Java EE `@MessageDriven` beans into fully non-blocking SQS listeners following the Scaffold Clean Architecture reactive pattern.

Key principles:
- **Fully non-blocking**: All I/O operations use `Mono.fromFuture()` or `Mono.fromCallable()`
- **Backpressure-aware**: Flux-based polling respects downstream demand
- **Manual acknowledgment**: Messages are deleted only after successful processing
- **Use Case delegation**: Business logic lives in the domain layer, returning `Mono<T>` or `Flux<T>`
- **Error isolation**: Individual message failures do not crash the polling loop

---

## 4. Requirements (Pre-conditions)

Before starting the migration:

1. **Source analysis complete**: The Java EE JMS `@MessageDriven` bean has been analyzed (use `get-java-ee-project-context` skill)
2. **Target project exists**: A Scaffold Clean Architecture (reactive) project is already scaffolded (use `scaffold-clean-architecture` skill with reactive type)
3. **Domain model defined**: Message payload models exist in `domain/model/`
4. **Use case defined**: Business logic use cases exist in `domain/usecase/` returning `Mono<T>` or `Flux<T>`
5. **AWS credentials configured**: IAM role or credentials available for SQS access
6. **SQS queue exists**: Target queue is provisioned in AWS (or LocalStack for local dev)

---

## 5. Target Project Structure

```
project-root/
├── domain/
│   ├── model/
│   │   ├── TransactionMessage.java          # Domain model for message payload
│   │   ├── enums/
│   │   │   └── TechnicalMessage.java        # Error message enumeration
│   │   └── exceptions/
│   │       ├── BusinessException.java       # Domain business exception (use cases)
│   │       └── TechnicalException.java      # Domain technical exception (adapters/entry-points)
│   └── usecase/
│       └── ProcessTransactionUseCase.java   # Business logic (returns Mono/Flux)
│
├── infrastructure/
│   └── entry-points/
│       └── sqs-listener/
│           ├── build.gradle                 # Module dependencies
│           ├── src/main/java/com/company/sqs/listener/
│           │   ├── SQSProcessor.java        # Function<Message, Mono<Void>>
│           │   ├── config/
│           │   │   ├── SQSConfig.java       # Bean wiring + SqsAsyncClient
│           │   │   └── SQSProperties.java   # @ConfigurationProperties
│           │   ├── dto/
│           │   │   └── TransactionEvent.java # Deserialization DTO
│           │   └── helper/
│           │       └── SQSListener.java     # Flux-based polling engine
│           └── src/test/java/com/company/sqs/listener/
│               └── SQSProcessorTest.java    # Unit tests with StepVerifier
│
└── applications/
    └── app-service/
        └── src/main/resources/
            └── application.yaml             # SQS configuration properties
```

---

## 6. Migration Mapping Table

| Java EE JMS | Reactive SQS (Scaffold Clean Architecture) |
|---|---|
| `@MessageDriven` bean | `SQSProcessor` implementing `Function<Message, Mono<Void>>` |
| `@ActivationConfigProperty` | `SQSProperties` with `@ConfigurationProperties(prefix = "entrypoint.sqs")` |
| `javax.jms.MessageListener` | `Function<Message, Mono<Void>>` (from `java.util.function`) |
| `onMessage(javax.jms.Message)` | `apply(software.amazon.awssdk.services.sqs.model.Message)` |
| `TextMessage.getText()` | `message.body()` |
| `ObjectMessage.getObject()` | `objectMapper.readValue(message.body(), TargetType.class)` |
| JMS `ConnectionFactory` | `SqsAsyncClient` bean |
| Queue JNDI lookup (`java:comp/env/jms/queue`) | `SQSProperties.queueUrl` from `application.yaml` |
| Container-managed transactions | Manual message deletion after successful processing via `client.deleteMessage()` |
| MDB concurrency pool size | `SQSProperties.numberOfThreads` + `Schedulers.fromExecutorService()` |
| `@EJB` / `@Inject` injection | Constructor injection (`@RequiredArgsConstructor`) of Use Cases |
| JMS Dead Letter Queue | AWS SQS DLQ configuration (Redrive Policy at infrastructure level) |
| Message selectors (`selector = "type = 'ORDER'"`) | Message attribute filtering in processor logic |
| `@TransactionAttribute` | Reactive chain completion = success; error = message remains in queue |
| `@RunAs` security | IAM role-based access to SQS |

---

## 7. Scaffolding

Generate the SQS listener entry point module using the Scaffold Clean Architecture plugin:

```bash
# Full command
gradle generateEntryPoint --type=sqs

# Short alias
gradle gep --type sqs
```

This generates the base structure under `infrastructure/entry-points/sqs-listener/` with:
- `build.gradle` with AWS SDK SQS dependency
- Base package structure
- Placeholder configuration class

> **Post-scaffolding fixes (Spring Boot 4.x — apply before running the app):**
> 1. **Rename the `SqsAsyncClient` `@Bean` method.** The generator emits a `@Bean public SqsAsyncClient configSqs(...)`
>    in the listener's `SQSConfig` AND (if an SQS sender driven-adapter is also generated) an identical
>    `configSqs` in `SQSSenderConfig`. Two beans named `configSqs` → startup failure
>    (`A bean with that name has already been defined`). Rename them to `listenerSqsAsyncClient` and
>    `senderSqsAsyncClient` and inject each with `@Qualifier`.
> 2. **Fix the Jackson package.** If the generated `SQSProcessor`/`SQSSender` import
>    `com.fasterxml.jackson.databind.ObjectMapper`, rewrite to `tools.jackson.databind.ObjectMapper`
>    and expose it as an explicit `@Bean("sqsObjectMapper")` injected via `@Qualifier`
>    (Boot 4.x does not auto-configure the Jackson 2 `ObjectMapper`).

After scaffolding, implement the components described in sections 8–11.

---

## 8. Complete Component Reference

### 8.1 SQSListener (Polling Engine)

The `SQSListener` is the core polling engine. It uses `Flux` to continuously poll SQS, delegates processing to the `Function<Message, Mono<Void>>` processor, and deletes messages upon successful processing.

```java
package com.company.sqs.listener.helper;

import com.company.sqs.listener.config.SQSProperties;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Log4j2
@Builder
public class SQSListener {

    private final SqsAsyncClient client;
    private final SQSProperties properties;
    private final Function<Message, Mono<Void>> processor;

    /**
     * Starts the polling loop with the configured number of threads.
     * Each thread subscribes to the same Flux pipeline independently.
     */
    public SQSListener start() {
        ExecutorService service = Executors.newFixedThreadPool(properties.getNumberOfThreads());
        Flux<Void> flow = listenRetryRepeat().publishOn(Schedulers.fromExecutorService(service));
        for (var i = 0; i < properties.getNumberOfThreads(); i++) {
            flow.subscribe();
        }
        return this;
    }

    private Flux<Void> listenRetryRepeat() {
        return listen()
                .doOnError(e -> log.error("Error listening sqs queue", e))
                .repeat();
    }

    private Flux<Void> listen() {
        return getMessages()
                .flatMap(message -> processor.apply(message)
                        .then(confirm(message)))
                .onErrorContinue((e, o) -> log.error("Error listening sqs message", e));
    }

    private Mono<Void> confirm(Message message) {
        return Mono.fromCallable(() -> getDeleteMessageRequest(message.receiptHandle()))
                .flatMap(request -> Mono.fromFuture(client.deleteMessage(request)))
                .then();
    }

    private Flux<Message> getMessages() {
        return Mono.fromCallable(this::getReceiveMessageRequest)
                .flatMap(request -> Mono.fromFuture(client.receiveMessage(request)))
                .flatMapMany(response -> Flux.fromIterable(response.messages()));
    }

    private ReceiveMessageRequest getReceiveMessageRequest() {
        return ReceiveMessageRequest.builder()
                .queueUrl(properties.getQueueUrl())
                .maxNumberOfMessages(properties.getMaxNumberOfMessages())
                .waitTimeSeconds(properties.getWaitTimeSeconds())
                .visibilityTimeout(properties.getVisibilityTimeout())
                .build();
    }

    private DeleteMessageRequest getDeleteMessageRequest(String receiptHandle) {
        return DeleteMessageRequest.builder()
                .queueUrl(properties.getQueueUrl())
                .receiptHandle(receiptHandle)
                .build();
    }
}
```

### 8.2 SQSProcessor (Message Handler)

The `SQSProcessor` implements the `Function<Message, Mono<Void>>` contract. It deserializes the message body and delegates to the domain use case.

```java
package com.company.sqs.listener;

import com.company.model.enums.TechnicalMessage;
import com.company.model.exceptions.BusinessException;
import com.company.model.exceptions.TechnicalException;
import com.company.sqs.listener.dto.TransactionEvent;
import com.company.usecase.ProcessTransactionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;
import tools.jackson.databind.ObjectMapper;

import java.util.function.Function;
import java.util.function.Predicate;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final ObjectMapper objectMapper;
    private final ProcessTransactionUseCase processTransactionUseCase;

    @Override
    public Mono<Void> apply(Message message) {
        return deserialize(message.body())
                .doOnNext(event -> log.info("Message received: {}",
                        kv("transactionId", event.transactionId())))
                .flatMap(event -> processTransactionUseCase.execute(event.transactionId()))
                .onErrorMap(Predicate.not(BusinessException.class::isInstance)
                                .and(Predicate.not(TechnicalException.class::isInstance)),
                        throwable -> new TechnicalException(throwable, TechnicalMessage.INTERNAL_ERROR))
                .onErrorResume(throwable -> {
                    log.error("Error processing message: {}", throwable.getMessage(), throwable);
                    return Mono.empty();
                })
                .then();
    }

    private Mono<TransactionEvent> deserialize(String jsonString) {
        return Mono.fromCallable(() -> objectMapper.readValue(jsonString, TransactionEvent.class))
                .onErrorMap(Exception.class,
                        e -> new TechnicalException(e, TechnicalMessage.ERROR_MAPPING));
    }
}
```

### 8.3 SQSConfig (Bean Wiring)

```java
package com.company.sqs.listener.config;

import com.company.sqs.listener.helper.SQSListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.net.URI;
import java.util.function.Function;

@Configuration
public class SQSConfig {

    @Bean
    public SQSListener sqsListener(SqsAsyncClient client,
                                    SQSProperties properties,
                                    Function<Message, Mono<Void>> processor) {
        return SQSListener.builder()
                .client(client)
                .properties(properties)
                .processor(processor)
                .build()
                .start();
    }

    @Bean
    public SqsAsyncClient sqsAsyncClient(SQSProperties properties) {
        var builder = SqsAsyncClient.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create());

        // Support custom endpoint for LocalStack/testing
        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
        }

        return builder.build();
    }
}
```

### 8.4 SQSProperties (Configuration Binding)

```java
package com.company.sqs.listener.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "entrypoint.sqs")
public class SQSProperties {
    private String region;
    private String queueUrl;
    private String endpoint;
    private int waitTimeSeconds;
    private int maxNumberOfMessages;
    private int visibilityTimeout;
    private int numberOfThreads;
}
```

### 8.5 DTO Pattern (Message Payload)

Use Lombok `@Value` + `@Jacksonized` + `@Builder` for immutable deserialization DTOs:

```java
package com.company.sqs.listener.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class TransactionEvent {
    String transactionId;
    String messageId;
    String initTransactionDate;
    String eventType;
    String payload;
}
```

For Java 25 records (alternative without Lombok):

```java
package com.company.sqs.listener.dto;

import tools.jackson.annotation.JsonProperty;

public record TransactionEvent(
        @JsonProperty("transactionId") String transactionId,
        @JsonProperty("messageId") String messageId,
        @JsonProperty("initTransactionDate") String initTransactionDate,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("payload") String payload
) {}
```

---

## 9. Migration Examples

### Before: Java EE JMS @MessageDriven Bean

```java
package com.company.conciliation.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import com.google.gson.Gson;
import com.company.conciliation.business.ConciliationBusiness;
import com.company.conciliation.dto.ConciliationTransactionDTO;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java:comp/env/jms/conciliationQueue"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "maxSession",
            propertyValue = "5")
})
public class ConciliationMDB implements MessageListener {

    @EJB
    private ConciliationBusiness conciliationBusiness;

    private final Gson gson = new Gson();

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String body = textMessage.getText();
                ConciliationTransactionDTO dto = gson.fromJson(body, ConciliationTransactionDTO.class);
                conciliationBusiness.processConciliation(dto.getTransactionId());
            }
        } catch (JMSException e) {
            throw new RuntimeException("Error processing JMS message", e);
        } catch (Exception e) {
            // Log and swallow - message will be redelivered by container
            System.err.println("Error processing conciliation: " + e.getMessage());
        }
    }
}
```

### After: Reactive SQS Processor

```java
package com.company.sqs.listener;

import com.company.model.enums.TechnicalMessage;
import com.company.model.exceptions.BusinessException;
import com.company.model.exceptions.TechnicalException;
import com.company.sqs.listener.dto.ConciliationTransaction;
import com.company.usecase.ConciliationTransactionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;
import tools.jackson.databind.ObjectMapper;

import java.util.function.Function;
import java.util.function.Predicate;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final ObjectMapper objectMapper;
    private final ConciliationTransactionUseCase conciliationTransactionUseCase;

    @Override
    public Mono<Void> apply(Message message) {
        return deserialize(message.body())
                .doOnNext(transaction -> log.info("Processing conciliation: {}",
                        kv("transactionId", transaction.transactionId())))
                .flatMap(transaction ->
                        conciliationTransactionUseCase.process(transaction.transactionId()))
                .onErrorMap(Predicate.not(BusinessException.class::isInstance)
                                .and(Predicate.not(TechnicalException.class::isInstance)),
                        throwable -> new TechnicalException(throwable, TechnicalMessage.INTERNAL_ERROR))
                .onErrorResume(throwable -> {
                    log.error("Error processing conciliation message: {}", throwable.getMessage(), throwable);
                    return Mono.empty();
                })
                .then();
    }

    private Mono<ConciliationTransaction> deserialize(String jsonString) {
        return Mono.fromCallable(() -> objectMapper.readValue(jsonString, ConciliationTransaction.class))
                .onErrorMap(Exception.class,
                        e -> new TechnicalException(e, TechnicalMessage.ERROR_MAPPING));
    }
}
```

### After: DTO (replaces legacy DTO)

```java
package com.company.sqs.listener.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class ConciliationTransaction {
    String transactionId;
    String messageId;
    String initTransactionDate;
}
```

---

## 10. Use Case Integration

The processor delegates ALL business logic to domain use cases. Use cases return `Mono<T>` or `Flux<T>`.

### Use Case Interface (Domain Layer)

```java
package com.company.usecase;

import reactor.core.publisher.Mono;

public interface ConciliationTransactionUseCase {
    Mono<Void> process(String transactionId);
}
```

### Use Case Implementation (Domain Layer)

```java
package com.company.usecase;

import com.company.model.enums.TechnicalMessage;
import com.company.model.exceptions.BusinessException;
import com.company.model.gateway.TransactionGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class ConciliationTransactionUseCase {

    private final TransactionGateway transactionGateway;

    public Mono<Void> process(String transactionId) {
        return transactionGateway.findByTransactionId(transactionId)
                .switchIfEmpty(Mono.error(
                        new BusinessException(TechnicalMessage.TRANSACTION_NOT_FOUND)))
                .flatMap(transaction -> {
                    log.info("Processing transaction: {}", transactionId);
                    return transactionGateway.updateStatus(transactionId, "CONCILIATED");
                })
                .then();
    }
}
```

### Gateway Interface (Domain Layer)

```java
package com.company.model.gateway;

import com.company.model.Transaction;
import reactor.core.publisher.Mono;

public interface TransactionGateway {
    Mono<Transaction> findByTransactionId(String transactionId);
    Mono<Void> updateStatus(String transactionId, String status);
}
```

### Key Integration Rules

1. **Processor → Use Case**: The `SQSProcessor` calls the use case with extracted data
2. **Use Case → Gateway**: The use case calls gateway interfaces (implemented in driven adapters)
3. **No infrastructure in domain**: Use cases never import AWS SDK or Spring classes
4. **Reactive chain**: The entire path from processor to gateway is a single reactive chain

---

## 11. Error Handling Patterns

> **Contract** (only two exception types exist — `BusinessException` and `TechnicalException`, both carrying a `TechnicalMessage`; guided by `transfer_service_MS`):
> - **Use Cases** throw `BusinessException` for every domain/business outcome — including "not found" (e.g. `TRANSACTION_NOT_FOUND`), which is a known business result, never a technical failure.
> - **Entry-points** (the `SQSProcessor`) never construct `BusinessException` themselves; they only wrap genuinely unexpected/unclassified errors into `TechnicalException`, guarded so an already-classified `BusinessException` (from the use case) or `TechnicalException` (e.g. from `deserialize()`) is never re-wrapped.

### Pattern 1: onErrorMap (Wrap Unknown Errors)

Wrap unexpected exceptions into `TechnicalException`, guarding both existing domain exception types so a `BusinessException` from the use case (or an earlier `TechnicalException`, e.g. from `deserialize()`) is never re-classified:

```java
.onErrorMap(Predicate.not(BusinessException.class::isInstance)
                .and(Predicate.not(TechnicalException.class::isInstance)),
        throwable -> new TechnicalException(throwable, TechnicalMessage.INTERNAL_ERROR))
```

### Pattern 2: onErrorResume (Graceful Recovery)

Log the error and return `Mono.empty()` to allow the message to be deleted (prevents infinite reprocessing):

```java
.onErrorResume(throwable -> {
    log.error("Error processing message: {}", throwable.getMessage(), throwable);
    return Mono.empty();
})
```

### Pattern 3: onErrorContinue (Polling Loop Resilience)

Used in `SQSListener.listen()` to ensure one message failure does not crash the entire polling loop:

```java
.onErrorContinue((e, o) -> log.error("Error listening sqs message", e))
```

### Pattern 4: Deserialization Error Isolation

Wrap Jackson deserialization in `Mono.fromCallable()` and map errors:

```java
private Mono<TransactionEvent> deserialize(String jsonString) {
    return Mono.fromCallable(() -> objectMapper.readValue(jsonString, TransactionEvent.class))
            .onErrorMap(Exception.class,
                    e -> new TechnicalException(e, TechnicalMessage.ERROR_MAPPING));
}
```

### Pattern 5: Re-throw to Skip Deletion (Message Returns to Queue)

If you want a failed message to remain in the queue for retry (up to DLQ max receives), propagate the error instead of resuming:

```java
@Override
public Mono<Void> apply(Message message) {
    return deserialize(message.body())
            .flatMap(event -> processTransactionUseCase.execute(event.transactionId()))
            .onErrorMap(Predicate.not(BusinessException.class::isInstance)
                            .and(Predicate.not(TechnicalException.class::isInstance)),
                    throwable -> new TechnicalException(throwable, TechnicalMessage.INTERNAL_ERROR));
    // NO onErrorResume here — error propagates, message is NOT deleted
}
```

### Domain Exception Classes

```java
package com.company.model.exceptions;

import com.company.model.enums.TechnicalMessage;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final TechnicalMessage technicalMessage;

    public BusinessException(TechnicalMessage technicalMessage) {
        super(technicalMessage.getMessage());
        this.technicalMessage = technicalMessage;
    }
}

@Getter
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

```java
package com.company.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TechnicalMessage {
    INTERNAL_ERROR("Internal server error"),
    ERROR_MAPPING("Error mapping message body"),
    TRANSACTION_NOT_FOUND("Transaction not found");

    private final String message;
}
```

---

## 12. application.yaml Configuration

```yaml
entrypoint:
  sqs:
    region: us-east-1
    queue-url: https://sqs.us-east-1.amazonaws.com/123456789012/conciliation-queue
    endpoint: ""  # Leave empty for AWS; set to http://localhost:4566 for LocalStack
    wait-time-seconds: 20          # Long polling (max 20s)
    max-number-of-messages: 10     # Batch size per poll (max 10)
    visibility-timeout: 30         # Seconds before message reappears if not deleted
    number-of-threads: 3           # Concurrent polling subscribers
```

### Configuration Guidelines

| Property | Recommended Value | Rationale |
|---|---|---|
| `wait-time-seconds` | 20 | Maximum long polling reduces empty responses and cost |
| `max-number-of-messages` | 10 | Maximum batch size per receive call |
| `visibility-timeout` | 2× expected processing time | Prevents duplicate processing |
| `number-of-threads` | 2–5 | Balance throughput vs. resource usage |
| `endpoint` | empty for prod | Only set for LocalStack/local development |

### Profile-specific Configuration

```yaml
# application-local.yaml (for LocalStack)
entrypoint:
  sqs:
    region: us-east-1
    queue-url: http://localhost:4566/000000000000/conciliation-queue
    endpoint: http://localhost:4566
    wait-time-seconds: 5
    max-number-of-messages: 5
    visibility-timeout: 30
    number-of-threads: 1
```

---

## 13. build.gradle Dependencies

### Entry Point Module (`infrastructure/entry-points/sqs-listener/build.gradle`)

```groovy
dependencies {
    implementation project(':domain:model')
    implementation project(':domain:usecase')

    // AWS SDK v2 SQS (async)
    implementation 'software.amazon.awssdk:sqs'

    // Reactor
    implementation 'io.projectreactor:reactor-core'

    // Jackson 3.x (tools.jackson.* package)
    implementation 'tools.jackson.core:jackson-databind'
    implementation 'tools.jackson.core:jackson-annotations'

    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-webflux'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Logging
    implementation 'net.logstash.logback:logstash-logback-encoder'

    // Test
    testImplementation 'io.projectreactor:reactor-test'
    testImplementation 'org.mockito:mockito-core'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### Root `build.gradle` (BOM for AWS SDK)

```groovy
dependencyManagement {
    imports {
        mavenBom 'software.amazon.awssdk:bom:2.31.+'
    }
}
```

---

## 14. Test Patterns

### Unit Test for SQSProcessor

```java
package com.company.sqs.listener;

import com.company.model.enums.TechnicalMessage;
import com.company.model.exceptions.BusinessException;
import com.company.sqs.listener.dto.ConciliationTransaction;
import com.company.usecase.ConciliationTransactionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.model.Message;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SQSProcessorTest {

    @Mock
    private ConciliationTransactionUseCase conciliationTransactionUseCase;

    private ObjectMapper objectMapper;
    private SQSProcessor sqsProcessor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        sqsProcessor = new SQSProcessor(objectMapper, conciliationTransactionUseCase);
    }

    @Test
    @DisplayName("Should process message successfully when valid JSON")
    void should_ProcessMessage_When_ValidJson() {
        // Arrange
        var json = """
                {
                    "transactionId": "TXN-001",
                    "messageId": "MSG-001",
                    "initTransactionDate": "2026-08-24T10:00:00"
                }
                """;
        var message = Message.builder().body(json).build();

        when(conciliationTransactionUseCase.process("TXN-001"))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(sqsProcessor.apply(message))
                .verifyComplete();

        verify(conciliationTransactionUseCase).process("TXN-001");
    }

    @Test
    @DisplayName("Should handle deserialization error gracefully")
    void should_HandleError_When_InvalidJson() {
        // Arrange
        var message = Message.builder().body("invalid-json").build();

        // Act & Assert
        StepVerifier.create(sqsProcessor.apply(message))
                .verifyComplete(); // onErrorResume returns Mono.empty()

        verify(conciliationTransactionUseCase, never()).process(anyString());
    }

    @Test
    @DisplayName("Should handle use case error gracefully")
    void should_HandleError_When_UseCaseFails() {
        // Arrange
        var json = """
                {
                    "transactionId": "TXN-002",
                    "messageId": "MSG-002",
                    "initTransactionDate": "2026-08-24T10:00:00"
                }
                """;
        var message = Message.builder().body(json).build();

        when(conciliationTransactionUseCase.process("TXN-002"))
                .thenReturn(Mono.error(new BusinessException(TechnicalMessage.TRANSACTION_NOT_FOUND)));

        // Act & Assert
        StepVerifier.create(sqsProcessor.apply(message))
                .verifyComplete(); // BusinessException passes through onErrorMap unchanged, then onErrorResume handles it

        verify(conciliationTransactionUseCase).process("TXN-002");
    }

    @Test
    @DisplayName("Should wrap non-technical exceptions")
    void should_WrapException_When_UnexpectedError() {
        // Arrange
        var json = """
                {
                    "transactionId": "TXN-003",
                    "messageId": "MSG-003",
                    "initTransactionDate": "2026-08-24T10:00:00"
                }
                """;
        var message = Message.builder().body(json).build();

        when(conciliationTransactionUseCase.process("TXN-003"))
                .thenReturn(Mono.error(new RuntimeException("Unexpected DB error")));

        // Act & Assert
        StepVerifier.create(sqsProcessor.apply(message))
                .verifyComplete(); // onErrorMap wraps, then onErrorResume handles

        verify(conciliationTransactionUseCase).process("TXN-003");
    }
}
```

### Integration Test with Embedded LocalStack

```java
package com.company.sqs.listener;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Duration;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
class SQSListenerIntegrationTest {

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.5"))
            .withServices(SQS);

    @Autowired
    private SqsAsyncClient sqsAsyncClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("entrypoint.sqs.endpoint",
                () -> localStack.getEndpointOverride(SQS).toString());
        registry.add("entrypoint.sqs.region", localStack::getRegion);
        registry.add("entrypoint.sqs.queue-url",
                () -> localStack.getEndpointOverride(SQS) + "/000000000000/test-queue");
    }

    @Test
    void should_ConsumeAndProcess_When_MessageSentToQueue() {
        // Arrange
        var messageBody = """
                {"transactionId":"TXN-INT-001","messageId":"MSG-INT-001","initTransactionDate":"2026-08-24"}
                """;

        // Act
        sqsAsyncClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(localStack.getEndpointOverride(SQS) + "/000000000000/test-queue")
                .messageBody(messageBody)
                .build()).join();

        // Assert
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    // Verify use case was invoked or side effect occurred
                });
    }
}
```

---

## 15. Best Practices

### Message Idempotency

SQS provides at-least-once delivery. Design processors to be idempotent:

```java
public Mono<Void> process(String transactionId) {
    return transactionGateway.findByTransactionId(transactionId)
            .flatMap(transaction -> {
                if ("CONCILIATED".equals(transaction.getStatus())) {
                    log.info("Transaction already processed, skipping: {}", transactionId);
                    return Mono.empty();
                }
                return transactionGateway.updateStatus(transactionId, "CONCILIATED");
            })
            .then();
}
```

### Dead Letter Queue (DLQ)

Configure a DLQ at the AWS infrastructure level with a Redrive Policy:

```json
{
  "RedrivePolicy": {
    "deadLetterTargetArn": "arn:aws:sqs:us-east-1:123456789012:conciliation-queue-dlq",
    "maxReceiveCount": 3
  }
}
```

- Set `maxReceiveCount` to 3–5 depending on transient failure expectations
- Monitor DLQ depth with CloudWatch alarms
- Implement a DLQ consumer for alerting/manual reprocessing

### Visibility Timeout

- Set `visibility-timeout` to at least 2× your expected processing time
- If processing takes longer, the message reappears and may cause duplicate processing
- For variable workloads, consider extending visibility programmatically:

```java
private Mono<Void> extendVisibility(Message message, int additionalSeconds) {
    return Mono.fromFuture(client.changeMessageVisibility(
            ChangeMessageVisibilityRequest.builder()
                    .queueUrl(properties.getQueueUrl())
                    .receiptHandle(message.receiptHandle())
                    .visibilityTimeout(additionalSeconds)
                    .build()))
            .then();
}
```

### Graceful Shutdown

For production deployments, implement graceful shutdown to finish in-flight messages:

```java
@Configuration
public class ShutdownConfig {

    @Bean
    public DisposableBean sqsShutdown(SQSListener sqsListener) {
        return () -> {
            // Signal the listener to stop polling
            // Allow in-flight messages to complete (visibility timeout acts as safety net)
            log.info("SQS listener shutting down gracefully");
        };
    }
}
```

### Structured Logging

Use structured logging with Logstash markers for observability:

```java
log.info("Message processed successfully",
        kv("transactionId", event.transactionId()),
        kv("messageId", message.messageId()),
        kv("processingTimeMs", elapsed));
```

### Batch Processing Optimization

- Use `max-number-of-messages: 10` to reduce API calls
- Long polling (`wait-time-seconds: 20`) reduces empty responses and cost
- Tune `number-of-threads` based on downstream capacity

---

## 16. Post-Migration Validation Checklist

| # | Validation Item | How to Verify |
|---|---|---|
| 1 | SQS queue accessible | Application starts without credential errors |
| 2 | Messages consumed | Send test message, verify log output |
| 3 | Messages deleted after processing | Queue depth decreases after processing |
| 4 | Error handling works | Send malformed JSON, verify no crash + log entry |
| 5 | DLQ receives failed messages | Exhaust retries, verify DLQ depth increases |
| 6 | Idempotency | Send duplicate messages, verify single processing |
| 7 | Concurrency | Send batch of 50+ messages, verify parallel processing |
| 8 | Visibility timeout adequate | Processing completes before timeout expires |
| 9 | Graceful shutdown | Stop application, verify no message loss |
| 10 | Use case delegation | Business logic executes correctly end-to-end |
| 11 | Structured logs present | Verify JSON log output with transaction IDs |
| 12 | No blocking calls | Verify with BlockHound (no `block()` calls in reactive chain) |
| 13 | Memory stable under load | No OOM after sustained message throughput |
| 14 | LocalStack tests pass | Integration tests with Testcontainers succeed |
| 15 | Spring context starts | `./gradlew :app-service:bootRun` reaches `Started MainApplication` (NOT just `build -x test`, which never detects bean-wiring/name-collision errors) |

---

## 17. Constraints and Warnings

### MUST

- **MUST** use `SqsAsyncClient` (not `SqsClient` synchronous version)
- **MUST** wrap all AWS SDK calls in `Mono.fromFuture()` or `Mono.fromCallable()`
- **MUST** delete messages only after successful processing (manual acknowledgment)
- **MUST** use `onErrorContinue` in the polling loop to prevent single-message failures from crashing the listener
- **MUST** keep business logic in domain use cases, not in the processor
- **MUST** use constructor injection (never field injection with `@Autowired`)
- **MUST** give the `SqsAsyncClient` `@Bean` a **unique method name per module** whenever a project
  has BOTH an SQS listener (entry-point) and an SQS sender (driven-adapter) in the same application
  context (e.g. `listenerSqsAsyncClient` / `senderSqsAsyncClient`). The scaffold generators emit a
  method literally named `configSqs` in BOTH the entry-point `SQSConfig` and the driven-adapter
  `SQSSenderConfig`; left unchanged they produce two beans with the same name `configSqs` and the
  app fails to start with `A bean with that name has already been defined ... overriding is disabled`.
  After renaming, inject each client with `@Qualifier("...SqsAsyncClient")` so there is no
  by-type ambiguity between the two `SqsAsyncClient` beans.
- **MUST** provide the `ObjectMapper` used by the processor/sender as an explicit `@Bean`
  (e.g. `@Bean("sqsObjectMapper") tools.jackson.databind.ObjectMapper`) and inject it with
  `@Qualifier`, instead of relying on auto-configuration. Use the Jackson 3 package
  `tools.jackson.databind.ObjectMapper` — NOT `com.fasterxml.jackson.databind.ObjectMapper`
  (Jackson 2 is not auto-configured as a bean in Spring Boot 4.x). If the scaffold-generated code
  imports `com.fasterxml.jackson.*`, rewrite it to `tools.jackson.*`.

### MUST NOT

- **MUST NOT** use Spring Cloud AWS `@SqsListener` annotation (incompatible with this pattern)
- **MUST NOT** call `.block()` anywhere in the reactive chain
- **MUST NOT** use `Thread.sleep()` for polling delays (use `waitTimeSeconds` long polling)
- **MUST NOT** expose AWS SDK types in the domain layer
- **MUST NOT** auto-delete messages before processing completes
- **MUST NOT** catch and silently swallow exceptions without logging

### SHOULD

- **SHOULD** use `Mono.fromCallable()` for synchronous operations (Jackson deserialization)
- **SHOULD** implement idempotent message processing
- **SHOULD** configure a Dead Letter Queue for poison messages
- **SHOULD** use structured logging with transaction context
- **SHOULD** test with StepVerifier for unit tests and LocalStack for integration tests
- **SHOULD** set visibility timeout to at least 2× expected processing time

### SHOULD NOT

- **SHOULD NOT** process messages that fail deserialization (log and skip)
- **SHOULD NOT** retry indefinitely within the processor (rely on SQS redelivery + DLQ)
- **SHOULD NOT** create excessive threads (2–5 is typically sufficient)

---

## 18. IAM Permissions

The application's IAM role or user must have these SQS permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "SQSConsumerPermissions",
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes",
        "sqs:GetQueueUrl",
        "sqs:ChangeMessageVisibility"
      ],
      "Resource": [
        "arn:aws:sqs:us-east-1:123456789012:conciliation-queue"
      ]
    }
  ]
}
```

### Minimal Permissions Explanation

| Permission | Purpose |
|---|---|
| `sqs:ReceiveMessage` | Poll messages from the queue |
| `sqs:DeleteMessage` | Remove messages after successful processing |
| `sqs:GetQueueAttributes` | Health checks and monitoring |
| `sqs:GetQueueUrl` | Resolve queue URL from name (optional) |
| `sqs:ChangeMessageVisibility` | Extend visibility timeout for long-running tasks |

### Security Best Practices

- Use IAM roles (not access keys) in production (ECS Task Role, EKS IRSA, or Lambda execution role)
- Apply least-privilege: only the permissions listed above
- Restrict `Resource` to the specific queue ARN (no wildcards)
- Use VPC endpoints for SQS to avoid public internet traffic
- Enable server-side encryption (SSE-SQS or SSE-KMS) on the queue

---

## Quick Reference: Migration Steps

1. **Analyze** the `@MessageDriven` bean (identify queue, message type, business logic)
2. **Scaffold** the SQS entry point: `gradle gep --type sqs`
3. **Create** `SQSProperties` with queue configuration
4. **Create** `SQSConfig` with `SqsAsyncClient` and `SQSListener` beans
5. **Implement** `SQSListener` (polling engine) — copy from reference implementation
6. **Create** DTO for message deserialization
7. **Implement** `SQSProcessor` (Function<Message, Mono<Void>>)
8. **Wire** processor to domain use case
9. **Configure** `application.yaml` with queue URL and properties
10. **Write** unit tests with StepVerifier
11. **Write** integration tests with LocalStack + Testcontainers
12. **Validate** with the post-migration checklist
