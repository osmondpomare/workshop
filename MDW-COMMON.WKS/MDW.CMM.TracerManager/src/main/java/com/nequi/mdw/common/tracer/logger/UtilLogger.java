package com.nequi.mdw.common.tracer.logger;

import com.nequi.mdw.common.tracer.service.ServiceTypeEnum;

public class UtilLogger extends AbstractTracerLogger {

    public UtilLogger(Class<? extends Object> clase) {
        super(ServiceTypeEnum.UTIL, clase);
    }

    @Override
    protected String componentName() {
        return "UTIL";
    }
}
