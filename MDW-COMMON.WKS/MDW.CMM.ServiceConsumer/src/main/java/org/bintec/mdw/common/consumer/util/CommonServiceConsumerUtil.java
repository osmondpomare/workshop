package org.bintec.mdw.common.consumer.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.bintec.mdw.common.consumer.constant.ConstantCommonService;
import org.bintec.mdw.common.consumer.exception.CommonUtilException;
import org.bintec.mdw.common.tracer.service.GenericLogger;

public final class CommonServiceConsumerUtil {

    private CommonServiceConsumerUtil() {
    }

    public static boolean isEmpty(String value) {
        return null == value || value.trim().isEmpty();
    }

    public static <T> boolean validateRequest(String json, Class<T> type, String operation, GenericLogger logger) {
        if (isEmpty(json)) {
            logger.traceError(UtilJSON.generateString("Empty request for operation ", operation), null);
            return false;
        }
        try {
            UtilJSON.parsePayload(json, type);
            return true;
        } catch (CommonUtilException e) {
            logger.traceError(UtilJSON.generateString("Invalid request for operation ", operation), e);
            return false;
        }
    }

    public static String buildUrl(String endpoint, Map<String, String> pathParams, Map<String, String> queryParams)
            throws UnsupportedEncodingException {
        String url = endpoint;
        if (null != pathParams) {
            for (Map.Entry<String, String> param : pathParams.entrySet()) {
                url = url.replace("{" + param.getKey() + "}",
                        URLEncoder.encode(param.getValue(), ConstantCommonService.UTF_8));
            }
        }
        if (null != queryParams && !queryParams.isEmpty()) {
            StringBuilder builder = new StringBuilder(url);
            builder.append(url.contains("?") ? '&' : '?');
            boolean first = true;
            for (Map.Entry<String, String> param : queryParams.entrySet()) {
                if (!first) {
                    builder.append('&');
                }
                first = false;
                builder.append(URLEncoder.encode(param.getKey(), ConstantCommonService.UTF_8)).append('=')
                        .append(URLEncoder.encode(param.getValue(), ConstantCommonService.UTF_8));
            }
            url = builder.toString();
        }
        return url;
    }
}
