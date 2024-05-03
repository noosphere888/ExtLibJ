package com.samourai.wallet.dexConfig;

/**
 * This class is exposed by whirlpool-server on <whirlpool-server>/rest/dex-config
 */
public class DexConfigResponse {
    private String samouraiConfig; // SamouraiConfig serialized as JSON string
    private String signature; // signature of 'samouraiConfig' with SamouraiNetwork.signingAddress

    public DexConfigResponse() {
    }

    // used by whirlpool-server
    public DexConfigResponse(String samouraiConfig, String signature) {
        this.samouraiConfig = samouraiConfig;
        this.signature = signature;
    }

    public String getSamouraiConfig() {
        return samouraiConfig;
    }

    public void setSamouraiConfig(String samouraiConfig) {
        this.samouraiConfig = samouraiConfig;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
