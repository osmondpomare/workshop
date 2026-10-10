package com.nequi.bco.wks901.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GetReversalRSType(
        String reversalId,
        String messageId,
        String transactionId,
        String phoneNumber,
        String amount,
        String currency,
        String exchangeRate,
        String amountCop,
        String reason,
        String status,
        String eventStatus,
        String creationDate,
        String modificationDate
) {
}
