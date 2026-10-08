package com.nequi.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestHeaderType", propOrder = { "channel", "messageId", "clientId", "destination" })
public class RequestHeaderType implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected String channel;

    @XmlElement(required = true)
    protected String messageId;

    @XmlElement(required = true)
    protected String clientId;

    @XmlElement(required = true)
    protected DestinationType destination;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String value) {
        this.channel = value;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String value) {
        this.messageId = value;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String value) {
        this.clientId = value;
    }

    public DestinationType getDestination() {
        return destination;
    }

    public void setDestination(DestinationType value) {
        this.destination = value;
    }
}
