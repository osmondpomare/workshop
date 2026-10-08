package com.nequi.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatusType", propOrder = { "statusCode", "statusDesc" })
public class StatusType implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected String statusCode;

    @XmlElement(required = true)
    protected String statusDesc;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String value) {
        this.statusCode = value;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String value) {
        this.statusDesc = value;
    }
}
