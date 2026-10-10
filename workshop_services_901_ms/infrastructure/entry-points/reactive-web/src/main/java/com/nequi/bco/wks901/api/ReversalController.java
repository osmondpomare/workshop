package com.nequi.bco.wks901.api;

import com.nequi.bco.wks901.api.dto.MessageRQ;
import com.nequi.bco.wks901.api.dto.MessageRS;
import com.nequi.bco.wks901.api.mapper.ReversalMapper;
import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import com.nequi.bco.wks901.model.exceptions.BusinessException;
import com.nequi.bco.wks901.model.reverso.ReversalRegistration;
import com.nequi.bco.wks901.usecase.queryreversal.QueryReversalUseCase;
import com.nequi.bco.wks901.usecase.registerreversal.RegisterReversalUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Reactive WebFlux migration of the legacy JAX-RS ReversalService.
 * Preserves the body-level status contract: errors are returned inside the MessageRS envelope.
 */
@Slf4j
@RestController
@RequestMapping(value = "/services/backout/ReversalServices", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ReversalController {

    private final RegisterReversalUseCase registerReversalUseCase;
    private final QueryReversalUseCase queryReversalUseCase;
    private final ReversalMapper mapper;

    @PostMapping("/registerReversal")
    public Mono<MessageRS> registerReversal(@Valid @RequestBody MessageRQ request) {
        ReversalRegistration registration = mapper.toRegistration(request);
        return registerReversalUseCase.execute(registration)
                .map(reverso -> mapper.buildRegisterResponse(registration, reverso))
                .onErrorResume(error -> Mono.just(mapper.buildError(registration.getChannel(),
                        registration.getMessageId(), registration.getClientId(), resolve(error))));
    }

    @GetMapping("/getReversal/{messageId}")
    public Mono<MessageRS> getReversal(@PathVariable("messageId") String messageId,
                                       @RequestHeader(value = "channel", required = false) String channel) {
        return queryReversalUseCase.execute(messageId)
                .map(tuple -> mapper.buildQueryResponse(channel, tuple.getT1(), tuple.getT2()))
                .onErrorResume(error -> Mono.just(
                        mapper.buildError(channel, messageId, null, resolve(error))));
    }

    private TechnicalMessage resolve(Throwable error) {
        if (error instanceof BusinessException businessException) {
            return businessException.getTechnicalMessage();
        }
        log.error("Unexpected error processing reversal request", error);
        return TechnicalMessage.INTERNAL_ERROR;
    }
}
