package org.bintec.mdw.common.tracer.service;

import org.bintec.mdw.common.tracer.logger.BackOutLogger;
import org.bintec.mdw.common.tracer.logger.BusinessLogger;
import org.bintec.mdw.common.tracer.logger.FacadeLogger;
import org.bintec.mdw.common.tracer.logger.IntegrationLogger;
import org.bintec.mdw.common.tracer.logger.UtilLogger;

public final class LoggerFactory {

    private LoggerFactory() {
    }

    public static synchronized GenericLogger getLogger(ServiceTypeEnum serviceType, Class<? extends Object> clase) {
        GenericLogger genericLogger;
        switch (serviceType) {
        case INTEGRATION:
            genericLogger = new IntegrationLogger(clase);
            break;
        case BUSSINES:
            genericLogger = new BusinessLogger(clase);
            break;
        case BACKOUT:
            genericLogger = new BackOutLogger(clase);
            break;
        case FACADE:
            genericLogger = new FacadeLogger(clase);
            break;
        default:
            genericLogger = new UtilLogger(clase);
            break;
        }
        return genericLogger;
    }
}
