package com.nequi.bco.wks901.model.exceptions;

import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import lombok.Getter;

/**
 * Represents an unexpected technical failure (infrastructure, I/O) mapped to a server error.
 */
@Getter
public class TechnicalException extends RuntimeException {

    private final transient TechnicalMessage technicalMessage;

    public TechnicalException(Throwable cause, TechnicalMessage technicalMessage) {
        super(technicalMessage.getMessage(), cause);
        this.technicalMessage = technicalMessage;
    }

    public TechnicalException(TechnicalMessage technicalMessage) {
        super(technicalMessage.getMessage());
        this.technicalMessage = technicalMessage;
    }
}
