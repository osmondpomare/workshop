---
name: workshop-migrate-java-ee-db-entity-clases-reactive
description: >-
  Migrate JPA Entity classes from Java EE (versions 7 and 8) to a reactive Spring Boot project
  using Scaffold Clean Architecture with R2DBC Oracle. Transforms legacy @Entity classes into
  domain models (domain/model), gateway interfaces with Mono/Flux, use cases (domain/usecase),
  and R2DBC driven adapters (infrastructure/driven-adapters/r2dbc-oracle) using DatabaseClient
  with native SQL queries. Provides complete patterns for reactive persistence: connection pooling,
  MapStruct mapping, adapter configuration, and StepVerifier testing.
license: MIT
compatibility: Java 25, Spring Boot 4.x, Spring WebFlux, R2DBC Oracle, Project Reactor
metadata:
  author: criscast
  version: 1.0.0
  based-on: migrate-java-ee-db-entity-clases
---

# Migrate Java EE JPA Entities to Reactive R2DBC (Oracle)

## When to Use This Skill

Use this skill when migrating JPA Entity classes from a Java EE project (versions 7/8) to a **reactive** Spring Boot project that follows the **Scaffold Clean Architecture** plugin structure with **R2DBC Oracle** as the persistence layer.

This skill transforms:
- Legacy `@Entity` classes → **Domain models** (`domain/model`) with Lombok
- Legacy `EntityManager` / `@Repository` services → **Gateway interfaces** (`domain/model/gateways`) + **R2DBC Adapters** (`infrastructure/driven-adapters/r2dbc-oracle`)
- Legacy JPQL/HQL queries → **Native SQL** via `DatabaseClient.sql()`
- Legacy relationships → **Flattened domain models** or **separate queries** (R2DBC has no lazy loading)

## Target

- **Spring Boot:** 4.x (Spring Framework 7)
- **JDK:** 25
- **Reactive Stack:** Spring WebFlux + Project Reactor (`Mono<T>`, `Flux<T>`)
- **Persistence:** Spring Data R2DBC + Oracle R2DBC driver
- **Mapping:** MapStruct for DTO↔Domain conversion
- **Serialization:** Jackson 3.x (`tools.jackson.*` package)
- **Build Tool:** Gradle 9.2.1+
- **Architecture:** Scaffold Clean Architecture (reactive type)

## Context

You are an expert in migrating Java EE (7/8) applications with JPA/Hibernate persistence to reactive Spring Boot microservices using R2DBC on the Scaffold Clean Architecture. Your approach decomposes legacy `@Entity` classes into three distinct layers:

1. **Domain Model** (`domain/model/`) — Pure POJOs with Lombok, no persistence annotations
2. **Gateway Interface** (`domain/model/gateways/`) — Reactive contracts with `Mono<T>`/`Flux<T>`
3. **R2DBC Adapter** (`infrastructure/driven-adapters/r2dbc-oracle/`) — Implementation using `DatabaseClient` with native SQL

**Critical paradigm shift from JPA to R2DBC:**
- No `@Entity`, `@Table`, `@Column` annotations in domain models
- No lazy loading or relationship cascades
- No `EntityManager` — use `DatabaseClient.sql()` with bind parameters
- No JPQL — use native Oracle SQL
- Relationships are resolved via explicit separate queries or joins in SQL
- All I/O returns `Mono<T>` or `Flux<T>` — never blocking

## Requirements

### Pre-conditions

1. The **legacy source code** (Java EE project with JPA entities) must be provided or accessible.
2. The **analysis report** identifying Entity classes and their service mappings must be available.
3. The **destination project** must be a reactive Scaffold Clean Architecture project.
4. The R2DBC Oracle driven adapter module must exist or be generated:
   ```bash
   gradle generateDrivenAdapter --type=r2dbc
   # or
   gradle gda --type r2dbc
   ```

If any of these are missing, request them from the user. Do NOT use or create `.sh` or `.py` scripts for the migration. Can use subagents for parallel processing.

### Migration Rules

#### Rule 1: Domain Model Layer (`domain/model/`)

Transform each legacy `@Entity` class into a clean domain model:

| Legacy (JPA) | Reactive (Domain Model) |
|--------------|------------------------|
| `@Entity` | Remove — domain models have no persistence annotations |
| `@Table(name = "X")` | Remove — table mapping lives in adapter SQL queries |
| `@Column(name = "X")` | Remove — column mapping lives in adapter DTOs/mappers |
| `@Id @GeneratedValue` | Remove — ID handling is in the adapter layer |
| `javax.persistence.*` | Remove entirely |
| `java.sql.Timestamp` | `java.time.LocalDateTime` or `java.time.Instant` |
| `implements Serializable` | Remove |
| `serialVersionUID` | Remove |
| `protected` fields | Package-private (no modifier) |
| Manual getters/setters | Remove — Lombok generates them |
| `@OneToMany`, `@ManyToOne` | Replace with typed field or separate model |
| `List<Child>` relationship | Flatten or resolve in use case with separate gateway call |

