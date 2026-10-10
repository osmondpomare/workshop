---
name: workshop-migrate-java-ee-controller-clases-reactive
description: >-
  Migrate Entry-Point classes from Java EE (versions 7 and 8) to reactive WebFlux controllers
  on Scaffold Clean Architecture. Transforms JAX-RS @Path/@POST endpoints into annotated
  @RestController classes returning Mono<ResponseEntity<T>> or Mono<ServerResponse>, with
  reactive validation, WebFilter for correlation headers, @CircuitBreaker resilience,
  MapStruct DTO mapping, and reactive error handling via @ControllerAdvice.
license: MIT
compatibility: Java 25, Spring Boot 4.x, Spring WebFlux, Project Reactor
metadata:
  author: criscast
  version: 1.0.0
  based-on: migrate-java-ee-controller-clases
---

# Migrate Java EE Entry-Point Classes to Reactive WebFlux Controllers

## 1. When to Use

Use this skill when you need to:

- Migrate JAX-RS `@Path`/`@POST`/`@GET` endpoint classes from Java EE (7/8) to reactive Spring WebFlux controllers
- Transform blocking REST entry points into non-blocking reactive endpoints returning `Mono<ResponseEntity<T>>`
- Adopt the Scaffold Clean Architecture reactive pattern with annotated `@RestController` style
- Implement reactive correlation, validation, circuit breaker resilience, and error handling patterns
- Create entry points that delegate to domain Use Cases returning `Mono`/`Flux`

Do **NOT** use this skill if:
- The target project uses Spring MVC (blocking) — use `migrate-java-ee-controller-clases` instead
- You need RouterFunction/HandlerFunction style — this skill targets `--router false` (annotated controllers)
- The source is already a Spring Boot application

---

## 2. Target

| Aspect | Value |
|--------|-------|
| Java version | 25 |
| Spring Boot | 4.x (Spring Framework 7) |
| Web stack | Spring WebFlux + Project Reactor |
| Architecture | Scaffold Clean Architecture (reactive type) |
| Entry point | `infrastructure/entry-points/reactive-web/` |
| Scaffolding | `gradle gep --type webflux --router false` |
| JSON library | Jackson 3.x (`tools.jackson.*` package) |
| Build tool | Gradle 9.2.1+ |
| Validation | Jakarta Bean Validation 3.x + WebFlux reactive validation |
| Resilience | Resilience4j `@CircuitBreaker` |
| Mapping | MapStruct (componentModel = "spring") |

---

## 3. Context

You are a reactive Java migration expert specializing in converting legacy Java EE (7/8) JAX-RS
entry-point classes to Spring WebFlux annotated controllers on Scaffold Clean Architecture projects.

You understand:
- JAX-RS annotations (`@Path`, `@POST`, `@GET`, `@Consumes`, `@Produces`, `@HeaderParam`, `@Context`)
- Spring WebFlux annotated controller model (`@RestController`, `@RequestMapping`, `@PostMapping`)
- Project Reactor types (`Mono<T>`, `Flux<T>`) and reactive streams semantics
- Non-blocking reactive patterns (no `.block()`, no blocking I/O in reactive chain)
- Scaffold Clean Architecture layering: domain ↔ use cases ↔ infrastructure (entry-points, driven-adapters)
- Reactive error propagation via `Mono.error()` and `@RestControllerAdvice`

---

## 4. Requirements

### Pre-conditions

1. The target project has been created using Scaffold Clean Architecture (reactive type)
2. The entry-point module exists at `infrastructure/entry-points/reactive-web/`
   (generated via `gradle gep --type webflux --router false`)
3. Domain model classes exist in `domain/model/`
4. Use Case interfaces exist in `domain/usecase/` and return `Mono<T>` or `Flux<T>`
5. The source Java EE project has been analyzed (classes, endpoints, headers, DTOs identified)

### Migration Rules

