package com.samourai.wallet.httpClient;

import com.samourai.wallet.api.backend.beans.HttpException;

public class HttpNetworkException extends HttpException {
    public HttpNetworkException(String message) {
        super(message);
    }
    public HttpNetworkException(Exception cause) {
        super(cause);
    }
}
