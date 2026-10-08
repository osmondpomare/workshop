package com.nequi.mdw.common.tracer.service;

import com.nequi.mdw.common.tracer.util.TypeEnum;

public interface GenericLogger {

    void traceInfo(String infoMessage);

    void traceInfo(String jsonHeader, String jsonBody, TypeEnum type);

    void traceInfo(String serviceName, String operation, String messageId, String jsonBody, TypeEnum type);

    void traceDebug(String message);

    void traceDebug(Boolean isDebugable, String message);

    void traceError(String errorMessage, Throwable ex);

    void traceError(String errorMessage, Throwable ex, String messageId, String clientId);
}
