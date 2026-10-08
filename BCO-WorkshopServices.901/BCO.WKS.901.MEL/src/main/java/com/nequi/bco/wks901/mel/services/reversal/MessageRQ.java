package com.nequi.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageRQ", propOrder = { "requestMessage" })
@XmlRootElement(name = "MessageRQ")
public class MessageRQ implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected RequestMessageType requestMessage;

    public RequestMessageType getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(RequestMessageType value) {
        this.requestMessage = value;
    }
}
