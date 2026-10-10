package com.nequi.bco.wks901.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs")
public record SQSSenderProperties(
     String region,
     String queueUrl,
     String endpoint){
}
