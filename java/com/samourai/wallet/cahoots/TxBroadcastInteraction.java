package com.samourai.wallet.cahoots;

import com.samourai.wallet.cahoots.manual.ManualCahootsMessage;
import com.samourai.wallet.sorobanClient.SorobanInteraction;

public class TxBroadcastInteraction extends SorobanInteraction {
    private Cahoots signedCahoots;

    public TxBroadcastInteraction(Cahoots signedCahoots) {
        this(TypeInteraction.TX_BROADCAST, signedCahoots);
    }

    public TxBroadcastInteraction(TypeInteraction typeInteraction, Cahoots signedCahoots) {
        super(typeInteraction, new ManualCahootsMessage(signedCahoots));
        this.signedCahoots = signedCahoots;
    }

    public Cahoots getSignedCahoots() {
        return signedCahoots;
    }
}
