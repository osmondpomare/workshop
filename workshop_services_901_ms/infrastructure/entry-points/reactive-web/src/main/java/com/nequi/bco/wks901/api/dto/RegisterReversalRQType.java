package com.nequi.bco.wks901.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterReversalRQType(
        @NotBlank String transactionId,
        @NotBlank String phoneNumber,
        @NotBlank String amount,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String currency,
        String reason
) {
}
