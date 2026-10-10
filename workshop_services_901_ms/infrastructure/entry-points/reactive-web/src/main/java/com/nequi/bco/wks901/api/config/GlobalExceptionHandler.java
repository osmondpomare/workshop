package com.nequi.bco.wks901.api.config;

import com.nequi.bco.wks901.api.dto.MessageRS;
import com.nequi.bco.wks901.api.dto.ResponseHeaderType;
import com.nequi.bco.wks901.api.dto.ResponseMessageType;
import com.nequi.bco.wks901.api.dto.StatusType;
import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import com.nequi.bco.wks901.model.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized reactive error handler. Malformed/invalid requests (bean-validation or
 * body-decoding failures) are translated to the WKS901-001 MessageRS envelope, preserving
 * the legacy ReversalService contract where errors are returned in the response body.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @ExceptionHandler({WebExchangeBindException.class, ServerWebInputException.class,
            HttpMessageNotReadableException.class, DecodingException.class})
    @ResponseStatus(HttpStatus.OK)
    public Mono<MessageRS> handleInvalidRequest(Exception ex) {
        log.warn("Invalid reversal request: {}", ex.getMessage());
        return Mono.just(errorEnvelope(TechnicalMessage.INVALID_REQUEST));
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Mono<MessageRS> handleBusiness(BusinessException ex) {
        return Mono.just(errorEnvelope(ex.getTechnicalMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Mono<MessageRS> handleUnexpected(Exception ex) {
        log.error("Unexpected error in reversal entry point", ex);
        return Mono.just(errorEnvelope(TechnicalMessage.INTERNAL_ERROR));
    }

    private MessageRS errorEnvelope(TechnicalMessage message) {
        StatusType status = new StatusType(message.getCode(), message.getMessage());
        ResponseHeaderType header = new ResponseHeaderType(null, null, null,
                LocalDateTime.now().atZone(java.time.ZoneId.of("America/Bogota")).format(DATE_FORMAT), status);
        return new MessageRS(new ResponseMessageType(header, null));
    }
}
