package com.samourai.wallet.api.paynym.beans;

public class UnfollowPaynymRequest {
  private String target;
  private String signature;

  public UnfollowPaynymRequest(String target, String signature) {
    this.target = target;
    this.signature = signature;
  }

  public String getTarget() {
    return target;
  }

  public String getSignature() {
    return signature;
  }
}
