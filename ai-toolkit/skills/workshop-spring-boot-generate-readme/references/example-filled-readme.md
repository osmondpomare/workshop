# Example: Fully Populated README (reference)

This is a real, fully filled-in README following the template in `readme-template.md`, taken from a the company Spring Boot 4 / clean-architecture reactive microservice (`cocorebankint_authorize_ms`). Use it to calibrate tone, level of detail, and formatting — not to copy literal values (hostnames, table names, ports) into a different project.

---

# Estructura Recomendada para README de Aplicaciones Java

## Título del Proyecto
cocorebankint_authorize_ms

## Descripción
Una descripción breve y concisa del proyecto Java, explicando su propósito principal.

## Arquitectura de la Solución
Incluir una imagen de la arquitectura del proyecto y, si existe, un enlace a la documentación wiki de la arquitectura.

- Componentes principales y sus interacciones
- Flujo de datos
- Integraciones con otros sistemas

## Stack Tecnológico
> **Instrucciones**: Listar solo las tecnologías principales utilizadas en el proyecto. Para cada tecnología, especificar la versión exacta utilizada. Eliminar las tecnologías que no apliquen y añadir las que falten según corresponda.

- Java (Java 25)
- Framework principal (Spring Boot, Jakarta EE, Quarkus, Micronaut)
- Gestión de dependencias (Gradle)
- Bases de datos (PostgreSQL, MySQL, Oracle, MongoDB)
- ORM (Hibernate, JPA, MyBatis)
- Servicios en la nube AWS (específicos)

> **Nota**: Incluir "Librerías principales" solo si el proyecto utiliza librerías personalizadas proporcionadas por otros equipos.

