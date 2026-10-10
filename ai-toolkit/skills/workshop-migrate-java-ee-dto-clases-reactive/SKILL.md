---
name: workshop-migrate-java-ee-dto-clases-reactive
description: >-
  Migrate DTO classes from Java EE (versions 7 and 8) to a reactive Spring Boot project
  using the Scaffold Clean Architecture structure. Transforms legacy DTOs into domain models
  placed in the domain/model layer with Lombok annotations and gateway interfaces returning
  Mono/Flux reactive types.
license: MIT
compatibility: Java 25, Spring Boot 4.x, Spring WebFlux, Project Reactor
metadata:
  author: criscast
  version: 1.0.0
  based-on: migrate-java-ee-dto-clases
---

# Migrate Java EE DTO Classes — Reactive

## When to Use This Skill

Use this skill when migrating DTO classes from a Java EE project (versions 7/8) to a **reactive** Spring Boot project that follows the **Scaffold Clean Architecture** plugin structure.

This skill is specifically designed for target projects created with the `co.com.bancolombia.cleanArchitecture` Gradle plugin using the **reactive** project type (Spring WebFlux + Project Reactor).

## Target

- **Spring Boot:** 4.x (Spring Framework 7)
- **JDK:** 25
- **Reactive Stack:** Spring WebFlux + Project Reactor (`Mono<T>`, `Flux<T>`)
- **Jackson:** 3.x (`tools.jackson.*` package)
- **Validation:** Jakarta Bean Validation 3.1 (`jakarta.validation.constraints.*`)
- **Build Tool:** Gradle 9.2.1+
- **Architecture:** Scaffold Clean Architecture (reactive type)

## Context

You are an expert in migrating Java EE (7/8) applications to reactive Spring Boot microservices using the Scaffold Clean Architecture. Your approach transforms legacy DTO classes into **domain model entities** that live in the `domain/model` layer, following the architectural conventions where:

