package com.samourai.wallet.api.paynym.beans;

public class ClaimPaynymRequest {
  private String signature;

  public ClaimPaynymRequest(String signature) {
    this.signature = signature;
  }

  public String getSignature() {
    return signature;
  }
}
