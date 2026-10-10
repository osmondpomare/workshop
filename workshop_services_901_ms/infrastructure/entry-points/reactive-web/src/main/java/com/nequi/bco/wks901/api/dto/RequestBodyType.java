package com.nequi.bco.wks901.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RequestBodyType(
        @Valid RegisterReversalRQType registerReversalRQ
) {
}
