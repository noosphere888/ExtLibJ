package com.samourai.wallet.api.paynym;

public enum PaynymServer {
  PAYNYM_IS("https://paynym.is/api/v1");

  private String urlClear;

  PaynymServer(String urlClear) {
    this.urlClear = urlClear;
  }

  public String getUrl() {
    return urlClear;
  }

  public static PaynymServer get() {
    return PAYNYM_IS;
  }
}
