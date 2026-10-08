package com.nequi.bco.wks901.sec.bean.reversal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.nequi.bco.wks901.jpa.exception.JPAException;
import com.nequi.bco.wks901.jpa.model.Reverso;
import com.nequi.bco.wks901.mel.services.reversal.MessageRQ;
import com.nequi.bco.wks901.mel.services.reversal.MessageRS;
import com.nequi.bco.wks901.mel.services.reversal.RegisterReversalRQType;
import com.nequi.bco.wks901.mel.services.reversal.RegisterReversalRSType;
import com.nequi.bco.wks901.mel.services.reversal.RequestHeaderType;
import com.nequi.bco.wks901.mel.services.reversal.ResponseBodyType;
import com.nequi.bco.wks901.mel.services.reversal.ReversalNotificationType;
import com.nequi.bco.wks901.sec.bean.GenericSendJMSMesssageBean;
import com.nequi.bco.wks901.sec.bean.persistence.ReversoPersistenceBean;
import com.nequi.bco.wks901.sec.exception.WKS901Exception;
import com.nequi.bco.wks901.sec.util.ConstantWKS901;
import com.nequi.bco.wks901.sec.util.DynamoDBUtil;
import com.nequi.bco.wks901.sec.util.ExchangeRateUtil;
import com.nequi.bco.wks901.sec.util.ResponseBuilderUtil;
import com.nequi.mdw.common.consumer.util.CommonServiceConsumerUtil;
import com.nequi.mdw.common.consumer.util.UtilJSON;
import com.nequi.mdw.common.tracer.service.GenericLogger;
import com.nequi.mdw.common.tracer.service.LoggerFactory;
import com.nequi.mdw.common.tracer.service.ServiceTypeEnum;

@Stateless(name = "RegisterReversalBean")
@LocalBean
public class RegisterReversalBean {

    private GenericLogger logger;

    @EJB
    private ReversoPersistenceBean reversoPersistenceBean;

    @EJB
    private GenericSendJMSMesssageBean genericSendJMSMesssageBean;

