package com.nequi.bco.wks901.consumer.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Transport DTO for the exchange rate provider response (open.er-api.com format).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExchangeRateResponse(
        String result,
        @JsonProperty("base_code") String baseCode,
        Map<String, BigDecimal> rates
) {
}
