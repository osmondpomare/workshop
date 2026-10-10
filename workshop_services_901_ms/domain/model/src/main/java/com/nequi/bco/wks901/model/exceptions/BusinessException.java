package com.nequi.bco.wks901.model.exceptions;

import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import lombok.Getter;

/**
 * Represents a business rule violation (expected, recoverable outcome mapped to a client error).
 */
@Getter
public class BusinessException extends RuntimeException {

    private final transient TechnicalMessage technicalMessage;

    public BusinessException(TechnicalMessage technicalMessage) {
        super(technicalMessage.getMessage());
        this.technicalMessage = technicalMessage;
    }
}
