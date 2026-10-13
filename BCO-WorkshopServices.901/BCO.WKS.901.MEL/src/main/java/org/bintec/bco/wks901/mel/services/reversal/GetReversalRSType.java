package org.bintec.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetReversalRSType", propOrder = { "reversalId", "messageId", "transactionId", "phoneNumber", "amount", "currency", "exchangeRate", "amountCop", "reason", "status", "eventStatus", "creationDate", "modificationDate" })
public class GetReversalRSType implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected String reversalId;

    @XmlElement(required = true)
    protected String messageId;

    @XmlElement(required = true)
    protected String transactionId;

    @XmlElement(required = true)
    protected String phoneNumber;

    @XmlElement(required = true)
    protected String amount;

    @XmlElement(required = true)
    protected String currency;

    @XmlElement(required = false)
    protected String exchangeRate;

    @XmlElement(required = false)
    protected String amountCop;

    @XmlElement(required = false)
    protected String reason;

    @XmlElement(required = true)
    protected String status;

    @XmlElement(required = false)
    protected String eventStatus;

    @XmlElement(required = false)
    protected String creationDate;

    @XmlElement(required = false)
    protected String modificationDate;

    public String getReversalId() {
        return reversalId;
    }

    public void setReversalId(String value) {
        this.reversalId = value;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String value) {
        this.messageId = value;
    }

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

    public String getReason() {
        return reason;
    }

    public void setReason(String value) {
        this.reason = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String value) {
        this.eventStatus = value;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String value) {
        this.creationDate = value;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String value) {
        this.modificationDate = value;
    }
}
