#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9080/BCO.WKS.901.SEF/rest/services/backout/ReversalServices}"
CREDENTIALS="${CREDENTIALS:-wsuser:wspassword}"
MESSAGE_ID="${MESSAGE_ID:-MSG-$(date +%s)}"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

REQUEST=$(sed "s/\"MSG-0001\"/\"${MESSAGE_ID}\"/" "${ROOT_DIR}/requests/registerReversal.json")

echo "== registerReversal (${MESSAGE_ID})"
curl -s -u "${CREDENTIALS}" -H "Content-Type: application/json" -X POST \
  -d "${REQUEST}" "${BASE_URL}/registerReversal"
echo

sleep 2

echo "== getReversal (${MESSAGE_ID})"
curl -s -u "${CREDENTIALS}" -H "channel: MF-001" "${BASE_URL}/getReversal/${MESSAGE_ID}"
echo

echo "== PostgreSQL row"
docker exec wks-postgres psql -U workshop -d workshop -c \
  "SELECT id_mensaje, valor, moneda, tasa_cambio, valor_cop, estado FROM reverso WHERE id_mensaje='${MESSAGE_ID}';"

echo "== DynamoDB item"
docker exec wks-localstack awslocal dynamodb get-item --region us-east-1 \
  --table-name BCO_REVERSAL_EVENTS --key "{\"messageId\":{\"S\":\"${MESSAGE_ID}\"}}"
