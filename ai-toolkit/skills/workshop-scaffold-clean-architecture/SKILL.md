---
name: workshop-scaffold-clean-architecture
description: >-
  Provides architectural context and scaffolding capabilities for Java projects using the Scaffold Clean Architecture Gradle plugin.
  Use this skill as a context provider when executing other skills (migration, implementation, testing) on Clean Architecture projects.
  Analyzes existing project structure, identifies available modules and layers, and executes scaffold tasks (generate project, models, use cases, driven adapters, entry points, helpers, pipelines, tests) according to user requirements.
license: MIT
compatibility: Java 17+, Gradle 9.2.1+, Spring Boot 4.x
metadata:
  author: gabrimar
  version: 2.0.0
---

# Scaffold Clean Architecture

This skill serves two purposes:

1. **Context & Analysis Provider** — Analyze an existing Clean Architecture project to provide structural context to the AI Agent so other skills can correctly implement, migrate, or integrate features following the architecture's conventions.
2. **Scaffolding Executor** — Generate new projects and components using the `scaffold-clean-architecture` Gradle plugin tasks defined in `references/tasks/`.

## How This Skill Is Used

A user will typically ask:

> "According to the Scaffold Clean Architecture, use [another-skill] to implement/migrate [feature] in my project."

In this flow:

1. **This skill analyzes** the target project structure (layers, modules, packages, existing models, gateways, use cases, adapters, entry points).
2. **This skill provides context** to the AI Agent about where code should be placed, what conventions to follow, and what modules already exist.
3. **The other skill executes** the actual implementation/migration using the architectural context provided.

If the user asks to scaffold or generate components directly, this skill executes the corresponding `references/tasks/` instructions.

---

## Architecture Overview

The plugin organizes code into three layers with dependencies always pointing inward (see `references/intro.md`):

```
┌─────────────────────────────────────────────────────┐
│  Application Layer (outermost)                      │
│  └── app-service: assembles modules, main entry     │
├─────────────────────────────────────────────────────┤
│  Infrastructure Layer                               │
│  ├── entry-points/   (REST, Kafka, GraphQL, MCP...) │
│  ├── driven-adapters/(JPA, Mongo, R2DBC, SQS...)    │
│  └── helpers/        (shared utilities)             │
├─────────────────────────────────────────────────────┤
│  Domain Layer (innermost)                           │
│  ├── model/   (entities, value objects, gateways)   │
│  └── usecase/ (application business logic)          │
└─────────────────────────────────────────────────────┘
```

**Dependency Rule**: Source code dependencies can only point inward. Nothing in an inner circle can know anything about an outer circle.

---

## Feature: Analyze Project Structure

When this skill is invoked for context, perform the following analysis:

### Step 1: Validate it is a Clean Architecture Project

Check for the presence of the `co.com.bancolombia.cleanArchitecture` plugin in `build.gradle` or `main.gradle`. If not found, inform the user this is not a Scaffold Clean Architecture project and suggest alternatives.

### Step 2: Identify Project Metadata

Extract from project files:
- **Package name** (from `gradle.properties` or source tree)
- **Project name** (from `settings.gradle`)
- **Project type** (`reactive` or `imperative`)
- **Java version** (from `build.gradle` or `gradle.properties`)
- **Plugin version** (from `build.gradle`)

### Step 3: Map Existing Modules

Scan the project and catalog:

| Layer | Path | What to Look For |
|-------|------|------------------|
| Domain - Models | `domain/model/src/main/java/` | Entity classes, Gateway/Repository interfaces |
| Domain - Use Cases | `domain/usecase/src/main/java/` | UseCase classes, business logic |
| Infrastructure - Entry Points | `infrastructure/entry-points/` | Controller types (REST, WebFlux, Kafka, GraphQL, MCP, etc.) |
| Infrastructure - Driven Adapters | `infrastructure/driven-adapters/` | Adapter types (JPA, Mongo, R2DBC, Redis, S3, SQS, etc.) |
| Infrastructure - Helpers | `infrastructure/helpers/` | Shared utility modules |
| Application | `applications/app-service/` | Configuration classes, MainApplication |

### Step 4: Produce Context Summary

Generate a structured summary that other skills can consume:

```
## Project Context: [project_name]
- Type: reactive | imperative
- Package: co.com.example
- Java: 21
- Plugin: 4.6.1

### Domain Models
- Order (entity) → OrderGateway (interface)
- Customer (entity) → CustomerRepository (interface)

### Use Cases
- CreateOrderUseCase
- GetCustomerUseCase

### Entry Points
- reactive-web (WebFlux REST API)
- kafka-consumer

### Driven Adapters  
- jpa-repository
- rest-consumer (external API client)

### Helpers
- (none)
```

This context summary is then used by other skills to know:
- Where to place new classes
- Which gateway interfaces already exist for wiring
- What DTOs or models are available
- Whether the project is reactive or imperative (affects return types: `Mono<T>`/`Flux<T>` vs plain types)

---

## Feature: Execute Scaffold Tasks

When the user asks to generate or scaffold components, execute the corresponding task from `references/tasks/`. The available tasks are:

