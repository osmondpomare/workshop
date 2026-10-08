package com.nequi.bco.wks901.mel.services.reversal;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DestinationType", propOrder = { "serviceName", "serviceOperation", "serviceRegion", "serviceVersion" })
public class DestinationType implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected String serviceName;

    @XmlElement(required = true)
    protected String serviceOperation;

    @XmlElement(required = false)
    protected String serviceRegion;

    @XmlElement(required = false)
    protected String serviceVersion;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String value) {
        this.serviceName = value;
    }

    public String getServiceOperation() {
        return serviceOperation;
    }

    public void setServiceOperation(String value) {
        this.serviceOperation = value;
    }

    public String getServiceRegion() {
        return serviceRegion;
    }

    public void setServiceRegion(String value) {
        this.serviceRegion = value;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String value) {
        this.serviceVersion = value;
    }
}
