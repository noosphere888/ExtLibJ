package com.samourai.wallet.api.backend.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class BackendPushTxResponse {
  private static Logger log = LoggerFactory.getLogger(BackendPushTxResponse.class);
  private static final String CODE_VIOLATION_STRICT_MODE_VOUTS = "VIOLATION_STRICT_MODE_VOUTS";
  private static final String ERROR_KEY_MESSAGE="message";
  private static final String ERROR_KEY_CODE="code";

  public PushTxStatus status;
  public String data;
  public Object error; // string when error from node, PushTxError when address reuse

  public BackendPushTxResponse() {}

  public Map<String,Object> getErrorMap() {
    if (error == null || !(error instanceof  Map)) {
      return null;
    }
    return (Map)error;
  }

  public boolean isErrorAddressReuse() {
    Map errorMap = getErrorMap();
    if (errorMap == null) {
      return false;
    }
    String errorCode = (String)errorMap.get(ERROR_KEY_CODE);
    return errorCode != null && CODE_VIOLATION_STRICT_MODE_VOUTS.equals(errorCode);
  }

  public Collection<Integer> getAdressReuseOutputIndexs() {
    if (isErrorAddressReuse()) {
      return (Collection<Integer>)getErrorMap().get(ERROR_KEY_MESSAGE);
    }
    return new LinkedList<>();
  }

  public enum PushTxStatus {
    ok, error
  }

  @Override
  public String toString() {
    return "PushTxResponse{" +
            "status=" + status +
            ", data='" + data + '\'' +
            ", error=" + error +
            '}';
  }
}