**Annotations to use:**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
```

#### Rule 2: Gateway Interface (`domain/model/[entity]/gateways/`)

For each entity that has service operations, create a gateway interface:

```java
public interface EntityNameRepository {
    Mono<EntityName> findById(Long id);
    Flux<EntityName> findByCondition(String param);
    Mono<EntityName> save(EntityName entity);
    Mono<Void> deleteById(Long id);
}
```

- Methods MUST return `Mono<T>` (single) or `Flux<T>` (multiple)
- Use domain model types, never DTOs
- Method names should reflect business intent

#### Rule 3: Use Case Layer (`domain/usecase/`)

Create use case classes that orchestrate business logic:

```java
@RequiredArgsConstructor
public class EntityNameUseCase {
    private final EntityNameRepository entityNameRepository;

    public Mono<EntityName> getById(Long id) {
        return entityNameRepository.findById(id)
            .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.ENTITY_NAME_NOT_FOUND)));
    }
}
```

- Inject gateway interfaces (not adapters)
- No `@Service`, `@Component` annotations (DI wired in app-service)
- Use `@RequiredArgsConstructor` for constructor injection
- Return reactive types (`Mono<T>`, `Flux<T>`)
- **Error handling contract** (only `BusinessException`/`TechnicalException` exist, both carrying a `TechnicalMessage`; guided by `transfer_service_MS`): the repository/adapter only returns `Mono.empty()` when nothing matches — it never decides whether that is an error. The **use case** decides: if "not found" is a business-meaningful outcome for that lookup, it MUST raise `BusinessException` via `switchIfEmpty(...)`, as shown above. A use case never constructs `TechnicalException` itself — that classification belongs to the adapter's own error mapping (Rule 4).

#### Rule 4: R2DBC Adapter (`infrastructure/driven-adapters/r2dbc-oracle/`)

The adapter module implements gateway interfaces using `DatabaseClient`:

**Structure:**
```
infrastructure/driven-adapters/r2dbc-oracle/
├── src/main/java/[package]/r2dbc/
│   ├── adapter/
│   │   └── EntityNameAdapter.java      ← Implements gateway interface
│   ├── config/
│   │   ├── OracleConnectionConfig.java ← Connection pool warmup
│   │   └── EntityAdapterConfig.java    ← Bean wiring
│   ├── dto/
│   │   └── EntityNameDTO.java          ← DB row representation
│   └── mapper/
│       └── EntityNameMapper.java       ← MapStruct DTO↔Domain
├── src/test/java/[package]/r2dbc/
│   ├── EntityNameAdapterTest.java
│   └── config/
│       ├── OracleConnectionConfigTest.java
│       └── EntityAdapterConfigTest.java
└── build.gradle
```

#### Rule 5: Relationship Handling

Since R2DBC does NOT support lazy loading or automatic relationship resolution:

| JPA Relationship | R2DBC Strategy |
|-----------------|----------------|
| `@OneToOne` | Inline fields in parent model OR separate gateway call |
| `@ManyToOne` | Store FK as field, resolve via separate `Mono<Parent>` call |
| `@OneToMany` | Separate `Flux<Child>` gateway method, compose in use case |
| `@ManyToMany` | Junction table query, compose in use case |

**Example — Composing relationships in use case:**
```java
public Mono<OrderWithItems> getOrderWithItems(Long orderId) {
    return orderRepository.findById(orderId)
        .zipWith(orderItemRepository.findByOrderId(orderId).collectList())
        .map(tuple -> OrderWithItems.builder()
            .order(tuple.getT1())
            .items(tuple.getT2())
            .build());
}
```

## Target Project Structure

```
📦project-root
┣ 📂domain
┃ ┣ 📂model
┃ ┃ ┣ 📂src/main/java/[package]/model
┃ ┃ ┃ ┣ 📂[entity-name]/
┃ ┃ ┃ ┃ ┣ 📜EntityName.java              ← Domain model (@Data @Builder)
┃ ┃ ┃ ┃ ┗ 📂gateways/
┃ ┃ ┃ ┃   ┗ 📜EntityNameRepository.java   ← Interface with Mono/Flux
┃ ┃ ┃ ┣ 📂enums/
┃ ┃ ┃ ┃ ┗ 📜TechnicalMessage.java
┃ ┃ ┃ ┣ 📂exceptions/
┃ ┃ ┃ ┃ ┣ 📜BusinessException.java
┃ ┃ ┃ ┃ ┗ 📜TechnicalException.java
┃ ┃ ┃ ┗ 📂utils/
┃ ┃ ┃   ┗ 📜Constants.java
┃ ┃ ┗ 📜build.gradle
┃ ┗ 📂usecase
┃   ┣ 📂src/main/java/[package]/usecase/[entity-name]/
┃   ┃ ┗ 📜EntityNameUseCase.java
┃   ┣ 📂src/test/java/[package]/usecase/[entity-name]/
┃   ┃ ┗ 📜EntityNameUseCaseTest.java
┃   ┗ 📜build.gradle
┣ 📂infrastructure
┃ ┗ 📂driven-adapters
┃   ┗ 📂r2dbc-oracle
┃     ┣ 📂src/main/java/[package]/r2dbc/
┃     ┃ ┣ 📂adapter/
┃     ┃ ┃ ┗ 📜EntityNameAdapter.java
┃     ┃ ┣ 📂config/
┃     ┃ ┃ ┣ 📜OracleConnectionConfig.java
┃     ┃ ┃ ┗ 📜EntityAdapterConfig.java
┃     ┃ ┣ 📂dto/
┃     ┃ ┃ ┗ 📜EntityNameDTO.java
┃     ┃ ┗ 📂mapper/
┃     ┃   ┗ 📜EntityNameMapper.java
┃     ┣ 📂src/test/java/[package]/r2dbc/
┃     ┃ ┣ 📜EntityNameAdapterTest.java
┃     ┃ ┗ 📂config/
┃     ┃   ┣ 📜OracleConnectionConfigTest.java
┃     ┃   ┗ 📜EntityAdapterConfigTest.java
┃     ┗ 📜build.gradle
┗ 📂applications
  ┗ 📂app-service
    ┗ 📂src/main/resources/
      ┗ 📜application.yaml               ← R2DBC connection config
