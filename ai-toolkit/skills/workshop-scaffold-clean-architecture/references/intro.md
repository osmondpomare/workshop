# Scaffold Clean Architecture

Scaffold Clean Architecture is a Gradle plugin developed by [Bancolombia](https://github.com/bancolombia) that generates projects pre-configured with the principles of Robert C. Martin's Clean Architecture. Instead of building a multimodule Gradle structure by hand, the plugin scaffolds it for you, along with dependencies and configuration that keep the architecture consistent as the project grows.

> "The purpose of Scaffold Clean Architecture Gradle Plugin is to provide a way to create a project with a clean architecture structure."

- Documentation: https://bancolombia.github.io/scaffold-clean-architecture/docs/intro
- Repository: https://github.com/bancolombia/scaffold-clean-architecture
- Gradle Plugin Portal: https://plugins.gradle.org/plugin/co.com.bancolombia.cleanArchitecture
- Background reading: [Clean Architecture: aislando los detalles (Medium)](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)
- Elixir variant: https://bancolombia.github.io/scaffold-clean-architecture-ex/

## Architecture

The plugin organizes generated code into three layers, with dependencies always pointing inward:

**Domain layer** (innermost)
- `model` — domain entities and interfaces (ports)
- `usecase` — application logic that orchestrates business flows

**Infrastructure layer**
- `entry-points` — REST controllers, Kafka consumers, and other application entry points
- `driven-adapters` — implementations for external systems (databases, APIs, file systems)
- `helpers` — shared utilities

**Application layer** (outermost)
- Assembles the modules together and resolves dependencies
- Contains the main application entry point

## Key Features

- **Dual project types** — supports both imperative (Spring Boot) and reactive (Spring WebFlux) architectures
- **Project Reactor integration** — uses `Flux<T>` and `Mono<T>` publishers with non-blocking back-pressure
- **Reactive Commons** — asynchronous messaging over RabbitMQ via a functional API
- **Secrets Manager support** — keeps external credentials in runtime configuration instead of hardcoded values
- **Usage analytics** (opt-in/opt-out) — collects OS, Java version, task execution, and project-type data to help improve the plugin

## Requirements

- Java JRE 17 or later
- Gradle 9.2.1 or later

## Getting Started

Add the plugin to your `build.gradle`:

```groovy
plugins {
    id 'co.com.bancolombia.cleanArchitecture' version '4.6.1'
}
```

Quick start (Mac OS/Linux):

```bash
mkdir scaffold-quick-start
cd scaffold-quick-start
echo "plugins { id 'co.com.bancolombia.cleanArchitecture' version '4.6.1' }" > build.gradle
gradle wrapper
./gradlew ca --name=ScaffoldQuickStart
./gradlew gep --type webflux
./gradlew bootRun
```

Windows users can follow the equivalent steps using `.\gradlew` (PowerShell or CMD).

Once the app is running, verify it at:

```
http://localhost:8080/api/usecase/path
```

Customize the response by editing the `Handler` class inside `entry-points/reactive-web`.

The plugin also provides tasks for generating models, use cases, adapters, entry points, helpers, pipelines, and tests, plus utilities for validating project structure and managing modules. See the [Tasks](https://bancolombia.github.io/scaffold-clean-architecture/docs/intro) section of the docs for the full list.

## Analytics

Telemetry can be toggled at any time:

```bash
gradle analytics --enabled true    # enable
gradle analytics --enabled false   # disable
```

Collected data includes OS details, Java version, plugin version, task execution metrics, and workspace information — it does not include your source code or other sensitive data.

## Versioning

The project follows [Semantic Versioning](https://semver.org/) (MAJOR.MINOR.PATCH), applied consistently across its Reactive Commons dependencies.

## Contributing

Contributions are welcome — see the Contributing section of the [official documentation](https://bancolombia.github.io/scaffold-clean-architecture/docs/intro) for guidelines.

## License

Licensed under the [Apache License 2.0](https://github.com/bancolombia/scaffold-clean-architecture/blob/main/LICENSE.txt).