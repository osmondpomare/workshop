package com.nequi.mdw.common.tracer.logger;

import com.nequi.mdw.common.tracer.service.ServiceTypeEnum;

public class BackOutLogger extends AbstractTracerLogger {

    public BackOutLogger(Class<? extends Object> clase) {
        super(ServiceTypeEnum.BACKOUT, clase);
    }

    @Override
    protected String componentName() {
        return "BACKOUT";
    }
}