```

## Migration Examples

### Example 1: Simple Entity → Full Reactive Stack

#### Original JPA Entity (Java EE)
```java
package com.company.mdw.common.jpa.model.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "SISTEMA", schema = "MY_SCHEMA")
public class Sistema implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "SISTEMA_SISTEMAID_GENERATOR", sequenceName = "SISTEMA_SEQ1")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SISTEMA_SISTEMAID_GENERATOR")
    @Column(name = "SISTEMA_ID")
    private long sistemaId;

    private String descripcion;

    @Column(name = "FECHA_CREACION")
    private Timestamp fechaCreacion;

    @Column(name = "FECHA_MODIFICACION")
    private Timestamp fechaModificacion;

    private String nombre;

    @Column(name = "USR_CREACION")
    private String usrCreacion;

    @Column(name = "USR_MODIFICACION")
    private String usrModificacion;

    @OneToMany(mappedBy = "sistema")
    private List<CatalogoError> catalogoErrors;

    // constructors, getters, setters...
}
```

#### Migrated Domain Model
```java
package com.company.model.sistema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sistema {
    Long sistemaId;
    String descripcion;
    LocalDateTime fechaCreacion;
    LocalDateTime fechaModificacion;
    String nombre;
    String usrCreacion;
    String usrModificacion;
}
```

#### Gateway Interface
```java
package com.company.model.sistema.gateways;

import com.company.model.sistema.Sistema;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SistemaRepository {

    Mono<Sistema> findById(Long sistemaId);

    Flux<Sistema> findAll();

    Mono<Sistema> save(Sistema sistema);

    Mono<Void> deleteById(Long sistemaId);
}
```

#### Use Case
```java
package com.company.usecase.sistema;

import com.company.model.enums.TechnicalMessage;
import com.company.model.exceptions.BusinessException;
import com.company.model.sistema.Sistema;
import com.company.model.sistema.gateways.SistemaRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SistemaUseCase {

    private final SistemaRepository sistemaRepository;

    public Mono<Sistema> getById(Long sistemaId) {
        return sistemaRepository.findById(sistemaId)
            .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.SISTEMA_NOT_FOUND)));
    }

    public Flux<Sistema> getAll() {
        return sistemaRepository.findAll();
    }

    public Mono<Sistema> create(Sistema sistema) {
        return sistemaRepository.save(sistema);
    }
}
```

> Note: `findById` raises `BusinessException` when empty because looking up a single, known `Sistema` by id is expected to succeed — "not found" is a business outcome here. `getAll()` and `create(...)` are NOT given the same treatment: an empty list from `getAll()` is a normal result (not an error), and `save(...)` never returns empty.

#### R2DBC DTO (infrastructure layer)
```java
package com.company.r2dbc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SistemaDTO {
    private Long sistemaId;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private String nombre;
    private String usrCreacion;
    private String usrModificacion;
}
```

#### MapStruct Mapper
```java
package com.company.r2dbc.mapper;