- **One controller per resource/endpoint group** — do not merge unrelated endpoints
- **Thin controllers** — only request mapping, validation, mapping, and delegation
- **No business logic in controllers** — delegate exclusively to Use Cases
- **No blocking calls** — never use `.block()`, `Thread.sleep()`, or blocking I/O
- **DTOs stay in infrastructure** — request/response DTOs live in the entry-point module
- **Domain model is clean** — no Jackson annotations, no Spring annotations in domain
- **Correlation via WebFilter** — use `WebFilter` (not `OncePerRequestFilter`) for headers
- **ProblemDetail for errors** — RFC 7807 format via `@RestControllerAdvice`
- **Circuit breaker on every external-facing endpoint** — use `@CircuitBreaker` annotation

---

## 5. Target Project Structure

```
infrastructure/entry-points/reactive-web/
└── src/main/java/com/company/api/
    ├── ServiceController.java              # @RestController (entry point)
    ├── config/
    │   ├── CorrelationWebFilter.java       # WebFilter for correlation headers
    │   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
    │   └── CorsWebFluxConfig.java          # CORS configuration
    ├── dto/
    │   ├── request/
    │   │   └── ServiceRequest.java         # Request DTO with Jakarta validation
    │   └── response/
    │       └── ServiceResponse.java        # Response DTO
    ├── mapper/
    │   └── ServiceMapper.java              # MapStruct interface
    ├── validator/
    │   └── CustomValidator.java            # Custom validation logic (if needed)
    ├── utils/
    │   └── HeaderConstants.java            # Header name constants
    └── response/
        └── error/
            └── ErrorResponse.java          # Legacy error DTO (if needed alongside ProblemDetail)
```

---

## 6. Migration Rules — JAX-RS → WebFlux Mapping Table

| Java EE (JAX-RS) | Reactive WebFlux (annotated) |
|---|---|
| `@Path("/api/v1/resource")` | `@RestController` + `@RequestMapping("/api/v1/resource")` |
| `@POST` | `@PostMapping` |
| `@GET` | `@GetMapping` |
| `@PUT` | `@PutMapping` |
| `@DELETE` | `@DeleteMapping` |
| `@Consumes(MediaType.APPLICATION_JSON)` | Not needed (auto-configured) |
| `@Produces(MediaType.APPLICATION_JSON)` | Not needed (auto-configured) |
| `@HeaderParam("X-Message-Id")` | `@RequestHeader(value = "x-message-id", required = false)` |
| `@Context HttpHeaders` | `@RequestHeader HttpHeaders headers` |
| `@QueryParam("status")` | `@RequestParam("status")` |
| `@PathParam("id")` | `@PathVariable("id")` |
| `Response` return type | `Mono<ResponseEntity<T>>` |
| `Response.ok(body).build()` | `Mono.just(ResponseEntity.ok(body))` |
| `Response.status(201).entity(body).build()` | `Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(body))` |
| `Response.status(204).build()` | `Mono.just(ResponseEntity.noContent().build())` |
| Manual JSON parsing with `ObjectMapper` | `@RequestBody Mono<RequestDTO>` or `@Valid @RequestBody RequestDTO` |
| `@BeanParam` | `@ModelAttribute` or individual `@RequestParam` |
| `@Context UriInfo` | `ServerHttpRequest` parameter |
| `ExceptionMapper<T>` | `@RestControllerAdvice` + `@ExceptionHandler` |
| Servlet `Filter` | `WebFilter` |
| `OncePerRequestFilter` | `WebFilter` |
| `@Inject` / `@EJB` | Constructor injection (implicit, single constructor) |
| `@Stateless` EJB service | Use Case in `domain/usecase/` |

### Thin Controller Principle

Controllers MUST only:
1. Extract and validate request parameters/headers
2. Map DTOs to domain objects (via MapStruct)
3. Delegate to a Use Case
4. Map domain results back to response DTOs
5. Return `Mono<ResponseEntity<T>>`

Controllers MUST NOT:
- Contain business logic or conditional branching on business rules
- Call repositories or driven-adapters directly
- Perform logging of business events (delegate to use case)
- Catch and handle domain exceptions (delegate to `@RestControllerAdvice`)

---

## 7. Scaffolding

Generate the reactive web entry point using Scaffold Clean Architecture:

```bash
# Full command
gradle generateEntryPoint --type=webflux --router=false

# Shorthand
gradle gep --type webflux --router false
```

