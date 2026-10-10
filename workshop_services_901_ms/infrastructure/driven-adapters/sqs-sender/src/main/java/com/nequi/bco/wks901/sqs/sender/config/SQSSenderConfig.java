package com.nequi.bco.wks901.sqs.sender.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nequi.bco.wks901.model.reversal.gateways.ReversalNotificationGateway;
import com.nequi.bco.wks901.sqs.sender.SQSSender;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class SQSSenderConfig {

    @Bean
    public ReversalNotificationGateway reversalNotificationGateway(SQSSenderProperties properties,
                                                                   @Qualifier("senderSqsAsyncClient") SqsAsyncClient sqsAsyncClient,
                                                                   @Qualifier("sqsObjectMapper") ObjectMapper objectMapper) {
        return new SQSSender(properties, sqsAsyncClient, objectMapper);
    }

    @Bean(name = "sqsObjectMapper")
    @ConditionalOnMissingBean(name = "sqsObjectMapper")
    public ObjectMapper sqsObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public SqsAsyncClient senderSqsAsyncClient(SQSSenderProperties properties, MetricPublisher publisher) {
        return SqsAsyncClient.builder()
                .endpointOverride(resolveEndpoint(properties))
                .region(Region.of(properties.region()))
                .overrideConfiguration(o -> o.addMetricPublisher(publisher))
                .credentialsProvider(getProviderChain())
                .build();
    }

    private AwsCredentialsProviderChain getProviderChain() {
        return AwsCredentialsProviderChain.builder()
                .addCredentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .addCredentialsProvider(SystemPropertyCredentialsProvider.create())
                .addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
                .addCredentialsProvider(ProfileCredentialsProvider.create())
                .addCredentialsProvider(ContainerCredentialsProvider.builder().build())
                .addCredentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }

    private URI resolveEndpoint(SQSSenderProperties properties) {
        if (properties.endpoint() != null) {
            return URI.create(properties.endpoint());
        }
        return null;
    }
}
