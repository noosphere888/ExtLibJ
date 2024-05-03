package com.samourai.wallet.client.indexHandler;

import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.hd.Chain;

public interface IndexHandlerSupplier {
  IIndexHandler getIndexHandlerExternal();

  IIndexHandler getIndexHandlerWallet(BipWallet bipWallet, Chain chain);
}
