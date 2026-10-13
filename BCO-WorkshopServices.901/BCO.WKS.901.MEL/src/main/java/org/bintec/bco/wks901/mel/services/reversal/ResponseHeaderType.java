package org.bintec.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseHeaderType", propOrder = { "channel", "messageId", "clientId", "responseDate", "status" })
public class ResponseHeaderType implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = false)
    protected String channel;

    @XmlElement(required = false)
    protected String messageId;

    @XmlElement(required = false)
    protected String clientId;

    @XmlElement(required = true)
    protected String responseDate;

    @XmlElement(required = true)
    protected StatusType status;

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

    public String getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(String value) {
        this.responseDate = value;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType value) {
        this.status = value;
    }
}
