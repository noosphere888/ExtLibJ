package com.samourai.wallet.constants;

public class SamouraiAccountIndex {
  public static final int DEPOSIT = 0;
  public static final int RICOCHET = Integer.MAX_VALUE;			 // 2147483647
  public static final int POSTMIX = Integer.MAX_VALUE - 1;		 // 2147483646
  public static final int PREMIX = Integer.MAX_VALUE - 2;		 // 2147483645
  public static final int BADBANK = Integer.MAX_VALUE - 3;		 // 2147483644
  public static final int SWAPS_DEPOSIT = Integer.MAX_VALUE - 4; // 2147483643
  public static final int SWAPS_REFUNDS = Integer.MAX_VALUE - 5; // 2147483642
  public static final int SWAPS_ASB = Integer.MAX_VALUE - 6;	 // 2147483641

  public static int find(SamouraiAccount samouraiAccount) {
    switch (samouraiAccount) {
      case PREMIX: return PREMIX;
      case POSTMIX: return POSTMIX;
      case BADBANK: return BADBANK;
      case RICOCHET: return RICOCHET;
      case SWAPS_ASB: return SWAPS_ASB;
      case SWAPS_DEPOSIT: return SWAPS_DEPOSIT;
      case SWAPS_REFUNDS: return SWAPS_REFUNDS;
      default: return DEPOSIT;
    }
  }

  @Deprecated // TODO accountIndex should not
  public static SamouraiAccount find(int accountIndex) {
    for (SamouraiAccount samouraiAccount : SamouraiAccount.values()) {
      if (find(samouraiAccount) == accountIndex) {
        return samouraiAccount;
      }
    }
    return null;
  }
}
