package com.nequi.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestBodyType", propOrder = { "registerReversalRQ" })
public class RequestBodyType implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = false)
    protected RegisterReversalRQType registerReversalRQ;

    public RegisterReversalRQType getRegisterReversalRQ() {
        return registerReversalRQ;
    }

    public void setRegisterReversalRQ(RegisterReversalRQType value) {
        this.registerReversalRQ = value;
    }
}
