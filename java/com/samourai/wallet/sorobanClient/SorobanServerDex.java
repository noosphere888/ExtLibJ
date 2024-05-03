package com.samourai.wallet.sorobanClient;

import com.samourai.wallet.dexConfig.DexConfigProvider;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.wallet.util.RandomUtil;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

public enum SorobanServerDex {
  TESTNET(
          DexConfigProvider.getInstance().getSamouraiConfig().getSorobanServerDexTestnetClear(),
          DexConfigProvider.getInstance().getSamouraiConfig().getSorobanServerDexTestnetOnion(),
          TestNet3Params.get()),
  MAINNET(
          DexConfigProvider.getInstance().getSamouraiConfig().getSorobanServerDexMainnetClear(),
          DexConfigProvider.getInstance().getSamouraiConfig().getSorobanServerDexMainnetOnion(),
          MainNetParams.get());

  private static final Logger log = LoggerFactory.getLogger(SorobanServerDex.class);

  private Collection<String> sorobanUrlsClear;
  private Collection<String> sorobanUrlsOnion;
  private NetworkParameters params;

  SorobanServerDex(Collection<String> sorobanUrlsClear, Collection<String> sorobanUrlsOnion, NetworkParameters params) {
    this.sorobanUrlsClear = sorobanUrlsClear;
    this.sorobanUrlsOnion = sorobanUrlsOnion;
    this.params = params;
  }

  public Collection<String> getSorobanUrlsClear() {
    return sorobanUrlsClear;
  }

  public void setSorobanUrlsClear(Collection<String> sorobanUrlsClear) {
    this.sorobanUrlsClear = sorobanUrlsClear;
  }

  public Collection<String> getSorobanUrlsOnion() {
    return sorobanUrlsOnion;
  }

  public void setSorobanUrlsOnion(Collection<String> sorobanUrlsOnion) {
    this.sorobanUrlsOnion = sorobanUrlsOnion;
  }

  public String getSorobanUrlRandom(boolean onion) {
    String url = RandomUtil.getInstance().next(getSorobanUrls(onion));
    if (log.isDebugEnabled()) {
      log.debug("using SorobanServer: "+url);
    }
    return url;
  }

  public Collection<String> getSorobanUrls(boolean onion) {
    Collection<String> sorobanUrls = onion ? getSorobanUrlsOnion() : getSorobanUrlsClear();
    return sorobanUrls;
  }

  public NetworkParameters getParams() {
    return params;
  }

  public static Optional<SorobanServerDex> find(String value) {
    try {
      return Optional.of(valueOf(value));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public static SorobanServerDex get(NetworkParameters params) {
    if (FormatsUtilGeneric.getInstance().isTestNet(params)) {
      return TESTNET;
    }
    return MAINNET;
  }
}
