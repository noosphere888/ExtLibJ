package com.samourai.wallet.cahoots;

import com.samourai.wallet.bip47.rpc.BIP47Account;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.chain.ChainSupplier;

import java.util.List;

public interface CahootsWallet {
    List<CahootsUtxo> getUtxosWpkhByAccount(int account);
    String fetchAddressReceive(int account, boolean increment, BipFormat bipFormat) throws Exception;
    String fetchAddressChange(int account, boolean increment, BipFormat bipFormat) throws Exception;
    ChainSupplier getChainSupplier();
    byte[] getFingerprint();
    BIP47Account getBip47Account();
}