## Enlaces Importantes
- [SonarCloud](https://sonarcloud.io/project/overview?id=nombre-del-proyecto)
- [FluidAttacks Group](https://app.fluidattacks.com/groups/nombre-del-grupo)
- [Documentación JavaDoc](#)
- [Wiki del proyecto](#)
- [Repositorio de artefactos (Nexus/Artifactory)](#)
- [Repositorio de Infraestructura como Código (IaC)](#)
- [OpenSearch Dashboard](https://opensearch.example.com) - Monitoreo y análisis de logs
- [Dynatrace](https://dynatrace.example.com) - Monitoreo de rendimiento de aplicaciones
- [Grafana](https://grafana.example.com) - Visualización de métricas y dashboards

## Prerrequisitos
Software y herramientas necesarias para ejecutar el proyecto:
- JDK (versión específica)
- Gradle (versión específica)
- Docker y Docker Compose
- Acceso a servicios/recursos necesarios

## Variables de Entorno
> **Nota**: Las siguientes variables se encuentran configuradas en el archivo `application.yml` del proyecto.

| Variable | Descripción | Valor por defecto | Requerido |
|----------|-------------|-------------------|-----------|
| DB_ORACLE_HOST | URL de conexión a la base de datos Oracle | *Solicitar a the company* | Sí |
| DB_ORACLE_PORT | Puerto de conexión a la base de datos Oracle | *Solicitar a the company* | Sí |
| DB_ORACLE_DATABASE | Esquema de base de datos Oracle | *Solicitar a the company* | Sí |
| DB_ORACLE_USERNAME | Usuario de la base de datos Oracle | *Solicitar a the company* | Sí |
| DB_ORACLE_PASSWORD | Contraseña de usuario de la base de datos Oracle | *Solicitar a the company* | Sí |
| DB_ORACLE_POOL_INITIAL_SIZE | Número inicial de conexiones creadas en el pool de Oracle | 5 | Sí |
| DB_ORACLE_POOL_MAX_SIZE | Número máximo de conexiones permitidas en el pool de Oracle | 15 | Sí |
| DB_ORACLE_POOL_MAX_IDLE_TIME | Tiempo máximo que una conexión puede permanecer inactiva antes de ser cerrada | 300s | Sí |
| DB_ORACLE_POOL_MAX_LIFE_TIME | Tiempo máximo de vida de una conexión en el pool antes de ser renovada | 900s | Sí |
| DB_ORACLE_POOL_MAX_ACQUIRE_TIME | Tiempo máximo de espera para obtener una conexión del pool | 3s | Sí |
| DB_ORACLE_POOL_MAX_CREATE_CONNECTION_TIME | Tiempo máximo permitido para crear una nueva conexión a la base de datos | 5s | Sí |
| DB_POSTGRESQL_HOST | Host de conexión a la base de datos PostgreSQL | *Solicitar a the company* | Sí |
| DB_POSTGRESQL_PORT | Puerto de conexión a la base de datos PostgreSQL | *Solicitar a the company* | Sí |
| DB_POSTGRESQL_DATABASE | Nombre de la base de datos PostgreSQL | *Solicitar a the company* | Sí |
| DB_POSTGRESQL_SCHEMA | Esquema de la base de datos PostgreSQL | *Solicitar a the company* | Sí |
| DB_POSTGRESQL_USERNAME | Usuario de la base de datos PostgreSQL | *Solicitar a the company* | Sí |
| DB_POSTGRESQL_PASSWORD | Contraseña de usuario de la base de datos PostgreSQL | *Solicitar a the company* | Sí |
| DB_POSTGRESQL_POOL_INITIAL_SIZE | Número inicial de conexiones creadas en el pool de PostgreSQL | 5 | Sí |
| DB_POSTGRESQL_POOL_MAX_SIZE | Número máximo de conexiones permitidas en el pool de PostgreSQL | 15 | Sí |
| DB_POSTGRESQL_POOL_MAX_IDLE_TIME | Tiempo máximo que una conexión puede permanecer inactiva antes de ser cerrada | 300s | Sí |
| DB_POSTGRESQL_POOL_MAX_LIFE_TIME | Tiempo máximo de vida de una conexión en el pool antes de ser renovada | 900s | Sí |
| DB_POSTGRESQL_POOL_MAX_ACQUIRE_TIME | Tiempo máximo de espera para obtener una conexión del pool | 3s | Sí |
| DB_POSTGRESQL_POOL_MAX_CREATE_CONNECTION_TIME | Tiempo máximo permitido para crear una nueva conexión a la base de datos | 5s | Sí |
| TRANSACTION_TAX_FOUR_PER_THOUSAND | Impuesto del 4x1000 aplicado a las transacciones | 0.004 | Sí |
| TRANSACTION_MINIMUM_LIMI | Límite mínimo de transacción permitido | 10000 | Sí |
| TRANSACTION_MAXIMUM_LIMIT | Límite máximo de transacción permitido | 2000000 | Sí |
| CUSTOMER_HOST | URL base del servicio de clientes | *Solicitar a the company* | Sí |
| CUSTOMER_PATH | Path del endpoint de clientes | /api/v1/customers/{region}/{number} | Sí |
| CUSTOMER_CONNECTION_TIMEOUT | Timeout de conexión al servicio de clientes | 1000 | Sí |
| CUSTOMER_READ_TIMEOUT | Timeout de lectura del servicio de clientes | 3000 | Sí |
| CUSTOMER_WAS_USER | Usuario de autenticación para el servicio de clientes | *Solicitar a the company* | Sí |
| CUSTOMER_WAS_PASSWORD | Contraseña de autenticación para el servicio de clientes | *Solicitar a the company* | Sí |
| METRICS_ENABLE_ALL | Habilita o deshabilita todas las métricas | false | Si |
| METRICS_ENABLE_SYSTEM | Habilita o deshabilita las métricas del sistema | false | Si |
| METRICS_ENABLE_PROCESS | Habilita o deshabilita las métricas del proceso | false | Si |
| METRICS_ENABLE_APPLICATION | Habilita o deshabilita las métricas de la aplicación | false | Si |
| METRICS_ENABLE_DISK | Habilita o deshabilita las métricas de disco | false | Si |
| METRICS_ENABLE_JVM | Habilita o deshabilita las métricas de la JVM | true | Si |
| METRICS_ENABLE_HTTP_SERVER_REQ | Habilita o deshabilita las métricas de peticiones HTTP del servidor | false | Si |
| METRICS_ENABLE_LOGBACK | Habilita o deshabilita las métricas de Logback | false | Si |
| OTEL_COLLECTOR_ENABLED | Habilita o deshabilita la exportación de métricas al colector OTLP | false | Si |
| OTEL_COLLECTOR_HOST | URL del colector OpenTelemetry | http://localhost:4318 | Si |
| OTEL_COLLECTOR_STEP | Intervalo de tiempo entre cada envío de métricas al colector | 1m | Si |
| OTEL_COLLECTOR_AUTH_HEADER | Header de autorización para el colector OpenTelemetry | "" | Si |
| SERVICE_INSTANCE_ID | Identificador de la instancia del servicio en OpenTelemetry | corebankint-co-balance-local | Si |
| SERVICE_ENV | Namespace y ambiente de ejecución del servicio en OpenTelemetry | local | Si |
| CAPACITY | Capacidad del servicio en OpenTelemetry | corebankint | Si |
| SERVICE_ID | Identificador del servicio en OpenTelemetry | NQ000 | Si |
| COUNTRY | País del servicio en OpenTelemetry | co | Si |
| BIA | Indicador BIA del servicio en OpenTelemetry | false | Si |
| OTEL_TRACING_ENABLED | Habilita o deshabilita la exportación de trazas | false | Si |
| OTEL_TRACING_SAMPLING | Probabilidad de muestreo para trazas de OpenTelemetry (0.0 a 1.0) | 0.1 | Si |
| REGION | Región del servicio en OpenTelemetry | us-east-1 | Si |
| COMPONENT_TYPE | Tipo de componente del servicio en OpenTelemetry | ecs | Si |

## Instalación y Configuración
Esta sección debe incluir instrucciones paso a paso para configurar el proyecto:

```bash
# Configurar variables de entorno según la sección correspondiente

# Compilar el proyecto con Gradle
./gradlew build
```

## Configuración de Base de Datos Local
Para configurar la base de datos PostgreSQL local, primero iniciar el contenedor con Docker:

```shell
sudo docker run -d \
--name postgres \
--network pg-net \
-p 5432:5432 \
-e POSTGRES_DB=appdb \
postgres:15                              
```
### Crear tablas PostgreSQL

```sql
-- Tabla de balances de autorización
CREATE TABLE auth_balance (
    account_number VARCHAR(20) NOT NULL PRIMARY KEY,
    id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    phone_number VARCHAR(20) NOT NULL,
    balance DECIMAL NOT NULL,
    original_balance DECIMAL NOT NULL,
    available_debit_limit DECIMAL NOT NULL,
    available_credit_limit DECIMAL NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT now(),
    updated_date TIMESTAMP
);

-- Tabla de transacciones de autorización (particionada por hash de account_number)
CREATE TABLE auth_transactions (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY,
    phone_number VARCHAR(20) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(40) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    amount DECIMAL NOT NULL,
    transaction_code VARCHAR(40) NOT NULL,
    xml_information TEXT NOT NULL,
    reversed BOOLEAN NOT NULL,
    mark_as_synchronized BOOLEAN,
    transaction_confirm_code VARCHAR(20) NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT now(),
    updated_date TIMESTAMP,
    PRIMARY KEY (transaction_id, transaction_code, account_number)
) PARTITION BY HASH (account_number);
```

> **Nota**: Para consultar scripts adicionales de base de datos (migraciones, particiones, índices, etc.), visitar el repositorio [cocorebankint_authorizer_DB](https://github.com/CompanyTI/cocorebankint_authorizer_DB).

## Ejecución del Proyecto
Instrucciones para ejecutar el proyecto localmente:

```bash
# Ejecutar con Gradle
./gradlew bootRun

# Ejecutar JAR generado
java -jar build/libs/nombre-aplicacion.jar

# Ejecutar con Docker
docker-compose up
```

## Pruebas
Instrucciones para ejecutar las pruebas:

```bash
# Ejecutar todas las pruebas
./gradlew test

# Verificar cobertura con JaCoCo
./gradlew jacocoTestReport
```

## Endpoints API
> **Nota importante**: El siguiente endpoint es un ejemplo ilustrativo.

### POST /api/recursos - Crear nuevo recurso

**Headers requeridos:**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body ejemplo:**
```json
{
  "nombre": "Nuevo Recurso",
  "descripcion": "Descripción del nuevo recurso",
  "atributos": {
    "color": "verde",
    "tamaño": "mediano"
  }
}
```

**Ejemplo curl:**
```bash
curl -X POST \
  http://localhost:8080/api/recursos \
  -H 'Authorization: Bearer {token}' \
  -H 'Content-Type: application/json' \
  -d '{
    "nombre": "Nuevo Recurso",
    "descripcion": "Descripción del nuevo recurso",
    "atributos": {
      "color": "verde",
      "tamaño": "mediano"
    }
  }'
```

**Respuesta exitosa (201 Created):**
```json
{
  "id": 3,
  "nombre": "Nuevo Recurso",
  "descripcion": "Descripción del nuevo recurso",
  "fechaCreacion": "2025-06-09T15:45:30Z",
  "atributos": {
    "color": "verde",
    "tamaño": "mediano"
  }
}
```

**Códigos de error:**
| Código | Descripción |
|--------|-------------|
| 400 | Solicitud incorrecta (datos faltantes o inválidos) |
| 401 | No autorizado (token inválido o expirado) |
| 403 | Prohibido (sin permisos suficientes) |
| 409 | Conflicto (el recurso ya existe) |
| 500 | Error interno del servidor |

## Documentación API
Para consultar la documentación completa de la API, por favor refiérase al archivo `.metadata/openapi.yaml` que contiene la especificación OpenAPI.

Este archivo ha sido creado siguiendo las recomendaciones detalladas en:
[Configuración y Uso de SpringDoc para APIs](https://dev.azure.com/MyOrg/the company/_wiki/wikis/the company.wiki/445788/Configuracion-y-Uso-de-SpringDoc-para-APIs)

Endpoints disponibles en entorno local:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Despliegue
Instrucciones para desplegar en diferentes entornos:

### Despliegue con Docker
```bash
docker build -t nombre-proyecto:version .
docker run -p 8080:8080 nombre-proyecto:version
```

Para instrucciones detalladas sobre el despliegue en entornos de producción, consultar el [repositorio de IaC](#) donde se mantiene la configuración de infraestructura.

## Contacto
> **Nota importante**: En esta sección se debe hacer referencia al equipo responsable del soporte del proyecto. No incluir nombres de personas específicas.

**Ejemplo**:
```
Equipo responsable: Equipo de Desarrollo 
Canal de contacto: Canal de Teams del equipo si existe
```
