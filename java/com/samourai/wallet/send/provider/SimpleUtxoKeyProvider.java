package com.samourai.wallet.send.provider;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.TransactionOutPoint;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleUtxoKeyProvider implements UtxoKeyProvider {
  private BipFormatSupplier bipFormatSupplier;
  private Map<String, ECKey> keys;

  public SimpleUtxoKeyProvider() {
    this(BIP_FORMAT.PROVIDER);
  }

  public SimpleUtxoKeyProvider(BipFormatSupplier bipFormatSupplier) {
    this.bipFormatSupplier = bipFormatSupplier;
    this.keys = new LinkedHashMap<>();
  }

  public void setKey(TransactionOutPoint outPoint, ECKey key) {
    keys.put(outPoint.toString(), key);
  }

  @Override
  public byte[] _getPrivKey(String utxoHash, int utxoIndex) throws Exception {
    ECKey ecKey = keys.get(utxoHash + ":" + utxoIndex);
    if (ecKey == null) {
      return null;
    }
    return ecKey.getPrivKeyBytes();
  }

  @Override
  public byte[] _getPrivKeyBip47(UnspentOutput unspentOutput) throws Exception {
    throw new Exception("_getPrivKeyBip47 not implemented yet");
  }

  @Override
  public BipFormatSupplier getBipFormatSupplier() {
    return bipFormatSupplier;
  }
}
