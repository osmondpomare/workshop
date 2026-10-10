package com.nequi.bco.wks901.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DestinationType(
        String serviceName,
        String serviceOperation,
        String serviceRegion,
        String serviceVersion
) {
}
