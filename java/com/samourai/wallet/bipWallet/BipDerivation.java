package com.samourai.wallet.bipWallet;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.util.FormatsUtilGeneric;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BipDerivation {
  private static final Logger log = LoggerFactory.getLogger(BipDerivation.class);

  private int purpose;
  private int accountIndex;

  public BipDerivation(int purpose, int accountIndex) {
    this.purpose = purpose;
    this.accountIndex = accountIndex;
  }

  public String getPathAccount(NetworkParameters params) {
    int coinType = FormatsUtilGeneric.getInstance().getCoinType(params);
    return HD_Address.getPathAccount(purpose, coinType, accountIndex);
  }

  public String getPathChain(int chainIndex, NetworkParameters params) {
    int coinType = FormatsUtilGeneric.getInstance().getCoinType(params);
    return HD_Address.getPathChain(purpose, coinType, accountIndex, chainIndex);
  }

  public String getPathAddress(UnspentOutput utxo, NetworkParameters params) {
    return utxo.getPathAddress(purpose, accountIndex, params);
  }

  public String getPathAddress(HD_Address hdAddress) {
    int coinType = FormatsUtilGeneric.getInstance().getCoinType(hdAddress.getParams());
    return HD_Address.getPathAddress(purpose, coinType, accountIndex, hdAddress.getChainIndex(), hdAddress.getAddressIndex());
  }

  public int getPurpose() {
    return purpose;
  }

  public int getAccountIndex() {
    return accountIndex;
  }

  @Override
  public String toString() {
    return "BipDerivation{" +
            "purpose=" + purpose +
            ", accountIndex=" + accountIndex +
            '}';
  }
}
