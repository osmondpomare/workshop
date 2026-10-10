package com.nequi.bco.wks901.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StatusType(
        String statusCode,
        String statusDesc
) {
}
