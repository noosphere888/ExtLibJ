package com.samourai.wallet.sorobanClient;

import com.samourai.wallet.dexConfig.DexConfigProvider;
import com.samourai.wallet.util.FormatsUtilGeneric;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

import java.util.Optional;

public enum SorobanServer {
  TESTNET(
          DexConfigProvider.getInstance().getSamouraiConfig().getSorobanServerTestnetClear(),
          DexConfigProvider.getInstance().getSamouraiConfig().getSorobanServerTestnetOnion(),
          TestNet3Params.get()),
  MAINNET(
          DexConfigProvider.getInstance().getSamouraiConfig().getSorobanServerMainnetClear(),
          DexConfigProvider.getInstance().getSamouraiConfig().getSorobanServerMainnetOnion(),
          MainNetParams.get());

  private String sorobanUrlClear;
  private String sorobanUrlOnion;
  private NetworkParameters params;

  SorobanServer(String sorobanUrlClear, String sorobanUrlOnion, NetworkParameters params) {
    this.sorobanUrlClear = sorobanUrlClear;
    this.sorobanUrlOnion = sorobanUrlOnion;
    this.params = params;
  }

  public String getSorobanUrlClear() {
    return sorobanUrlClear;
  }

  public String getSorobanUrlOnion() {
    return sorobanUrlOnion;
  }

  public String getSorobanUrl(boolean onion) {
    String sorobanUrl = onion ? getSorobanUrlOnion() : getSorobanUrlClear();
    return sorobanUrl;
  }

  public NetworkParameters getParams() {
    return params;
  }

  public static Optional<SorobanServer> find(String value) {
    try {
      return Optional.of(valueOf(value));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public static SorobanServer get(NetworkParameters params) {
    if (FormatsUtilGeneric.getInstance().isTestNet(params)) {
      return TESTNET;
    }
    return MAINNET;
  }
}
