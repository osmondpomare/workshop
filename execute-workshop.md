# Full Workshop Commands

## Init Infrastructure and WAS Legacy Service

```bash
docker compose up --build
```

### Create WAS Reversal

```bash
curl --location 'http://localhost:9080/BCO.WKS.901.SEF/rest/services/backout/ReversalServices/registerReversal' \
--header 'Content-Type: application/json' \
--header 'Cookie: LtpaToken2=7c9Yf1fpeVg5f+CoxXWYxSO4Cw3/0wzNZXJ3pqJrPGL1F8sKkZeNKYgVUIWI6qTZ9EFqOYynNu+HT9FlqjywOXrzyjYykfF3yWs3/m6KW52D2aiwFr2y2pOI/E42MBhEaD82c7Gr4ezzWCVkufgrHNR58PzpgDJSd3n3Y4iPftmMSmlLl/7v/C1nY6RhtiTFVFjQK7yjukKy0EwFIxlCq39yPfGHtJWFHxccd1ToQ/9UHs8Jgg+8RrFvC7zmU/M1ecK31u7aouKRwduopY9zBK7fu/Tr4LMMEexTShSLWOef2RorIk8XZavVbN8J4cmo' \
--header 'Authorization: Basic d3N1c2VyOndzcGFzc3dvcmQ=' \
--data '{
    "requestMessage": {
        "header": {
            "channel": "MF-001",
            "messageId": "MSG-0001",
            "clientId": "3001234567",
            "destination": {
                "serviceName": "ReversalServices",
                "serviceOperation": "registerReversal",
                "serviceRegion": "C001",
                "serviceVersion": "1.0.0"
            }
        },
        "body": {
            "registerReversalRQ": {
                "transactionId": "TX-1001",
                "phoneNumber": "3001234567",
                "amount": "25.50",
                "currency": "USD",
                "reason": "Transfer rejected by provider"
            }
        }
    }
}'
```

### Get WAS Reversal

```bash
curl --location 'http://localhost:9080/BCO.WKS.901.SEF/rest/services/backout/ReversalServices/getReversal/MSG-0001' \
--header 'channel: MF-001' \
--header 'Cookie: LtpaToken2=7c9Yf1fpeVg5f+CoxXWYxSO4Cw3/0wzNZXJ3pqJrPGL1F8sKkZeNKYgVUIWI6qTZ9EFqOYynNu+HT9FlqjywOXrzyjYykfF3yWs3/m6KW52D2aiwFr2y2pOI/E42MBhEaD82c7Gr4ezzWCVkufgrHNR58PzpgDJSd3n3Y4iPftmMSmlLl/7v/C1nY6RhtiTFVFjQK7yjukKy0EwFIxlCq39yPfGHtJWFHxccd1ToQ/9UHs8Jgg+8RrFvC7zmU/M1ecK31u7aouKRwduopY9zBK7fu/Tr4LMMEexTShSLWOef2RorIk8XZavVbN8J4cmo'
```

### Install kiro
- Link shared on session
- https://kiro.dev/docs/cli/
- kiro-cli login --use-device-flow


### Install Toolkit

```bash
cd ai-toolkit
./install-toolkit.sh kiro
./install-agents.sh kiro
```

### Run Migration Process

```bash
cd prompts/migration
kiro-cli
/agents
```

Then:

1. Select the agent created before.
2. Copy the content from `execute-example.md`.
3. Execute and wait for completion.

### Validate the Result

1. Open the result in your IDE.
2. Execute the code from the IDE.

### Validate Health Check of Migrated Service

```bash
curl --location 'http://localhost:8080/actuator/health'
```

### Create Spring Boot Reversal

```bash
curl --location 'http://localhost:8080/services/backout/ReversalServices/registerReversal' \
--header 'Content-Type: application/json' \
--data '{
    "requestMessage": {
        "header": {
            "channel": "MF-001",
            "messageId": "MSG-0003",
            "clientId": "3001234567",
            "destination": {
                "serviceName": "ReversalServices",
                "serviceOperation": "registerReversal",
                "serviceRegion": "C001",
                "serviceVersion": "1.0.0"
            }
        },
        "body": {
            "registerReversalRQ": {
                "transactionId": "TX-1001",
                "phoneNumber": "3001234567",
                "amount": "25.50",
                "currency": "USD",
                "reason": "Transfer rejected by provider"
            }
        }
    }
}'
```

### Get Spring Boot Reversal

```bash
curl --location 'http://localhost:8080/services/backout/ReversalServices/getReversal/MSG-0003' \
--header 'channel: MF-001'
```

## Down Infrastructure and WAS Legacy Service

```bash
docker compose down -v
```
