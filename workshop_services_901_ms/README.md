# workshop_services_901_ms — Reversal (Backout) Services

Microservicio reactivo (Spring WebFlux + Project Reactor) que implementa los servicios de
**reverso (backout) de transacciones** `ReversalServices` (WKS901). Es la migración del proyecto
legado Java EE 7 **BCO-WorkshopServices.901** (desplegado sobre Open Liberty) a Spring Boot 4.x con
arquitectura **Scaffold Clean Architecture**.

El servicio permite registrar un reverso con conversión de moneda (tasa de cambio externa),
consultar el estado de un reverso y confirmar de forma asíncrona la notificación del reverso.

## Prerrequisitos

- Java 25
- Gradle 9.2.1+ (incluye wrapper `./gradlew`, distribución 9.7.1)
- PostgreSQL (persistencia reactiva vía R2DBC)
- DynamoDB (auditoría de eventos de reverso)
- Cola de mensajería SQS (notificación asíncrona de reverso)
- Servicio externo de tasas de cambio (por defecto `https://open.er-api.com`)

## Stack Tecnológico

- Spring Boot 4.1.x (Spring Framework 7)
- Spring WebFlux + Project Reactor (`Mono`/`Flux`)
- Spring Data R2DBC (PostgreSQL) con `DatabaseClient` y SQL nativo
- AWS SDK v2 (DynamoDB async, SQS async)
- MapStruct 1.6.3 (mapeo DTO ↔ dominio en adapters)
- SpringDoc OpenAPI 3.0.0 (WebFlux)
- Logback + logstash-logback-encoder (logs JSON estructurados con enmascaramiento de PII)
- Micrometer + Prometheus (métricas) y Spring Boot Actuator

## Arquitectura (Scaffold Clean Architecture)

Las dependencias apuntan siempre hacia el dominio.

```
domain/
├── model/        Modelos de dominio + gateways (puertos)
│   └── model/{reverso, parametro, reversal, enums, exceptions, utils}
└── usecase/      Casos de uso (lógica de negocio reactiva)
    └── usecase/{registerreversal, queryreversal, notifyreversal}
infrastructure/
├── entry-points/
│   ├── reactive-web/   Controlador WebFlux (REST)
│   └── sqs-listener/   Listener SQS (consumidor de notificaciones)
├── driven-adapters/
│   ├── r2dbc-postgresql/  Persistencia REVERSO / PARAMETRO
│   ├── rest-consumer/     Cliente WebClient de tasas de cambio
│   ├── dynamo-db/         Almacén de eventos de reverso
│   └── sqs-sender/        Publicador de notificaciones de reverso
└── helpers/metrics/       Publicador de métricas Micrometer
applications/
└── app-service/           Ensamblaje, main, configuración, logging
```

### Mapeo con el proyecto legado

| Legado (Java EE) | Migrado (Reactivo) |
|------------------|--------------------|
| `ReversalService` (JAX-RS) | `ReversalController` (`@RestController` WebFlux) |
| `MdbNotifyReversalBean` (JMS MDB) | `SQSProcessor` (listener SQS) |
| `RegisterReversalBean` | `RegisterReversalUseCase` |
| `QueryReversalBean` | `QueryReversalUseCase` |
| `NotifyReversalBean` | `NotifyReversalUseCase` |
| `ReversoJPAServiceIMPL` / `EntityManager` | `ReversoAdapter` (R2DBC `DatabaseClient`) |
| `ParameterJPAServiceIMPL` | `ParametroAdapter` (R2DBC) |
| `ExchangeRateUtil` + `ServiceBroker` | `ExchangeRateAdapter` (WebClient) |
| `DynamoDBUtil` (AWS SDK v1) | `ReversalEventAdapter` (AWS SDK v2 async) |
| `GenericSendJMSMesssageBean` (JMS) | `SQSSender` (SQS async) |

## Endpoints

