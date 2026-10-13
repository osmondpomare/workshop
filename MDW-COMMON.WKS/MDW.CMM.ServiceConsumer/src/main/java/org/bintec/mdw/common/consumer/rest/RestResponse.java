package org.bintec.mdw.common.consumer.rest;

public class RestResponse {

    private final int statusCode;
    private final String body;
    private final long elapsedMillis;

    public RestResponse(int statusCode, String body, long elapsedMillis) {
        this.statusCode = statusCode;
        this.body = body;
        this.elapsedMillis = elapsedMillis;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
}
