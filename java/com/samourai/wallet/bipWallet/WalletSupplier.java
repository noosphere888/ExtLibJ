package com.samourai.wallet.bipWallet;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.constants.BIP_WALLET;
import com.samourai.wallet.hd.BipAddress;
import com.samourai.wallet.constants.SamouraiAccount;

import java.util.Collection;

public interface WalletSupplier {
  Collection<BipWallet> getWallets();

  Collection<BipWallet> getWallets(SamouraiAccount samouraiAccount);

  BipWallet getWallet(SamouraiAccount account, BipFormat bipFormat);

  BipWallet getWallet(BIP_WALLET bip);

  BipWallet getWalletByXPub(String pub);

  BipWallet getWalletById(String id);

  String[] getXPubs(boolean withIgnoredAccounts, BipFormat... bipFormats);
}
