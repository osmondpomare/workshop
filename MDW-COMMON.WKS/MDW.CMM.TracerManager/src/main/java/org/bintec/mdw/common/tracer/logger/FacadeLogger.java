package org.bintec.mdw.common.tracer.logger;

import org.bintec.mdw.common.tracer.service.ServiceTypeEnum;

public class FacadeLogger extends AbstractTracerLogger {

    public FacadeLogger(Class<? extends Object> clase) {
        super(ServiceTypeEnum.FACADE, clase);
    }

    @Override
    protected String componentName() {
        return "FACADE";
    }
}
