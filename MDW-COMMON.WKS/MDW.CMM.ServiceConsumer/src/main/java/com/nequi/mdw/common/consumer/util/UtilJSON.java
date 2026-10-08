package com.nequi.mdw.common.consumer.util;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nequi.mdw.common.consumer.constant.ConstantCommonService;
import com.nequi.mdw.common.consumer.exception.CommonUtilException;

public final class UtilJSON {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private UtilJSON() {
    }

    public static <T> T parsePayload(String json, Class<T> type) throws CommonUtilException {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new CommonUtilException(ConstantCommonService.ERROR_MESSAGE_PARSE, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(String json) throws CommonUtilException {
        return parsePayload(json, Map.class);
    }

    public static JsonNode parseToTree(String json) throws CommonUtilException {
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            throw new CommonUtilException(ConstantCommonService.ERROR_MESSAGE_PARSE, e);
        }
    }

    public static String parseObjectToString(Object value) throws CommonUtilException {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (IOException e) {
            throw new CommonUtilException(ConstantCommonService.ERROR_MESSAGE_PARSE, e);
        }
    }

    public static String generateString(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (null != value) {
                builder.append(value);
            }
        }
        return builder.toString();
    }
}
