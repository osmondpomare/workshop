# Full WorkShop Commands

## Init Infrastrucuture and WAS Legacy Service
cd workshop
docker compose up --build

### Create WAS Reverse
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

### Get WAS Reverse
curl --location 'http://localhost:9080/BCO.WKS.901.SEF/rest/services/backout/ReversalServices/getReversal/MSG-0001' \
--header 'channel: MF-001' \
--header 'Cookie: LtpaToken2=7c9Yf1fpeVg5f+CoxXWYxSO4Cw3/0wzNZXJ3pqJrPGL1F8sKkZeNKYgVUIWI6qTZ9EFqOYynNu+HT9FlqjywOXrzyjYykfF3yWs3/m6KW52D2aiwFr2y2pOI/E42MBhEaD82c7Gr4ezzWCVkufgrHNR58PzpgDJSd3n3Y4iPftmMSmlLl/7v/C1nY6RhtiTFVFjQK7yjukKy0EwFIxlCq39yPfGHtJWFHxccd1ToQ/9UHs8Jgg+8RrFvC7zmU/M1ecK31u7aouKRwduopY9zBK7fu/Tr4LMMEexTShSLWOef2RorIk8XZavVbN8J4cmo'


### Validate Health Check Service Migrated
curl --location 'http://localhost:8080/actuator/health'

### Create Spring Boot Reverse
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

### Get Spring Boot Reverse
curl --location 'http://localhost:8080/services/backout/ReversalServices/getReversal/MSG-0003' \
--header 'channel: MF-001'

## Down Infrastrucuture and WAS Legacy Service
docker compose down -v