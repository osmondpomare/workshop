package com.nequi.bco.wks901.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RequestMessageType(
        @NotNull @Valid RequestHeaderType header,
        @NotNull @Valid RequestBodyType body
) {
}
