package org.bintec.bco.wks901.sec.exception;

public class WKS901Exception extends Exception {

    private static final long serialVersionUID = 1L;

    private final String code;

    public WKS901Exception(String code, String message) {
        super(message);
        this.code = code;
    }

    public WKS901Exception(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
