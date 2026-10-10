---
sidebar_position: 5
---

# Generate Driven Adapter

The **`generateDrivenAdapter | gda`** task will generate a module in Infrastructure layer, this task has one required
parameter `type`.<br/>
Whether you'll use generic one also parameter `name` is required.

```shell
gradle generateDrivenAdapter --type=[drivenAdapterType]
gradle gda --type [drivenAdapterType]
```

| Type                     | Name                           | Parameter           | Values                                | Default               |
|--------------------------|--------------------------------|---------------------|---------------------------------------|-----------------------|
| **generic**              | Empty Driven Adapter           | `--name`            | String                                | -                     |
| **asynceventbus**        | Async Event Bus                | `--eda`             | `true`, `false`                       | `false`               |
|                          |                                | `--tech`            | `rabbitmq`, `kafka`, `rabbitmq,kafka` | `rabbitmq`            |
| **binstash**             | Bin Stash                      | `--cache-mode`      | `LOCAL`, `CENTRALIZED`, `HYBRID`      | `LOCAL`               |
| **cognitotokenprovider** | Cognito token generator        | -                   | -                                     | -                     |
| **dynamodb**             | Dynamo DB adapter              | -                   | -                                     | -                     |
| **jpa**                  | JPA Repository                 | `--secret`          | `true`, `false`                       | `false`               | 
| **kms**                  | AWS Key Management Service     | -                   | -                                     | -                     |
| **mongodb**              | Mongo Repository               | `--secret`          | `true`, `false`                       | `false`               |
| **mq**                   | JMS MQ Client to send messages | -                   | -                                     | -                     |
| **r2dbc**                | R2dbc Postgresql Client        | -                   | -                                     | -                     |
| **redis**                | Redis                          | `--mode`            | `template`, `repository`              | `template`            |
|                          |                                | `--secret`          | `true`, `false`                       | `false`               |
| **restconsumer**         | Rest Client Consumer           | `--url`             | String (URL)                          | -                     |
|                          |                                | `--from-swagger`    | File path                             | `swagger.yaml`        |
| **rsocket**              | RSocket Requester              | -                   | -                                     | -                     |
| **s3**                   | AWS Simple Storage Service     | -                   | -                                     | -                     |
| **secrets**              | AWS Secrets Manager    | `--secrets-backend` | `aws_secrets_manager`, `vault`        | `aws_secrets_manager` |
| **secretskafkastrimzi**  | Secrets for Kafka Strimzi      | `--secret-name`     | String                                | -                     |
| **sqs**                  | SQS message sender             | -                   | -                                     | -                     |

_**This task will generate something like that:**_

   ```bash
   📦infrastructure
   ┣ 📂driven-adapters
   ┃ ┗ 📂jpa-repository
   ┃ ┃ ┣ 📂src
   ┃ ┃ ┃ ┣ 📂main
   ┃ ┃ ┃ ┃ ┗ 📂java
   ┃ ┃ ┃ ┃ ┃ ┗ 📂[package]
   ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂jpa
   ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂config
   ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜DBSecret.java
   ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂helper
   ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜AdapterOperations.java
   ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JPARepository.java
   ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜JPARepositoryAdapter.java
   ┃ ┃ ┃ ┗ 📂test
   ┃ ┃ ┃ ┃ ┗ 📂java
   ┃ ┃ ┃ ┃ ┃ ┗ 📂[package]
   ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂jpa
   ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂helper
   ┃ ┃ ┗ 📜build.gradle
   ```