| # | Task | Gradle Command | Reference |
|---|------|----------------|-----------|
| 1 | Generate Project | `gradle cleanArchitecture` / `gradle ca` | `references/tasks/1-generate-project.md` |
| 2 | Generate Model | `gradle generateModel` / `gradle gm` | `references/tasks/2-generate-model.md` |
| 3 | Generate Use Case | `gradle generateUseCase` / `gradle guc` | `references/tasks/3-generate-use-case.md` |
| 4 | Generate Driven Adapter | `gradle generateDrivenAdapter` / `gradle gda` | `references/tasks/4-generate-driven-adapter.md` |
| 5 | Generate Entry Point | `gradle generateEntryPoint` / `gradle gep` | `references/tasks/5-generate-entry-point.md` |
| 6 | Generate Helper | `gradle generateHelper` / `gradle gh` | `references/tasks/6-generate-helper.md` |
| 7 | Generate Pipeline | `gradle generatePipeline` / `gradle gpl` | `references/tasks/7-generate-pipeline.md` |
| 8 | Generate Acceptance Test | `gradle generateAcceptanceTest` / `gradle gat` | `references/tasks/8-generate-acceptance-test.md` |
| 9 | Generate Performance Test | `gradle generatePerformanceTest` / `gradle gpt` | `references/tasks/9-generate-performance-test.md` |
| 10 | Validate Structure | `gradle validateStructure` / `gradle vs` | `references/tasks/10-validate-structure.md` |
| 11 | Delete Module | `gradle deleteModule` / `gradle dm` | `references/tasks/11-delete-module.md` |
| 12 | Update Project | `gradle updateCleanArchitecture` / `gradle u` | `references/tasks/12-update-project.md` |

**Before executing any task**, read the corresponding reference file for the full parameter specification, available types, and generated structure.

---

## Pre-requisites

Before starting, verify the user's environment:

- **Java 17+**: Run `java --version`
- **Gradle 9.2.1+**: Run `gradle --version`

---

## Instructions

### When Used as Context Provider (Analysis Mode)

1. Execute Steps 1–4 from "Analyze Project Structure" above.
2. Pass the context summary to the requesting skill/agent.
3. The requesting skill uses this context to:
   - Place code in the correct layer and package
   - Wire dependencies following the Dependency Rule
   - Use reactive types (`Mono<T>`, `Flux<T>`) if project type is `reactive`
   - Implement gateway interfaces defined in the domain when creating driven adapters
   - Create DTOs in the infrastructure layer, never in domain
   - Delegate business logic to use cases, keeping entry points thin

### When Used for Scaffolding (Execution Mode)

#### Creating a New Project

1. Verify the current directory is empty. If not, show an error and suggest generating components for an existing project.
2. Prompt for: `project_name`, `package_name`, `java_version`.
3. Create the initial `build.gradle`:
   ```bash
   cat > build.gradle <<EOF
   plugins {
       id 'co.com.bancolombia.cleanArchitecture' version '4.6.1'
   }
   EOF
   ```
4. Execute: `gradle cleanArchitecture --package=<package_name> --type=reactive --name=<project_name> --java-version=<java_version>`
5. All other parameters remain at their defaults.

#### Generating Components for an Existing Project

Read the corresponding `references/tasks/` file for the selected component type and follow its parameter specification. For each component:

1. Prompt the user for required parameters (type, name, options).
2. Execute the Gradle task with the user's parameters.
3. Ask if the user wants to generate logic/code for the component.
4. If yes, generate code following these rules:
   - **Models**: Generate fields, Lombok annotations (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
   - **Use Cases**: Wire existing models and gateways. Create missing gateways if needed. Use `Mono<T>`/`Flux<T>` for reactive projects. Include Javadoc. Do NOT annotate with `@Component`. Generate test class.
   - **Entry Points**: Wire existing use cases. Define DTOs for transport layer. Communicate with use cases using domain models only. Generate test class. For REST/WebFlux, generate OpenAPI spec (not Postman collections).
   - **Driven Adapters**: Implement domain gateway interfaces. Define internal DTOs for external communication. Communicate with use cases using domain models and Java primitives only. Generate test class.

### After Any Step Completed

- Run `gradle build -x test` to verify the project compiles (only if source files were changed/generated).
- Do NOT generate `docker-compose` files unless explicitly requested.
- Do NOT generate README files inside modules unless explicitly requested.
- Update the root `README.md` with relevant information about generated modules.

---

## Integration with Other Skills

This skill provides context for these common workflows:

| User Request | This Skill Provides | Other Skill Executes |
|---|---|---|
| "Migrate Java EE service to this project" | Layer mapping, package structure, existing gateways | `migrate-java-ee-business-logic-classes` |
| "Add unit tests for my use case" | Use case dependencies, model types, gateway mocks needed | `spring-boot-implement-unit-testing` |
| "Implement logging in this project" | Module structure, application.yml location | `spring-boot-implement-logback` |
| "Add OpenAPI docs to my entry point" | Entry point type, existing DTOs, route paths | `spring-boot-implement-openapi` |
| "Set up actuator monitoring" | Application module path, existing config | `spring-boot-implement-actuator` |
| "Migrate JPA entities to this project" | Driven adapter type, domain model mapping | `migrate-java-ee-db-entity-clases` |
| "Create a new Spring Boot project" | — (this skill IS the project creator for Clean Arch) | — |

---

## Key Conventions (for Context Consumers)

Any skill consuming this project's context MUST respect:

1. **Dependency Rule**: Domain layer has ZERO external dependencies. Use cases depend ONLY on models.
2. **Gateway Pattern**: Domain defines interfaces (ports). Infrastructure implements them (adapters).
3. **No DTOs in Domain**: Transport objects live in `entry-points` or `driven-adapters`, never in `domain/`.
4. **Reactive by Default**: If project type is `reactive`, all return types should use `Mono<T>` or `Flux<T>`.
5. **Thin Entry Points**: Controllers only map HTTP/messaging to use case calls. No business logic.
6. **Package Structure**: `[base-package].[module-name].[layer-specific-subpackage]`
7. **Testing**: Every generated component must have a corresponding test class mirroring the source structure.
