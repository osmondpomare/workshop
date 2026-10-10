package com.nequi.bco.wks901.model.utils;

/**
 * Domain-level constants for the reversal service.
 */
public final class Constants {

    private Constants() {
    }

    // Database-driven parameters (environment variable candidates)
    public static final String PARAM_TARGET_CURRENCY = "BCO_WKS901_MONEDA_DESTINO";
    public static final String PARAM_MAX_REDELIVERY = "BCO_WKS901_MAX_REINTENTOS";
    public static final String DEFAULT_TARGET_CURRENCY = "COP";
    public static final int DEFAULT_MAX_REDELIVERY = 3;

    // Reversal status lifecycle
    public static final String STATUS_REGISTERED = "REGISTERED";
    public static final String STATUS_NOTIFIED = "NOTIFIED";
    public static final String STATUS_FAILED = "FAILED";

    // Monetary scales
    public static final int AMOUNT_SCALE = 2;
    public static final int RATE_SCALE = 6;

    // Validation
    public static final String CURRENCY_PATTERN = "^[A-Z]{3}$";

    // Default audit user
    public static final String DEFAULT_USER = "Nequi";
}
