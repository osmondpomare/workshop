package com.nequi.bco.wks901.model.reverso.gateways;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Resolves the exchange rate from a source currency to a target currency.
 * Replaces the legacy ExchangeRateUtil + ServiceBroker call to ExchangeRateServices.
 */
public interface ExchangeRateGateway {

    Mono<BigDecimal> getExchangeRate(String sourceCurrency, String targetCurrency, String messageId);
}