This generates an annotated `@RestController` style entry point (NOT `RouterFunction`/`HandlerFunction`).

The generated module will be located at:
```
infrastructure/entry-points/reactive-web/
```

After generation, add the module dependency in the main `build.gradle`:
```groovy
dependencies {
    implementation project(':reactive-web')
}
```

---

## 8. Complete Migration Examples

### Before: Java EE JAX-RS Entry Point

```java
package co.com.company.services.ws.rest;

import co.com.company.services.business.IServiceBusiness;
import co.com.company.services.dto.ServiceRequestDTO;
import co.com.company.services.dto.ServiceResponseDTO;
import co.com.company.services.utils.HeadersUtils;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/v1/service")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ServiceResource {

    @EJB
    private IServiceBusiness serviceBusiness;

    @POST
    public Response execute(@Context HttpHeaders httpHeaders, ServiceRequestDTO request) {
        String messageId = httpHeaders.getHeaderString("X-Message-Id");
        String phoneNumber = httpHeaders.getHeaderString("X-Phone-Number");

        try {
            ServiceResponseDTO response = serviceBusiness.execute(request, messageId, phoneNumber);
            return Response.ok(response).build();
        } catch (BusinessException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(e.getCode(), e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("TECH_ERROR", "Internal error"))
                .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getById(@Context HttpHeaders httpHeaders, @PathParam("id") String id) {
        String messageId = httpHeaders.getHeaderString("X-Message-Id");
        try {
            ServiceResponseDTO response = serviceBusiness.findById(id, messageId);
            return Response.ok(response).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("NOT_FOUND", e.getMessage()))
                .build();
        }
    }
}
```

### After: Reactive WebFlux @RestController

```java
package com.company.api;

import com.company.api.dto.request.ServiceRequest;
import com.company.api.dto.response.ServiceResponse;
import com.company.api.mapper.ServiceMapper;
import com.company.usecase.service.ServiceUseCase;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/service")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {

    private final ServiceUseCase serviceUseCase;
    private final ServiceMapper mapper;

    @PostMapping
    @CircuitBreaker(name = "service-execute", fallbackMethod = "executeFallback")
    public Mono<ResponseEntity<ServiceResponse>> execute(
            @RequestHeader(value = "x-message-id", required = false) String messageId,
            @RequestHeader(value = "x-phone-number", required = false) String phoneNumber,
            @Valid @RequestBody ServiceRequest request) {

        return serviceUseCase.execute(mapper.toDomain(request, messageId, phoneNumber))
            .map(result -> ResponseEntity.ok(mapper.toResponse(result)));
    }

    @GetMapping("/{id}")
    @CircuitBreaker(name = "service-get", fallbackMethod = "getByIdFallback")
    public Mono<ResponseEntity<ServiceResponse>> getById(
            @RequestHeader(value = "x-message-id", required = false) String messageId,
            @PathVariable String id) {

        return serviceUseCase.findById(id)
            .map(result -> ResponseEntity.ok(mapper.toResponse(result)));
    }

    // Fallback methods
    public Mono<ResponseEntity<ServiceResponse>> executeFallback(
            String messageId, String phoneNumber, ServiceRequest request, Exception ex) {
        log.error("Execute fallback triggered for messageId={}", messageId, ex);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    public Mono<ResponseEntity<ServiceResponse>> getByIdFallback(
            String messageId, String id, Exception ex) {
        log.error("GetById fallback triggered for id={}", id, ex);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }
}
```

---

## 9. Reactive Correlation WebFilter

Replaces Servlet `Filter` or `OncePerRequestFilter` from Java EE. Propagates correlation headers
through the reactive context using `reactor.util.context.Context`.

```java
package com.company.api.config;

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

    public static final String MESSAGE_ID_KEY = "messageId";
    public static final String CONSUMER_ID_KEY = "consumerId";
    public static final String PHONE_NUMBER_KEY = "phoneNumber";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String messageId = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst("X-Message-Id"))
            .orElse(UUID.randomUUID().toString());

        String consumerId = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst("X-Consumer-Id"))
            .orElse("unknown");

        String phoneNumber = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst("X-Phone-Number"))
            .orElse("");

        // Add correlation ID to response headers
        exchange.getResponse().getHeaders().add("X-Message-Id", messageId);

        return chain.filter(exchange)
            .contextWrite(Context.of(
                MESSAGE_ID_KEY, messageId,
                CONSUMER_ID_KEY, consumerId,
                PHONE_NUMBER_KEY, phoneNumber
            ));
    }
}
```

