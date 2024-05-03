package com.samourai.wallet.sorobanClient;

import com.samourai.wallet.cahoots.TypeInteraction;

public abstract class SorobanInteraction implements SorobanReply {
  private TypeInteraction typeInteraction;
  private SorobanMessage replyAccept;

  public SorobanInteraction(TypeInteraction typeInteraction, SorobanMessage replyAccept) {
    this.typeInteraction = typeInteraction;
    this.replyAccept = replyAccept;
  }

  public SorobanInteraction(SorobanInteraction interaction) {
    this.typeInteraction = interaction.getTypeInteraction();
    this.replyAccept = interaction.getReplyAccept();
  }

  public TypeInteraction getTypeInteraction() {
    return typeInteraction;
  }

  public SorobanMessage getReplyAccept() {
    return replyAccept;
  }

  @Override
  public String toString() {
    return typeInteraction.name();
  }
}
