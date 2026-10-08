package com.nequi.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegisterReversalRSType", propOrder = { "reversalId", "status", "exchangeRate", "amountCop" })
public class RegisterReversalRSType implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected String reversalId;

    @XmlElement(required = true)
    protected String status;

    @XmlElement(required = false)
    protected String exchangeRate;

    @XmlElement(required = false)
    protected String amountCop;

    public String getReversalId() {
        return reversalId;
    }

    public void setReversalId(String value) {
        this.reversalId = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String value) {
        this.exchangeRate = value;
    }

    public String getAmountCop() {
        return amountCop;
    }

    public void setAmountCop(String value) {
        this.amountCop = value;
    }
}