import com.company.model.sistema.Sistema;
import com.company.r2dbc.dto.SistemaDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SistemaMapper {

    SistemaMapper MAPPER = Mappers.getMapper(SistemaMapper.class);

    Sistema dtoToDomain(SistemaDTO dto);

    SistemaDTO domainToDto(Sistema domain);
}
```

#### R2DBC Adapter
```java
package com.company.r2dbc.adapter;

import com.company.model.enums.TechnicalMessage;
import com.company.model.exceptions.TechnicalException;
import com.company.model.sistema.Sistema;
import com.company.model.sistema.gateways.SistemaRepository;
import com.company.r2dbc.dto.SistemaDTO;
import com.company.r2dbc.mapper.SistemaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Predicate;

import static net.logstash.logback.argument.StructuredArguments.kv;

@RequiredArgsConstructor
@Slf4j
public class SistemaAdapter implements SistemaRepository {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Sistema> findById(Long sistemaId) {
        return Mono.defer(() -> databaseClient
                .sql("""
                    SELECT SISTEMA_ID, DESCRIPCION, FECHA_CREACION, FECHA_MODIFICACION,
                           NOMBRE, USR_CREACION, USR_MODIFICACION
                    FROM MY_SCHEMA.SISTEMA
                    WHERE SISTEMA_ID = :sistemaId
                    """)
                .bind("sistemaId", sistemaId)
                .map((row, metadata) -> SistemaDTO.builder()
                    .sistemaId(row.get("SISTEMA_ID", Long.class))
                    .descripcion(row.get("DESCRIPCION", String.class))
                    .fechaCreacion(row.get("FECHA_CREACION", java.time.LocalDateTime.class))
                    .fechaModificacion(row.get("FECHA_MODIFICACION", java.time.LocalDateTime.class))
                    .nombre(row.get("NOMBRE", String.class))
                    .usrCreacion(row.get("USR_CREACION", String.class))
                    .usrModificacion(row.get("USR_MODIFICACION", String.class))
                    .build())
                .one()
                .map(SistemaMapper.MAPPER::dtoToDomain)
                .doOnSuccess(result -> log.info("DB findById response", kv("sistemaId", sistemaId)))
                .doOnSubscribe(sub -> log.info("DB findById request", kv("sistemaId", sistemaId))))
            .doOnError(error -> log.error("DB findById error", error))
            .onErrorMap(Predicate.not(TechnicalException.class::isInstance),
                ex -> new TechnicalException(ex, TechnicalMessage.INTERNAL_ERROR));
    }

    @Override
    public Flux<Sistema> findAll() {
        return Flux.defer(() -> databaseClient
                .sql("""
                    SELECT SISTEMA_ID, DESCRIPCION, FECHA_CREACION, FECHA_MODIFICACION,
                           NOMBRE, USR_CREACION, USR_MODIFICACION
                    FROM MY_SCHEMA.SISTEMA
                    """)
                .map((row, metadata) -> SistemaDTO.builder()
                    .sistemaId(row.get("SISTEMA_ID", Long.class))
                    .descripcion(row.get("DESCRIPCION", String.class))
                    .fechaCreacion(row.get("FECHA_CREACION", java.time.LocalDateTime.class))
                    .fechaModificacion(row.get("FECHA_MODIFICACION", java.time.LocalDateTime.class))
                    .nombre(row.get("NOMBRE", String.class))
                    .usrCreacion(row.get("USR_CREACION", String.class))
                    .usrModificacion(row.get("USR_MODIFICACION", String.class))
                    .build())
                .all()
                .map(SistemaMapper.MAPPER::dtoToDomain))
            .doOnError(error -> log.error("DB findAll error", error))
            .onErrorMap(Predicate.not(TechnicalException.class::isInstance),
                ex -> new TechnicalException(ex, TechnicalMessage.INTERNAL_ERROR));
    }

    @Override
    public Mono<Sistema> save(Sistema sistema) {
        return Mono.defer(() -> databaseClient
                .sql("""
                    INSERT INTO MY_SCHEMA.SISTEMA
                    (DESCRIPCION, NOMBRE, USR_CREACION, FECHA_CREACION)
                    VALUES (:descripcion, :nombre, :usrCreacion, CURRENT_TIMESTAMP)
                    """)
                .bindValues(Map.of(
                    "descripcion", sistema.getDescripcion(),
                    "nombre", sistema.getNombre(),
                    "usrCreacion", sistema.getUsrCreacion()
                ))
                .fetch()
                .rowsUpdated()
                .thenReturn(sistema))
            .doOnError(error -> log.error("DB save error", error))
            .onErrorMap(Predicate.not(TechnicalException.class::isInstance),
                ex -> new TechnicalException(ex, TechnicalMessage.INTERNAL_ERROR));
    }

