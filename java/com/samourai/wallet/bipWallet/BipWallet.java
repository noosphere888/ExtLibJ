package com.samourai.wallet.bipWallet;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.client.indexHandler.IIndexHandler;
import com.samourai.wallet.client.indexHandler.IndexHandlerSupplier;
import com.samourai.wallet.constants.SamouraiAccount;
import com.samourai.wallet.hd.*;
import com.samourai.wallet.util.Util;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class BipWallet {
  private static final Logger log = LoggerFactory.getLogger(BipWallet.class);

  private BipFormatSupplier bipFormatSupplier;
  private String id;
  private HD_Wallet hdWallet;
  private HD_Account hdAccount;
  private IndexHandlerSupplier indexHandlerSupplier;
  private SamouraiAccount samouraiAccount;
  private BipDerivation derivation;
  private Collection<BipFormat> bipFormats;
  private BipFormat bipFormatDefault;
  private String bipPub; // xpub, ypub, zpub...
  private String xPub; // pub forced as xpub

  public BipWallet(BipFormatSupplier bipFormatSupplier, String id, HD_Wallet bip44w, IndexHandlerSupplier indexHandlerSupplier, SamouraiAccount samouraiAccount, BipDerivation derivation, Collection<BipFormat> bipFormats, BipFormat bipFormatDefault) {
    this.bipFormatSupplier = bipFormatSupplier;
    this.id = id;
    this.hdWallet = new HD_Wallet(derivation.getPurpose(), bip44w);
    this.hdAccount = this.hdWallet.getAccount(derivation.getAccountIndex());
    this.indexHandlerSupplier = indexHandlerSupplier;
    this.samouraiAccount = samouraiAccount;
    this.derivation = derivation;
    this.bipFormats = bipFormats;
    this.bipFormatDefault = bipFormatDefault;
    this.bipPub = getBipFormatDefault().getBipPub(hdAccount);
    this.xPub = hdAccount.xpubstr();
  }

  // address

  public BipAddress getNextAddressReceive(){
    return getNextAddressReceive(bipFormatDefault, true);
  }

  public BipAddress getNextAddressReceive(boolean increment){
    return getNextAddressReceive(bipFormatDefault, increment);
  }

  public BipAddress getNextAddressReceive(BipFormat bipFormat){
    return getNextAddressReceive(bipFormat, true);
  }

  public BipAddress getNextAddressReceive(BipFormat bipFormat, boolean increment) {
    int nextAddressIndex = increment ? getIndexHandlerReceive().getAndIncrement() : getIndexHandlerReceive().get();
    return getAddressAt(Chain.RECEIVE.getIndex(), nextAddressIndex, bipFormat);
  }

  public BipAddress getNextAddressChange() {
    return getNextAddressChange(bipFormatDefault, true);
  }

  public BipAddress getNextAddressChange(boolean increment) {
    return getNextAddressChange(bipFormatDefault, increment);
  }

  public BipAddress getNextAddressChange(BipFormat bipFormat) {
    return getNextAddressChange(bipFormat, true);
  }

  public BipAddress getNextAddressChange(BipFormat bipFormat, boolean increment) {
    int nextAddressIndex =
            increment ? getIndexHandlerChange().getAndIncrement() : getIndexHandlerChange().get();
    return getAddressAt(Chain.CHANGE.getIndex(), nextAddressIndex, bipFormat);
  }

  public BipAddress getAddressAt(int chainIndex, int addressIndex) {
    return getAddressAt(chainIndex, addressIndex, bipFormatDefault);
  }

  public BipAddress getAddressAt(int chainIndex, int addressIndex, BipFormat bipFormat) {
    HD_Address hdAddress = hdWallet.getAddressAt(derivation.getAccountIndex(), chainIndex, addressIndex);
    return new BipAddress(hdAddress, derivation, bipFormat);
  }

  public BipAddress getAddressAt(UnspentOutput utxo) {
    if (!utxo.hasPath()) {
      return null; // bip47
    }
    BipFormat bipFormat = bipFormatSupplier.findByAddress(utxo.addr, getParams());
    return getAddressAt(utxo.computePathChainIndex(), utxo.computePathAddressIndex(), bipFormat);
  }

  //

  public String getId() {
    return id;
  }

  public SamouraiAccount getAccount() {
    return samouraiAccount;
  }

  public IIndexHandler getIndexHandlerReceive() {
    return getIndexHandler(Chain.RECEIVE);
  }

  public IIndexHandler getIndexHandlerChange() {
    return getIndexHandler(Chain.CHANGE);
  }

  public IIndexHandler getIndexHandler(Chain chain) {
    return indexHandlerSupplier.getIndexHandlerWallet(this, chain);
  }

  public BipDerivation getDerivation() {
    return derivation;
  }

  public Collection<BipFormat> getBipFormats() {
    return bipFormats;
  }

  public BipFormat getBipFormatDefault() {
    return bipFormatDefault;
  }

  public String getBipPub() {
    return bipPub;
  }

  public String getXPub() {
    return xPub;
  }

  public NetworkParameters getParams() {
    return hdWallet.getParams();
  }

  public HD_Wallet getHdWallet() {
    return hdWallet;
  }

  public HD_Account getHdAccount() {
    return hdAccount;
  }

  @Override
  public String toString() {
    return "BipWallet{" +
            "id='" + id + '\'' +
            ", samouraiAccount=" + samouraiAccount +
            ", derivation=" + derivation +
            ", bipFormatDefault=" + bipFormatDefault +
            ", bipFormats=" + bipFormats +
            ", xPub='" + Util.maskString(xPub) + '\'' +
            ", bipPub='" + Util.maskString(bipPub) + '\'' +
            '}';
  }
}
