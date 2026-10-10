package com.nequi.bco.wks901.sqs.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nequi.bco.wks901.model.reversal.ReversalNotification;
import com.nequi.bco.wks901.usecase.notifyreversal.NotifyReversalUseCase;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

/**
 * Reactive migration of the legacy MdbNotifyReversalBean (JMS MDB).
 * Consumes a reversal notification message and delegates to NotifyReversalUseCase.
 * SQS native redelivery + DLQ replaces the legacy JMSXDeliveryCount redelivery cap.
 */
@Service
@Log4j2
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final NotifyReversalUseCase notifyReversalUseCase;
    private final ObjectMapper objectMapper;

    public SQSProcessor(NotifyReversalUseCase notifyReversalUseCase,
                        @Qualifier("sqsObjectMapper") ObjectMapper objectMapper) {
        this.notifyReversalUseCase = notifyReversalUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> apply(Message message) {
        return Mono.fromCallable(() -> objectMapper.readValue(message.body(), ReversalNotification.class))
                .doOnNext(notification -> log.info("Processing reversal notification: {}",
                        notification.getReversalId()))
                .flatMap(notifyReversalUseCase::execute)
                .doOnError(error -> log.error("Error processing reversal notification", error));
    }
}
