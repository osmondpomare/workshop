package com.nequi.mdw.common.tracer.logger;

import com.nequi.mdw.common.tracer.service.ServiceTypeEnum;

public class FacadeLogger extends AbstractTracerLogger {

    public FacadeLogger(Class<? extends Object> clase) {
        super(ServiceTypeEnum.FACADE, clase);
    }

    @Override
    protected String componentName() {
        return "FACADE";
    }
}
