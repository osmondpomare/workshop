package com.nequi.mdw.common.tracer.logger;

import com.nequi.mdw.common.tracer.service.ServiceTypeEnum;

public class IntegrationLogger extends AbstractTracerLogger {

    public IntegrationLogger(Class<? extends Object> clase) {
        super(ServiceTypeEnum.INTEGRATION, clase);
    }

    @Override
    protected String componentName() {
        return "INTEGRATION";
    }
}
