package com.samourai.wallet.api.paynym.beans;

import com.samourai.wallet.bip47.rpc.PaymentCode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

public class PaynymState {
  private boolean claimed;
  private PaymentCode paymentCode;
  private String nymID; // null when not claimed
  private String nymName; // null when not claimed
  private String nymAvatar; // null when not claimed
  private Boolean segwit; // null when not claimed
  private Collection<PaynymContact> following;
  private Collection<PaynymContact> followers;

  // claimed
  public PaynymState(PaymentCode paymentCode, String nymID, String nymName, String nymAvatar, boolean segwit, Collection<PaynymContact> following, Collection<PaynymContact> followers) {
    this.claimed = true;
    this.paymentCode = paymentCode;
    this.nymID = nymID;
    this.nymName = nymName;
    this.nymAvatar = nymAvatar;
    this.segwit = segwit;
    this.following = following;
    this.followers = followers;
  }

  // not claimed
  public PaynymState(PaymentCode paymentCode) {
    this.claimed = false;
    this.paymentCode = paymentCode;
    this.nymID = null;
    this.nymName = null;
    this.nymAvatar = null;
    this.segwit = null;
    this.following = new LinkedList<>();
    this.followers = new LinkedList<>();
  }

  public boolean equals(PaynymState o) {
    if (this == o) return true;
    return claimed == o.claimed &&
            Objects.equals(paymentCode, o.paymentCode) &&
            Objects.equals(nymID, o.nymID) &&
            Objects.equals(nymName, o.nymName) &&
            Objects.equals(nymAvatar, o.nymAvatar) &&
            Objects.equals(segwit, o.segwit) &&
            Objects.equals(following, o.following) &&
            Objects.equals(followers, o.followers);
  }

  public boolean isClaimed() {
    return claimed;
  }

  public PaymentCode getPaymentCode() {
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

  public Boolean isSegwit() {
    return segwit;
  }

  public Collection<PaynymContact> getFollowing() {
    return following;
  }

  public Collection<PaynymContact> getFollowers() {
    return followers;
  }
}
