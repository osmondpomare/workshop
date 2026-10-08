package com.nequi.bco.wks901.sec.bean;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.nequi.bco.wks901.sec.util.ConstantWKS901;

@Stateless(name = ConstantWKS901.COMMON_STRING_GENERIC_JMS_MESSAGE)
@LocalBean
public class GenericSendJMSMesssageBean {

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendJmsStringMessage(String jmsStringMessage, String queueName) throws JMSException, NamingException {

        InitialContext initialContext = null;
        Context myEnv = null;
        QueueConnection queueConn = null;
        QueueSession queueSession = null;
        QueueSender queueSender = null;

        try {
            initialContext = new InitialContext();
            myEnv = (Context) initialContext.lookup(ConstantWKS901.COMMON_STRING_ENVIROMENT);

            QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) myEnv
                    .lookup(ConstantWKS901.COMMON_STRING_QUEUE_FACTORY);
            Queue queue = (Queue) myEnv.lookup(queueName);

            queueConn = queueConnectionFactory.createQueueConnection();
            queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queueSender = queueSession.createSender(queue);

            TextMessage textMsg = queueSession.createTextMessage();
            textMsg.setText(jmsStringMessage);
            queueSender.send(textMsg);

        } finally {
            if (null != queueSender) {
                queueSender.close();
            }
            if (null != queueSession) {
                queueSession.close();
            }
            if (null != queueConn) {
                queueConn.close();
            }
            if (null != myEnv) {
                myEnv.close();
            }
            if (null != initialContext) {
                initialContext.close();
            }
        }
    }
}
