package com.nequi.bco.wks901.sec.bean.reversal;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.nequi.bco.wks901.jpa.exception.JPAException;
import com.nequi.bco.wks901.jpa.model.Reverso;
import com.nequi.bco.wks901.mel.services.reversal.GetReversalRSType;
import com.nequi.bco.wks901.mel.services.reversal.MessageRS;
import com.nequi.bco.wks901.mel.services.reversal.ResponseBodyType;
import com.nequi.bco.wks901.sec.bean.persistence.ReversoPersistenceBean;
import com.nequi.bco.wks901.sec.exception.WKS901Exception;
import com.nequi.bco.wks901.sec.util.ConstantWKS901;
import com.nequi.bco.wks901.sec.util.DynamoDBUtil;
import com.nequi.bco.wks901.sec.util.ResponseBuilderUtil;
import com.nequi.mdw.common.tracer.service.GenericLogger;
import com.nequi.mdw.common.tracer.service.LoggerFactory;
import com.nequi.mdw.common.tracer.service.ServiceTypeEnum;

@Stateless(name = "QueryReversalBean")
@LocalBean
public class QueryReversalBean {

    private GenericLogger logger;

    @EJB
    private ReversoPersistenceBean reversoPersistenceBean;

    @PostConstruct
    void init() {
        logger = LoggerFactory.getLogger(ServiceTypeEnum.BACKOUT, QueryReversalBean.class);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public MessageRS getReversal(String messageId, String channel) {
        try {
            Reverso reverso = reversoPersistenceBean.findByMessageId(messageId);
            if (null == reverso) {
                throw new WKS901Exception(ConstantWKS901.ERROR_CODE_NOT_FOUND, ConstantWKS901.ERROR_DESC_NOT_FOUND);
            }

            GetReversalRSType getReversalRS = new GetReversalRSType();
            getReversalRS.setReversalId(reverso.getIdReverso());
            getReversalRS.setMessageId(reverso.getIdMensaje());
            getReversalRS.setTransactionId(reverso.getIdTransaccion());
            getReversalRS.setPhoneNumber(reverso.getNumeroCelular());
            getReversalRS.setAmount(reverso.getValor().toPlainString());
            getReversalRS.setCurrency(reverso.getMoneda());
            getReversalRS.setExchangeRate(null == reverso.getTasaCambio() ? null : reverso.getTasaCambio().toPlainString());
            getReversalRS.setAmountCop(null == reverso.getValorCop() ? null : reverso.getValorCop().toPlainString());
            getReversalRS.setReason(reverso.getMotivo());
            getReversalRS.setStatus(reverso.getEstado());
            getReversalRS.setEventStatus(DynamoDBUtil.getEventStatus(messageId));
            getReversalRS.setCreationDate(ResponseBuilderUtil.formatDate(reverso.getFechaCreacion()));
            getReversalRS.setModificationDate(ResponseBuilderUtil.formatDate(reverso.getFechaModificacion()));

            ResponseBodyType body = new ResponseBodyType();
            body.setGetReversalRS(getReversalRS);
            return ResponseBuilderUtil.buildResponse(channel, messageId, reverso.getNumeroCelular(),
                    ConstantWKS901.SUCCESS_CODE, ConstantWKS901.SUCCESS_DESC, body);

        } catch (WKS901Exception e) {
            logger.traceError(e.getMessage(), e, messageId, null);
            return ResponseBuilderUtil.buildResponse(channel, messageId, null, e.getCode(), e.getMessage(), null);
        } catch (JPAException e) {
            logger.traceError(e.getMessage(), e, messageId, null);
            return ResponseBuilderUtil.buildResponse(channel, messageId, null, ConstantWKS901.ERROR_CODE_DATABASE,
                    ConstantWKS901.ERROR_DESC_DATABASE, null);
        }
    }
}
