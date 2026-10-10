package com.nequi.bco.wks901.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Root request transport object for the reversal registration endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MessageRQ(
        @NotNull @Valid RequestMessageType requestMessage
) {
}
