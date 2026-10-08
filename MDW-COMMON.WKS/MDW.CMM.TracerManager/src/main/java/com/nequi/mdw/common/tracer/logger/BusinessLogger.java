package com.nequi.mdw.common.tracer.logger;

import com.nequi.mdw.common.tracer.service.ServiceTypeEnum;

public class BusinessLogger extends AbstractTracerLogger {

    public BusinessLogger(Class<? extends Object> clase) {
        super(ServiceTypeEnum.BUSSINES, clase);
    }

    @Override
    protected String componentName() {
        return "BUSINESS";
    }
}
