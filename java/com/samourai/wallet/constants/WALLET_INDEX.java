package com.samourai.wallet.constants;

import com.samourai.wallet.bipWallet.BipDerivation;
import com.samourai.wallet.hd.Chain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum WALLET_INDEX {

  BIP44_RECEIVE(BIP_WALLET.DEPOSIT_BIP44, Chain.RECEIVE),
  BIP44_CHANGE(BIP_WALLET.DEPOSIT_BIP44, Chain.CHANGE),

  BIP49_RECEIVE(BIP_WALLET.DEPOSIT_BIP49, Chain.RECEIVE),
  BIP49_CHANGE(BIP_WALLET.DEPOSIT_BIP49, Chain.CHANGE),

  BIP84_RECEIVE(BIP_WALLET.DEPOSIT_BIP84, Chain.RECEIVE),
  BIP84_CHANGE(BIP_WALLET.DEPOSIT_BIP84, Chain.CHANGE),

  PREMIX_RECEIVE(BIP_WALLET.PREMIX_BIP84, Chain.RECEIVE),
  PREMIX_CHANGE(BIP_WALLET.PREMIX_BIP84, Chain.CHANGE),

  POSTMIX_RECEIVE(BIP_WALLET.POSTMIX_BIP84, Chain.RECEIVE),
  POSTMIX_CHANGE(BIP_WALLET.POSTMIX_BIP84, Chain.CHANGE),

  BADBANK_RECEIVE(BIP_WALLET.BADBANK_BIP84, Chain.RECEIVE),
  BADBANK_CHANGE(BIP_WALLET.BADBANK_BIP84, Chain.CHANGE),

  RICOCHET_RECEIVE(BIP_WALLET.RICOCHET_BIP84, Chain.RECEIVE),
  RICOCHET_CHANGE(BIP_WALLET.RICOCHET_BIP84, Chain.CHANGE),

  SWAPS_ASB_RECEIVE(BIP_WALLET.ASB_BIP84, Chain.RECEIVE),
  SWAPS_ASB_CHANGE(BIP_WALLET.ASB_BIP84, Chain.CHANGE),

  SWAPS_DEPOSIT_RECEIVE(BIP_WALLET.SWAPS_DEPOSIT, Chain.RECEIVE),
  SWAPS_DEPOSIT_CHANGE(BIP_WALLET.SWAPS_DEPOSIT, Chain.CHANGE),

  SWAPS_REFUNDS_RECEIVE(BIP_WALLET.SWAPS_REFUNDS, Chain.RECEIVE),
  SWAPS_REFUNDS_CHANGE(BIP_WALLET.SWAPS_REFUNDS, Chain.CHANGE),
  ;

  private static final Logger log = LoggerFactory.getLogger(WALLET_INDEX.class);
  private BIP_WALLET bipWallet;
  private Chain chain;

  WALLET_INDEX(BIP_WALLET bipWallet, Chain chain) {
    this.bipWallet = bipWallet;
    this.chain = chain;
  }

  // used by android
  public static WALLET_INDEX find(BipDerivation bipDerivation, Chain chain) {
    for (WALLET_INDEX walletIndex : WALLET_INDEX.values()) {
      // chain
      if (walletIndex.getChain().equals(chain)) {
        // accountIndex
        if (walletIndex.getBipWallet().getBipDerivation().getAccountIndex() == bipDerivation.getAccountIndex()) {
          // bipFormat
          if (walletIndex.getBipWallet().getBipDerivation().getPurpose() == bipDerivation.getPurpose()) {
            return walletIndex;
          }
        }
      }
    }
    log.warn("WALLET_INDEX not found for accountIndex="+bipDerivation.getAccountIndex()+", purpose="+bipDerivation.getPurpose()+", chainIndex="+chain.getIndex());
    return null;
  }

  // used by Android
  public static WALLET_INDEX findChangeIndex(int account, int purpose) {
    if (account == SamouraiAccountIndex.POSTMIX) {
      return WALLET_INDEX.POSTMIX_CHANGE;
    }
    /* if (account == SamouraiAccount.PREMIX.getAccountIndex()) {
      return WALLET_INDEX.PREMIX_CHANGE;
    } */
    if (purpose == 84) {
      return WALLET_INDEX.BIP84_CHANGE;
    } else if (purpose == 49) {
      return WALLET_INDEX.BIP49_CHANGE;
    } else {
      return WALLET_INDEX.BIP44_CHANGE;
    }
  }

  public BIP_WALLET getBipWallet() {
    return bipWallet;
  }

  public Chain getChain() {
    return chain;
  }

  public int getChainIndex() {
    return chain.getIndex();
  }
}
