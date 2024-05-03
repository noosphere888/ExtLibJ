package com.samourai.wallet.api.backend.seenBackend;

import java.util.Map;

public class SeenResponse {
  private Map<String, Boolean> seenResponse;

  public SeenResponse(Map<String, Boolean> seenResponse) {
    this.seenResponse = seenResponse;
  }

  public boolean isSeen(String address) {
    return seenResponse.containsKey(address) && seenResponse.get(address);
  }
}
