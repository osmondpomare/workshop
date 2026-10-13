package org.bintec.bco.wks901.sec.bean.notify;

import javax.annotation.PostConstruct;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.bintec.bco.wks901.mel.services.reversal.ReversalNotificationType;
import org.bintec.bco.wks901.sec.bean.persistence.ReversoPersistenceBean;
import org.bintec.bco.wks901.sec.util.ConstantWKS901;
import org.bintec.bco.wks901.sec.util.MessageTracerUtil;
import org.bintec.mdw.common.consumer.exception.CommonUtilException;
import org.bintec.mdw.common.consumer.util.CommonServiceConsumerUtil;
import org.bintec.mdw.common.consumer.util.UtilJSON;
import org.bintec.mdw.common.tracer.service.GenericLogger;
import org.bintec.mdw.common.tracer.service.LoggerFactory;
import org.bintec.mdw.common.tracer.service.ServiceTypeEnum;

@MessageDriven(name = "MdbNotifyReversalBean", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class MdbNotifyReversalBean implements MessageListener {

    private GenericLogger logger;

    @EJB
    private NotifyReversalBean notifyReversalBean;

    @EJB
    private ReversoPersistenceBean reversoPersistenceBean;

    @PostConstruct
    void init() {
        logger = LoggerFactory.getLogger(ServiceTypeEnum.BACKOUT, MdbNotifyReversalBean.class);
    }

    public void onMessage(Message message) {

        logger.traceInfo(ConstantWKS901.TRACE_MDB_START);

        try {
            if (!(message instanceof TextMessage)) {
                logger.traceError(ConstantWKS901.TRACE_MDB_INVALID_MESSAGE, null);
                return;
            }

            String text = ((TextMessage) message).getText();
            int deliveryCount = message.propertyExists(ConstantWKS901.COMMON_STRING_JMSX_DELIVERY_COUNT)
                    ? message.getIntProperty(ConstantWKS901.COMMON_STRING_JMSX_DELIVERY_COUNT)
                    : 1;

            if (message.getJMSRedelivered()) {
                logger.traceInfo(UtilJSON.generateString(ConstantWKS901.TRACE_MDB_ATTEMPT,
                        String.valueOf(deliveryCount)));
            }

            if (deliveryCount > maxRedelivery()) {
                logger.traceError(UtilJSON.generateString(ConstantWKS901.TRACE_MDB_MAX_ATTEMPTS, text), null);
                return;
            }

            if (CommonServiceConsumerUtil.validateRequest(text, ReversalNotificationType.class,
                    ConstantWKS901.SERVICE_OPERATION_NOTIFY, logger)) {
                ReversalNotificationType notification = UtilJSON.parsePayload(text, ReversalNotificationType.class);
                MessageTracerUtil.traceRequestMessage(null, notification, logger);
                notifyReversalBean.notifyReversal(notification);
            }

            logger.traceInfo(ConstantWKS901.TRACE_MDB_END);

        } catch (JMSException e) {
            logger.traceError(ConstantWKS901.ERROR_DESC_JMS, e);
        } catch (CommonUtilException e) {
            logger.traceError(ConstantWKS901.ERROR_DESC_INVALID_REQUEST, e);
        }
    }

    private int maxRedelivery() {
        try {
            return Integer.parseInt(reversoPersistenceBean.getParametroValor(ConstantWKS901.PARAM_MAX_REDELIVERY,
                    String.valueOf(ConstantWKS901.DEFAULT_MAX_REDELIVERY)));
        } catch (Exception e) {
            return ConstantWKS901.DEFAULT_MAX_REDELIVERY;
        }
    }
}
