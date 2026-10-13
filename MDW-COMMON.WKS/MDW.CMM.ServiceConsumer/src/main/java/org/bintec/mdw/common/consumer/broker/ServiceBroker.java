package org.bintec.mdw.common.consumer.broker;

import java.io.IOException;
import java.util.Map;

import org.bintec.mdw.common.consumer.constant.ConstantCommonService;
import org.bintec.mdw.common.consumer.exception.CommonUtilException;
import org.bintec.mdw.common.consumer.exception.ServiceConsumerException;
import org.bintec.mdw.common.consumer.pojo.ServicePojo;
import org.bintec.mdw.common.consumer.registry.ResourceLocatorSingleton;
import org.bintec.mdw.common.consumer.rest.GenericRestClient;
import org.bintec.mdw.common.consumer.rest.RestResponse;
import org.bintec.mdw.common.consumer.util.CommonServiceConsumerUtil;
import org.bintec.mdw.common.consumer.util.UtilJSON;
import org.bintec.mdw.common.tracer.service.GenericLogger;
import org.bintec.mdw.common.tracer.util.TypeEnum;

public final class ServiceBroker {

    private ServiceBroker() {
    }

    public static String invoke(String serviceName, String operation, Map<String, String> pathParams,
            Map<String, String> queryParams, Object request, String messageId, GenericLogger logger)
            throws ServiceConsumerException {

        ServicePojo service = ResourceLocatorSingleton.getInstance().getConnectionData(serviceName, operation);
        if (null == service) {
            throw new ServiceConsumerException(ConstantCommonService.ERROR_CODE_SERVICE_NOT_FOUND,
                    UtilJSON.generateString(ConstantCommonService.ERROR_MESSAGE_SERVICE_NOT_FOUND, serviceName,
                            ConstantCommonService.COMMON_STRING_DOT, operation));
        }

        String payload = null;
        String url;
        try {
            if (null != request) {
                payload = request instanceof String ? (String) request : UtilJSON.parseObjectToString(request);
            }
            url = CommonServiceConsumerUtil.buildUrl(service.getEndpoint(), pathParams, queryParams);
        } catch (CommonUtilException e) {
            throw new ServiceConsumerException(ConstantCommonService.ERROR_CODE_PARSE,
                    ConstantCommonService.ERROR_MESSAGE_PARSE, e);
        } catch (IOException e) {
            throw new ServiceConsumerException(ConstantCommonService.ERROR_CODE_PARSE,
                    ConstantCommonService.ERROR_MESSAGE_PARSE, e);
        }

        logger.traceInfo(serviceName, operation, messageId,
                UtilJSON.generateString("{\"method\":\"", service.getMethod(), "\",\"url\":\"", url, "\"}"),
                TypeEnum.RQ);

        RestResponse response;
        try {
            response = GenericRestClient.execute(service, url, payload);
        } catch (IOException e) {
            logger.traceError(UtilJSON.generateString(ConstantCommonService.ERROR_MESSAGE_COMMUNICATION, url), e,
                    messageId, null);
            throw new ServiceConsumerException(ConstantCommonService.ERROR_CODE_COMMUNICATION,
                    UtilJSON.generateString(ConstantCommonService.ERROR_MESSAGE_COMMUNICATION, url), e);
        }

        logger.traceInfo(serviceName, operation, messageId,
                UtilJSON.generateString("{\"statusCode\":", String.valueOf(response.getStatusCode()),
                        ",\"elapsedMillis\":", String.valueOf(response.getElapsedMillis()), "}"),
                TypeEnum.RS);

        if (!response.isSuccessful()) {
            throw new ServiceConsumerException(ConstantCommonService.ERROR_CODE_HTTP_STATUS,
                    UtilJSON.generateString(ConstantCommonService.ERROR_MESSAGE_HTTP_STATUS, url),
                    response.getStatusCode(), null);
        }
        return response.getBody();
    }

    public static <T> T invoke(String serviceName, String operation, Map<String, String> pathParams,
            Map<String, String> queryParams, Object request, Class<T> responseType, String messageId,
            GenericLogger logger) throws ServiceConsumerException {
        String body = invoke(serviceName, operation, pathParams, queryParams, request, messageId, logger);
        try {
            return UtilJSON.parsePayload(body, responseType);
        } catch (CommonUtilException e) {
            throw new ServiceConsumerException(ConstantCommonService.ERROR_CODE_PARSE,
                    ConstantCommonService.ERROR_MESSAGE_PARSE, e);
        }
    }
}