### Accessing Correlation Context in Downstream Operators

```java
// Inside a reactive chain, retrieve context values:
Mono.deferContextual(ctx -> {
    String messageId = ctx.getOrDefault("messageId", "N/A");
    log.info("Processing with messageId={}", messageId);
    return Mono.just(result);
});
```

---

## 10. Reactive Error Handling (@RestControllerAdvice)

Centralized error handling using `@RestControllerAdvice` with RFC 7807 `ProblemDetail` responses.
All handlers return `Mono<ResponseEntity<ProblemDetail>>`.

```java
package com.company.api.config;

import com.company.model.enums.TechnicalMessage;
import com.company.model.exceptions.BusinessException;
import com.company.model.exceptions.TechnicalException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleValidation(WebExchangeBindException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .collect(Collectors.joining(", "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        problem.setTitle("Validation Error");
        problem.setType(URI.create("https://api.company.com/errors/validation-error"));
        problem.setProperty("source", "SELF");
        return Mono.just(ResponseEntity.badRequest().body(problem));
    }

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleBusiness(BusinessException ex) {
        log.warn("Business error: {}", ex.getTechnicalMessage());
        HttpStatus status = resolveBusinessStatus(ex.getTechnicalMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getTechnicalMessage().getMessage());
        problem.setTitle("Business Error");
        problem.setType(URI.create("https://api.company.com/errors/business-error"));
        problem.setProperty("code", ex.getTechnicalMessage().getCode());
        return Mono.just(ResponseEntity.status(status).body(problem));
    }

    /**
     * "Not found", "rejected", "unavailable" and other business outcomes all arrive as
     * BusinessException — the specific TechnicalMessage decides the HTTP status, mirroring
     * the per-message branching in TransferHandler (transfer_service_MS).
     */
    private HttpStatus resolveBusinessStatus(TechnicalMessage technicalMessage) {
        String name = technicalMessage.name();
        if (name.endsWith("_NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }
        if (name.endsWith("_UNAVAILABLE") || name.endsWith("_UNAVAILABLE_ERROR")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if (name.endsWith("_UNAUTHORIZED") || name.endsWith("_UNAUTHORIZED_ERROR")) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.BAD_REQUEST;
    }

    @ExceptionHandler(TechnicalException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleTechnical(TechnicalException ex) {
        log.error("Technical error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Technical Error");
        problem.setType(URI.create("https://api.company.com/errors/technical-error"));
        return Mono.just(ResponseEntity.internalServerError().body(problem));
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleCircuitBreaker(CallNotPermittedException ex) {
        log.warn("Circuit breaker open: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable, please retry later");
        problem.setTitle("Service Unavailable");
        problem.setType(URI.create("https://api.company.com/errors/service-unavailable"));
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetail>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.company.com/errors/internal-error"));
        return Mono.just(ResponseEntity.internalServerError().body(problem));
    }
}
```

---

## 11. @CircuitBreaker Pattern with Fallback Methods

Apply `@CircuitBreaker` from Resilience4j on every endpoint method. The fallback method **must**
match the original method signature with an additional `Exception` parameter at the end.

```java
@PostMapping
@CircuitBreaker(name = "service-execute", fallbackMethod = "executeFallback")
public Mono<ResponseEntity<ServiceResponse>> execute(
        @RequestHeader(value = "x-message-id", required = false) String messageId,
        @Valid @RequestBody ServiceRequest request) {
    return serviceUseCase.execute(mapper.toDomain(request, messageId))
        .map(result -> ResponseEntity.ok(mapper.toResponse(result)));
}

// Fallback: same params + Exception at end, same return type
public Mono<ResponseEntity<ServiceResponse>> executeFallback(
        String messageId, ServiceRequest request, Exception ex) {
    log.error("Fallback for execute, messageId={}", messageId, ex);
    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
}
```