- Domain models use Lombok annotations (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- Gateway interfaces define reactive contracts using `Mono<T>` and `Flux<T>`
- Models are grouped by entity/domain concern under `domain/model/src/main/java/[package]/model/[entity-name]/`
- Gateway interfaces live in a `gateways` sub-package within each entity folder

## Requirements

### Pre-conditions

1. The **legacy source code** (Java EE project) must be provided or accessible.
2. The **analysis report** identifying DTO classes and their usage status (IN_USE / UNUSED) must be available.
3. The **destination project** must be a reactive Scaffold Clean Architecture project (verify `co.com.bancolombia.cleanArchitecture` plugin with `type=reactive` in `build.gradle` or `main.gradle`).

If any of these are missing, request them from the user. Do NOT use or create `.sh` or `.py` scripts for the migration. Can use subagents for parallel processing.

### Migration Rules

1. **Only migrate IN_USE DTOs** — DTOs marked as UNUSED (zero references in active Business Logic or Entry Points) MUST NOT be migrated.

2. **Target Location — Domain Model Layer:**
   ```
   domain/model/src/main/java/[package]/model/[entity-name]/
   ```
   NOT in `infrastructure/entry-points/` or a flat `dto/` folder.

3. **Removal of Legacy Artifacts:**
   - Remove all imports from `javax.xml.bind.annotation.*`
   - Remove all JAXB annotations (`@XmlAccessorType`, `@XmlType`, `@XmlElement`, etc.)
   - Remove all JAXB-generated comments (file headers, schema documentation blocks)
   - Remove `serialVersionUID` fields and `Serializable` interface implementation

4. **Field Visibility:**
   - All fields must be package-private (no access modifier) when using Lombok `@Data`
   - If a field was `protected` in the source, it becomes package-private in the model

5. **Lombok Annotations:**
   - Add `@Data` (provides getters, setters, toString, equals, hashCode)
   - Add `@Builder` (provides builder pattern for object construction)
   - Add `@NoArgsConstructor` and `@AllArgsConstructor` when the model needs to be deserialized by Jackson
   - Remove manual getters/setters/constructors (Lombok generates them)

6. **Domain Grouping:**
   - Group models by their integration domain or bounded context
   - Example: `model/balance/`, `model/authentication/`, `model/controllist/`
   - Each domain group can have its own `gateways/` sub-package for repository interfaces

7. **Gateway Interfaces (when applicable):**
   - If the migrated DTO is used as a return type or parameter in service interactions, create a corresponding gateway interface
   - Gateway methods MUST return `Mono<T>` (single result) or `Flux<T>` (multiple results)
   - Gateway interfaces live in `[entity-package]/gateways/`

8. **Request/Response DTOs for Entry Points:**
   - DTOs that are strictly transport objects (REST request/response) remain as Records in `infrastructure/entry-points/` module
   - Add `@JsonIgnoreProperties(ignoreUnknown = true)` on transport DTOs
   - Add Jakarta validation annotations on request DTOs (`@NotBlank`, `@NotNull`, `@Pattern`, `@Size`)
   - Map XSD `minOccurs=1` to `@NotBlank` (Strings) or `@NotNull` (other types)
   - Map XSD `pattern` facets to `@Pattern(regexp = "...")`

9. **Enums and Constants:**
   - Migrate enums to `domain/model/src/main/java/[package]/model/enums/`
   - Migrate constants to `domain/model/src/main/java/[package]/model/utils/Constants.java`

10. **Compilation Verification:**
    - After migration, run `./gradlew build -x test` to verify compilation
    - Ensure no circular dependencies between domain and infrastructure layers

## Target Project Structure

```
📦project-root
┣ 📂domain
┃ ┣ 📂model
┃ ┃ ┣ 📂src
┃ ┃ ┃ ┣ 📂main/java/[package]/model
┃ ┃ ┃ ┃ ┣ 📂[entity-name]/
┃ ┃ ┃ ┃ ┃ ┣ 📜EntityName.java          ← Domain model (@Data @Builder)
┃ ┃ ┃ ┃ ┃ ┣ 📜RelatedEntity.java       ← Nested/related models
┃ ┃ ┃ ┃ ┃ ┗ 📂gateways/
┃ ┃ ┃ ┃ ┃   ┗ 📜EntityNameRepository.java ← Interface with Mono/Flux
┃ ┃ ┃ ┃ ┣ 📂enums/
┃ ┃ ┃ ┃ ┃ ┗ 📜TechnicalMessage.java
┃ ┃ ┃ ┃ ┣ 📂exceptions/
┃ ┃ ┃ ┃ ┃ ┣ 📜BusinessException.java
┃ ┃ ┃ ┃ ┃ ┗ 📜TechnicalException.java
┃ ┃ ┃ ┃ ┗ 📂utils/
┃ ┃ ┃ ┃   ┗ 📜Constants.java
┃ ┃ ┃ ┗ 📂test/java/[package]/model
┃ ┃ ┗ 📜build.gradle
┃ ┗ 📂usecase
┃   ┗ ...
┣ 📂infrastructure
┃ ┣ 📂entry-points
┃ ┃ ┗ 📂reactive-web/src/main/java/[package]/api/
┃ ┃   ┗ 📂dto/                          ← Transport DTOs (Records)
┃ ┃     ┣ 📜RequestDTO.java
┃ ┃     ┗ 📜ResponseDTO.java
┃ ┗ 📂driven-adapters
┃   ┗ ...
┗ 📂applications
  ┗ 📂app-service
```

## Migration Examples

### Example 1: Legacy DTO → Domain Model

#### Original (Java EE)
```java
//
// Este archivo ha sido generado por JAXB v2.2.8-b130911.1802
//

package com.company.fis623.messaging.services.agi;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Clase Java para BalanceType complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BalanceType", propOrder = {
    "balanceAmount",
    "numPockets",
    "availableDebitLimit"
})
public class BalanceType implements Serializable {

    private final static long serialVersionUID = 1L;
    protected String balanceAmount;
    protected String numPockets;
    protected String availableDebitLimit;

    public String getBalanceAmount() { return balanceAmount; }
    public void setBalanceAmount(String value) { this.balanceAmount = value; }
    public String getNumPockets() { return numPockets; }
    public void setNumPockets(String value) { this.numPockets = value; }
    public String getAvailableDebitLimit() { return availableDebitLimit; }
    public void setAvailableDebitLimit(String value) { this.availableDebitLimit = value; }
}
```

#### Migrated (Domain Model — Reactive)
```java
package com.company.model.balance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Balance {
    String balanceAmount;
    String numPockets;
    String availableDebitLimit;
}
```

#### Gateway Interface
```java
package com.company.model.balance.gateways;

import com.company.model.balance.Balance;
import reactor.core.publisher.Mono;

public interface BalanceRepository {

    Mono<Balance> getBalance(String accountNumber);
}
```

### Example 2: Legacy Request DTO → Transport Record (Entry Point)

#### Original (Java EE)
```java
//
// Generado por JAXB
//
package com.company.securityservices.messaging.services;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DocumentValidationRQ")
public class DocumentValidationRQ implements Serializable {

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected String phoneNumber;
    @XmlElement(required = true)
    protected String typeId;
    @XmlElement(required = true)
    protected String idNumber;
    protected String expeditionDate;
    protected String birthDate;
    @XmlElement(required = true)
    protected String name1;
    protected String name2;
    @XmlElement(required = true)
    protected String lastName1;
    protected String lastName2;

    // getters and setters...
}
```

#### Migrated (Transport Record — Entry Point layer)
```java
package com.company.api.dto;

import tools.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocumentValidationRQ(
    @NotBlank @Pattern(regexp = "^\\d{10}$") String phoneNumber,
    @NotBlank @Pattern(regexp = "^(CC|TI|CE|PT)$") String typeId,
    @NotBlank @Pattern(regexp = "^\\d+$") String idNumber,
    String expeditionDate,
    String birthDate,
    @NotBlank String name1,
    String name2,
    @NotBlank String lastName1,
    String lastName2
) {}
```

### Example 3: Complex Domain Model with Nested Types

#### Original (Java EE)
```java
package com.company.fis623.messaging.services;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuthenticationUserType")
public class AuthenticationUserType implements Serializable {
    protected String phoneNumber;
    protected String clientPublicKey;
    protected String messageId;
    protected String deviceId;
    protected String appVersion;
    protected String authMethod;
    protected String provider;

    // getters/setters...
}
```

#### Migrated (Domain Model with Builder)
```java
package com.company.model.authentication;

import com.company.model.enums.InternalAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AuthenticationUser {

    String phoneNumber;
    String clientPublicKey;
    String messageId;
    String deviceId;
    String appVersion;
    String authMethod;
    InternalAuthProvider provider;
}
```

#### Gateway Interface
```java
package com.company.model.authentication.gateways;

import com.company.model.authentication.AuthenticationResult;
import com.company.model.authentication.AuthenticationUser;
import reactor.core.publisher.Mono;

public interface AuthenticationRepository {

    Mono<AuthenticationResult> authenticateUser(AuthenticationUser authenticationUser);
}
```

## Decision Matrix: Domain Model vs. Transport DTO

| Criteria | → Domain Model (`domain/model/`) | → Transport Record (`infrastructure/entry-points/`) |
|----------|----------------------------------|-----------------------------------------------------|
| Used by Use Cases | ✅ Yes | ❌ No |
| Returned by Gateway interfaces | ✅ Yes | ❌ No |
| Represents a business entity | ✅ Yes | ❌ No |
| HTTP request body | ❌ No | ✅ Yes |
| HTTP response body | ❌ No | ✅ Yes |
| Serialized to/from external APIs | Indirectly (via adapters) | ✅ Yes |
| Has business logic | ✅ Possible | ❌ Never |
| Mutable | ✅ Yes (Lombok @Data) | ❌ No (Record) |
| Needs validation annotations | ❌ No | ✅ Yes |

## Instructions

### Step 1: Validate Target Project

1. Confirm the target project uses Scaffold Clean Architecture with reactive type.
2. Identify the base package from `gradle.properties` or source tree (e.g., `com.company`).
3. Map existing domain models in `domain/model/src/main/java/`.

### Step 2: Classify DTOs

For each DTO in the analysis report marked as IN_USE:

1. **If the DTO represents a business entity** (used by use cases, returned by services, has domain meaning):
   → Migrate to `domain/model/src/main/java/[package]/model/[entity-group]/`
   → Use Lombok `@Data @Builder`
   → Create gateway interface if it participates in I/O operations

2. **If the DTO is strictly a transport object** (REST request/response, external API contract):
   → Migrate to `infrastructure/entry-points/reactive-web/src/main/java/[package]/api/dto/`
   → Use Java Records
   → Add `@JsonIgnoreProperties` and Jakarta validation annotations

3. **If the DTO is an enum** → `domain/model/.../model/enums/`

4. **If the DTO contains constants** → `domain/model/.../model/utils/Constants.java`

### Step 3: Execute Migration

For each classified DTO:

1. Create the target file in the correct location.
2. Apply the transformation rules (remove JAXB, add Lombok/Records, etc.).
3. Ensure package declarations match the new location.
4. Add necessary imports (`lombok.*`, `reactor.core.publisher.*`, `jakarta.validation.constraints.*`).

### Step 4: Create Gateway Interfaces

For domain models that participate in reactive data flows:

1. Create a `gateways/` sub-package under the entity package.
2. Define the interface with methods returning `Mono<T>` or `Flux<T>`.
3. Gateway interfaces must NOT have implementation details — they are ports.

### Step 5: Verify Compilation

```bash
./gradlew build -x test
```

Ensure zero compilation errors and no circular dependencies between domain and infrastructure layers.

## Best Practices

- Follow Steering Java Naming Conventions.
- Domain models should be clean of framework annotations (no `@Entity`, no `@Table`, no `@Column`).
- The domain layer MUST have zero external dependencies except Lombok and Reactor Core.
- Gateway interfaces are the boundary between domain and infrastructure.
- Use `@Builder(toBuilder = true)` when the model needs to be partially updated.
- Prefer `@Getter @Setter` over `@Data` when `toString`/`equals`/`hashCode` are not needed or cause issues with circular references.
- Name gateway interfaces as `[EntityName]Repository` or `[EntityName]Gateway` depending on the operation nature.
- Keep models focused — split large legacy DTOs into smaller domain objects if they have distinct responsibilities.

## Post-Migration Validation Checklist

- [ ] **Only IN_USE DTOs migrated**: No DTOs without active consumers
- [ ] **Domain models in correct location**: `domain/model/src/main/java/[package]/model/[entity]/`
- [ ] **Transport DTOs in entry-points**: Request/Response Records in `infrastructure/entry-points/`
- [ ] **Lombok annotations applied**: `@Data @Builder` on domain models
- [ ] **Gateway interfaces created**: With `Mono<T>`/`Flux<T>` return types
- [ ] **No JAXB artifacts**: Zero imports from `javax.xml.bind.annotation`
- [ ] **No JAXB comments**: No auto-generated schema documentation
- [ ] **No Serializable**: Removed `implements Serializable` and `serialVersionUID`
- [ ] **Validation on transport DTOs**: `@NotBlank`/`@NotNull`/`@Pattern` matching legacy XSD constraints
- [ ] **@JsonIgnoreProperties on transport DTOs**: All transport DTOs tolerate unknown fields
- [ ] **Models grouped by domain**: Organized under entity-specific packages
- [ ] **Package-private fields**: No `protected` or `private` on domain model fields (Lombok handles access)
- [ ] **Compilation passes**: `./gradlew build -x test` exits with 0
- [ ] **No circular dependencies**: Domain layer has no imports from infrastructure

## Dependencies (domain/model build.gradle)

The domain model module should include these dependencies:

```groovy
dependencies {
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'io.projectreactor:reactor-core'
}
```

No Spring, no Jackson, no Jakarta validation in the domain layer. Those belong in infrastructure modules.
