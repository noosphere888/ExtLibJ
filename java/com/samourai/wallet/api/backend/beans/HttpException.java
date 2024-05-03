package com.samourai.wallet.api.backend.beans;

public abstract class HttpException extends Exception {

  public HttpException(Exception cause) {
    super(cause);
  }

  public HttpException(String message) {
    super(message);
  }
}
