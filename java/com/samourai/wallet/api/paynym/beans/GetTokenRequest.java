package com.samourai.wallet.api.paynym.beans;

public class GetTokenRequest {
  private String code;

  public GetTokenRequest(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
