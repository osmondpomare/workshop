#!/bin/bash
set -e
awslocal sqs create-queue \
  --queue-name BCO-NotifyReversal \
  --region us-east-1 || true
