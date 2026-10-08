package com.nequi.mdw.common.consumer.registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import com.nequi.mdw.common.consumer.constant.ConstantCommonService;
import com.nequi.mdw.common.consumer.pojo.ServicePojo;

public final class ResourceLocatorSingleton implements ResourceLocator {

    private static ResourceLocatorSingleton instance;

    private volatile Properties registry;

    private ResourceLocatorSingleton() {
        reload();
    }

    public static synchronized ResourceLocator getInstance() {
        if (null == instance) {
            instance = new ResourceLocatorSingleton();
        }
        return instance;
    }

    public ServicePojo getConnectionData(String serviceName, String operation) {
        String prefix = serviceName + ConstantCommonService.COMMON_STRING_DOT + operation
                + ConstantCommonService.COMMON_STRING_DOT;
        String endpoint = resolve(registry.getProperty(prefix + ConstantCommonService.KEY_ENDPOINT));
        if (null == endpoint || endpoint.trim().isEmpty()) {
            return null;
        }
        ServicePojo pojo = new ServicePojo();
        pojo.setServiceName(serviceName);
        pojo.setOperation(operation);
        pojo.setEndpoint(endpoint.trim());
        pojo.setMethod(registry.getProperty(prefix + ConstantCommonService.KEY_METHOD,
                ConstantCommonService.DEFAULT_METHOD).trim().toUpperCase());
        pojo.setConnectTimeout(toInt(registry.getProperty(prefix + ConstantCommonService.KEY_CONNECT_TIMEOUT),
                ConstantCommonService.DEFAULT_CONNECT_TIMEOUT));
        pojo.setReadTimeout(toInt(registry.getProperty(prefix + ConstantCommonService.KEY_READ_TIMEOUT),
                ConstantCommonService.DEFAULT_READ_TIMEOUT));
        String headerPrefix = prefix + ConstantCommonService.KEY_HEADER_PREFIX;
        for (String key : registry.stringPropertyNames()) {
            if (key.startsWith(headerPrefix)) {
                pojo.getHeaders().put(key.substring(headerPrefix.length()), resolve(registry.getProperty(key)));
            }
        }
        return pojo;
    }

    public synchronized void reload() {
        Properties properties = new Properties();
        String path = System.getProperty(ConstantCommonService.REGISTRY_PATH_PROPERTY);
        if (null == path || path.trim().isEmpty()) {
            path = System.getenv(ConstantCommonService.REGISTRY_PATH_ENV);
        }
        if (null != path && !path.trim().isEmpty() && new File(path.trim()).isFile()) {
            loadFile(properties, path.trim());
        } else {
            loadClasspath(properties);
        }
        this.registry = properties;
    }

    private void loadFile(Properties properties, String path) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(path);
            properties.load(new InputStreamReader(stream, ConstantCommonService.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(ConstantCommonService.ERROR_MESSAGE_REGISTRY_LOAD + path, e);
        } finally {
            close(stream);
        }
    }

    private void loadClasspath(Properties properties) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (null == loader) {
            loader = ResourceLocatorSingleton.class.getClassLoader();
        }
        InputStream stream = loader.getResourceAsStream(ConstantCommonService.REGISTRY_CLASSPATH_RESOURCE);
        if (null == stream) {
            return;
        }
        try {
            properties.load(new InputStreamReader(stream, ConstantCommonService.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(ConstantCommonService.ERROR_MESSAGE_REGISTRY_LOAD
                    + ConstantCommonService.REGISTRY_CLASSPATH_RESOURCE, e);
        } finally {
            close(stream);
        }
    }

    private static String resolve(String value) {
        if (null == value) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (index < value.length()) {
            int start = value.indexOf("${", index);
            if (start < 0) {
                result.append(value.substring(index));
                break;
            }
            int end = value.indexOf('}', start);
            if (end < 0) {
                result.append(value.substring(index));
                break;
            }
            result.append(value, index, start);
            String expression = value.substring(start + 2, end);
            String name = expression;
            String defaultValue = ConstantCommonService.COMMON_STRING_EMPTY;
            int separator = expression.indexOf(':');
            if (separator >= 0) {
                name = expression.substring(0, separator);
                defaultValue = expression.substring(separator + 1);
            }
            String replacement = System.getProperty(name);
            if (null == replacement) {
                replacement = System.getenv(name);
            }
            result.append(null == replacement ? defaultValue : replacement);
            index = end + 1;
        }
        return result.toString();
    }

    private static int toInt(String value, int defaultValue) {
        if (null == value || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static void close(InputStream stream) {
        if (null != stream) {
            try {
                stream.close();
            } catch (IOException e) {
                stream = null;
            }
        }
    }
}
