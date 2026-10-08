package com.nequi.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReversalNotificationType", propOrder = { "messageId", "reversalId", "clientId", "channel" })
@XmlRootElement(name = "ReversalNotificationType")
public class ReversalNotificationType implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected String messageId;

    @XmlElement(required = true)
    protected String reversalId;

    @XmlElement(required = false)
    protected String clientId;

    @XmlElement(required = false)
    protected String channel;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String value) {
        this.messageId = value;
    }

    public String getReversalId() {
        return reversalId;
    }

    public void setReversalId(String value) {
        this.reversalId = value;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String value) {
        this.clientId = value;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String value) {
        this.channel = value;
    }
}
