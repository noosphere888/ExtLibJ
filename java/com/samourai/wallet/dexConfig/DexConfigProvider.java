package com.samourai.wallet.dexConfig;

public class DexConfigProvider {
    private static DexConfigProvider instance;

    public static DexConfigProvider getInstance() {
        if (instance == null) {
            instance = new DexConfigProvider();
        }
        return instance;
    }

    private SamouraiConfig samouraiConfig;
    private Long lastLoad = null;

    protected DexConfigProvider() {
        // initialize default config
        this.samouraiConfig = new SamouraiConfig();
    }

    /*public void load(IBackendClient httpClient, NetworkParameters networkParameters, boolean onion) throws Exception {
        WhirlpoolServer whirlpoolServer = WhirlpoolServer.getByNetworkParameters(networkParameters);
        String dexURL = whirlpoolServer.getServerUrl(onion) + ENDPOINT_DEXCONFIG;
        load(httpClient, networkParameters, dexURL, whirlpoolServer.getSamouraiNetwork().getSigningAddress());
    }

    public void load(IBackendClient httpClient, NetworkParameters networkParameters, String dexURL, String signingAddress) throws Exception {
        DexConfigResponse dexConfigResponse = httpClient.getJson(dexURL, DexConfigResponse.class, null);

        if (MessageSignUtilGeneric.getInstance().verifySignedMessage(
                signingAddress,
                dexConfigResponse.getSamouraiConfig(),
                dexConfigResponse.getSignature(),
                networkParameters)) {
            this.samouraiConfig = JSONUtils.getInstance().getObjectMapper().readValue(dexConfigResponse.getSamouraiConfig(), SamouraiConfig.class);
            this.lastLoad = System.currentTimeMillis();

            // update SorobanServerDex
            //SorobanServerDex.setFrom(samouraiConfig);
        } else {
            throw new Exception("Invalid DexConfig signature");
        }
    }*/

    public SamouraiConfig getSamouraiConfig() {
        return samouraiConfig;
    }

    public Long getLastLoad() {
        return lastLoad;
    }
}
