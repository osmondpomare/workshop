package com.nequi.bco.wks901.dynamodb.adapter;

import com.nequi.bco.wks901.model.exceptions.TechnicalException;
import com.nequi.bco.wks901.model.reverso.Reverso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReversalEventAdapterTest {

    @Mock
    private DynamoDbAsyncClient client;

    private ReversalEventAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ReversalEventAdapter(client, "BCO_REVERSAL_EVENTS");
    }

    private Reverso reverso() {
        return Reverso.builder()
                .idReverso("R1").idMensaje("MSG-1").idTransaccion("TX-1").numeroCelular("3001234567")
                .valor(new BigDecimal("100.00")).moneda("USD").valorCop(new BigDecimal("400000.00"))
                .estado("REGISTERED").fechaCreacion(LocalDateTime.now()).build();
    }

    @Test
    void shouldPutReversalEvent() {
        given(client.putItem(any(PutItemRequest.class)))
                .willReturn(CompletableFuture.completedFuture(PutItemResponse.builder().build()));

        StepVerifier.create(adapter.putReversalEvent(reverso())).verifyComplete();
    }

    @Test
    void shouldReturnEventStatus() {
        GetItemResponse response = GetItemResponse.builder()
                .item(Map.of("eventStatus", AttributeValue.builder().s("NOTIFIED").build()))
                .build();
        given(client.getItem(any(GetItemRequest.class)))
                .willReturn(CompletableFuture.completedFuture(response));

        StepVerifier.create(adapter.getEventStatus("MSG-1"))
                .expectNext("NOTIFIED")
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenNoItem() {
        given(client.getItem(any(GetItemRequest.class)))
                .willReturn(CompletableFuture.completedFuture(GetItemResponse.builder().build()));

        StepVerifier.create(adapter.getEventStatus("MSG-1")).verifyComplete();
    }

    @Test
    void shouldMapErrorToTechnicalException() {
        CompletableFuture<PutItemResponse> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("dynamo down"));
        given(client.putItem(any(PutItemRequest.class))).willReturn(failed);

        StepVerifier.create(adapter.putReversalEvent(reverso()))
                .expectError(TechnicalException.class)
                .verify();
    }
}
