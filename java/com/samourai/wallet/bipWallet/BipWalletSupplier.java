package com.samourai.wallet.bipWallet;

import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.client.indexHandler.IndexHandlerSupplier;
import com.samourai.wallet.hd.HD_Wallet;

import java.util.Collection;

public interface BipWalletSupplier {
    public Collection<BipWallet> getBipWallets(BipFormatSupplier bipFormatSupplier, HD_Wallet bip44w, IndexHandlerSupplier indexHandlerSupplier);
}
