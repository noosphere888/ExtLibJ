package com.samourai.wallet.api.backend;

import com.samourai.wallet.dexConfig.DexConfigProvider;

public enum BackendServer {
  MAINNET(
          DexConfigProvider.getInstance().getSamouraiConfig().getBackendServerMainnetClear(),
          DexConfigProvider.getInstance().getSamouraiConfig().getBackendServerMainnetOnion()),
  TESTNET(
          DexConfigProvider.getInstance().getSamouraiConfig().getBackendServerTestnetClear(),
          DexConfigProvider.getInstance().getSamouraiConfig().getBackendServerTestnetOnion());

  private String backendUrlClear;
  private String backendUrlOnion;

  BackendServer(String backendUrlClear, String backendUrlOnion) {
    this.backendUrlClear = backendUrlClear;
    this.backendUrlOnion = backendUrlOnion;
  }

  public String getBackendUrl(boolean onion) {
    return onion ? backendUrlOnion : backendUrlClear;
  }

  public String getBackendUrlClear() {
    return backendUrlClear;
  }

  public String getBackendUrlOnion() {
    return backendUrlOnion;
  }

  public static BackendServer get(boolean isTestnet) {
    return isTestnet ? BackendServer.TESTNET : BackendServer.MAINNET;
  }
}
