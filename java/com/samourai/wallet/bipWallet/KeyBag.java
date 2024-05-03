package com.samourai.wallet.bipWallet;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import org.bitcoinj.core.ECKey;

import java.util.LinkedHashMap;
import java.util.Map;

public class KeyBag {
    private final Map<String,byte[]> privKeys;

    public KeyBag() {
        this.privKeys = new LinkedHashMap<>();
    }

    public void add(UnspentOutput unspentOutput, byte[] privKeyBytes) {
        String hashKey = hashKey(unspentOutput);
        this.privKeys.put(hashKey, privKeyBytes);
    }

    public byte[] getPrivKeyBytes(UnspentOutput unspentOutput) {
        String hashKey = hashKey(unspentOutput);
        return privKeys.get(hashKey);
    }

    public Map<String, ECKey> toMap() {
        Map<String,ECKey> kb = new LinkedHashMap<>();
        for (Map.Entry<String,byte[]> e : privKeys.entrySet()) {
            kb.put(e.getKey(), ECKey.fromPrivate(e.getValue()));
        }
        return kb;
    }

    public int size() {
        return privKeys.size();
    }

    private static String hashKey(UnspentOutput unspentOutput) {
        return hashKey(unspentOutput.tx_hash, unspentOutput.tx_output_n);
    }

    private static String hashKey(String hash, int index) {
        return hash + ":" + index;
    }
}