### Fallback Rules
- Fallback method name must match the `fallbackMethod` attribute value
- Fallback must have the **same parameters** as the annotated method, plus `Exception` (or `Throwable`) as the last parameter
- Fallback must return the **same type** as the original method
- Fallback should log the error and return a graceful degraded response
- Fallback must **not** throw exceptions

---

## 12. MapStruct Mapper Pattern

MapStruct mappers convert between infrastructure DTOs and domain model objects.
Use `componentModel = "spring"` for Spring injection.

```java
package com.company.api.mapper;

import com.company.api.dto.request.ServiceRequest;
import com.company.api.dto.response.ServiceResponse;
import com.company.model.service.ServiceDomain;
import com.company.model.service.ServiceResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceMapper {

    @Mapping(target = "messageId", source = "messageId")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "documentType", source = "request.documentType")
    @Mapping(target = "documentNumber", source = "request.documentNumber")
    ServiceDomain toDomain(ServiceRequest request, String messageId, String phoneNumber);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "transactionId", source = "transactionId")
    ServiceResponse toResponse(ServiceResult result);
}
```

### MapStruct with Multiple Sources

```java
@Mapping(target = "field", source = "request.field")
@Mapping(target = "header", source = "headerValue")
DomainObject toDomain(RequestDTO request, String headerValue);
```

### MapStruct Dependency (build.gradle)

```groovy
dependencies {
    implementation 'org.mapstruct:mapstruct:1.6.3'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.3'
}
```

---

## 13. Reactive Validation Pattern

With `@Valid @RequestBody`, Spring WebFlux throws `WebExchangeBindException` when validation fails.
This is handled by the `GlobalExceptionHandler`.

### Request DTO with Jakarta Validation

```java
package com.company.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.annotation.JsonIgnoreProperties;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotBlank(message = "Document type is required")
    @Pattern(regexp = "^(CC|TI|CE|PT)$", message = "Document type must be CC, TI, CE, or PT")
    private String documentType;

    @NotBlank(message = "Document number is required")
    @Size(min = 5, max = 15, message = "Document number must be between 5 and 15 characters")
    private String documentNumber;
}
```

### Controller with @Valid

```java
@PostMapping
public Mono<ResponseEntity<ServiceResponse>> execute(
        @Valid @RequestBody ServiceRequest request) {
    // If validation fails, WebExchangeBindException is thrown automatically
    // and handled by GlobalExceptionHandler.handleValidation()
    return serviceUseCase.execute(mapper.toDomain(request))
        .map(result -> ResponseEntity.ok(mapper.toResponse(result)));
}
```

### Custom Reactive Validator (optional)

```java
package com.company.api.validator;

import com.company.api.dto.request.ServiceRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ServiceRequestValidator {

    public Mono<ServiceRequest> validate(ServiceRequest request) {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            return Mono.error(new IllegalArgumentException("Phone number is required"));
        }
        return Mono.just(request);
    }
}
```

---

## 14. DTOs Pattern

### Request DTO

```java
package com.company.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.annotation.JsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRequest {

    @NotBlank
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @NotBlank
    @Pattern(regexp = "^(CC|TI|CE|PT)$")
    private String documentType;

    @NotBlank
    private String documentNumber;
}
```

### Response DTO

```java
package com.company.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.annotation.JsonInclude;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceResponse {

    private String transactionId;
    private String status;
    private String message;
    private Object data;
}
```

### Header Constants

```java
package com.company.api.utils;

public final class HeaderConstants {

    private HeaderConstants() {}

    public static final String X_MESSAGE_ID = "x-message-id";
    public static final String X_PHONE_NUMBER = "x-phone-number";
    public static final String X_CONSUMER_ID = "x-consumer-id";
    public static final String X_CHANNEL = "x-channel";
    public static final String X_REQUEST_TIMESTAMP = "x-request-timestamp";
}
```

---

## 15. Use Case Integration

Controllers delegate to Use Cases defined in `domain/usecase/`. Use Cases return reactive types.

### Use Case Interface (in domain)

