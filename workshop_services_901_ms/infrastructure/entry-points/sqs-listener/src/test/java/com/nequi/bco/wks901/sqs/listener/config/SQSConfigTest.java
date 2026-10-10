package com.nequi.bco.wks901.sqs.listener.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.metrics.LoggingMetricPublisher;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SQSConfigTest {

    @InjectMocks
    private SQSConfig sqsConfig;

    @Mock
    private SqsAsyncClient sqsAsyncClient;

    @Mock
    private SQSProperties sqsProperties;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        when(sqsProperties.region()).thenReturn("us-east-1");
        when(sqsProperties.queueUrl()).thenReturn("http://localhost:4566/00000000000/queue-sqs");
        when(sqsProperties.waitTimeSeconds()).thenReturn(20);
        when(sqsProperties.maxNumberOfMessages()).thenReturn(10);
        when(sqsProperties.numberOfThreads()).thenReturn(1);
    }

    @Test
    void configSQSListenerIsNotNull() {
        assertThat(sqsConfig.sqsListener(sqsAsyncClient, sqsProperties, message -> Mono.empty())).isNotNull();
    }

    @Test
    void configSqsIsNotNull() {
        var loggingMetricPublisher = LoggingMetricPublisher.create();
        assertThat(sqsConfig.configSqs(sqsProperties, loggingMetricPublisher)).isNotNull();
    }

    @Test
    void configSqsWhenEndpointIsNotNull() {
        var loggingMetricPublisher = LoggingMetricPublisher.create();
        when(sqsProperties.endpoint()).thenReturn("http://localhost:4566");
        assertThat(sqsConfig.configSqs(sqsProperties, loggingMetricPublisher)).isNotNull();
    }

    @Test
    void resolveEndpointIsNull() {
        assertThat(sqsConfig.resolveEndpoint(sqsProperties)).isNull();
    }
}
