package com.nequi.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestMessageType", propOrder = { "header", "body" })
public class RequestMessageType implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected RequestHeaderType header;

    @XmlElement(required = true)
    protected RequestBodyType body;

    public RequestHeaderType getHeader() {
        return header;
    }

    public void setHeader(RequestHeaderType value) {
        this.header = value;
    }

    public RequestBodyType getBody() {
        return body;
    }

    public void setBody(RequestBodyType value) {
        this.body = value;
    }
}