    @PostConstruct
    void init() {
        logger = LoggerFactory.getLogger(ServiceTypeEnum.BACKOUT, RegisterReversalBean.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public MessageRS registerReversal(MessageRQ messageRQ) {

        RequestHeaderType header = null;
        Reverso reverso = null;

        if (null != messageRQ && null != messageRQ.getRequestMessage()) {
            header = messageRQ.getRequestMessage().getHeader();
        }

        try {
            validateRequest(messageRQ);
            RegisterReversalRQType request = messageRQ.getRequestMessage().getBody().getRegisterReversalRQ();

            if (null != reversoPersistenceBean.findByMessageId(header.getMessageId())) {
                throw new WKS901Exception(ConstantWKS901.ERROR_CODE_DUPLICATED, ConstantWKS901.ERROR_DESC_DUPLICATED);
            }

            String targetCurrency = reversoPersistenceBean.getParametroValor(ConstantWKS901.PARAM_TARGET_CURRENCY,
                    ConstantWKS901.DEFAULT_TARGET_CURRENCY);
            String sourceCurrency = request.getCurrency().trim().toUpperCase();
            BigDecimal amount = new BigDecimal(request.getAmount().trim()).setScale(ConstantWKS901.AMOUNT_SCALE,
                    RoundingMode.HALF_UP);
            BigDecimal rate = ExchangeRateUtil.getExchangeRate(sourceCurrency, targetCurrency, header.getMessageId(),
                    logger).setScale(ConstantWKS901.RATE_SCALE, RoundingMode.HALF_UP);

            reverso = reversoPersistenceBean.saveReverso(buildReverso(header, request, sourceCurrency, amount, rate));

            DynamoDBUtil.putReversalEvent(reverso);

            sendNotification(header, reverso);

            RegisterReversalRSType registerReversalRS = new RegisterReversalRSType();
            registerReversalRS.setReversalId(reverso.getIdReverso());
            registerReversalRS.setStatus(reverso.getEstado());
            registerReversalRS.setExchangeRate(rate.toPlainString());
            registerReversalRS.setAmountCop(reverso.getValorCop().toPlainString());
            ResponseBodyType body = new ResponseBodyType();
            body.setRegisterReversalRS(registerReversalRS);

            return ResponseBuilderUtil.buildResponse(header, ConstantWKS901.SUCCESS_CODE,
                    ConstantWKS901.SUCCESS_DESC, body);

        } catch (WKS901Exception e) {
            logger.traceError(e.getMessage(), e, messageId(header), clientId(header));
            markAsFailed(reverso);
            return ResponseBuilderUtil.buildResponse(header, e.getCode(), e.getMessage(), null);
        } catch (JPAException e) {
            logger.traceError(e.getMessage(), e, messageId(header), clientId(header));
            markAsFailed(reverso);
            return ResponseBuilderUtil.buildResponse(header, ConstantWKS901.ERROR_CODE_DATABASE,
                    ConstantWKS901.ERROR_DESC_DATABASE, null);
        } catch (RuntimeException e) {
            logger.traceError(e.getMessage(), e, messageId(header), clientId(header));
            markAsFailed(reverso);
            return ResponseBuilderUtil.buildResponse(header, ConstantWKS901.ERROR_CODE_UNEXPECTED,
                    ConstantWKS901.ERROR_DESC_UNEXPECTED, null);
        }
    }

    private void sendNotification(RequestHeaderType header, Reverso reverso) throws WKS901Exception {
        ReversalNotificationType notification = new ReversalNotificationType();
        notification.setMessageId(reverso.getIdMensaje());
        notification.setReversalId(reverso.getIdReverso());
        notification.setClientId(header.getClientId());
        notification.setChannel(header.getChannel());
        try {
            genericSendJMSMesssageBean.sendJmsStringMessage(UtilJSON.parseObjectToString(notification),
                    ConstantWKS901.COMMON_STRING_QUEUE_NOTIFY_REVERSAL);
        } catch (Exception e) {
            throw new WKS901Exception(ConstantWKS901.ERROR_CODE_JMS, ConstantWKS901.ERROR_DESC_JMS, e);
        }
    }

    private Reverso buildReverso(RequestHeaderType header, RegisterReversalRQType request, String currency,
            BigDecimal amount, BigDecimal rate) {
        Reverso reverso = new Reverso();
        reverso.setIdReverso(UUID.randomUUID().toString());
        reverso.setIdMensaje(header.getMessageId());
        reverso.setIdTransaccion(request.getTransactionId());
        reverso.setNumeroCelular(request.getPhoneNumber());
        reverso.setValor(amount);
        reverso.setMoneda(currency);
        reverso.setTasaCambio(rate);
        reverso.setValorCop(amount.multiply(rate).setScale(ConstantWKS901.AMOUNT_SCALE, RoundingMode.HALF_UP));
        reverso.setMotivo(request.getReason());
        reverso.setEstado(ConstantWKS901.STATUS_REGISTERED);
        reverso.setCanal(header.getChannel());
        reverso.setFechaCreacion(new Date());
        reverso.setUsuarioCreacion(ReversoPersistenceBean.currentUser());
        return reverso;
    }

    private void validateRequest(MessageRQ messageRQ) throws WKS901Exception {
        if (null == messageRQ || null == messageRQ.getRequestMessage()
                || null == messageRQ.getRequestMessage().getHeader()
                || null == messageRQ.getRequestMessage().getBody()
                || null == messageRQ.getRequestMessage().getBody().getRegisterReversalRQ()) {
            throw invalid();
        }
        RequestHeaderType header = messageRQ.getRequestMessage().getHeader();
        RegisterReversalRQType request = messageRQ.getRequestMessage().getBody().getRegisterReversalRQ();
        if (CommonServiceConsumerUtil.isEmpty(header.getMessageId())
                || CommonServiceConsumerUtil.isEmpty(request.getTransactionId())
                || CommonServiceConsumerUtil.isEmpty(request.getPhoneNumber())
                || CommonServiceConsumerUtil.isEmpty(request.getAmount())
                || CommonServiceConsumerUtil.isEmpty(request.getCurrency())
                || !request.getCurrency().trim().toUpperCase().matches(ConstantWKS901.CURRENCY_PATTERN)) {
            throw invalid();
        }
        try {
            if (new BigDecimal(request.getAmount().trim()).signum() <= 0) {
                throw invalid();
            }
        } catch (NumberFormatException e) {
            throw invalid();
        }
    }

    private void markAsFailed(Reverso reverso) {
        if (null == reverso) {
            return;
        }
        try {
            reversoPersistenceBean.updateEstado(reverso.getIdReverso(), ConstantWKS901.STATUS_FAILED);
        } catch (Exception e) {
            logger.traceError(ConstantWKS901.ERROR_DESC_DATABASE, e, reverso.getIdMensaje(), null);
        }
    }

    private static WKS901Exception invalid() {
        return new WKS901Exception(ConstantWKS901.ERROR_CODE_INVALID_REQUEST,
                ConstantWKS901.ERROR_DESC_INVALID_REQUEST);
    }

    private static String messageId(RequestHeaderType header) {
        return null == header ? null : header.getMessageId();
    }

    private static String clientId(RequestHeaderType header) {
        return null == header ? null : header.getClientId();
    }
}
