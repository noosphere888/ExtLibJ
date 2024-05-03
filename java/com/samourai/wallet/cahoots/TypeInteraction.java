package com.samourai.wallet.cahoots;

import com.samourai.wallet.cahoots.CahootsTypeUser;

public enum TypeInteraction {
  TX_BROADCAST(CahootsTypeUser.SENDER, 4),
  TX_BROADCAST_MULTI(CahootsTypeUser.SENDER, 6);

  private CahootsTypeUser typeUser;
  private int step;

  TypeInteraction(CahootsTypeUser typeUser, int step) {
    this.typeUser = typeUser;
    this.step = step;
  }

  public CahootsTypeUser getTypeUser() {
    return typeUser;
  }

  public int getStep() {
    return step;
  }
}
