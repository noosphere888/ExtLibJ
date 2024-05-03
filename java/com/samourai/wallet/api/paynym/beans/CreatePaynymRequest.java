package com.samourai.wallet.api.paynym.beans;

public class CreatePaynymRequest {
  private String code;

  public CreatePaynymRequest(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
