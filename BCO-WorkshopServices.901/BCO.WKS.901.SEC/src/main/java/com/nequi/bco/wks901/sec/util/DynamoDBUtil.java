package com.nequi.bco.wks901.sec.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.nequi.bco.wks901.jpa.model.Reverso;
import com.nequi.bco.wks901.sec.exception.WKS901Exception;

public final class DynamoDBUtil {

    private static AmazonDynamoDB client;

    private DynamoDBUtil() {
    }

    private static synchronized AmazonDynamoDB getClient() {
        if (null == client) {
            String region = getEnv(ConstantWKS901.DYNAMO_REGION_ENV, ConstantWKS901.DYNAMO_DEFAULT_REGION);
            String endpoint = getEnv(ConstantWKS901.DYNAMO_ENDPOINT_ENV, null);
            AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard()
                    .withCredentials(getCredentials());
            if (null != endpoint) {
                builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region));
            } else {
                builder.withRegion(region);
            }
            client = builder.build();
        }
        return client;
    }

    private static AWSCredentialsProvider getCredentials() {
        String accessKey = getEnv(ConstantWKS901.DYNAMO_ACCESS_KEY_ENV, null);
        String secretKey = getEnv(ConstantWKS901.DYNAMO_SECRET_KEY_ENV, null);
        if (null != accessKey && null != secretKey) {
            return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
        }
        return DefaultAWSCredentialsProviderChain.getInstance();
    }

    private static String getTable() {
        return getEnv(ConstantWKS901.DYNAMO_TABLE_ENV, ConstantWKS901.DYNAMO_DEFAULT_TABLE);
    }

    public static void putReversalEvent(Reverso reverso) throws WKS901Exception {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put(ConstantWKS901.DYNAMO_ATTR_MESSAGE_ID, new AttributeValue().withS(reverso.getIdMensaje()));
        item.put(ConstantWKS901.DYNAMO_ATTR_REVERSAL_ID, new AttributeValue().withS(reverso.getIdReverso()));
        item.put(ConstantWKS901.DYNAMO_ATTR_TRANSACTION_ID, new AttributeValue().withS(reverso.getIdTransaccion()));
        item.put(ConstantWKS901.DYNAMO_ATTR_PHONE_NUMBER, new AttributeValue().withS(reverso.getNumeroCelular()));
        item.put(ConstantWKS901.DYNAMO_ATTR_AMOUNT, new AttributeValue().withN(reverso.getValor().toPlainString()));
        item.put(ConstantWKS901.DYNAMO_ATTR_CURRENCY, new AttributeValue().withS(reverso.getMoneda()));
        item.put(ConstantWKS901.DYNAMO_ATTR_AMOUNT_COP,
                new AttributeValue().withN(reverso.getValorCop().toPlainString()));
        item.put(ConstantWKS901.DYNAMO_ATTR_EVENT_STATUS, new AttributeValue().withS(reverso.getEstado()));
        item.put(ConstantWKS901.DYNAMO_ATTR_EVENT_DATE,
                new AttributeValue().withS(ResponseBuilderUtil.formatDate(reverso.getFechaCreacion())));
        try {
            getClient().putItem(new PutItemRequest().withTableName(getTable()).withItem(item)
                    .withConditionExpression(ConstantWKS901.DYNAMO_CONDITION_NEW_ITEM));
        } catch (AmazonClientException e) {
            throw new WKS901Exception(ConstantWKS901.ERROR_CODE_DYNAMO, ConstantWKS901.ERROR_DESC_DYNAMO, e);
        }
    }

    public static void updateEventStatus(String messageId, String status) throws WKS901Exception {
        Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        key.put(ConstantWKS901.DYNAMO_ATTR_MESSAGE_ID, new AttributeValue().withS(messageId));
        Map<String, AttributeValue> values = new HashMap<String, AttributeValue>();
        values.put(ConstantWKS901.DYNAMO_VALUE_STATUS, new AttributeValue().withS(status));
        values.put(ConstantWKS901.DYNAMO_VALUE_NOTIFIED_DATE,
                new AttributeValue().withS(ResponseBuilderUtil.formatDate(new Date())));
        try {
            getClient().updateItem(new UpdateItemRequest().withTableName(getTable()).withKey(key)
                    .withUpdateExpression(ConstantWKS901.DYNAMO_UPDATE_STATUS)
                    .withExpressionAttributeValues(values));
        } catch (AmazonClientException e) {
            throw new WKS901Exception(ConstantWKS901.ERROR_CODE_DYNAMO, ConstantWKS901.ERROR_DESC_DYNAMO, e);
        }
    }

    public static String getEventStatus(String messageId) throws WKS901Exception {
        Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        key.put(ConstantWKS901.DYNAMO_ATTR_MESSAGE_ID, new AttributeValue().withS(messageId));
        try {
            GetItemResult result = getClient()
                    .getItem(new GetItemRequest().withTableName(getTable()).withKey(key).withConsistentRead(true));
            if (null == result.getItem() || null == result.getItem().get(ConstantWKS901.DYNAMO_ATTR_EVENT_STATUS)) {
                return null;
            }
            return result.getItem().get(ConstantWKS901.DYNAMO_ATTR_EVENT_STATUS).getS();
        } catch (AmazonClientException e) {
            throw new WKS901Exception(ConstantWKS901.ERROR_CODE_DYNAMO, ConstantWKS901.ERROR_DESC_DYNAMO, e);
        }
    }

    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (null == value || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
