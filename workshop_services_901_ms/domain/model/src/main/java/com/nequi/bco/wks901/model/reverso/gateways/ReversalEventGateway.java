package com.nequi.bco.wks901.model.reverso.gateways;

import com.nequi.bco.wks901.model.reverso.Reverso;
import reactor.core.publisher.Mono;

/**
 * Persists and queries reversal audit events in the external event store (DynamoDB).
 * Replaces the legacy DynamoDBUtil operations.
 */
public interface ReversalEventGateway {

    Mono<Void> putReversalEvent(Reverso reverso);

    Mono<Void> updateEventStatus(String messageId, String status);

    Mono<String> getEventStatus(String messageId);
}
