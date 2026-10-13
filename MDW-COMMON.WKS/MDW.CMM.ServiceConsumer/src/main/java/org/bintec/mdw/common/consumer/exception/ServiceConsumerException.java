package org.bintec.mdw.common.consumer.exception;

public class ServiceConsumerException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String code;
    private final int httpStatus;

    public ServiceConsumerException(String code, String message) {
        this(code, message, -1, null);
    }

    public ServiceConsumerException(String code, String message, Throwable cause) {
        this(code, message, -1, cause);
    }

    public ServiceConsumerException(String code, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
