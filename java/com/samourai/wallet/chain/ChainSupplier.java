package com.samourai.wallet.chain;

import com.samourai.wallet.api.backend.beans.WalletResponse;

public interface ChainSupplier {
  WalletResponse.InfoBlock getLatestBlock();
}
