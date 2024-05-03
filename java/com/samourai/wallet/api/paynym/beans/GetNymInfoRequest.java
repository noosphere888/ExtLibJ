package com.samourai.wallet.api.paynym.beans;

public class GetNymInfoRequest {
  private String nym;

  public GetNymInfoRequest(String nym) {
    this.nym = nym;
  }

  public String getNym() {
    return nym;
  }
}
