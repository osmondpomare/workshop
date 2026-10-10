package com.nequi.bco.wks901.api.mapper;

import com.nequi.bco.wks901.api.dto.GetReversalRSType;
import com.nequi.bco.wks901.api.dto.MessageRQ;
import com.nequi.bco.wks901.api.dto.MessageRS;
import com.nequi.bco.wks901.api.dto.RegisterReversalRSType;
import com.nequi.bco.wks901.api.dto.ResponseBodyType;
import com.nequi.bco.wks901.api.dto.ResponseHeaderType;
import com.nequi.bco.wks901.api.dto.ResponseMessageType;
import com.nequi.bco.wks901.api.dto.StatusType;
import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import com.nequi.bco.wks901.model.reverso.ReversalRegistration;
import com.nequi.bco.wks901.model.reverso.Reverso;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Assembles transport responses (replaces the legacy ResponseBuilderUtil) and maps
 * the inbound request into the domain {@link ReversalRegistration} input.
 */
@Component
public class ReversalMapper {

    private static final String TIME_ZONE = "America/Bogota";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public ReversalRegistration toRegistration(MessageRQ request) {
        var header = request.requestMessage().header();
        var body = request.requestMessage().body().registerReversalRQ();
        return ReversalRegistration.builder()
                .messageId(header.messageId())
                .clientId(header.clientId())
                .channel(header.channel())
                .transactionId(body.transactionId())
                .phoneNumber(body.phoneNumber())
                .amount(body.amount())
                .currency(body.currency())
                .reason(body.reason())
                .build();
    }

    public MessageRS buildRegisterResponse(ReversalRegistration registration, Reverso reverso) {
        RegisterReversalRSType registerRS = new RegisterReversalRSType(
                reverso.getIdReverso(),
                reverso.getEstado(),
                reverso.getTasaCambio() == null ? null : reverso.getTasaCambio().toPlainString(),
                reverso.getValorCop() == null ? null : reverso.getValorCop().toPlainString());
        ResponseBodyType body = new ResponseBodyType(registerRS, null);
        return build(registration.getChannel(), registration.getMessageId(), registration.getClientId(),
                TechnicalMessage.SUCCESS, body);
    }

    public MessageRS buildQueryResponse(String channel, Reverso reverso, String eventStatus) {
        GetReversalRSType getRS = new GetReversalRSType(
                reverso.getIdReverso(),
                reverso.getIdMensaje(),
                reverso.getIdTransaccion(),
                reverso.getNumeroCelular(),
                reverso.getValor() == null ? null : reverso.getValor().toPlainString(),
                reverso.getMoneda(),
                reverso.getTasaCambio() == null ? null : reverso.getTasaCambio().toPlainString(),
                reverso.getValorCop() == null ? null : reverso.getValorCop().toPlainString(),
                reverso.getMotivo(),
                reverso.getEstado(),
                emptyToNull(eventStatus),
                formatDate(reverso.getFechaCreacion()),
                formatDate(reverso.getFechaModificacion()));
        ResponseBodyType body = new ResponseBodyType(null, getRS);
        return build(channel, reverso.getIdMensaje(), null, TechnicalMessage.SUCCESS, body);
    }

    public MessageRS buildError(String channel, String messageId, String clientId, TechnicalMessage message) {
        return build(channel, messageId, clientId, message, null);
    }

    private MessageRS build(String channel, String messageId, String clientId, TechnicalMessage message,
                            ResponseBodyType body) {
        StatusType status = new StatusType(message.getCode(), message.getMessage());
        ResponseHeaderType header = new ResponseHeaderType(channel, messageId, clientId,
                formatDate(java.time.LocalDateTime.now()), status);
        return new MessageRS(new ResponseMessageType(header, body));
    }

    private static String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        ZonedDateTime zoned = dateTime.atZone(ZoneId.of(TIME_ZONE));
        return zoned.format(DATE_FORMAT);
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}
