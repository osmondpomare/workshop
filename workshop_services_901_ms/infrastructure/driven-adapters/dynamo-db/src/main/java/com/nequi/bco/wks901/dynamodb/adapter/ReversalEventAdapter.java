package com.nequi.bco.wks901.dynamodb.adapter;

import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import com.nequi.bco.wks901.model.exceptions.TechnicalException;
import com.nequi.bco.wks901.model.reverso.Reverso;
import com.nequi.bco.wks901.model.reverso.gateways.ReversalEventGateway;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * DynamoDB adapter for reversal audit events (AWS SDK v2 async, non-blocking).
 * Replaces the legacy DynamoDBUtil (AWS SDK v1): conditional put (no overwrite),
 * updateItem of eventStatus/notifiedDate, and getItem of eventStatus.
 */
@Slf4j
public class ReversalEventAdapter implements ReversalEventGateway {

    private static final String ATTR_MESSAGE_ID = "messageId";
    private static final String ATTR_REVERSAL_ID = "reversalId";
    private static final String ATTR_TRANSACTION_ID = "transactionId";
    private static final String ATTR_PHONE_NUMBER = "phoneNumber";
    private static final String ATTR_AMOUNT = "amount";
    private static final String ATTR_CURRENCY = "currency";
    private static final String ATTR_AMOUNT_COP = "amountCop";
    private static final String ATTR_EVENT_STATUS = "eventStatus";
    private static final String ATTR_EVENT_DATE = "eventDate";
    private static final String ATTR_NOTIFIED_DATE = "notifiedDate";
    private static final String CONDITION_NEW_ITEM = "attribute_not_exists(messageId)";
    private static final String UPDATE_STATUS = "SET eventStatus = :status, notifiedDate = :notifiedDate";

    private final DynamoDbAsyncClient client;
    private final String tableName;

    public ReversalEventAdapter(DynamoDbAsyncClient client, String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    @Override
    public Mono<Void> putReversalEvent(Reverso reverso) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(ATTR_MESSAGE_ID, str(reverso.getIdMensaje()));
        item.put(ATTR_REVERSAL_ID, str(reverso.getIdReverso()));
        item.put(ATTR_TRANSACTION_ID, str(reverso.getIdTransaccion()));
        item.put(ATTR_PHONE_NUMBER, str(reverso.getNumeroCelular()));
        item.put(ATTR_AMOUNT, num(reverso.getValor().toPlainString()));
        item.put(ATTR_CURRENCY, str(reverso.getMoneda()));
        item.put(ATTR_AMOUNT_COP, num(reverso.getValorCop().toPlainString()));
        item.put(ATTR_EVENT_STATUS, str(reverso.getEstado()));
        item.put(ATTR_EVENT_DATE, str(formatDate(reverso.getFechaCreacion())));

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .conditionExpression(CONDITION_NEW_ITEM)
                .build();

        return Mono.fromFuture(() -> client.putItem(request))
                .doOnError(error -> log.error("DynamoDB putReversalEvent error", error))
                .onErrorMap(Predicate.not(TechnicalException.class::isInstance),
                        ex -> new TechnicalException(ex, TechnicalMessage.DYNAMO_ERROR))
                .then();
    }

    @Override
    public Mono<Void> updateEventStatus(String messageId, String status) {
        Map<String, AttributeValue> key = Map.of(ATTR_MESSAGE_ID, str(messageId));
        Map<String, AttributeValue> values = Map.of(
                ":status", str(status),
                ":notifiedDate", str(formatDate(LocalDateTime.now())));

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression(UPDATE_STATUS)
                .expressionAttributeValues(values)
                .build();

        return Mono.fromFuture(() -> client.updateItem(request))
                .doOnError(error -> log.error("DynamoDB updateEventStatus error", error))
                .onErrorMap(Predicate.not(TechnicalException.class::isInstance),
                        ex -> new TechnicalException(ex, TechnicalMessage.DYNAMO_ERROR))
                .then();
    }

    @Override
    public Mono<String> getEventStatus(String messageId) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(ATTR_MESSAGE_ID, str(messageId)))
                .consistentRead(true)
                .build();

        return Mono.fromFuture(() -> client.getItem(request))
                .flatMap(response -> {
                    if (!response.hasItem() || response.item().get(ATTR_EVENT_STATUS) == null) {
                        return Mono.empty();
                    }
                    return Mono.just(response.item().get(ATTR_EVENT_STATUS).s());
                })
                .doOnError(error -> log.error("DynamoDB getEventStatus error", error))
                .onErrorMap(Predicate.not(TechnicalException.class::isInstance),
                        ex -> new TechnicalException(ex, TechnicalMessage.DYNAMO_ERROR));
    }

    private static AttributeValue str(String value) {
        return AttributeValue.builder().s(value == null ? "" : value).build();
    }

    private static AttributeValue num(String value) {
        return AttributeValue.builder().n(value).build();
    }

    private static String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
