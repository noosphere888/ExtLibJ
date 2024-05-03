package com.samourai.wallet.api.paynym.beans;

import java.util.Collection;

public class GetNymInfoResponse {
  public Collection<PaynymCode> codes;
  public String nymID;
  public String nymName;
  public String nymAvatar;
  public boolean segwit;
  public Collection<PaynymContact> following;
  public Collection<PaynymContact> followers;

  public GetNymInfoResponse() {}
}
