package com.samourai.wallet.api.paynym.beans;

public class PaynymClaim {
  private String paymentCode;
  private String nymID;
  private String nymName;
  private String nymAvatar;

  public PaynymClaim(String paymentCode, String nymID, String nymName, String nymAvatar) {
    this.paymentCode = paymentCode;
    this.nymID = nymID;
    this.nymName = nymName;
    this.nymAvatar = nymAvatar;
  }

  public String getPaymentCode() {
    return paymentCode;
  }

  public String getNymID() {
    return nymID;
  }

  public String getNymName() {
    return nymName;
  }

  public String getNymAvatar() {
    return nymAvatar;
  }
}
