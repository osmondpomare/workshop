#!/bin/bash
set -e
awslocal dynamodb create-table \
  --table-name BCO_REVERSAL_EVENTS \
  --attribute-definitions AttributeName=messageId,AttributeType=S \
  --key-schema AttributeName=messageId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1 || true