    @Override
    public Mono<Void> deleteById(Long sistemaId) {
        return Mono.defer(() -> databaseClient
                .sql("DELETE FROM MY_SCHEMA.SISTEMA WHERE SISTEMA_ID = :sistemaId")
                .bind("sistemaId", sistemaId)
                .fetch()
                .rowsUpdated()
                .then())
            .doOnError(error -> log.error("DB deleteById error", error))
            .onErrorMap(Predicate.not(TechnicalException.class::isInstance),
                ex -> new TechnicalException(ex, TechnicalMessage.INTERNAL_ERROR));
    }
}
```

#### Adapter Config
```java
package com.company.r2dbc.config;

import com.company.model.sistema.gateways.SistemaRepository;
import com.company.r2dbc.adapter.SistemaAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
public class SistemaAdapterConfig {

    @Bean
    public SistemaRepository sistemaRepository(DatabaseClient databaseClient) {
        return new SistemaAdapter(databaseClient);
    }
}
```

#### Oracle Connection Config (shared)
```java
package com.company.r2dbc.config;

import io.r2dbc.pool.ConnectionPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OracleConnectionConfig {

    @Bean
    public ApplicationRunner warmupPool(ConnectionPool connectionPool) {
        return args -> connectionPool.warmup()
                .doOnError(error -> log.error("Connection warm-up failed", error))
                .subscribe(count -> log.info("Warmed up {} connections", count));
    }
}
```

### Example 2: Entity with Relationships → Separate Queries

#### Original JPA Entity with @OneToMany
```java
@Entity
@Table(name = "CATALOGO_ERROR", schema = "MY_SCHEMA")
public class CatalogoError implements Serializable {
    @Id
    @Column(name = "CATALOGO_ERROR_ID")
    private long catalogoErrorId;

    @Column(name = "COD_ERROR")
    private String codError;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "SISTEMA_ID")
    private Sistema sistema;

    // getters/setters...
}
```

#### Migrated Domain Model (relationship flattened)
```java
package com.company.model.catalogoerror;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoError {
    Long catalogoErrorId;
    String codError;
    String descripcion;
    Long sistemaId;  // FK stored as primitive — no object reference
}
```

#### Gateway for child entity
```java
package com.company.model.catalogoerror.gateways;

import com.company.model.catalogoerror.CatalogoError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogoErrorRepository {

    Mono<CatalogoError> findById(Long catalogoErrorId);

    Flux<CatalogoError> findBySistemaId(Long sistemaId);
}
```

#### Use Case composing relationships
```java
package com.company.usecase.catalogoerror;

import com.company.model.catalogoerror.CatalogoError;
import com.company.model.catalogoerror.gateways.CatalogoErrorRepository;
import com.company.model.sistema.Sistema;
import com.company.model.sistema.gateways.SistemaRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class CatalogoErrorUseCase {

    private final CatalogoErrorRepository catalogoErrorRepository;
    private final SistemaRepository sistemaRepository;

    public Mono<CatalogoError> getById(Long id) {
        return catalogoErrorRepository.findById(id);
    }

    /**
     * Resolves the relationship that was @ManyToOne in JPA
     * by composing two reactive calls.
     */
    public Mono<SistemaWithErrors> getSistemaWithErrors(Long sistemaId) {
        return sistemaRepository.findById(sistemaId)
            .zipWith(catalogoErrorRepository.findBySistemaId(sistemaId).collectList())
            .map(tuple -> SistemaWithErrors.builder()
                .sistema(tuple.getT1())
                .errors(tuple.getT2())
                .build());
    }
}
```

### Example 3: Legacy Service with EntityManager → R2DBC Adapter

#### Original Legacy Service (Java EE)
```java
package com.company.mdw.common.jpa.service;

@Stateless
public class SistemaServiceImpl implements SistemaService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Sistema findById(long sistemaId) {
        return entityManager.find(Sistema.class, sistemaId);
    }

    @Override
    public List<Sistema> findByNombre(String nombre) {
        TypedQuery<Sistema> query = entityManager.createQuery(
            "SELECT s FROM Sistema s WHERE s.nombre = :nombre", Sistema.class);
        query.setParameter("nombre", nombre);
        return query.getResultList();
    }
}
```

#### Migrated R2DBC Adapter
```java
package com.company.r2dbc.adapter;

