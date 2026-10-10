package com.nequi.bco.wks901.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RequestHeaderType(
        @NotBlank String channel,
        @NotBlank String messageId,
        @NotBlank String clientId,
        @Valid DestinationType destination
) {
}
