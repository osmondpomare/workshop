# the company Standard README Template (Spring Boot / Clean Architecture)

This is the canonical structure to produce for Java/Spring Boot microservices at the company (Gradle
Clean Architecture plugin or Maven layered projects). Copy this skeleton, then replace every
`{{PLACEHOLDER}}` with a value discovered in the project (see `data-sources.md`). Never leave a
`{{PLACEHOLDER}}` token in the final file — either fill it, or replace it with the literal marker
`*Solicitar a the company*` (secrets/credentials with no default) or `_Pendiente por definir_` (info that
truly cannot be derived from the repo, e.g. an architecture diagram image).

Keep section order and headings exactly as below. Omit a section only if it is genuinely not
applicable (e.g. no local database) — never omit "Variables de Entorno", "Instalación y
Configuración", "Ejecución del Proyecto" or "Pruebas".

```markdown
# {{PROJECT_TITLE}}

## Descripción
{{ONE_TO_TWO_PARAGRAPH_DESCRIPTION}}

## Arquitectura de la Solución
Incluir una imagen de la arquitectura del proyecto y, si existe, un enlace a la documentación wiki de la arquitectura.

- Componentes principales y sus interacciones
- Flujo de datos
- Integraciones con otros sistemas

{{ARCHITECTURE_NOTES_FROM_MODULE_LAYOUT}}

## Stack Tecnológico
- Java ({{JAVA_VERSION}})
- {{MAIN_FRAMEWORK_AND_VERSION}}
- {{DEPENDENCY_MANAGEMENT_TOOL_AND_VERSION}}
- {{DATABASES_DETECTED}}
- {{ORM_OR_DATA_ACCESS_DETECTED}}
- {{AWS_OR_CLOUD_SERVICES_DETECTED}}

<!-- Only include "Librerías principales" if custom shared libraries (internal the company artifacts) are used -->

## Enlaces Importantes
- [SonarCloud](#)
- [FluidAttacks Group](#)
- [Documentación JavaDoc](#)
- [Wiki del proyecto](#)
- [Repositorio de artefactos (Nexus/Artifactory)](#)
- [Repositorio de Infraestructura como Código (IaC)](#)
{{ADDITIONAL_LINKS_FROM_METADATA}}

## Prerrequisitos
- JDK {{JAVA_VERSION}}
- {{BUILD_TOOL}} {{BUILD_TOOL_VERSION}}
- Docker y Docker Compose
- {{OTHER_REQUIRED_ACCESS_OR_TOOLING}}

## Variables de Entorno
> **Nota**: Las siguientes variables se encuentran configuradas en {{CONFIG_FILE_NAMES}}.

| Variable | Descripción | Valor por defecto | Requerido |
|----------|-------------|-------------------|-----------|
{{ENV_VAR_TABLE_ROWS}}

## Instalación y Configuración
```bash
# Configurar variables de entorno según la sección correspondiente

{{BUILD_COMMAND}}
```

## Configuración de Base de Datos Local
<!-- Omit this whole section if the project has no database -->
{{LOCAL_DATABASE_SETUP_STEPS}}

> **Nota**: Para consultar scripts adicionales de base de datos, visitar {{DB_MIGRATION_REPO_OR_FOLDER}}.

## Ejecución del Proyecto
```bash
{{RUN_COMMANDS}}
```

## Pruebas
```bash
{{TEST_COMMANDS}}
```

## Endpoints API
{{ENDPOINTS_FROM_OPENAPI_OR_CONTROLLERS}}

## Documentación API
Para consultar la documentación completa de la API, por favor refiérase al archivo `{{OPENAPI_SPEC_PATH}}` que contiene la especificación OpenAPI.

Endpoints disponibles en entorno local:
- Swagger UI: `{{SWAGGER_UI_URL}}`
- OpenAPI JSON: `{{OPENAPI_JSON_URL}}`

## Despliegue
### Despliegue con Docker
```bash
docker build -t {{PROJECT_NAME}}:version .
docker run -p {{APP_PORT}}:{{APP_PORT}} {{PROJECT_NAME}}:version
```

{{DEPLOYMENT_PIPELINE_NOTES}}

## Contacto
> No incluir nombres de personas específicas, solo el equipo/canal responsable.

**Ejemplo**:
```
Equipo responsable: {{OWNING_TEAM}}
Canal de contacto: {{CONTACT_CHANNEL}}
```
```

## Notes on tone and depth
- Written in Spanish, matching the corporate documentation style used across the company microservice
  repositories.
- Concise and factual — this is operational documentation for engineers who will run, deploy, or
  debug the service, not marketing copy.
- Every command block must be copy-pasteable exactly as written (correct working directory,
  correct flags for the detected build tool).
- Tables (env vars, error codes) must stay valid GitHub-flavored Markdown.
