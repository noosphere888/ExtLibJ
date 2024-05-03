package com.samourai.wallet.api.pairing;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Optional;
import com.samourai.wallet.constants.SamouraiNetwork;

public enum PairingNetwork {
    MAINNET("mainnet", SamouraiNetwork.MAINNET),
    TESTNET("testnet", SamouraiNetwork.TESTNET);

    private String value;
    private SamouraiNetwork samouraiNetwork;

    PairingNetwork(String value, SamouraiNetwork samouraiNetwork) {
        this.value = value;
        this.samouraiNetwork = samouraiNetwork;
    }

    public static Optional<PairingNetwork> find(String value) {
        for (PairingNetwork item : PairingNetwork.values()) {
            if (item.value.equals(value)) {
                return Optional.of(item);
            }
        }
        return Optional.absent();
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public SamouraiNetwork getSamouraiNetwork() {
        return samouraiNetwork;
    }
}