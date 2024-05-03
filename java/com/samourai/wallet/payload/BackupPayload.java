package com.samourai.wallet.payload;

import com.fasterxml.jackson.databind.JsonNode;
import com.samourai.wallet.api.pairing.PairingDojo;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.hd.HD_WalletFactoryGeneric;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.wallet.util.JSONUtils;
import org.bitcoinj.core.NetworkParameters;
import org.json.JSONObject;

public class BackupPayload {
    private JSONObject wallet;
    private JSONObject meta;

    public BackupPayload() {
        this.wallet = null;
        this.meta = null;
    }

    public static BackupPayload parse(String json) throws Exception {
        BackupPayload backupPayload = JSONUtils.getInstance().getObjectMapper().readValue(json, BackupPayload.class);
        backupPayload.validate();
        return backupPayload;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject()
                .put("wallet", wallet)
                .put("meta", meta);
        return jsonObject;
    }

    public void validate() throws Exception {
        if (!wallet.has("seed")) {
            throw new Exception("Invalid wallet.seed");
        }

        PairingDojo pairingDojo = computePairingDojo();
        if (pairingDojo != null) {
            pairingDojo.validate();
        }
    }

    public boolean isWalletTestnet() {
        if (!wallet.has("testnet")) {
            return false;
        }
        return wallet.getBoolean("testnet");
    }

    public NetworkParameters computeNetworkParameters() {
        return FormatsUtilGeneric.getInstance().getNetworkParams(isWalletTestnet());
    }

    public String getWalletSeed() {
        return wallet.getString("seed");
    }

    public String getWalletPassphrase() {
        if (!wallet.has("passphrase")) {
            return null;
        }
        return wallet.getString("passphrase");
    }

    public HD_Wallet computeHdWallet() throws Exception {
        byte[] seed = org.apache.commons.codec.binary.Hex.decodeHex(getWalletSeed().toCharArray());
        String passphrase = getWalletPassphrase();
        NetworkParameters params = computeNetworkParameters();
        return HD_WalletFactoryGeneric.getInstance().getHD(44, seed, passphrase, params);
    }

    public PairingDojo computePairingDojo() throws Exception {
        if (!wallet.has("dojo")) return null;
        JSONObject dojoNode = wallet.getJSONObject("dojo");
        return PairingDojo.parse(dojoNode);
    }

    public JSONObject getWallet() {
        return wallet;
    }

    public void setWallet(JsonNode wallet) {
        this.wallet = new JSONObject(wallet.toString());
    }

    public JSONObject getMeta() {
        return meta;
    }

    public void setMeta(JsonNode meta) {
        this.meta = new JSONObject(meta.toString());
    }
}
