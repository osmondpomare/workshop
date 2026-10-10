package com.nequi.bco.wks901.model.enums;

import lombok.Getter;

/**
 * Catalog of business/technical messages for the reversal service.
 * Mirrors the legacy WKS901 error codes and descriptions.
 */
@Getter
public enum TechnicalMessage {

    SUCCESS("0", "SUCCESS", ""),
    INVALID_REQUEST("WKS901-001", "Invalid request message", ""),
    DUPLICATED("WKS901-002", "A reversal already exists for this messageId", ""),
    EXCHANGE_SERVICE_ERROR("WKS901-003", "Unable to obtain exchange rate", ""),
    DATABASE_ERROR("WKS901-004", "Database error", ""),
    DYNAMO_ERROR("WKS901-005", "Error registering reversal event", ""),
    JMS_ERROR("WKS901-006", "Error sending reversal notification", ""),
    NOT_FOUND("WKS901-007", "Reversal not found", ""),
    INTERNAL_ERROR("WKS901-999", "Unexpected error", "");

    private final String code;
    private final String message;
    private final String param;

    TechnicalMessage(String code, String message, String param) {
        this.code = code;
        this.message = message;
        this.param = param;
    }
}
