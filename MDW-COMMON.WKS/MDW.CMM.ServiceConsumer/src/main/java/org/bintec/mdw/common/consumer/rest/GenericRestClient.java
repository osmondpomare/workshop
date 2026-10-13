package org.bintec.mdw.common.consumer.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.bintec.mdw.common.consumer.constant.ConstantCommonService;
import org.bintec.mdw.common.consumer.pojo.ServicePojo;

public final class GenericRestClient {

    private GenericRestClient() {
    }

    public static RestResponse execute(ServicePojo service, String url, String payload) throws IOException {
        long start = System.currentTimeMillis();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setRequestMethod(service.getMethod());
            connection.setConnectTimeout(service.getConnectTimeout());
            connection.setReadTimeout(service.getReadTimeout());
            connection.setRequestProperty(ConstantCommonService.ACCEPT, ConstantCommonService.APPLICATION_JSON);
            for (Map.Entry<String, String> header : service.getHeaders().entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            if (null != payload && !"GET".equals(service.getMethod())) {
                connection.setDoOutput(true);
                connection.setRequestProperty(ConstantCommonService.CONTENT_TYPE, ConstantCommonService.APPLICATION_JSON);
                OutputStream output = connection.getOutputStream();
                try {
                    output.write(payload.getBytes(ConstantCommonService.UTF_8));
                } finally {
                    output.close();
                }
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String body = read(stream);
            return new RestResponse(status, body, System.currentTimeMillis() - start);
        } finally {
            connection.disconnect();
        }
    }

    private static String read(InputStream stream) throws IOException {
        if (null == stream) {
            return ConstantCommonService.COMMON_STRING_EMPTY;
        }
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = stream.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return new String(buffer.toByteArray(), ConstantCommonService.UTF_8);
        } finally {
            stream.close();
        }
    }
}
