package org.bintec.mdw.common.consumer.constant;

public final class ConstantCommonService {

    public static final String REGISTRY_PATH_ENV = "SERVICE_REGISTRY_PATH";
    public static final String REGISTRY_PATH_PROPERTY = "service.registry.path";
    public static final String REGISTRY_CLASSPATH_RESOURCE = "service-registry.properties";

    public static final String KEY_ENDPOINT = "endpoint";
    public static final String KEY_METHOD = "method";
    public static final String KEY_CONNECT_TIMEOUT = "connectTimeout";
    public static final String KEY_READ_TIMEOUT = "readTimeout";
    public static final String KEY_HEADER_PREFIX = "header.";

    public static final String DEFAULT_METHOD = "GET";
    public static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    public static final int DEFAULT_READ_TIMEOUT = 10000;

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT = "Accept";
    public static final String APPLICATION_JSON = "application/json";
    public static final String UTF_8 = "UTF-8";

    public static final String ERROR_CODE_SERVICE_NOT_FOUND = "CMM-001";
    public static final String ERROR_CODE_COMMUNICATION = "CMM-002";
    public static final String ERROR_CODE_HTTP_STATUS = "CMM-003";
    public static final String ERROR_CODE_PARSE = "CMM-004";

    public static final String ERROR_MESSAGE_SERVICE_NOT_FOUND = "Service not registered: ";
    public static final String ERROR_MESSAGE_COMMUNICATION = "Communication error invoking: ";
    public static final String ERROR_MESSAGE_HTTP_STATUS = "Unexpected HTTP status from: ";
    public static final String ERROR_MESSAGE_PARSE = "Error parsing JSON payload";
    public static final String ERROR_MESSAGE_REGISTRY_LOAD = "Unable to load service registry from: ";

    public static final String COMMON_STRING_EMPTY = "";
    public static final String COMMON_STRING_DOT = ".";

    private ConstantCommonService() {
    }
}
