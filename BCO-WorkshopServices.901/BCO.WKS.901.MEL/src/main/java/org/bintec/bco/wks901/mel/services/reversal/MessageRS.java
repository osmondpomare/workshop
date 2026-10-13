package org.bintec.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageRS", propOrder = { "responseMessage" })
@XmlRootElement(name = "MessageRS")
public class MessageRS implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected ResponseMessageType responseMessage;

    public ResponseMessageType getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(ResponseMessageType value) {
        this.responseMessage = value;
    }
}
