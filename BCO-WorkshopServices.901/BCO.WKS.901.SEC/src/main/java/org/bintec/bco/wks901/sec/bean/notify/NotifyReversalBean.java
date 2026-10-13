package org.bintec.bco.wks901.sec.bean.notify;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.bintec.bco.wks901.jpa.exception.JPAException;
import org.bintec.bco.wks901.jpa.model.Reverso;
import org.bintec.bco.wks901.mel.services.reversal.ReversalNotificationType;
import org.bintec.bco.wks901.sec.bean.persistence.ReversoPersistenceBean;
import org.bintec.bco.wks901.sec.exception.RedeliveryAttemptedRuntimeException;
import org.bintec.bco.wks901.sec.exception.WKS901Exception;
import org.bintec.bco.wks901.sec.util.ConstantWKS901;
import org.bintec.bco.wks901.sec.util.DynamoDBUtil;
import org.bintec.mdw.common.consumer.util.UtilJSON;
import org.bintec.mdw.common.tracer.service.GenericLogger;
import org.bintec.mdw.common.tracer.service.LoggerFactory;
import org.bintec.mdw.common.tracer.service.ServiceTypeEnum;

@Stateless(name = "NotifyReversalBean")
@LocalBean
public class NotifyReversalBean {

    private GenericLogger logger;

    @EJB
    private ReversoPersistenceBean reversoPersistenceBean;

    @PostConstruct
    void init() {
        logger = LoggerFactory.getLogger(ServiceTypeEnum.BACKOUT, NotifyReversalBean.class);
    }

    public void notifyReversal(ReversalNotificationType notification) {
        try {
            Reverso reverso = reversoPersistenceBean.updateEstado(notification.getReversalId(),
                    ConstantWKS901.STATUS_NOTIFIED);
            DynamoDBUtil.updateEventStatus(notification.getMessageId(), ConstantWKS901.STATUS_NOTIFIED);
            logger.traceInfo(UtilJSON.generateString(ConstantWKS901.TRACE_NOTIFIED, reverso.getIdReverso()));
        } catch (JPAException e) {
            logger.traceError(e.getMessage(), e, notification.getMessageId(), notification.getClientId());
            throw new RedeliveryAttemptedRuntimeException(e.getMessage(), e);
        } catch (WKS901Exception e) {
            logger.traceError(e.getMessage(), e, notification.getMessageId(), notification.getClientId());
            throw new RedeliveryAttemptedRuntimeException(e.getMessage(), e);
        }
    }
}
