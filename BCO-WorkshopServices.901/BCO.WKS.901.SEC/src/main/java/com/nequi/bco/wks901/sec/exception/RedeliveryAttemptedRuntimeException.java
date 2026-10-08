package com.nequi.bco.wks901.sec.exception;

public class RedeliveryAttemptedRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RedeliveryAttemptedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
