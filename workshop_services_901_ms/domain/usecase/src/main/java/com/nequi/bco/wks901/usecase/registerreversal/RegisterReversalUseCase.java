package com.nequi.bco.wks901.usecase.registerreversal;

import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import com.nequi.bco.wks901.model.exceptions.BusinessException;
import com.nequi.bco.wks901.model.parametro.gateways.ParametroRepository;
import com.nequi.bco.wks901.model.reversal.ReversalNotification;
import com.nequi.bco.wks901.model.reversal.gateways.ReversalNotificationGateway;
import com.nequi.bco.wks901.model.reverso.ReversalRegistration;
import com.nequi.bco.wks901.model.reverso.Reverso;
import com.nequi.bco.wks901.model.reverso.gateways.ExchangeRateGateway;
import com.nequi.bco.wks901.model.reverso.gateways.ReversalEventGateway;
import com.nequi.bco.wks901.model.reverso.gateways.ReversoRepository;
import com.nequi.bco.wks901.model.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reactive migration of the legacy RegisterReversalBean orchestration.
 * Order preserved: validate -> duplicate check -> resolve target currency ->
 * resolve exchange rate -> persist reversal -> store audit event -> send notification.
 */
@Slf4j
@RequiredArgsConstructor
public class RegisterReversalUseCase {

    private final ReversoRepository reversoRepository;
    private final ParametroRepository parametroRepository;
    private final ExchangeRateGateway exchangeRateGateway;
    private final ReversalEventGateway reversalEventGateway;
    private final ReversalNotificationGateway reversalNotificationGateway;

    public Mono<Reverso> execute(ReversalRegistration registration) {
        return validate(registration)
                .then(Mono.defer(() -> ensureNotDuplicated(registration.getMessageId())))
                .then(Mono.defer(this::resolveTargetCurrency))
                .flatMap(targetCurrency -> buildAndPersist(registration, targetCurrency))
                .flatMap(reverso -> reversalEventGateway.putReversalEvent(reverso).thenReturn(reverso))
                .flatMap(reverso -> sendNotification(registration, reverso).thenReturn(reverso))
                .onErrorResume(error -> markAsFailed(registration, error));
    }

    private Mono<Void> validate(ReversalRegistration registration) {
        return Mono.defer(() -> {
            if (isBlank(registration.getMessageId()) || isBlank(registration.getTransactionId())
                    || isBlank(registration.getPhoneNumber()) || isBlank(registration.getAmount())
                    || isBlank(registration.getCurrency())
                    || !registration.getCurrency().trim().toUpperCase().matches(Constants.CURRENCY_PATTERN)) {
                return Mono.error(new BusinessException(TechnicalMessage.INVALID_REQUEST));
            }
            try {
                if (new BigDecimal(registration.getAmount().trim()).signum() <= 0) {
                    return Mono.error(new BusinessException(TechnicalMessage.INVALID_REQUEST));
                }
            } catch (NumberFormatException e) {
                return Mono.error(new BusinessException(TechnicalMessage.INVALID_REQUEST));
            }
            return Mono.empty();
        });
    }

    private Mono<Void> ensureNotDuplicated(String messageId) {
        return reversoRepository.findByMessageId(messageId)
                .flatMap(existing -> Mono.<Void>error(new BusinessException(TechnicalMessage.DUPLICATED)))
                .switchIfEmpty(Mono.empty());
    }

    private Mono<String> resolveTargetCurrency() {
        return parametroRepository.getParametro(Constants.PARAM_TARGET_CURRENCY)
                .map(parametro -> parametro.getValor() == null || parametro.getValor().trim().isEmpty()
                        ? Constants.DEFAULT_TARGET_CURRENCY
                        : parametro.getValor().trim())
                .defaultIfEmpty(Constants.DEFAULT_TARGET_CURRENCY);
    }

    private Mono<Reverso> buildAndPersist(ReversalRegistration registration, String targetCurrency) {
        String sourceCurrency = registration.getCurrency().trim().toUpperCase();
        BigDecimal amount = new BigDecimal(registration.getAmount().trim())
                .setScale(Constants.AMOUNT_SCALE, RoundingMode.HALF_UP);
        return exchangeRateGateway.getExchangeRate(sourceCurrency, targetCurrency, registration.getMessageId())
                .map(rate -> rate.setScale(Constants.RATE_SCALE, RoundingMode.HALF_UP))
                .flatMap(rate -> reversoRepository.save(buildReverso(registration, sourceCurrency, amount, rate)));
    }

    private Reverso buildReverso(ReversalRegistration registration, String currency, BigDecimal amount,
                                 BigDecimal rate) {
        return Reverso.builder()
                .idReverso(UUID.randomUUID().toString())
                .idMensaje(registration.getMessageId())
                .idTransaccion(registration.getTransactionId())
                .numeroCelular(registration.getPhoneNumber())
                .valor(amount)
                .moneda(currency)
                .tasaCambio(rate)
                .valorCop(amount.multiply(rate).setScale(Constants.AMOUNT_SCALE, RoundingMode.HALF_UP))
                .motivo(registration.getReason())
                .estado(Constants.STATUS_REGISTERED)
                .canal(registration.getChannel())
                .fechaCreacion(LocalDateTime.now())
                .usuarioCreacion(Constants.DEFAULT_USER)
                .build();
    }

    private Mono<Void> sendNotification(ReversalRegistration registration, Reverso reverso) {
        ReversalNotification notification = ReversalNotification.builder()
                .messageId(reverso.getIdMensaje())
                .reversalId(reverso.getIdReverso())
                .clientId(registration.getClientId())
                .channel(registration.getChannel())
                .build();
        return reversalNotificationGateway.send(notification);
    }

    private Mono<Reverso> markAsFailed(ReversalRegistration registration, Throwable error) {
        if (error instanceof BusinessException) {
            return Mono.error(error);
        }
        log.error("Unexpected error registering reversal", error);
        return reversoRepository.findByMessageId(registration.getMessageId())
                .flatMap(reverso -> reversoRepository.updateEstado(reverso.getIdReverso(),
                        Constants.STATUS_FAILED, Constants.DEFAULT_USER))
                .then(Mono.error(error));
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
