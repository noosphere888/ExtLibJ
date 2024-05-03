package com.samourai.wallet.bipWallet;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.client.indexHandler.IndexHandlerSupplier;
import com.samourai.wallet.constants.SamouraiAccount;
import com.samourai.wallet.constants.BIP_WALLET;
import com.samourai.wallet.hd.BipAddress;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WalletSupplierImpl implements WalletSupplier {
  private static final Logger log = LoggerFactory.getLogger(WalletSupplierImpl.class);

  private BipFormatSupplier bipFormatSupplier;
  private IndexHandlerSupplier indexHandlerSupplier;
  private final Map<SamouraiAccount, Collection<BipWallet>> walletsByAccount;
  private final Map<String, BipWallet> walletsByXPub;
  private final Map<String, BipWallet> walletsById;

  private final Map<SamouraiAccount, Map<BipFormat, BipWallet>> walletsByAccountByAddressType; // no custom registrations here

  public WalletSupplierImpl(BipFormatSupplier bipFormatSupplier, IndexHandlerSupplier indexHandlerSupplier) {
    this.bipFormatSupplier = bipFormatSupplier;
    this.indexHandlerSupplier = indexHandlerSupplier;

    this.walletsByAccount = new LinkedHashMap<>();
    for (SamouraiAccount samouraiAccount : SamouraiAccount.values()) {
      walletsByAccount.put(samouraiAccount, new LinkedList<>());
    }

    this.walletsByXPub = new LinkedHashMap<>();
    this.walletsById = new LinkedHashMap<>();

    this.walletsByAccountByAddressType = new LinkedHashMap<>();
    for (SamouraiAccount samouraiAccount : SamouraiAccount.values()) {
      walletsByAccountByAddressType.put(samouraiAccount, new LinkedHashMap<>());
    }
  }

  public WalletSupplierImpl(BipFormatSupplier bipFormatSupplier, IndexHandlerSupplier indexHandlerSupplier, HD_Wallet bip44w, BipWalletSupplier bipWalletSupplier) {
    this(bipFormatSupplier, indexHandlerSupplier);

    // register wallets
    register(bip44w, bipWalletSupplier);
  }

  public void register(HD_Wallet bip44w, BipWalletSupplier bipWalletSupplier) {
    for (BipWallet bipWallet : bipWalletSupplier.getBipWallets(bipFormatSupplier, bip44w, indexHandlerSupplier)) {
      register(bipWallet);
    }
  }

  public void register(BipWallet bipWallet) {
    walletsByAccount.get(bipWallet.getAccount()).add(bipWallet);
    walletsByXPub.put(bipWallet.getXPub(), bipWallet);
    walletsById.put(bipWallet.getId(), bipWallet);
    for (BipFormat bipFormat : bipWallet.getBipFormats()) {
      walletsByAccountByAddressType.get(bipWallet.getAccount()).put(bipFormat, bipWallet);
    }
    if (log.isDebugEnabled()) {
      log.debug("+BipWallet["+bipWallet.getId()+"]: "+bipWallet);
    }
    // no walletsByAccountByAddressType here
  }

  public void register(String id, HD_Wallet bip44w, SamouraiAccount samouraiAccount, BipDerivation derivation, Collection<BipFormat> bipFormats, BipFormat bipFormatDefault) {
    BipWallet bipWallet = new BipWallet(bipFormatSupplier, id, bip44w, indexHandlerSupplier, samouraiAccount, derivation, bipFormats, bipFormatDefault);
    register(bipWallet);
  }

  @Override
  public Collection<BipWallet> getWallets() {
    return walletsByXPub.values();
  }

  @Override
  public Collection<BipWallet> getWallets(SamouraiAccount samouraiAccount) {
    return walletsByAccount.get(samouraiAccount);
  }

  @Override
  public BipWallet getWalletByXPub(String xpub) {
    if (xpub == null) {
      throw new IllegalArgumentException("xpub arg cannot be null");
    }
    BipWallet bipWallet = walletsByXPub.get(xpub);
    if (bipWallet == null) {
      log.error("BipWallet not found for: " + xpub);
      return null;
    }
    return bipWallet;
  }

  @Override
  public BipWallet getWalletById(String id) {
    return walletsById.get(id);
  }

  @Override
  public BipWallet getWallet(BIP_WALLET bip) {
    return getWalletById(bip.name());
  }

  @Override
  public BipWallet getWallet(SamouraiAccount account, BipFormat bipFormat) {
    return walletsByAccountByAddressType.get(account).get(bipFormat);
  }

  @Override
  public String[] getXPubs(boolean withIgnoredAccounts, BipFormat... bipFormats) {
    List<String> xPubs = new LinkedList<>();
    for (BipWallet bipWallet : walletsByXPub.values()) {
      // filter ignoredAccounts
      if (withIgnoredAccounts || bipWallet.getAccount().isActive()) {
        // filter bipFormats
        if (bipFormats == null || bipFormats.length == 0 || !Util.intersection(bipWallet.getBipFormats(), Arrays.asList(bipFormats)).isEmpty()) {
          String xpub = bipWallet.getXPub();
          xPubs.add(xpub);
        }
      }
    }
    return xPubs.toArray(new String[] {});
  }


}