```java
package com.company.usecase.service;

import com.company.model.service.ServiceDomain;
import com.company.model.service.ServiceResult;
import reactor.core.publisher.Mono;

public interface ServiceUseCase {

    Mono<ServiceResult> execute(ServiceDomain request);

    Mono<ServiceResult> findById(String id);
}
```

### Controller Delegating to Use Case

```java
@RestController
@RequestMapping("/api/v1/service")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {

    private final ServiceUseCase serviceUseCase;  // Use Case, NOT @Service
    private final ServiceMapper mapper;

    @PostMapping
    @CircuitBreaker(name = "service-execute", fallbackMethod = "executeFallback")
    public Mono<ResponseEntity<ServiceResponse>> execute(
            @RequestHeader(value = "x-message-id", required = false) String messageId,
            @RequestHeader(value = "x-phone-number", required = false) String phoneNumber,
            @Valid @RequestBody ServiceRequest request) {

        return serviceUseCase.execute(mapper.toDomain(request, messageId, phoneNumber))
            .map(result -> ResponseEntity.ok(mapper.toResponse(result)))
            .defaultIfEmpty(ResponseEntity.noContent().build());
    }
}
```

### Key Points
- Use Cases are injected via **constructor** (single constructor, no `@Autowired`)
- Use Cases return `Mono<T>` or `Flux<T>` — never blocking types
- The controller uses `.map()` to transform the result into `ResponseEntity`
- Use `.defaultIfEmpty()` for empty Mono scenarios
- Use `.switchIfEmpty(Mono.error(...))` for "not found" scenarios

---

## 16. Test Pattern — WebTestClient

Integration tests for reactive controllers use `WebTestClient`.

```java
package com.company.api;

import com.company.api.dto.request.ServiceRequest;
import com.company.api.dto.response.ServiceResponse;
import com.company.api.mapper.ServiceMapper;
import com.company.model.service.ServiceResult;
import com.company.usecase.service.ServiceUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(ServiceController.class)
class ServiceControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ServiceUseCase serviceUseCase;

    @MockBean
    private ServiceMapper mapper;

    private ServiceRequest validRequest;
    private ServiceResult domainResult;
    private ServiceResponse expectedResponse;

    @BeforeEach
    void setUp() {
        validRequest = ServiceRequest.builder()
            .phoneNumber("3001234567")
            .documentType("CC")
            .documentNumber("123456789")
            .build();

        domainResult = ServiceResult.builder()
            .transactionId("txn-123")
            .status("SUCCESS")
            .build();

        expectedResponse = ServiceResponse.builder()
            .transactionId("txn-123")
            .status("SUCCESS")
            .build();
    }

    @Test
    void should_ReturnOk_When_ValidRequest() {
        when(mapper.toDomain(any(), any(), any())).thenReturn(any());
        when(serviceUseCase.execute(any())).thenReturn(Mono.just(domainResult));
        when(mapper.toResponse(any())).thenReturn(expectedResponse);

        webTestClient.post()
            .uri("/api/v1/service")
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-message-id", "msg-001")
            .header("x-phone-number", "3001234567")
            .bodyValue(validRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ServiceResponse.class)
            .isEqualTo(expectedResponse);
    }

    @Test
    void should_ReturnBadRequest_When_InvalidRequest() {
        ServiceRequest invalidRequest = ServiceRequest.builder()
            .phoneNumber("")  // blank, fails @NotBlank
            .documentType("INVALID")  // fails @Pattern
            .documentNumber("")
            .build();

        webTestClient.post()
            .uri("/api/v1/service")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Validation Error");
    }

    @Test
    void should_ReturnConflict_When_BusinessException() {
        when(mapper.toDomain(any(), any(), any())).thenReturn(any());
        when(serviceUseCase.execute(any()))
            .thenReturn(Mono.error(new BusinessException("BIZ_001", "Business rule violated")));

        webTestClient.post()
            .uri("/api/v1/service")
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-message-id", "msg-002")
            .bodyValue(validRequest)
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.title").isEqualTo("Business Error");
    }

    @Test
    void should_ReturnServiceUnavailable_When_CircuitBreakerOpen() {
        when(mapper.toDomain(any(), any(), any())).thenReturn(any());
        when(serviceUseCase.execute(any()))
            .thenReturn(Mono.error(new CallNotPermittedException(null)));

        webTestClient.post()
            .uri("/api/v1/service")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(validRequest)
            .exchange()
            .expectStatus().isEqualTo(503);
    }
}
```

