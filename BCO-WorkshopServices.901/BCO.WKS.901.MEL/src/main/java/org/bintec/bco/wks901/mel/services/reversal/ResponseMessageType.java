package org.bintec.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseMessageType", propOrder = { "header", "body" })
public class ResponseMessageType implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected ResponseHeaderType header;

    @XmlElement(required = false)
    protected ResponseBodyType body;

    public ResponseHeaderType getHeader() {
        return header;
    }

    public void setHeader(ResponseHeaderType value) {
        this.header = value;
    }

    public ResponseBodyType getBody() {
        return body;
    }

    public void setBody(ResponseBodyType value) {
        this.body = value;
    }
}
