package com.nequi.bco.wks901.usecase.notifyreversal;

import com.nequi.bco.wks901.model.reversal.ReversalNotification;
import com.nequi.bco.wks901.model.reverso.gateways.ReversalEventGateway;
import com.nequi.bco.wks901.model.reverso.gateways.ReversoRepository;
import com.nequi.bco.wks901.model.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Reactive migration of the legacy NotifyReversalBean (JMS consumer logic).
 * Updates the reversal status to NOTIFIED and updates the event store status.
 */
@Slf4j
@RequiredArgsConstructor
public class NotifyReversalUseCase {

    private final ReversoRepository reversoRepository;
    private final ReversalEventGateway reversalEventGateway;

    public Mono<Void> execute(ReversalNotification notification) {
        return reversoRepository.updateEstado(notification.getReversalId(), Constants.STATUS_NOTIFIED,
                        Constants.DEFAULT_USER)
                .flatMap(reverso -> reversalEventGateway.updateEventStatus(notification.getMessageId(),
                        Constants.STATUS_NOTIFIED))
                .doOnSuccess(ignored -> log.info("Reversal notified: {}", notification.getReversalId()))
                .doOnError(error -> log.error("Error notifying reversal", error));
    }
}
