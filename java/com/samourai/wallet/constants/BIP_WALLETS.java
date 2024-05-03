package com.samourai.wallet.constants;

import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.bipWallet.BipWalletSupplier;
import com.samourai.wallet.client.indexHandler.IndexHandlerSupplier;
import com.samourai.wallet.hd.HD_Wallet;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

// BipWallets to manage, used as BipWalletSupplier for DojoDataSourceFactory()
public enum BIP_WALLETS implements BipWalletSupplier {
  WHIRLPOOL(SamouraiAccount.DEPOSIT, SamouraiAccount.PREMIX, SamouraiAccount.POSTMIX, SamouraiAccount.BADBANK),
  WALLET(SamouraiAccount.DEPOSIT, SamouraiAccount.PREMIX, SamouraiAccount.POSTMIX, SamouraiAccount.BADBANK,
          SamouraiAccount.RICOCHET),
  SWAPS(SamouraiAccount.DEPOSIT, SamouraiAccount.PREMIX, SamouraiAccount.POSTMIX, SamouraiAccount.BADBANK,
          SamouraiAccount.RICOCHET,
          SamouraiAccount.SWAPS_ASB, SamouraiAccount.SWAPS_DEPOSIT, SamouraiAccount.SWAPS_REFUNDS);

  private SamouraiAccount[] accounts;

  BIP_WALLETS(SamouraiAccount... accounts) {
    this.accounts = accounts;
  }

  public BIP_WALLET[] getBIP_WALLETS() {
    return BIP_WALLET.getListByAccounts(accounts);
  }

  @Override
  public Collection<BipWallet> getBipWallets(BipFormatSupplier bipFormatSupplier, HD_Wallet bip44w, IndexHandlerSupplier indexHandlerSupplier) {
    return Arrays.stream(getBIP_WALLETS()).map(bip_wallet ->
                bip_wallet.newBipWallet(bipFormatSupplier, bip44w, indexHandlerSupplier))
            .collect(Collectors.toList());
  }

  public SamouraiAccount[] getAccounts() {
    return accounts;
  }
}
