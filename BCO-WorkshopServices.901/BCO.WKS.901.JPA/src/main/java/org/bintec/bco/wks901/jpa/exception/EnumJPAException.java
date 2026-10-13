package org.bintec.bco.wks901.jpa.exception;

public enum EnumJPAException {

    DB_ERROR("JPA-001", "Database error"),
    NOT_FOUND("JPA-002", "Record not found"),
    DUPLICATED("JPA-003", "Duplicated record");

    private final String code;
    private final String message;

    EnumJPAException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
