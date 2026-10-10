package com.nequi.bco.wks901.consumer.exchangerate;

import com.nequi.bco.wks901.consumer.exchangerate.dto.ExchangeRateResponse;
import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import com.nequi.bco.wks901.model.exceptions.BusinessException;
import com.nequi.bco.wks901.model.exceptions.TechnicalException;
import com.nequi.bco.wks901.model.reverso.gateways.ExchangeRateGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.function.Predicate;

/**
 * WebClient adapter for the external Exchange Rate service.
 * Replaces the legacy ExchangeRateUtil + ServiceBroker call to ExchangeRateServices.getLatestRates.
 * Endpoint: GET /v6/latest/{currency}; expects result == "success" and rates.{targetCurrency}.
 */
@Slf4j
@RequiredArgsConstructor
public class ExchangeRateAdapter implements ExchangeRateGateway {

    private static final String RESULT_SUCCESS = "success";
    private static final String PATH = "/v6/latest/{currency}";

    private final WebClient exchangeRateWebClient;

    @Override
    public Mono<BigDecimal> getExchangeRate(String sourceCurrency, String targetCurrency, String messageId) {
        if (sourceCurrency.equalsIgnoreCase(targetCurrency)) {
            return Mono.just(BigDecimal.ONE);
        }
        return exchangeRateWebClient.get()
                .uri(PATH, sourceCurrency)
                .retrieve()
                .bodyToMono(ExchangeRateResponse.class)
                .doOnError(error -> log.error("Error calling exchange rate service", error))
                .flatMap(response -> mapRate(response, targetCurrency))
                .onErrorMap(Predicate.not(BusinessException.class::isInstance),
                        ex -> new TechnicalException(ex, TechnicalMessage.EXCHANGE_SERVICE_ERROR));
    }

    private Mono<BigDecimal> mapRate(ExchangeRateResponse response, String targetCurrency) {
        if (response == null || !RESULT_SUCCESS.equals(response.result())
                || response.rates() == null || response.rates().get(targetCurrency) == null) {
            return Mono.error(new BusinessException(TechnicalMessage.EXCHANGE_SERVICE_ERROR));
        }
        BigDecimal rate = response.rates().get(targetCurrency);
        log.info("Exchange rate obtained: {}/{}={}", response.baseCode(), targetCurrency, rate.toPlainString());
        return Mono.just(rate);
    }
}
