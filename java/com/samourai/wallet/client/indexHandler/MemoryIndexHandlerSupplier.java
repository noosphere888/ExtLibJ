package com.samourai.wallet.client.indexHandler;

import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.hd.Chain;
import org.bitcoinj.core.NetworkParameters;

import java.util.LinkedHashMap;
import java.util.Map;

public class MemoryIndexHandlerSupplier implements IndexHandlerSupplier {
  private Map<String,IIndexHandler> indexHandlers = new LinkedHashMap<>();

  @Override
  public IIndexHandler getIndexHandlerWallet(BipWallet bipWallet, Chain chain) {
    NetworkParameters params = bipWallet.getParams();
    String pathAccount = bipWallet.getDerivation().getPathChain(chain.getIndex(), params);
    if (!indexHandlers.containsKey(pathAccount)) {
      indexHandlers.put(pathAccount, new MemoryIndexHandler());
    }
    return indexHandlers.get(pathAccount);
  }

  @Override
  public IIndexHandler getIndexHandlerExternal() {
    return new MemoryIndexHandler();
  }
}
