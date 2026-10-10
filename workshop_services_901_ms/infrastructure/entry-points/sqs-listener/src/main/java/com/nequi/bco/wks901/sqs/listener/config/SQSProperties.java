package com.nequi.bco.wks901.sqs.listener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "entrypoint.sqs")
public record SQSProperties(
        String region,
        String endpoint,
        String queueUrl,
        int waitTimeSeconds,
        int visibilityTimeoutSeconds,
        int maxNumberOfMessages,
        int numberOfThreads) {
}
