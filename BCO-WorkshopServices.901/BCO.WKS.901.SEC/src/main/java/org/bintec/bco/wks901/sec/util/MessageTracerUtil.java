package org.bintec.bco.wks901.sec.util;

import org.bintec.mdw.common.consumer.util.UtilJSON;
import org.bintec.mdw.common.tracer.service.GenericLogger;
import org.bintec.mdw.common.tracer.util.TypeEnum;

public final class MessageTracerUtil {

    private MessageTracerUtil() {
    }

    public static void traceRequestMessage(Object header, Object payload, GenericLogger logger) {
        trace(header, payload, TypeEnum.RQ, logger);
    }

    public static void traceResponseMessage(Object header, Object payload, GenericLogger logger) {
        trace(header, payload, TypeEnum.RS, logger);
    }

    private static void trace(Object header, Object payload, TypeEnum type, GenericLogger logger) {
        String headerString = ConstantWKS901.COMMON_STRING_EMPTY;
        String bodyString = ConstantWKS901.COMMON_STRING_EMPTY;
        try {
            if (null != header) {
                headerString = UtilJSON.parseObjectToString(header);
            }
            if (null != payload) {
                bodyString = UtilJSON.parseObjectToString(payload);
            }
            logger.traceInfo(headerString, bodyString, type);
        } catch (Exception e) {
            logger.traceError(ConstantWKS901.TRACE_PARSE_FAILED, e);
        }
    }
}
