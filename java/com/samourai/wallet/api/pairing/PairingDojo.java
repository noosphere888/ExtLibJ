package com.samourai.wallet.api.pairing;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class PairingDojo {
    private static final Logger log = LoggerFactory.getLogger(PairingDojo.class);

    private String url; // may be null
    private String apikey; // may be null

    public PairingDojo() {
    }

    public PairingDojo(String url, String apikey) {
        this.url = url;
        this.apikey = apikey;
    }

    public static PairingDojo parse(JSONObject jsonObject) throws Exception {
        if (!jsonObject.has("pairing")) return null;
        JSONObject pairing = jsonObject.getJSONObject("pairing");

        String url = pairing.getString("url");
        String apiKey = pairing.getString("apikey");
        if (url == null || apiKey == null) return null;

        PairingDojo pairingDojo = new PairingDojo(url, apiKey);
        pairingDojo.validate();
        return pairingDojo;
    }

    public JSONObject toJson() {
        JSONObject paring = new JSONObject()
                .put("apikey", apikey)
                .put("url", url);
        JSONObject dojoParams = new JSONObject()
                .put("pairing", paring);
        return dojoParams;
    }

    public void validate() throws Exception {
        // url
        if (StringUtils.isEmpty(url)) {
            throw new Exception("Invalid pairing.url");
        }
        try {
            new URL(url);
        } catch (Exception e) {
            log.error("", e);
            throw new Exception("Invalid pairing.url");
        }

        // apikey
        if (StringUtils.isEmpty(apikey)) {
            throw new Exception("Invalid pairing.apikey");
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }
}