---

## 17. Dependencies (build.gradle for entry-point module)

```groovy
// infrastructure/entry-points/reactive-web/build.gradle

dependencies {
    implementation project(':model')
    implementation project(':usecase')

    // Spring WebFlux
    implementation 'org.springframework.boot:spring-boot-starter-webflux'

    // Jakarta Validation
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Resilience4j (Spring Boot 4.x — do NOT use resilience4j-spring-boot3, it fails at startup with
    // "Module 'io.github.resilience4j:resilience4j-spring-boot3' is only compatible with Spring Boot 3.x").
    // Only add these if the controller actually uses @CircuitBreaker; otherwise omit them entirely.
    implementation 'io.github.resilience4j:resilience4j-spring-boot4:2.4.0'
    implementation 'io.github.resilience4j:resilience4j-reactor:2.3.0'

    // MapStruct
    implementation 'org.mapstruct:mapstruct:1.6.3'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.3'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Jackson 3.x (tools.jackson.* package)
    implementation 'tools.jackson.core:jackson-databind:3.0.0'
    implementation 'tools.jackson.core:jackson-annotations:3.0.0'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'io.projectreactor:reactor-test'
}
```

---

## 18. Configuration (application.yaml)

### Circuit Breaker Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      service-execute:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
        slow-call-duration-threshold: 2s
        slow-call-rate-threshold: 80
      service-get:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
```

### CORS Configuration

```yaml
spring:
  webflux:
    cors:
      allowed-origins: "*"
      allowed-methods: GET, POST, PUT, DELETE, OPTIONS
      allowed-headers: "*"
      max-age: 3600
```

### Programmatic CORS Configuration (alternative)

```java
package com.company.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsWebFluxConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
```

### Server Configuration

```yaml
server:
  port: 8080
  netty:
    connection-timeout: 5000

spring:
  application:
    name: service-name
  jackson:
    default-property-inclusion: non_null
    deserialization:
      fail-on-unknown-properties: false
