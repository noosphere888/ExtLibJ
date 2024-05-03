package com.samourai.wallet.httpClient;

import com.samourai.wallet.api.backend.beans.HttpException;

public class HttpSystemException extends HttpException {
    public HttpSystemException(String message) {
        super(message);
    }
    public HttpSystemException(Exception cause) {
        super(cause);
    }
}
