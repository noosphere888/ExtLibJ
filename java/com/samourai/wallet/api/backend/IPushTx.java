package com.samourai.wallet.api.backend;

import java.util.Collection;

public interface IPushTx {
    String pushTx(String hexTx) throws Exception;
    String pushTx(String txHex, Collection<Integer> strictModeVouts) throws Exception;
}
