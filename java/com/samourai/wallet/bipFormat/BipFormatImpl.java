package com.samourai.wallet.bipFormat;

import com.samourai.wallet.hd.HD_Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BipFormatImpl implements BipFormat {
  private static final Logger log = LoggerFactory.getLogger(BipFormatImpl.class);

  private String id;
  private String label;

  public BipFormatImpl(String id, String label) {
    this.id = id;
    this.label = label;
  }

  @Override
  public String getAddressString(HD_Address hdAddress) {
    return getToAddress(hdAddress.getECKey(), hdAddress.getParams());
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public String toString() {
    return id;
  }
}