import com.company.model.sistema.Sistema;
import com.company.model.sistema.gateways.SistemaRepository;
import com.company.r2dbc.dto.SistemaDTO;
import com.company.r2dbc.mapper.SistemaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class SistemaAdapter implements SistemaRepository {

    private final DatabaseClient databaseClient;

    @Override
    public Flux<Sistema> findByNombre(String nombre) {
        return databaseClient
            .sql("""
                SELECT SISTEMA_ID, DESCRIPCION, FECHA_CREACION, FECHA_MODIFICACION,
                       NOMBRE, USR_CREACION, USR_MODIFICACION
                FROM MY_SCHEMA.SISTEMA
                WHERE NOMBRE = :nombre
                """)
            .bind("nombre", nombre)
            .map((row, metadata) -> SistemaDTO.builder()
                .sistemaId(row.get("SISTEMA_ID", Long.class))
                .descripcion(row.get("DESCRIPCION", String.class))
                .fechaCreacion(row.get("FECHA_CREACION", java.time.LocalDateTime.class))
                .fechaModificacion(row.get("FECHA_MODIFICACION", java.time.LocalDateTime.class))
                .nombre(row.get("NOMBRE", String.class))
                .usrCreacion(row.get("USR_CREACION", String.class))
                .usrModificacion(row.get("USR_MODIFICACION", String.class))
                .build())
            .all()
            .map(SistemaMapper.MAPPER::dtoToDomain);
    }
}
```

## Adapter Test Pattern

```java
package com.company.r2dbc;

import com.company.model.sistema.Sistema;
import com.company.r2dbc.adapter.SistemaAdapter;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SistemaAdapterTest {

    SistemaAdapter sistemaAdapter;

    @Mock
    DatabaseClient databaseClient;

    @Mock
    DatabaseClient.GenericExecuteSpec executeSpec;

    @Mock
    FetchSpec<Sistema> fetchSpec;

    @Mock
    Row row;

    @Mock
    RowMetadata rowMetadata;

    @BeforeEach
    void setUp() {
        sistemaAdapter = new SistemaAdapter(databaseClient);
    }

    @Test
    void shouldFindByIdSuccessfully() {
        buildMockDBCall();
        when(row.get("SISTEMA_ID", Long.class)).thenReturn(1L);
        when(row.get("DESCRIPCION", String.class)).thenReturn("Test Sistema");
        when(row.get("NOMBRE", String.class)).thenReturn("NombreTest");
        when(row.get("FECHA_CREACION", java.time.LocalDateTime.class)).thenReturn(LocalDateTime.now());
        when(row.get("FECHA_MODIFICACION", java.time.LocalDateTime.class)).thenReturn(LocalDateTime.now());
        when(row.get("USR_CREACION", String.class)).thenReturn("admin");
        when(row.get("USR_MODIFICACION", String.class)).thenReturn("admin");

        StepVerifier.create(sistemaAdapter.findById(1L))
            .assertNext(sistema -> {
                assertEquals(1L, sistema.getSistemaId());
                assertEquals("Test Sistema", sistema.getDescripcion());
                assertEquals("NombreTest", sistema.getNombre());
            })
            .verifyComplete();

        verify(databaseClient).sql(anyString());
    }

    @Test
    void shouldFailWithDbError() {
        when(databaseClient.sql(anyString())).thenThrow(new RuntimeException("DB Error"));

        StepVerifier.create(sistemaAdapter.findById(1L))
            .expectError()
            .verify();
    }

    private void buildMockDBCall() {
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        final BiFunction[] mapperHolder = new BiFunction[1];
        when(executeSpec.map(any(BiFunction.class))).thenAnswer(inv -> {
            mapperHolder[0] = inv.getArgument(0);
            return fetchSpec;
        });
        when(fetchSpec.one()).thenAnswer(inv ->
            Mono.just(mapperHolder[0].apply(row, rowMetadata))
        );
    }
}
```

## Use Case Test Pattern

```java
package com.company.usecase.sistema;

import com.company.model.sistema.Sistema;
import com.company.model.sistema.gateways.SistemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SistemaUseCaseTest {

    @Mock
    private SistemaRepository sistemaRepository;

    SistemaUseCase sistemaUseCase;

    @BeforeEach
    void setUp() {
        sistemaUseCase = new SistemaUseCase(sistemaRepository);
    }

    @Test
    void shouldGetByIdSuccessfully() {
        given(sistemaRepository.findById(1L))
            .willReturn(Mono.just(Sistema.builder()
                .sistemaId(1L)
                .nombre("Test")
                .descripcion("Description")
                .build()));

        StepVerifier.create(sistemaUseCase.getById(1L))
            .assertNext(sistema -> {
                assertEquals(1L, sistema.getSistemaId());
                assertEquals("Test", sistema.getNombre());
            })
            .verifyComplete();
    }
}
```

## R2DBC Oracle Configuration

### build.gradle (driven-adapter module)

```groovy
dependencies {
    implementation project(':model')
    implementation 'org.springframework:spring-context'
    implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
    implementation "com.oracle.database.r2dbc:oracle-r2dbc:${oracleR2dbcVersion}"
    implementation 'org.reactivecommons.utils:object-mapper-api:0.1.0'
    implementation 'io.r2dbc:r2dbc-proxy'

    testImplementation 'org.reactivecommons.utils:object-mapper:0.1.0'

    implementation "org.mapstruct:mapstruct:${mapstructVersion}"
    annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
}
```

### application.yaml (R2DBC section)

```yaml
spring:
  r2dbc:
    url: r2dbc:oracle://${DB_HOST:localhost}:${DB_PORT:1521}/${DB_DATABASE:ORCL}
    username: "${DB_USERNAME:}"
    password: "${DB_PASSWORD:}"
    pool:
      initial-size: "${DB_POOL_INITIAL_SIZE:5}"
      max-size: "${DB_POOL_MAX_SIZE:15}"
      max-idle-time: "${DB_POOL_MAX_IDLE_TIME:300s}"
      max-life-time: "${DB_POOL_MAX_LIFE_TIME:900s}"
      max-acquire-time: "${DB_POOL_MAX_ACQUIRE_TIME:3s}"
      max-create-connection-time: "${DB_POOL_MAX_CREATE_CONNECTION_TIME:5s}"
    properties:
      autoCommit: true

