package com.nequi.bco.wks901.usecase.queryreversal;

import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import com.nequi.bco.wks901.model.exceptions.BusinessException;
import com.nequi.bco.wks901.model.reverso.Reverso;
import com.nequi.bco.wks901.model.reverso.gateways.ReversalEventGateway;
import com.nequi.bco.wks901.model.reverso.gateways.ReversoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * Reactive migration of the legacy QueryReversalBean.
 * Finds a reversal by messageId and enriches it with the event status from the event store.
 */
@Slf4j
@RequiredArgsConstructor
public class QueryReversalUseCase {

    private final ReversoRepository reversoRepository;
    private final ReversalEventGateway reversalEventGateway;

    public Mono<Tuple2<Reverso, String>> execute(String messageId) {
        return reversoRepository.findByMessageId(messageId)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.NOT_FOUND)))
                .flatMap(reverso -> reversalEventGateway.getEventStatus(messageId)
                        .map(status -> reactor.util.function.Tuples.of(reverso, status))
                        .defaultIfEmpty(reactor.util.function.Tuples.of(reverso, "")));
    }
}
