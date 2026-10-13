package org.bintec.mdw.common.tracer.logger;

import org.bintec.mdw.common.tracer.service.ServiceTypeEnum;

public class IntegrationLogger extends AbstractTracerLogger {

    public IntegrationLogger(Class<? extends Object> clase) {
        super(ServiceTypeEnum.INTEGRATION, clase);
    }

    @Override
    protected String componentName() {
        return "INTEGRATION";
    }
}
