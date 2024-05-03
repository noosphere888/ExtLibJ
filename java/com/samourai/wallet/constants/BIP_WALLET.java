package com.samourai.wallet.constants;

import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.bipWallet.BipDerivation;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.client.indexHandler.IndexHandlerSupplier;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.hd.Purpose;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collection;

public enum BIP_WALLET {
  // use first bipFormat as bipFormatDefault
  DEPOSIT_BIP44(SamouraiAccount.DEPOSIT, new BipDerivation(Purpose.PURPOSE_44, SamouraiAccountIndex.DEPOSIT), BIP_FORMAT.LEGACY),
  DEPOSIT_BIP49(SamouraiAccount.DEPOSIT, new BipDerivation(Purpose.PURPOSE_49, SamouraiAccountIndex.DEPOSIT), BIP_FORMAT.SEGWIT_COMPAT),
  DEPOSIT_BIP84(SamouraiAccount.DEPOSIT, new BipDerivation(Purpose.PURPOSE_84, SamouraiAccountIndex.DEPOSIT), BIP_FORMAT.SEGWIT_NATIVE),
  PREMIX_BIP84(SamouraiAccount.PREMIX, new BipDerivation(Purpose.PURPOSE_84, SamouraiAccountIndex.PREMIX), BIP_FORMAT.SEGWIT_NATIVE),
  POSTMIX_BIP84(SamouraiAccount.POSTMIX, new BipDerivation(Purpose.PURPOSE_84, SamouraiAccountIndex.POSTMIX), BIP_FORMAT.SEGWIT_NATIVE, BIP_FORMAT.SEGWIT_COMPAT, BIP_FORMAT.LEGACY),

  ASB_BIP84(SamouraiAccount.SWAPS_ASB, new BipDerivation(Purpose.PURPOSE_84, SamouraiAccountIndex.SWAPS_ASB), BIP_FORMAT.SEGWIT_NATIVE),
  SWAPS_DEPOSIT(SamouraiAccount.SWAPS_DEPOSIT, new BipDerivation(Purpose.PURPOSE_84, SamouraiAccountIndex.SWAPS_DEPOSIT), BIP_FORMAT.SEGWIT_NATIVE),
  SWAPS_REFUNDS(SamouraiAccount.SWAPS_REFUNDS, new BipDerivation(Purpose.PURPOSE_84, SamouraiAccountIndex.SWAPS_REFUNDS), BIP_FORMAT.SEGWIT_NATIVE),

  // ignored accounts
  BADBANK_BIP84(SamouraiAccount.BADBANK, new BipDerivation(Purpose.PURPOSE_84, SamouraiAccountIndex.BADBANK), BIP_FORMAT.SEGWIT_NATIVE),
  RICOCHET_BIP84(SamouraiAccount.RICOCHET, new BipDerivation(Purpose.PURPOSE_84, SamouraiAccountIndex.RICOCHET), BIP_FORMAT.SEGWIT_NATIVE);

  private SamouraiAccount account;
  private BipDerivation bipDerivation;
  private Collection<BipFormat> bipFormats;
  private BipFormat bipFormatDefault;

  BIP_WALLET(SamouraiAccount account, BipDerivation bipDerivation, BipFormat... bipFormats) {
    this.account = account;
    this.bipDerivation = bipDerivation;
    this.bipFormats = Arrays.asList(bipFormats);
    this.bipFormatDefault = this.bipFormats.iterator().next();  // use first BipFormat as default
  }
  public static BIP_WALLET[] getListByAccounts(SamouraiAccount... accounts) {
    return Arrays.stream(BIP_WALLET.values()).filter(
                    bipWallet -> ArrayUtils.contains(accounts, bipWallet.getAccount()))
            .toArray(i -> new BIP_WALLET[i]);
  }

  public BipWallet newBipWallet(BipFormatSupplier bipFormatSupplier, HD_Wallet bip44w, IndexHandlerSupplier indexHandlerSupplier) {
    return new BipWallet(bipFormatSupplier, name(), bip44w, indexHandlerSupplier,
            account, bipDerivation, bipFormats, bipFormatDefault);
  }

  public SamouraiAccount getAccount() {
    return account;
  }

  public BipDerivation getBipDerivation() {
    return bipDerivation;
  }

  public Collection<BipFormat> getBipFormats() {
    return bipFormats;
  }

  public BipFormat getBipFormatDefault() {
    return bipFormatDefault;
  }
}
