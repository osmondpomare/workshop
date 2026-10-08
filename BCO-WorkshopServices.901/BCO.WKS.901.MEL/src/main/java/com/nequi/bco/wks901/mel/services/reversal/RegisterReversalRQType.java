package com.nequi.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegisterReversalRQType", propOrder = { "transactionId", "phoneNumber", "amount", "currency", "reason" })
public class RegisterReversalRQType implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected String transactionId;

    @XmlElement(required = true)
    protected String phoneNumber;

    @XmlElement(required = true)
    protected String amount;

    @XmlElement(required = true)
    protected String currency;

    @XmlElement(required = false)
    protected String reason;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String value) {
        this.transactionId = value;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String value) {
        this.phoneNumber = value;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String value) {
        this.amount = value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String value) {
        this.currency = value;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String value) {
        this.reason = value;
    }
}
