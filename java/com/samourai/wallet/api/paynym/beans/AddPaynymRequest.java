package com.samourai.wallet.api.paynym.beans;

public class AddPaynymRequest {
  private String nym;
  private String code;
  private String signature;

  public AddPaynymRequest(String nym, String code, String signature) {
    this.nym = nym;
    this.code = code;
    this.signature = signature;
  }

  public String getNym() {
    return nym;
  }

  public String getCode() {
    return code;
  }

  public String getSignature() {
    return signature;
  }
}
