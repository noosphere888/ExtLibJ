package com.samourai.wallet.httpClient;

import com.samourai.wallet.api.backend.beans.HttpException;

public class HttpResponseException extends HttpException {
    private String responseBody;
    private int statusCode;

    public HttpResponseException(Exception cause, String responseBody, int statusCode) {
        super(cause);
        this.responseBody = responseBody;
        this.statusCode = statusCode;
    }

    public HttpResponseException(String message, String responseBody, int statusCode) {
        super(message);
        this.responseBody = responseBody;
        this.statusCode = statusCode;
    }

    public HttpResponseException(String responseBody, int statusCode) {
        this("response statusCode="+statusCode,
        responseBody, statusCode);
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "HttpResponseException{" +
                "message=" + getMessage() + ", " +
                "responseBody='" + responseBody + '\'' +
                '}';
    }
}
