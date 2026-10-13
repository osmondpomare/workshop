package org.bintec.mdw.common.tracer.logger;

import org.bintec.mdw.common.tracer.service.ServiceTypeEnum;

public class BusinessLogger extends AbstractTracerLogger {

    public BusinessLogger(Class<? extends Object> clase) {
        super(ServiceTypeEnum.BUSSINES, clase);
    }

    @Override
    protected String componentName() {
        return "BUSINESS";
    }
}
