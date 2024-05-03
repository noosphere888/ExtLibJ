package com.samourai.wallet.payload;

import com.samourai.wallet.crypto.AESUtil;
import com.samourai.wallet.util.CharSequenceX;
import com.samourai.wallet.util.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupEncrypted {
    private static final Logger log = LoggerFactory.getLogger(BackupEncrypted.class);
    public static final int VERSION_1 = 1;
    public static final int VERSION_2 = 2;
    public static final int VERSION_CURRENT = VERSION_2;

    private String payload;
    private int version;
    private boolean external;

    public BackupEncrypted() {
    }

    public BackupEncrypted(String payload, boolean external) {
        this.version = VERSION_CURRENT;
        this.payload = payload;
        this.external = external;
    }

    public static BackupEncrypted parse(String json) throws Exception {
        BackupEncrypted backupPayload = JSONUtils.getInstance().getObjectMapper().readValue(json, BackupEncrypted.class);
        backupPayload.validate();
        return backupPayload;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject()
                .put("payload", payload)
                .put("version", version)
                .put("external", external);
        return jsonObject;
    }

    public void validate() throws Exception {
        if (StringUtils.isEmpty(payload)) {
            throw new Exception("Invalid .payload");
        }
    }

    // encryption

    public static String decryptPayload(String encrypted, String passwordStr, int version) throws Exception {
        if (passwordStr == null) {
            return encrypted;
        }
        String decrypted = null;
        try {
            CharSequenceX password = new CharSequenceX(passwordStr);
            if (version == BackupEncrypted.VERSION_1) {
                decrypted = AESUtil.decrypt(encrypted, password, AESUtil.DefaultPBKDF2Iterations);
            } else if (version == BackupEncrypted.VERSION_2) {
                decrypted = AESUtil.decryptSHA256(encrypted, password);
            }
        } catch (Exception e) {
            log.error("Unable to decrypt", e);
        }
        if (StringUtils.isEmpty(decrypted)) {
            throw new Exception("Unable to decrypt");
        }
        return decrypted;
    }

    public static String encryptPayload(String data, String passwordStr) throws Exception {
        return AESUtil.encryptSHA256(data, new CharSequenceX(passwordStr));
    }

    public static BackupEncrypted encrypt(BackupPayload backupPayload, String passwordStr) throws Exception {
        String backupPayloadStr = backupPayload.toJson().toString();
        String encryptedPayload = AESUtil.encryptSHA256(backupPayloadStr, new CharSequenceX(passwordStr));
        return new BackupEncrypted(encryptedPayload, false);
    }

    public static BackupPayload decrypt(String encryptedPayload, String passwordStr, int version) throws Exception {
        String decrypted = decryptPayload(encryptedPayload, passwordStr, version);
        return BackupPayload.parse(decrypted);
    }

    public BackupPayload decrypt(String passwordStr) throws Exception {
        return decrypt(payload, passwordStr, version);
    }

    //

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }
}