management:
  health:
    r2dbc:
      enabled: true
```

## Instructions

### Step 1: Validate Target Project & Generate R2DBC Module

1. Confirm the target project uses Scaffold Clean Architecture with reactive type.
2. Identify the base package from `gradle.properties` or source tree.
3. If the `r2dbc-oracle` driven adapter module does not exist, generate it:
   ```bash
   gradle gda --type r2dbc
   ```
4. Rename the generated module folder from `r2dbc-postgresql` to `r2dbc-oracle` if needed.
5. Update `build.gradle` to use Oracle R2DBC driver instead of PostgreSQL.

### Step 2: Identify Entities and Services to Migrate

From the analysis report:
1. List all `@Entity` classes and their table mappings.
2. Identify service classes (`EntityManager` usage, `@NamedQuery`, JPQL queries).
3. Map JPA relationships (`@OneToMany`, `@ManyToOne`, etc.).
4. Determine which entities match in the Service Interface ↔ Implementation Mapping section.

### Step 3: Create Domain Models

For each entity:
1. Create the domain model in `domain/model/src/main/java/[package]/model/[entity-name]/`.
2. Remove all JPA/persistence annotations.
3. Replace `Timestamp` → `LocalDateTime`.
4. Remove `Serializable`, `serialVersionUID`.
5. Add Lombok annotations (`@Data @Builder @NoArgsConstructor @AllArgsConstructor`).
6. Flatten relationships to primitive FK fields.

### Step 4: Create Gateway Interfaces

For each entity with service operations:
1. Create the gateway interface in `[entity-package]/gateways/`.
2. Define methods with `Mono<T>`/`Flux<T>` return types.
3. Method signatures must use domain models only.

### Step 5: Create Use Cases

For each entity with business logic:
1. Create use case in `domain/usecase/src/main/java/[package]/usecase/[entity-name]/`.
2. Inject gateway interfaces via constructor.
3. Compose relationship resolution using reactive operators (`zipWith`, `flatMap`, etc.).
4. Create corresponding test class with `StepVerifier`.

### Step 6: Create R2DBC Adapter Components

For each gateway interface:
1. **DTO** — Create in `r2dbc/dto/` with `@Data @Builder @AllArgsConstructor @NoArgsConstructor`.
2. **Mapper** — Create MapStruct interface in `r2dbc/mapper/`.
3. **Adapter** — Implement gateway using `DatabaseClient.sql()` with native SQL.
4. **Config** — Create `@Configuration` class to wire adapter bean.
5. **Test** — Create adapter test with `MockitoExtension` + `StepVerifier`.

### Step 7: Configure Application

1. Add R2DBC configuration to `application.yaml`.
2. Add `OracleConnectionConfig` for connection pool warmup.
3. Verify with `./gradlew build -x test`.

### Step 8: Migrate Query Constants

If the legacy project has a `ConstantDataBaseJPA` or similar utility:
- Extract table names and schema into the adapter's SQL queries directly.
- Or create a constants class in the adapter module (NOT in domain).

## JPQL → Native SQL Migration Reference

| JPQL Pattern | R2DBC Native SQL Equivalent |
|---|---|
| `entityManager.find(Entity.class, id)` | `databaseClient.sql("SELECT ... WHERE ID = :id").bind("id", id).map(...).one()` |
| `entityManager.createQuery("SELECT e FROM Entity e")` | `databaseClient.sql("SELECT ... FROM TABLE").map(...).all()` |
| `entityManager.createQuery("... WHERE e.field = :param")` | `databaseClient.sql("... WHERE FIELD = :param").bind("param", value)` |
| `entityManager.persist(entity)` | `databaseClient.sql("INSERT INTO ...").bindValues(map).fetch().rowsUpdated()` |
| `entityManager.merge(entity)` | `databaseClient.sql("UPDATE ... SET ... WHERE ID = :id").bind(...)` |
| `entityManager.remove(entity)` | `databaseClient.sql("DELETE FROM ... WHERE ID = :id").bind("id", id)` |
| `@NamedQuery(query = "...")` | Extract SQL into adapter method directly |
| Pagination (`setFirstResult`/`setMaxResults`) | `OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY` (Oracle 12c+) |

## Best Practices

- **No JPA annotations in domain** — Domain models are pure POJOs with Lombok.
- **No blocking calls** — All I/O returns `Mono<T>` or `Flux<T>`.
- **Bind parameters always** — Never concatenate values into SQL strings (prevents SQL injection).
- **Use `Mono.defer()`** — Wrap adapter logic in `Mono.defer()` to ensure lazy subscription.
- **Error mapping** — Adapters map unexpected DB failures to `TechnicalException` via `onErrorMap` (never `BusinessException`). Use cases decide whether an empty result is a business error and raise `BusinessException` via `switchIfEmpty(...)` (never `TechnicalException`). See `transfer_service_MS` for this split.
- **Structured logging** — Use `kv()` from logstash for structured log arguments.
- **Connection pool warmup** — Always include `OracleConnectionConfig` for production readiness.
- **MapStruct for mapping** — Never manually map DTO↔Domain; use MapStruct interfaces.
- **Test with StepVerifier** — All reactive code must be tested with `StepVerifier`.
- **Separate DTOs per layer** — Adapter DTOs live in `r2dbc/dto/`, never shared with domain.
- **Constants in adapter** — Table/schema names belong in adapter SQL, not domain constants.

## Post-Migration Validation Checklist

- [ ] **Domain models created**: In `domain/model/src/main/java/[package]/model/[entity]/`
- [ ] **No JPA annotations in domain**: Zero `@Entity`, `@Table`, `@Column` imports
- [ ] **No javax.persistence imports**: All removed
- [ ] **Timestamp → LocalDateTime**: All `java.sql.Timestamp` replaced
- [ ] **No Serializable**: Removed from all domain models
- [ ] **Gateway interfaces created**: With `Mono<T>`/`Flux<T>` return types
- [ ] **Use cases created**: In `domain/usecase/` with gateway injection
- [ ] **`BusinessException` on not-found lookups**: single-entity `getById`-style use cases raise `BusinessException` via `switchIfEmpty(...)` when the entity must exist; use cases never construct `TechnicalException`
- [ ] **R2DBC adapters implement gateways**: Using `DatabaseClient.sql()`
- [ ] **MapStruct mappers created**: For DTO↔Domain conversion
- [ ] **Adapter configs created**: `@Configuration` classes wiring beans
- [ ] **OracleConnectionConfig exists**: Connection pool warmup bean
- [ ] **application.yaml configured**: `spring.r2dbc.*` properties set
- [ ] **Tests created**: Adapter tests with StepVerifier, UseCase tests with Mockito
- [ ] **No EntityManager usage**: All replaced with `DatabaseClient`
- [ ] **Relationships flattened**: `@OneToMany`/`@ManyToOne` resolved via separate queries
- [ ] **Bind parameters used**: No string concatenation in SQL
- [ ] **Compilation passes**: `./gradlew build -x test` exits with 0
- [ ] **No circular dependencies**: Domain has no imports from infrastructure

## Dependencies Summary

### domain/model/build.gradle
```groovy
dependencies {
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'io.projectreactor:reactor-core'
}
```

### domain/usecase/build.gradle
```groovy
dependencies {
    implementation project(':model')
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'io.projectreactor:reactor-core'
    testImplementation 'io.projectreactor:reactor-test'
}
```

### infrastructure/driven-adapters/r2dbc-oracle/build.gradle
```groovy
dependencies {
    implementation project(':model')
    implementation 'org.springframework:spring-context'
    implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
    implementation "com.oracle.database.r2dbc:oracle-r2dbc:${oracleR2dbcVersion}"
    implementation 'org.reactivecommons.utils:object-mapper-api:0.1.0'
    implementation 'io.r2dbc:r2dbc-proxy'

    testImplementation 'org.reactivecommons.utils:object-mapper:0.1.0'
    testImplementation 'io.projectreactor:reactor-test'

    implementation "org.mapstruct:mapstruct:${mapstructVersion}"
    annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
}
```

## Scaffolding the R2DBC Module

If the target project does not yet have the R2DBC driven adapter module, generate it using the Scaffold Clean Architecture plugin:

```bash
gradle generateDrivenAdapter --type=r2dbc
# or shorthand:
gradle gda --type r2dbc
```

This creates the base structure under `infrastructure/driven-adapters/`. Then:
1. Rename the folder to `r2dbc-oracle` if it was generated as `r2dbc-postgresql`.
2. Replace the PostgreSQL R2DBC driver dependency with Oracle R2DBC.
3. Add MapStruct dependencies.
4. Update `settings.gradle` if the module name changed.
