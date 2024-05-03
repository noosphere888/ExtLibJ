package com.samourai.wallet.send.beans;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.bipFormat.BipFormat;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

import java.util.Collection;

public class SweepPreview {
    private long amount;
    private String address;
    private BipFormat bipFormat;
    private long fee;
    private Collection<UnspentOutput> utxos;
    private ECKey privKey;
    private NetworkParameters params;

    public SweepPreview(long amount, String address, BipFormat bipFormat, long fee, Collection<UnspentOutput> utxos, ECKey privKey, NetworkParameters params) {
        this.amount = amount;
        this.address = address;
        this.bipFormat = bipFormat;
        this.fee = fee;
        this.utxos = utxos;
        this.privKey = privKey;
        this.params = params;
    }

    public long getAmount() {
        return amount;
    }

    public String getAddress() {
        return address;
    }

    public BipFormat getBipFormat() {
        return bipFormat;
    }

    public long getFee() {
        return fee;
    }

    public Collection<UnspentOutput> getUtxos() {
        return utxos;
    }

    public ECKey getPrivKey() {
        return privKey;
    }

    public NetworkParameters getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "SweepPreview{" +
                "amount=" + amount +
                ", address='" + address + '\'' +
                ", bipFormat=" + bipFormat +
                ", fee=" + fee +
                ", utxos=" + utxos + '}';
    }
}
