package org.bintec.mdw.common.tracer.logger;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bintec.mdw.common.tracer.service.GenericLogger;
import org.bintec.mdw.common.tracer.service.ServiceTypeEnum;
import org.bintec.mdw.common.tracer.util.CommonHelper;
import org.bintec.mdw.common.tracer.util.TracerConfig;
import org.bintec.mdw.common.tracer.util.TypeEnum;

public abstract class AbstractTracerLogger implements GenericLogger {

    private static final String LOGGER_PREFIX = "org.bintec.tracer.";

    private final Logger logger;
    private final String className;
    private final ServiceTypeEnum serviceType;

    protected AbstractTracerLogger(ServiceTypeEnum serviceType, Class<? extends Object> clase) {
        this.serviceType = serviceType;
        this.className = clase.getName();
        this.logger = Logger.getLogger(LOGGER_PREFIX + serviceType.name() + "." + clase.getSimpleName());
    }

    public void traceInfo(String infoMessage) {
        Map<String, Object> entry = baseEntry("INFO");
        entry.put("message", infoMessage);
        write(Level.INFO, entry);
    }

    public void traceInfo(String jsonHeader, String jsonBody, TypeEnum type) {
        Map<String, Object> entry = baseEntry("INFO");
        entry.put("type", type.name());
        entry.put("header", CommonHelper.jsonOrText(jsonHeader));
        entry.put("body", CommonHelper.jsonOrText(jsonBody));
        write(Level.INFO, entry);
    }

    public void traceInfo(String serviceName, String operation, String messageId, String jsonBody, TypeEnum type) {
        Map<String, Object> entry = baseEntry("INFO");
        entry.put("type", type.name());
        entry.put("serviceName", serviceName);
        entry.put("operation", operation);
        entry.put("messageId", messageId);
        entry.put("body", CommonHelper.jsonOrText(jsonBody));
        write(Level.INFO, entry);
    }

    public void traceDebug(String message) {
        traceDebug(Boolean.valueOf(TracerConfig.isDebugEnabled()), message);
    }

    public void traceDebug(Boolean isDebugable, String message) {
        if (null == isDebugable || !isDebugable.booleanValue()) {
            return;
        }
        Map<String, Object> entry = baseEntry("DEBUG");
        entry.put("message", message);
        write(Level.INFO, entry);
    }

    public void traceError(String errorMessage, Throwable ex) {
        traceError(errorMessage, ex, null, null);
    }

    public void traceError(String errorMessage, Throwable ex, String messageId, String clientId) {
        Map<String, Object> entry = baseEntry("ERROR");
        entry.put("messageId", messageId);
        entry.put("clientId", clientId);
        entry.put("message", errorMessage);
        if (null != ex) {
            entry.put("exception", ex.getClass().getName());
            entry.put("exceptionMessage", ex.getMessage());
            entry.put("stackTrace", CommonHelper.stackTrace(ex));
        }
        write(Level.SEVERE, entry);
    }

    private Map<String, Object> baseEntry(String level) {
        Map<String, Object> entry = CommonHelper.newEntry();
        entry.put("timestamp", CommonHelper.currentDate());
        entry.put("level", level);
        entry.put("serviceType", serviceType.name());
        entry.put("component", componentName());
        entry.put("class", className);
        entry.put("thread", Thread.currentThread().getName());
        return entry;
    }

    private void write(Level level, Map<String, Object> entry) {
        if (!TracerConfig.isTraceable() && level != Level.SEVERE) {
            return;
        }
        logger.log(level, CommonHelper.toJson(entry));
    }

    protected abstract String componentName();
}
