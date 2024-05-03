package com.samourai.wallet.api.backend.beans;

import java.util.Collection;

public class BackendPushTxException extends Exception {
  private String pushTxError;
  private Collection<Integer> voutsAddressReuse;

  public BackendPushTxException(String pushTxError) {
    this(pushTxError, null);
  }

  public BackendPushTxException(String pushTxError, Collection<Integer> voutsAddressReuse) {
    super(pushTxError);
    this.pushTxError = pushTxError;
    this.voutsAddressReuse = voutsAddressReuse;
  }

  public String getPushTxError() {
    return pushTxError;
  }

  public Collection<Integer> getVoutsAddressReuse() {
    return voutsAddressReuse;
  }
}
