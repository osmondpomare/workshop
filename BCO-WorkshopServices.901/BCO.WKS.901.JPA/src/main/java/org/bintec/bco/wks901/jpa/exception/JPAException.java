package org.bintec.bco.wks901.jpa.exception;

public class JPAException extends Exception {

    private static final long serialVersionUID = 1L;

    private final EnumJPAException error;

    public JPAException(EnumJPAException error, String detail, Throwable cause) {
        super(error.getCode() + " " + error.getMessage() + " - " + detail, cause);
        this.error = error;
    }

    public EnumJPAException getError() {
        return error;
    }
}
