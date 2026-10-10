package com.nequi.bco.wks901.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Root response transport object for the reversal endpoints.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record MessageRS(
        ResponseMessageType responseMessage
) {
}
