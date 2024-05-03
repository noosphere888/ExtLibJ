package com.samourai.wallet.bipFormat;

import com.samourai.wallet.hd.HD_Account;
import com.samourai.wallet.hd.HD_Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;

public interface BipFormat {
  String getId();
  String getLabel();
  String getBipPub(HD_Account hdAccount);
  String getAddressString(HD_Address hdAddress);
  String getToAddress(ECKey ecKey, NetworkParameters params);
  void sign(Transaction tx, int inputIndex, ECKey key) throws Exception;
}
