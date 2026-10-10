---
name: workshop-spring-boot-implement-openapi-reactive
description: >-
  Implement SpringDoc OpenAPI 3.0 documentation for reactive WebFlux microservices on Scaffold
  Clean Architecture. Uses programmatic OpenAPI bean configuration (not annotations) for
  Router+Handler pattern, springdoc-openapi-starter-webflux-api dependency, openApi Gradle
  plugin to generate .metadata/openapi.yaml, swagger-ui disabled in production, and api-docs
  served at /api-spec path.
license: MIT
compatibility: Java 25, Spring Boot 4.x, Spring WebFlux, SpringDoc OpenAPI 3.0
metadata:
  author: criscast
  version: 1.0.0
  based-on: spring-boot-implement-openapi
---

# SpringDoc OpenAPI 3.0 for Reactive WebFlux (Scaffold Clean Architecture)

## When to Use

Use this skill when you need to implement OpenAPI documentation for a Spring Boot reactive microservice that uses:
- Spring WebFlux with Router+Handler functional pattern
- Scaffold Clean Architecture (reactive type)
- Programmatic OpenAPI specification (NOT annotation-based)
- Gradle build system with openApi spec generation plugin
- Production deployment where Swagger UI is disabled

This skill is specifically designed for services where `@Operation`, `@ApiResponse`, and `@Tag` annotations **cannot** be used because the Router+Handler pattern does not support them.

## Target

| Component         | Version / Technology                        |
|-------------------|---------------------------------------------|
| Java              | 25                                          |
| Spring Boot       | 4.x (Spring Framework 7)                   |
| Reactive Stack    | Spring WebFlux + Project Reactor            |
| Architecture      | Scaffold Clean Architecture (reactive type) |
| OpenAPI Library   | SpringDoc OpenAPI 3.0.0                     |
| Gradle            | 9.2.1+                                     |
| OpenAPI Plugin    | org.springdoc.openapi-gradle-plugin 1.9.0   |

## Context

You are a reactive OpenAPI documentation expert specializing in programmatic OpenAPI specification for Spring WebFlux microservices. You build the entire OpenAPI spec via Java `@Configuration` beans using the `io.swagger.v3.oas.models` API, because the Router+Handler functional endpoint pattern does not support annotation-driven documentation.

You understand the Scaffold Clean Architecture layout:
- `infrastructure/entry-points/reactive-web/` — where the OpenApiSpecConfig lives
- `applications/app-service/` — where the Gradle openApi plugin is applied
- `.metadata/openapi.yaml` — where the generated spec is output for CI/CD tooling

## Requirements

Before implementing, ensure:
1. The project uses Scaffold Clean Architecture (reactive type)
2. Spring WebFlux is the web framework (NOT Spring MVC)
3. Endpoints use Router+Handler pattern (RouterFunction<ServerResponse>)
4. Gradle is the build tool (NOT Maven)
5. The project targets Java 25 and Spring Boot 4.x

## Procedure

### Step 1: Add version variables to root build.gradle ext block

In the root `build.gradle`, add the SpringDoc version variables to the `ext` block:

```groovy
ext {
    // ... existing version variables
    springDocOpenApiPluginVersion = '1.9.0'
    springDocOpenApiVersion = '3.0.0'
}
```

### Step 2: Add openApi Gradle plugin to root plugins block

In the root `build.gradle`, add the plugin declaration:

```groovy
plugins {
    // ... existing plugins
    id 'org.springdoc.openapi-gradle-plugin' version "${springDocOpenApiPluginVersion}" apply false
}
```

### Step 3: Apply plugin in applications/app-service/build.gradle and configure openApi task

In `applications/app-service/build.gradle`:

```groovy
apply plugin: 'org.springframework.boot'
apply plugin: 'org.springdoc.openapi-gradle-plugin'

dependencies {
    // ... existing dependencies
}

openApi {
    apiDocsUrl.set("http://localhost:8080/api-spec")
    outputDir.set(layout.projectDirectory.dir("../../.metadata"))
    outputFileName.set("openapi.yaml")
    waitTimeInSeconds.set(10)
}
```

