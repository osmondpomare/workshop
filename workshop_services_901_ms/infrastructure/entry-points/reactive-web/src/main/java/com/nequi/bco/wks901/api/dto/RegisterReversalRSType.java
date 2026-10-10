package com.nequi.bco.wks901.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterReversalRSType(
        String reversalId,
        String status,
        String exchangeRate,
        String amountCop
) {
}
