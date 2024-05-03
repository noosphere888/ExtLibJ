package com.samourai.wallet.api.paynym.beans;

public class FollowPaynymRequest {
  private String target;
  private String signature;

  public FollowPaynymRequest(String target, String signature) {
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
