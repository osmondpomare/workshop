package org.bintec.mdw.common.tracer.logger;

import org.bintec.mdw.common.tracer.service.ServiceTypeEnum;

public class BackOutLogger extends AbstractTracerLogger {

    public BackOutLogger(Class<? extends Object> clase) {
        super(ServiceTypeEnum.BACKOUT, clase);
    }

    @Override
    protected String componentName() {
        return "BACKOUT";
    }
}
