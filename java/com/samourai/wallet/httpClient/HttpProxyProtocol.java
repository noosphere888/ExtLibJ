package com.samourai.wallet.httpClient;

import java.util.Optional;

public enum HttpProxyProtocol {
  HTTP,
  SOCKS,
  SOCKS5;

  public static Optional<HttpProxyProtocol> find(String value) {
    try {
      return Optional.of(valueOf(value));
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