```

---

## 19. Best Practices

### Reactive Patterns

1. **Never block** — avoid `.block()`, `.blockFirst()`, `.blockLast()` in reactive chains
2. **Use operators** — prefer `.map()`, `.flatMap()`, `.switchIfEmpty()` over imperative logic
3. **Error propagation** — use `Mono.error()` to propagate errors through the reactive chain
4. **Context propagation** — use `contextWrite()` and `deferContextual()` for correlation data
5. **Backpressure** — respect backpressure signals; use `.limitRate()` for `Flux` when needed

### Controller Design

6. **Thin controllers** — maximum 5-10 lines per method body
7. **Single responsibility** — one controller per resource/domain concept
8. **Consistent naming** — method names reflect the action: `execute`, `findById`, `create`, `update`
9. **Explicit HTTP status** — always use `ResponseEntity` for control over status codes
10. **Header extraction** — use `@RequestHeader` with `required = false` for optional correlation headers

### Error Handling

11. **Centralized handling** — all errors handled in `@RestControllerAdvice`, not in controllers
12. **ProblemDetail (RFC 7807)** — standard error response format for all endpoints
13. **No stack traces** — never expose internal details in API responses
14. **Log at correct level** — `ERROR` for technical failures, `WARN` for business rule violations
15. **Circuit breaker fallback** — always provide a graceful degraded response

### Testing

16. **WebTestClient** — use for all controller integration tests
17. **Mock Use Cases** — mock at the use case boundary, not internal details
18. **Test error scenarios** — validate 4xx and 5xx responses in tests
19. **Test validation** — verify that invalid requests return proper ProblemDetail responses

### Security and Performance

20. **No sensitive data in logs** — mask phone numbers, document numbers in log statements
21. **Timeout operators** — use `.timeout(Duration.ofSeconds(n))` for external calls
22. **Resource cleanup** — use `.doFinally()` for cleanup operations
23. **Immutable DTOs** — prefer Records or Lombok `@Builder` with no setters for responses

---

## 20. Post-Migration Validation Checklist

- [ ] All JAX-RS annotations (`@Path`, `@POST`, `@GET`, etc.) have been replaced with Spring WebFlux equivalents
- [ ] All endpoints return `Mono<ResponseEntity<T>>` (never raw objects or blocking types)
- [ ] `@Valid` is applied to all `@RequestBody` parameters with Jakarta Validation constraints
- [ ] `@CircuitBreaker` is applied to all endpoint methods with corresponding fallback methods
- [ ] Fallback methods have correct signature (same params + Exception, same return type)
- [ ] `GlobalExceptionHandler` handles: `WebExchangeBindException`, `BusinessException`, `TechnicalException`, `CallNotPermittedException`
- [ ] `CorrelationWebFilter` extracts and propagates correlation headers via reactive `Context`
- [ ] MapStruct mapper converts between DTOs and domain objects
- [ ] DTOs live in `infrastructure/entry-points/reactive-web/` (not in domain)
- [ ] Domain model has no infrastructure annotations (no Jackson, no Spring, no Jakarta validation)
- [ ] No `.block()` calls anywhere in the reactive chain
- [ ] No business logic in controllers — all delegated to Use Cases
- [ ] Controller constructor injection only (no `@Autowired`, no field injection)
- [ ] WebTestClient tests cover: happy path, validation errors, business errors, circuit breaker
- [ ] Application starts without errors: `gradle bootRun`
- [ ] All endpoints respond correctly: test with `curl` or HTTP client

---

## 21. Constraints and Warnings

### Constraints

- **Java 25 only** — uses modern Java features (Records, pattern matching, sealed classes)
- **Spring Boot 4.x only** — not compatible with Spring Boot 2.x/3.x APIs
- **Annotated style only** — this skill uses `--router false`; do NOT mix with `RouterFunction`
- **Jackson 3.x** — use `tools.jackson.*` package imports, NOT `com.fasterxml.jackson.*`
- **Gradle 9.2.1+** — not compatible with Maven or older Gradle versions
- **Resilience4j 2.x+** — uses Spring Boot 3+ compatible version

### Warnings

| ⚠️ Warning | Description |
|---|---|
| No `.block()` | Never call `.block()` in a reactive WebFlux controller — it deadlocks the event loop |
| No `Thread.sleep()` | Use `Mono.delay()` or `delayElement()` for delays in reactive chains |
| No mutable state | Controllers must be stateless; avoid instance fields that change |
| No `ResponseEntity.body()` outside Mono | Always wrap in `Mono.just(ResponseEntity...)` |
| Fallback signature | Fallback methods must exactly match the annotated method's parameter types + Exception |
| Jackson 3.x package | Import from `tools.jackson.annotation.*`, NOT `com.fasterxml.jackson.annotation.*` |
| WebExchangeBindException | This is the reactive equivalent of `MethodArgumentNotValidException` |
| Context vs ThreadLocal | Reactive context replaces `ThreadLocal` — use `contextWrite()` not `ThreadLocal` |

---

## 22. References

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor Reference](https://projectreactor.io/docs/core/release/reference/)
- [Scaffold Clean Architecture](https://github.com/bancolombia/scaffold-clean-architecture)
- [Resilience4j Spring Boot](https://resilience4j.readme.io/docs/getting-started-3)
- [MapStruct Reference](https://mapstruct.org/documentation/stable/reference/html/)
- [RFC 7807 - Problem Details](https://www.rfc-editor.org/rfc/rfc7807)
- [Jakarta Bean Validation 3.0](https://jakarta.ee/specifications/bean-validation/3.0/)
- [Jackson 3.x Migration](https://github.com/FasterXML/jackson/wiki/Jackson-3.x)
- [Spring Boot 4.x Release Notes](https://github.com/spring-projects/spring-boot/wiki)
- [WebTestClient Documentation](https://docs.spring.io/spring-framework/reference/testing/webtestclient.html)