### Step 4: Add springdoc-openapi-starter-webflux-api to reactive-web/build.gradle

In `infrastructure/entry-points/reactive-web/build.gradle`:

```groovy
dependencies {
    // ... existing dependencies
    implementation "org.springdoc:springdoc-openapi-starter-webflux-api:${springDocOpenApiVersion}"
}
```

> **WARNING:** Do NOT use `springdoc-openapi-starter-webmvc-ui`. That is for Spring MVC only. WebFlux requires `springdoc-openapi-starter-webflux-api`.

### Step 5: Add springdoc configuration to application.yaml

In `applications/app-service/src/main/resources/application.yaml`:

```yaml
springdoc:
  api-docs:
    enabled: true
    path: /api-spec
  swagger-ui:
    enabled: false
```

### Step 6: Create OpenApiSpecConfig.java in reactive-web api/config/

Create the file at:
`infrastructure/entry-points/reactive-web/src/main/java/[base-package]/api/config/OpenApiSpecConfig.java`

See the full template in the [OpenApiSpecConfig Template](#openapispecconfig-template) section below.

### Step 7: Generate openapi.yaml

Run the Gradle task to start the application and generate the spec:

```bash
./gradlew generateOpenApiDocs
```

This will:
1. Start the Spring Boot application
2. Fetch the OpenAPI spec from `http://localhost:8080/api-spec`
3. Save it to `.metadata/openapi.yaml`
4. Stop the application

## OpenApiSpecConfig Template

```java
package com.company.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiSpecConfig {

    private static final String JSON_MEDIA_TYPE = "application/json";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(buildInfo())
            .servers(buildServers())
            .tags(buildTags())
            .components(buildComponents())
            .paths(buildPaths());
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/api/v1/**")
            .build();
    }

    // ==================== INFO ====================

    private Info buildInfo() {
        return new Info()
            .title("My Service API")
            .description("Reactive microservice API documentation")
            .version("1.0.0")
            .contact(new Contact()
                .name("Team Name")
                .email("team@company.com")
                .url("https://confluence.example.com/team"))
            .license(new License()
                .name("Proprietary")
                .url("https://company.com"));
    }

    // ==================== SERVERS ====================

    private List<Server> buildServers() {
        return List.of(
            new Server()
                .url("http://localhost:8080")
                .description("Local Development"),
            new Server()
                .url("https://api.dev.company.com")
                .description("Development Environment"),
            new Server()
                .url("https://api.company.com")
                .description("Production Environment")
        );
    }

    // ==================== TAGS ====================

    private List<Tag> buildTags() {
        return List.of(
            new Tag()
                .name("orders")
                .description("Order management operations"),
            new Tag()
                .name("health")
                .description("Health check endpoints")
        );
    }

    // ==================== COMPONENTS ====================

    private Components buildComponents() {
        return new Components()
            .schemas(buildSchemas())
            .examples(buildExamples());
    }

    // ==================== SCHEMAS ====================

    @SuppressWarnings("rawtypes")
    private Map<String, Schema> buildSchemas() {
        return Map.of(
            "CreateOrderRequest", createOrderRequestSchema(),
            "OrderResponse", createOrderResponseSchema(),
            "ErrorResponse", createErrorResponseSchema(),
            "PaginatedResponse", createPaginatedResponseSchema()
        );
    }

    @SuppressWarnings("rawtypes")
    private Schema createOrderRequestSchema() {
        return new Schema<>()
            .type("object")
            .description("Request body to create a new order")
            .addProperty("customerId", new Schema<>()
                .type("string")
                .description("Customer unique identifier")
                .example("CUST-12345"))
            .addProperty("amount", new Schema<>()
                .type("number")
                .format("double")
                .description("Order amount in COP")
                .example(150000.00))
            .addProperty("description", new Schema<>()
                .type("string")
                .description("Order description")
                .maxLength(500)
                .example("Purchase order for services"))
            .addProperty("items", new Schema<>()
                .type("array")
                .description("List of order items")
                .items(new Schema<>().$ref("#/components/schemas/OrderItem")))
            .required(List.of("customerId", "amount"));
    }

    @SuppressWarnings("rawtypes")
    private Schema createOrderResponseSchema() {
        return new Schema<>()
            .type("object")
            .description("Order response")
            .addProperty("orderId", new Schema<>()
                .type("string")
                .format("uuid")
                .description("Generated order identifier"))
            .addProperty("status", new Schema<>()
                .type("string")
                .description("Order status")
                ._enum(List.of("CREATED", "PROCESSING", "COMPLETED", "FAILED")))
            .addProperty("createdAt", new Schema<>()
                .type("string")
                .format("date-time")
                .description("Order creation timestamp"))
            .addProperty("amount", new Schema<>()
                .type("number")
                .format("double")
                .description("Order total amount"));
    }

    @SuppressWarnings("rawtypes")
    private Schema createErrorResponseSchema() {
        return new Schema<>()
            .type("object")
            .description("Standard error response")
            .addProperty("type", new Schema<>()
                .type("string")
                .description("Error type URI")
                .example("https://api.company.com/errors/validation-error"))
            .addProperty("title", new Schema<>()
                .type("string")
                .description("Short error title")
                .example("Validation Error"))
            .addProperty("status", new Schema<>()
                .type("integer")
                .format("int32")
                .description("HTTP status code")
                .example(400))
            .addProperty("detail", new Schema<>()
                .type("string")
                .description("Detailed error message")
                .example("The customerId field is required"))
            .addProperty("instance", new Schema<>()
                .type("string")
                .description("Request path")
                .example("/api/v1/orders"))
            .required(List.of("type", "title", "status", "detail"));
    }

    @SuppressWarnings("rawtypes")
    private Schema createPaginatedResponseSchema() {
        return new Schema<>()
            .type("object")
            .description("Paginated response wrapper")
            .addProperty("content", new Schema<>()
                .type("array")
                .description("Page content")
                .items(new Schema<>().type("object")))
            .addProperty("page", new Schema<>()
                .type("integer")
                .description("Current page number"))
            .addProperty("size", new Schema<>()
                .type("integer")
                .description("Page size"))
            .addProperty("totalElements", new Schema<>()
                .type("integer")
                .format("int64")
                .description("Total number of elements"))
            .addProperty("totalPages", new Schema<>()
                .type("integer")
                .description("Total number of pages"));
    }

    // ==================== EXAMPLES ====================

    private Map<String, Example> buildExamples() {
        return Map.of(
            "CreateOrderRequestExample", createOrderRequestExample(),
            "OrderResponseExample", createOrderResponseExample(),
            "ValidationErrorExample", createValidationErrorExample(),
            "InternalErrorExample", createInternalErrorExample(),
            "ServiceUnavailableExample", createServiceUnavailableExample()
        );
    }

    private Example createOrderRequestExample() {
        return new Example()
            .summary("Create order request")
            .description("Example of a valid order creation request")
            .value("""
                {
                    "customerId": "CUST-12345",
                    "amount": 150000.00,
                    "description": "Purchase order for services",
                    "items": [
                        {
                            "productId": "PROD-001",
                            "quantity": 2,
                            "unitPrice": 75000.00
                        }
                    ]
                }
                """);
    }

    private Example createOrderResponseExample() {
        return new Example()
            .summary("Order created successfully")
            .description("Example response after successful order creation")
            .value("""
                {
                    "orderId": "550e8400-e29b-41d4-a716-446655440000",
                    "status": "CREATED",
                    "createdAt": "2026-08-24T10:30:00Z",
                    "amount": 150000.00
                }
                """);
    }

    private Example createValidationErrorExample() {
        return new Example()
            .summary("Validation error")
            .description("Example response when request validation fails")
            .value("""
                {
                    "type": "https://api.company.com/errors/validation-error",
                    "title": "Validation Error",
                    "status": 400,
                    "detail": "The customerId field is required",
                    "instance": "/api/v1/orders"
                }
                """);
    }

    private Example createInternalErrorExample() {
        return new Example()
            .summary("Internal server error")
            .description("Example response for unexpected server errors")
            .value("""
                {
                    "type": "https://api.company.com/errors/internal-error",
                    "title": "Internal Server Error",
                    "status": 500,
                    "detail": "An unexpected error occurred while processing the request",
                    "instance": "/api/v1/orders"
                }
                """);
    }

    private Example createServiceUnavailableExample() {
        return new Example()
            .summary("Service unavailable")
            .description("Example response when a downstream service is unavailable")
            .value("""
                {
                    "type": "https://api.company.com/errors/service-unavailable",
                    "title": "Service Unavailable",
                    "status": 503,
                    "detail": "The payment service is temporarily unavailable",
                    "instance": "/api/v1/orders"
                }
                """);
    }

    // ==================== PATHS ====================

    private Paths buildPaths() {
        var paths = new Paths();
        paths.addPathItem("/api/v1/orders", buildOrdersPathItem());
        paths.addPathItem("/api/v1/orders/{orderId}", buildOrderByIdPathItem());
        return paths;
    }

    private PathItem buildOrdersPathItem() {
        return new PathItem()
            .post(buildCreateOrderOperation())
            .get(buildListOrdersOperation());
    }

    private PathItem buildOrderByIdPathItem() {
        return new PathItem()
            .get(buildGetOrderByIdOperation());
    }

    // ==================== OPERATIONS ====================

    private Operation buildCreateOrderOperation() {
        return new Operation()
            .operationId("createOrder")
            .summary("Create a new order")
            .description("Creates a new order for the specified customer")
            .tags(List.of("orders"))
            .parameters(List.of(buildChannelHeader(), buildCorrelationIdHeader()))
            .requestBody(buildCreateOrderRequestBody())
            .responses(buildCreateOrderResponses());
    }

    private Operation buildListOrdersOperation() {
        return new Operation()
            .operationId("listOrders")
            .summary("List orders")
            .description("Retrieves a paginated list of orders")
            .tags(List.of("orders"))
            .parameters(List.of(
                buildChannelHeader(),
                buildCorrelationIdHeader(),
                buildPageQueryParam(),
                buildSizeQueryParam()
            ))
            .responses(buildListOrdersResponses());
    }

    private Operation buildGetOrderByIdOperation() {
        return new Operation()
            .operationId("getOrderById")
            .summary("Get order by ID")
            .description("Retrieves a specific order by its unique identifier")
            .tags(List.of("orders"))
            .parameters(List.of(
                buildChannelHeader(),
                buildCorrelationIdHeader(),
                buildOrderIdPathParam()
            ))
            .responses(buildGetOrderByIdResponses());
    }

    // ==================== PARAMETERS ====================

    @SuppressWarnings("rawtypes")
    private Parameter buildChannelHeader() {
        return new Parameter()
            .in("header")
            .name("X-Channel")
            .description("Request channel identifier")
            .required(true)
            .schema(new Schema<>()
                .type("string")
                ._enum(List.of("APP", "WEB", "USSD"))
                .example("APP"));
    }

    @SuppressWarnings("rawtypes")
    private Parameter buildCorrelationIdHeader() {
        return new Parameter()
            .in("header")
            .name("X-Correlation-Id")
            .description("Unique correlation identifier for request tracing")
            .required(true)
            .schema(new Schema<>()
                .type("string")
                .format("uuid")
                .example("550e8400-e29b-41d4-a716-446655440000"));
    }

    @SuppressWarnings("rawtypes")
    private Parameter buildOrderIdPathParam() {
        return new Parameter()
            .in("path")
            .name("orderId")
            .description("Order unique identifier")
            .required(true)
            .schema(new Schema<>()
                .type("string")
                .format("uuid")
                .example("550e8400-e29b-41d4-a716-446655440000"));
    }

    @SuppressWarnings("rawtypes")
    private Parameter buildPageQueryParam() {
        return new Parameter()
            .in("query")
            .name("page")
            .description("Page number (zero-based)")
            .required(false)
            .schema(new Schema<>()
                .type("integer")
                .format("int32")
                ._default(0)
                .minimum(java.math.BigDecimal.ZERO)
                .example(0));
    }

    @SuppressWarnings("rawtypes")
    private Parameter buildSizeQueryParam() {
        return new Parameter()
            .in("query")
            .name("size")
            .description("Number of items per page")
            .required(false)
            .schema(new Schema<>()
                .type("integer")
                .format("int32")
                ._default(20)
                .minimum(java.math.BigDecimal.ONE)
                .maximum(java.math.BigDecimal.valueOf(100))
                .example(20));
    }

    // ==================== REQUEST BODIES ====================

    private RequestBody buildCreateOrderRequestBody() {
        return new RequestBody()
            .description("Order creation payload")
            .required(true)
            .content(new Content()
                .addMediaType(JSON_MEDIA_TYPE, new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/CreateOrderRequest"))
                    .addExamples("default",
                        new Example().$ref("#/components/examples/CreateOrderRequestExample"))));
    }

    // ==================== RESPONSES ====================

    private ApiResponses buildCreateOrderResponses() {
        return new ApiResponses()
            .addApiResponse("201", new ApiResponse()
                .description("Order created successfully")
                .content(new Content()
                    .addMediaType(JSON_MEDIA_TYPE, new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/OrderResponse"))
                        .addExamples("default",
                            new Example().$ref("#/components/examples/OrderResponseExample")))))
            .addApiResponse("400", buildBadRequestResponse())
            .addApiResponse("401", buildUnauthorizedResponse())
            .addApiResponse("500", buildInternalServerErrorResponse())
            .addApiResponse("503", buildServiceUnavailableResponse());
    }

    private ApiResponses buildListOrdersResponses() {
        return new ApiResponses()
            .addApiResponse("200", new ApiResponse()
                .description("Orders retrieved successfully")
                .content(new Content()
                    .addMediaType(JSON_MEDIA_TYPE, new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/PaginatedResponse")))))
            .addApiResponse("400", buildBadRequestResponse())
            .addApiResponse("401", buildUnauthorizedResponse())
            .addApiResponse("500", buildInternalServerErrorResponse());
    }

    private ApiResponses buildGetOrderByIdResponses() {
        return new ApiResponses()
            .addApiResponse("200", new ApiResponse()
                .description("Order retrieved successfully")
                .content(new Content()
                    .addMediaType(JSON_MEDIA_TYPE, new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/OrderResponse"))
                        .addExamples("default",
                            new Example().$ref("#/components/examples/OrderResponseExample")))))
            .addApiResponse("400", buildBadRequestResponse())
            .addApiResponse("401", buildUnauthorizedResponse())
            .addApiResponse("404", buildNotFoundResponse())
            .addApiResponse("500", buildInternalServerErrorResponse());
    }

    // ==================== COMMON RESPONSES ====================

    private ApiResponse buildBadRequestResponse() {
        return new ApiResponse()
            .description("Bad request - validation error")
            .content(new Content()
                .addMediaType(JSON_MEDIA_TYPE, new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                    .addExamples("default",
                        new Example().$ref("#/components/examples/ValidationErrorExample"))));
    }

    private ApiResponse buildUnauthorizedResponse() {
        return new ApiResponse()
            .description("Unauthorized - missing or invalid credentials");
    }

    private ApiResponse buildNotFoundResponse() {
        return new ApiResponse()
            .description("Resource not found")
            .content(new Content()
                .addMediaType(JSON_MEDIA_TYPE, new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }

    private ApiResponse buildInternalServerErrorResponse() {
        return new ApiResponse()
            .description("Internal server error")
            .content(new Content()
                .addMediaType(JSON_MEDIA_TYPE, new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                    .addExamples("default",
                        new Example().$ref("#/components/examples/InternalErrorExample"))));
    }

    private ApiResponse buildServiceUnavailableResponse() {
        return new ApiResponse()
            .description("Service temporarily unavailable")
            .content(new Content()
                .addMediaType(JSON_MEDIA_TYPE, new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                    .addExamples("default",
                        new Example().$ref("#/components/examples/ServiceUnavailableExample"))));
    }
}
```

## application.yaml — springdoc Section

```yaml
springdoc:
  api-docs:
    enabled: true
    path: /api-spec
  swagger-ui:
    enabled: false
```

This configuration:
- Enables the OpenAPI JSON/YAML endpoint at `/api-spec`
- Disables Swagger UI (not bundled with webflux-api starter, but explicitly disabled for safety)
- The openApi Gradle plugin fetches the spec from this path

## Gradle Configuration

### Root build.gradle

```groovy
ext {
    springDocOpenApiPluginVersion = '1.9.0'
    springDocOpenApiVersion = '3.0.0'
}

plugins {
    id 'org.springdoc.openapi-gradle-plugin' version "${springDocOpenApiPluginVersion}" apply false
}
```

### applications/app-service/build.gradle

```groovy
apply plugin: 'org.springframework.boot'
apply plugin: 'org.springdoc.openapi-gradle-plugin'

dependencies {
    implementation project(':reactive-web')
    // ... other module dependencies
}

openApi {
    apiDocsUrl.set("http://localhost:8080/api-spec")
    outputDir.set(layout.projectDirectory.dir("../../.metadata"))
    outputFileName.set("openapi.yaml")
    waitTimeInSeconds.set(10)
}
```

### infrastructure/entry-points/reactive-web/build.gradle

```groovy
dependencies {
    implementation project(':model')
    implementation project(':usecase')
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation "org.springdoc:springdoc-openapi-starter-webflux-api:${springDocOpenApiVersion}"
}
```

## Building Schemas Programmatically

### Object Schema

```java
@SuppressWarnings("rawtypes")
private Schema createObjectSchema() {
    return new Schema<>()
        .type("object")
        .description("Description of the object")
        .addProperty("fieldName", new Schema<>()
            .type("string")
            .description("Field description")
            .example("example value"))
        .required(List.of("fieldName"));
}
```

### String Schema with Enum

```java
@SuppressWarnings("rawtypes")
private Schema createStatusSchema() {
    return new Schema<>()
        .type("string")
        .description("Status values")
        ._enum(List.of("ACTIVE", "INACTIVE", "SUSPENDED"));
}
```

### Integer Schema with Constraints

```java
@SuppressWarnings("rawtypes")
private Schema createAmountSchema() {
    return new Schema<>()
        .type("integer")
        .format("int64")
        .description("Amount in cents")
        .minimum(java.math.BigDecimal.ZERO)
        .maximum(java.math.BigDecimal.valueOf(999999999));
}
```

### Array Schema

```java
@SuppressWarnings("rawtypes")
private Schema createItemsArraySchema() {
    return new Schema<>()
        .type("array")
        .description("List of items")
        .items(new Schema<>().$ref("#/components/schemas/OrderItem"))
        .minItems(1)
        .maxItems(50);
}
```

### Schema with $ref (Reference)

```java
@SuppressWarnings("rawtypes")
private Schema createRefSchema() {
    return new Schema<>().$ref("#/components/schemas/OrderResponse");
}
```

### Nested Object Schema

```java
@SuppressWarnings("rawtypes")
private Schema createAddressSchema() {
    return new Schema<>()
        .type("object")
        .description("Address information")
        .addProperty("street", new Schema<>().type("string").example("Calle 10"))
        .addProperty("city", new Schema<>().type("string").example("Medellín"))
        .addProperty("state", new Schema<>().type("string").example("Antioquia"))
        .addProperty("zipCode", new Schema<>().type("string").pattern("^\\d{6}$").example("050001"))
        .required(List.of("street", "city"));
}
```

## Building Parameters

### Header Parameter

```java
@SuppressWarnings("rawtypes")
private Parameter buildHeader(String name, String description, boolean required) {
    return new Parameter()
        .in("header")
        .name(name)
        .description(description)
        .required(required)
        .schema(new Schema<>().type("string"));
}
```

### Path Parameter

```java
@SuppressWarnings("rawtypes")
private Parameter buildPathParam(String name, String description, String format) {
    return new Parameter()
        .in("path")
        .name(name)
        .description(description)
        .required(true)
        .schema(new Schema<>()
            .type("string")
            .format(format));
}
```

### Query Parameter with Default Value

```java
@SuppressWarnings("rawtypes")
private Parameter buildQueryParam(String name, String description, Object defaultValue) {
    return new Parameter()
        .in("query")
        .name(name)
        .description(description)
        .required(false)
        .schema(new Schema<>()
            .type("string")
            ._default(defaultValue));
}
```

## Building RequestBody

### JSON RequestBody with Schema Reference

```java
private RequestBody buildJsonRequestBody(String description, String schemaRef, String exampleRef) {
    return new RequestBody()
        .description(description)
        .required(true)
        .content(new Content()
            .addMediaType("application/json", new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/" + schemaRef))
                .addExamples("default",
                    new Example().$ref("#/components/examples/" + exampleRef))));
}
```

### RequestBody with Multiple Examples

```java
private RequestBody buildRequestBodyWithMultipleExamples() {
    return new RequestBody()
        .description("Request payload")
        .required(true)
        .content(new Content()
            .addMediaType("application/json", new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/CreateOrderRequest"))
                .addExamples("basic",
                    new Example().$ref("#/components/examples/BasicOrderExample"))
                .addExamples("full",
                    new Example().$ref("#/components/examples/FullOrderExample"))));
}
```

## Building ApiResponses

### Standard Response Set (200, 400, 401, 500, 503)

```java
private ApiResponses buildStandardResponses(String successDescription, String responseSchemaRef) {
    return new ApiResponses()
        .addApiResponse("200", new ApiResponse()
            .description(successDescription)
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/" + responseSchemaRef)))))
        .addApiResponse("400", new ApiResponse()
            .description("Bad request - validation error")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))))
        .addApiResponse("401", new ApiResponse()
            .description("Unauthorized"))
        .addApiResponse("500", new ApiResponse()
            .description("Internal server error")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))))
        .addApiResponse("503", new ApiResponse()
            .description("Service unavailable")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))));
}
```

### 201 Created Response

```java
private ApiResponse buildCreatedResponse(String schemaRef, String exampleRef) {
    return new ApiResponse()
        .description("Resource created successfully")
        .content(new Content()
            .addMediaType("application/json", new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/" + schemaRef))
                .addExamples("default",
                    new Example().$ref("#/components/examples/" + exampleRef))));
}
```

### 404 Not Found Response

```java
private ApiResponse buildNotFoundResponse(String detail) {
    return new ApiResponse()
        .description("Resource not found")
        .content(new Content()
            .addMediaType("application/json", new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                .addExamples("default", new Example()
                    .summary("Not found")
                    .value("""
                        {
                            "type": "https://api.company.com/errors/not-found",
                            "title": "Not Found",
                            "status": 404,
                            "detail": "%s",
                            "instance": "/api/v1/orders/unknown-id"
                        }
                        """.formatted(detail)))));
}
```

## Building Examples with Text Blocks

```java
private Example buildJsonExample(String summary, String description, String jsonBody) {
    return new Example()
        .summary(summary)
        .description(description)
        .value(jsonBody);
}

// Usage with text blocks:
private Example createTransferExample() {
    return buildJsonExample(
        "Transfer request",
        "Example of a transfer between accounts",
        """
            {
                "sourceAccount": "4567890123",
                "targetAccount": "9876543210",
                "amount": 50000.00,
                "currency": "COP",
                "description": "Monthly rent payment"
            }
            """);
}
```

## GroupedOpenApi Patterns

### Group by Feature Domain

```java
@Bean
public GroupedOpenApi ordersApi() {
    return GroupedOpenApi.builder()
        .group("orders")
        .pathsToMatch("/api/v1/orders/**")
        .build();
}

@Bean
public GroupedOpenApi paymentsApi() {
    return GroupedOpenApi.builder()
        .group("payments")
        .pathsToMatch("/api/v1/payments/**")
        .build();
}
```

### Group with Package Filters

```java
@Bean
public GroupedOpenApi internalApi() {
    return GroupedOpenApi.builder()
        .group("internal")
        .pathsToMatch("/internal/**")
        .packagesToScan("com.company.api.internal")
        .build();
}
```

### Group Excluding Paths

```java
@Bean
public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .pathsToMatch("/api/**")
        .pathsToExclude("/api/internal/**", "/api/admin/**")
        .build();
}
```

## Best Practices

1. **Use $ref for all reusable schemas** — Define schemas once in Components and reference them everywhere via `new Schema<>().$ref("#/components/schemas/SchemaName")`

2. **Define common error responses once** — Create private helper methods that return standard `ApiResponse` objects for 400, 401, 404, 500, 503

3. **Use text blocks for JSON examples** — Java text blocks (triple-quote strings) provide readable, maintainable JSON examples without escape characters

4. **Keep the config class organized** — Use clear section comments and private helper methods. Group related code: Info, Servers, Tags, Components, Schemas, Examples, Paths, Parameters, RequestBody, Responses

5. **Define all paths explicitly** — Since Router+Handler doesn't auto-detect endpoints, you must manually define every path and operation

6. **Include correlation/tracing headers** — Always document X-Correlation-Id, X-Channel, and any other standard headers your service expects

7. **Use operationId on every Operation** — This enables better client code generation and API navigation

8. **Version your API in the path** — Use `/api/v1/`, `/api/v2/` prefixes and group accordingly

9. **Document all possible status codes** — Include 200/201, 400, 401, 404, 500, 503 as appropriate for each endpoint

10. **Match examples to schemas** — Ensure example JSON values match the schema property types and constraints

## Post-Implementation Validation Checklist

- [ ] `./gradlew generateOpenApiDocs` completes without errors
- [ ] `.metadata/openapi.yaml` file is generated
- [ ] `curl http://localhost:8080/api-spec` returns valid OpenAPI JSON
- [ ] Swagger UI is NOT accessible (disabled in config)
- [ ] All endpoint paths are documented in the spec
- [ ] All request/response schemas are defined in Components
- [ ] All examples use text blocks and valid JSON
- [ ] Required headers (X-Channel, X-Correlation-Id) are documented
- [ ] Error responses (400, 401, 500, 503) follow RFC 7807 ProblemDetail format
- [ ] `springdoc-openapi-starter-webflux-api` is in reactive-web dependencies (NOT webmvc-ui)
- [ ] No `@Operation`, `@Tag`, or `@ApiResponse` annotations exist on handlers
- [ ] GroupedOpenApi bean is configured for endpoint grouping
- [ ] api-docs path is `/api-spec` (not `/v3/api-docs`)

## Constraints and Warnings

| Constraint | Reason |
|---|---|
| Do NOT use `@Tag`, `@Operation`, `@ApiResponse` annotations on handlers | Router+Handler functional pattern does not process these annotations |
| Do NOT enable `springdoc.swagger-ui.enabled: true` | Production services must not expose Swagger UI |
| Do NOT use `springdoc-openapi-starter-webmvc-ui` | That artifact is for Spring MVC; it will cause runtime conflicts with WebFlux |
| api-docs path MUST be `/api-spec` | The openApi Gradle plugin and CI/CD tooling expect this path |
| Output MUST go to `.metadata/openapi.yaml` | Backstage catalog and CI pipelines read from this location |
| openApi task MUST set `waitTimeInSeconds` | The app needs time to start before the plugin fetches the spec |
| Schemas MUST use `$ref` for reusability | Avoid duplicating schema definitions across multiple operations |
| Examples MUST use text blocks | Improves readability and avoids JSON escape character noise |
| `@SuppressWarnings("rawtypes")` is required on Schema methods | The OpenAPI models use raw generic types by design |
| Do NOT add springdoc dependency to `app-service` | It belongs in `reactive-web` module only; app-service just applies the Gradle plugin |

### Common Errors

| Error | Cause | Fix |
|---|---|---|
| `generateOpenApiDocs` hangs | App doesn't start or wrong port | Verify app starts on 8080 and `/api-spec` responds |
| Empty spec generated | No `@Bean OpenAPI` detected | Ensure `OpenApiSpecConfig` is component-scanned |
| 404 on `/api-spec` | Missing springdoc config in application.yaml | Add `springdoc.api-docs.path: /api-spec` |
| ClassNotFoundException for springdoc | Wrong artifact | Use `springdoc-openapi-starter-webflux-api`, not webmvc variants |
| Annotations ignored on handlers | Using `@Operation` on handler methods | Switch to programmatic path definitions in OpenApiSpecConfig |
| Schema not found in output | Missing from Components map | Add schema to `buildSchemas()` return map |
