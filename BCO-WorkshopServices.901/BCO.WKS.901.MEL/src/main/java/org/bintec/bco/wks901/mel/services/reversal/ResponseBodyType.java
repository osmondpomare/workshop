package org.bintec.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseBodyType", propOrder = { "registerReversalRS", "getReversalRS" })
public class ResponseBodyType implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = false)
    protected RegisterReversalRSType registerReversalRS;

    @XmlElement(required = false)
    protected GetReversalRSType getReversalRS;

    public RegisterReversalRSType getRegisterReversalRS() {
        return registerReversalRS;
    }

    public void setRegisterReversalRS(RegisterReversalRSType value) {
        this.registerReversalRS = value;
    }

    public GetReversalRSType getGetReversalRS() {
        return getReversalRS;
    }

    public void setGetReversalRS(GetReversalRSType value) {
        this.getReversalRS = value;
    }
}
