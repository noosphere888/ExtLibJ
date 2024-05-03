package com.samourai.wallet.cahoots;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Optional;
import com.samourai.wallet.cahoots.multi.MultiCahoots;
import com.samourai.wallet.cahoots.stonewallx2.STONEWALLx2;
import com.samourai.wallet.cahoots.stowaway.Stowaway;
import com.samourai.wallet.send.beans.SpendType;

public enum CahootsType {
    STONEWALLX2(0, "StonewallX2", true, SpendType.CAHOOTS_STONEWALL2X, STONEWALLx2.class.getName()),
    STOWAWAY(1, "Stowaway", false, SpendType.CAHOOTS_STOWAWAY, Stowaway.class.getName()),
    MULTI(2, "MultiCahoots", false, SpendType.CAHOOTS_MULTI, MultiCahoots.class.getName()),
    TX0X2(3, "Tx0x2", true, SpendType.TX0X2, "com.samourai.wallet.cahoots.tx0x2.Tx0x2"), // implemented in whirlpool-client
    TX0X2_MULTI(4, "MultiTx0x2", true, SpendType.TX0X2_MULTI, "com.samourai.wallet.cahoots.tx0x2.MultiTx0x2"); // implemented in whirlpool-client

    private int value;
    private String label;
    private boolean minerFeeShared;
    private SpendType spendType;
    private String cahootsClassName;

    CahootsType(int value, String label, boolean minerFeeShared, SpendType spendType, String cahootsClassName) {
        this.value = value;
        this.label = label;
        this.minerFeeShared = minerFeeShared;
        this.spendType = spendType;
        this.cahootsClassName = cahootsClassName;
    }

    public static Optional<CahootsType> find(int value) {
      for (CahootsType item : CahootsType.values()) {
          if (item.value == value) {
              return Optional.of(item);
          }
      }
      return Optional.absent();
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public boolean isMinerFeeShared() {
        return minerFeeShared;
    }

    public SpendType getSpendType() {
        return spendType;
    }

    public String getCahootsClassName() {
        return cahootsClassName;
    }
}