Ruta base: `/services/backout/ReversalServices`

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/registerReversal` | Registra un reverso. Body: `MessageRQ`. Respuesta: `MessageRS` con `RegisterReversalRSType`. |
| GET | `/getReversal/{messageId}` | Consulta un reverso por `messageId` (header `channel`). Respuesta: `MessageRS` con `GetReversalRSType`. |

> Las respuestas siguen el contrato legado: HTTP 200 siempre, con el código de estado de negocio
> dentro del cuerpo (`responseMessage.header.status.statusCode`).

Especificación OpenAPI disponible en `/api-spec` (Swagger UI deshabilitado).

### Catálogo de códigos de estado

| Código | Descripción |
|--------|-------------|
| `0` | SUCCESS |
| `WKS901-001` | Mensaje de solicitud inválido |
| `WKS901-002` | Ya existe un reverso para este messageId |
| `WKS901-003` | No fue posible obtener la tasa de cambio |
| `WKS901-004` | Error de base de datos |
| `WKS901-005` | Error registrando el evento de reverso |
| `WKS901-006` | Error enviando la notificación de reverso |
| `WKS901-007` | Reverso no encontrado |
| `WKS901-999` | Error inesperado |

## Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `DB_HOST` | Host PostgreSQL | `localhost` |
| `DB_PORT` | Puerto PostgreSQL | `5432` |
| `DB_NAME` | Nombre de base de datos | `workshop` |
| `DB_SCHEMA` | Esquema | `public` |
| `DB_USER` | Usuario de base de datos | `workshop` |
| `DB_PASSWORD` | Contraseña de base de datos | `workshop` |
| `EXCHANGE_RATE_BASE_URL` | URL base del servicio de tasas | `https://open.er-api.com` |
| `EXCHANGE_RATE_TIMEOUT` | Timeout (ms) del cliente de tasas | `10000` |
| `AWS_REGION` | Región AWS | `us-east-1` |
| `AWS_ACCESS_KEY_ID` | Access key AWS (opcional, si no se usa cadena de credenciales por defecto) | *(vacío)* |
| `AWS_SECRET_ACCESS_KEY` | Secret key AWS (opcional) | *(vacío)* |
| `DYNAMO_ENDPOINT` | Endpoint DynamoDB (local/LocalStack) | *(vacío)* |
| `DYNAMO_REVERSAL_TABLE` | Tabla de eventos de reverso | `BCO_REVERSAL_EVENTS` |
| `SQS_ENDPOINT` | Endpoint SQS (local/LocalStack) | `http://localhost:4566` |
| `NOTIFY_REVERSAL_QUEUE_URL` | URL de la cola de notificación de reverso | `http://localhost:4566/000000000000/BCO-NotifyReversal` |
| `SQS_WAIT_TIME_SECONDS` | Long polling (s) | `20` |
| `SQS_VISIBILITY_TIMEOUT_SECONDS` | Visibility timeout (s) | `30` |
| `SQS_MAX_MESSAGES` | Máximo de mensajes por lote | `10` |
| `SQS_THREADS` | Hilos de consumo | `1` |

### Parámetros de negocio (tabla `PARAMETRO`)

| Código | Descripción | Default |
|--------|-------------|---------|
| `BCO_WKS901_MONEDA_DESTINO` | Moneda destino para la conversión | `COP` |
| `BCO_WKS901_MAX_REINTENTOS` | Reintentos máximos de notificación | `3` |

## Flujo de Registro de Reverso

1. Validar la solicitud (campos requeridos, moneda `^[A-Z]{3}$`, monto > 0).
2. Verificar que no exista un reverso para el `messageId` (idempotencia).
3. Resolver la moneda destino desde `PARAMETRO` (default `COP`).
4. Obtener la tasa de cambio (servicio externo; escala 6).
5. Persistir el reverso (`valorCop = monto × tasa`, escala 2, estado `REGISTERED`).
6. Registrar el evento en DynamoDB (`putItem` condicional).
7. Publicar la notificación en SQS.

El consumidor SQS actualiza el estado a `NOTIFIED` y actualiza el evento en DynamoDB.

## Compilar, Probar y Ejecutar

```bash
# Compilar
./gradlew build -x test

# Ejecutar pruebas unitarias
./gradlew test

# Build completo (compila, pruebas, ArchitectureTest, validateStructure)
./gradlew build

# Ejecutar la aplicación
./gradlew :app-service:bootRun
```

La aplicación expone el puerto `8080`.

## Observabilidad

- Logs: JSON estructurado (`logback.xml`) con enmascaramiento de PII
  (`password`, `otp`, `token`, `idNumber`, `documentNumber`, `phoneNumber`, `numeroCelular`, `clientId`).
- Correlación: `CorrelationWebFilter` propaga `x-message-id` y `channel` a MDC y Reactor Context.
- Actuator: `health`, `info`, `metrics`, `prometheus` expuestos en `/actuator/*`.

## Pruebas

El proyecto incluye pruebas unitarias reactivas con JUnit 5, Mockito y `StepVerifier`
(casos de uso, controlador con `WebTestClient`, adapters, listener SQS y mappers), además de
`ArchitectureTest` (reglas de Clean Architecture) y `validateStructure`.

## DevOps

El proyecto incluye `deployment/Dockerfile` generado por el scaffold. El proyecto legado no contenía
`Jenkinsfile` ni `azure-pipelines.yml`; la configuración de pipeline queda _pendiente por definir_.
