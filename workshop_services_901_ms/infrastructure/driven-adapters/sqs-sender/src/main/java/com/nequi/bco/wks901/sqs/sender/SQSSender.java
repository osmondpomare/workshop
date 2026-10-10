package com.nequi.bco.wks901.sqs.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import com.nequi.bco.wks901.model.exceptions.TechnicalException;
import com.nequi.bco.wks901.model.reversal.ReversalNotification;
import com.nequi.bco.wks901.model.reversal.gateways.ReversalNotificationGateway;
import com.nequi.bco.wks901.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.function.Predicate;

/**
 * SQS implementation of ReversalNotificationGateway. Reactive migration of the legacy
 * GenericSendJMSMesssageBean JMS send to queue BCO-NotifyReversal.Q.
 */
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements ReversalNotificationGateway {

    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> send(ReversalNotification notification) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(notification))
                .flatMap(body -> Mono.fromFuture(client.sendMessage(buildRequest(body))))
                .doOnNext(response -> log.debug("Reversal notification sent {}", response.messageId()))
                .doOnError(error -> log.error("Error sending reversal notification", error))
                .onErrorMap(Predicate.not(TechnicalException.class::isInstance),
                        ex -> new TechnicalException(ex, TechnicalMessage.JMS_ERROR))
                .then();
    }

    private SendMessageRequest buildRequest(String body) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(body)
                .build();
    }
}
