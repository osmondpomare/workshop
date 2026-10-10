package com.nequi.bco.wks901.dynamodb.config;

import com.nequi.bco.wks901.dynamodb.adapter.ReversalEventAdapter;
import com.nequi.bco.wks901.model.reverso.gateways.ReversalEventGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;

@Configuration
public class DynamoDbConfig {

    @Bean
    public DynamoDbAsyncClient dynamoDbAsyncClient(
            @Value("${aws.region:us-east-1}") String region,
            @Value("${aws.dynamodb.endpoint:}") String endpoint,
            @Value("${aws.accessKeyId:}") String accessKeyId,
            @Value("${aws.secretAccessKey:}") String secretAccessKey) {

        var builder = DynamoDbAsyncClient.builder()
                .region(Region.of(region));

        if (!accessKeyId.isBlank() && !secretAccessKey.isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

    @Bean
    public ReversalEventGateway reversalEventGateway(DynamoDbAsyncClient dynamoDbAsyncClient,
                                                     @Value("${aws.dynamodb.reversal-table:BCO_REVERSAL_EVENTS}") String tableName) {
        return new ReversalEventAdapter(dynamoDbAsyncClient, tableName);
    }
}
