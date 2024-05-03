package com.samourai.wallet.payload;

import com.samourai.wallet.api.pairing.PairingDojo;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.bipWallet.WalletSupplier;
import com.samourai.wallet.constants.BIP_WALLET;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.wallet.util.SystemUtil;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class PayloadUtilGeneric {
    private static final Logger log = LoggerFactory.getLogger(PayloadUtilGeneric.class);

    private static PayloadUtilGeneric instance = null;

    protected PayloadUtilGeneric() { ; }

    public static PayloadUtilGeneric getInstance() {
        if(instance == null) {
            instance = new PayloadUtilGeneric();
        }
        return instance;
    }

    public boolean isBackupFile(String data) {
        try {
            JSONObject jsonObj = new JSONObject(data);
            if (jsonObj != null && jsonObj.has("payload")) {
                return true;
            }
        } catch (Exception e) {
            log.error("Not a backup file: "+e.getMessage());
        }
        return false;
    }

    public BackupPayload readBackup(File backupFile, String passwordStr) throws Exception {
        String data = SystemUtil.readFile(backupFile);
        return readBackup(data, passwordStr);
    }

    public BackupPayload readBackup(String data, String passwordStr) throws Exception {
        // parse
        String encrypted = null;
        int version = BackupEncrypted.VERSION_1;
        try {
            BackupEncrypted backupEncrypted = BackupEncrypted.parse(data);
            encrypted = backupEncrypted.getPayload();
            version = backupEncrypted.getVersion();
        }
        catch(Exception e) {
            log.error("BackupEncrypted.parse() failed", e);
        }
        // not a json stream, assume v0
        if(encrypted == null)    {
            encrypted = data;
        }

        // decrypt
        return BackupEncrypted.decrypt(encrypted, passwordStr, version);
    }

    public BackupPayload computeBackupPayload(WalletSupplier walletSupplier, String scode, PairingDojo pairingDojo) throws Exception {
        BipWallet bip44Wallet = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP44);
        if (bip44Wallet == null) {
            return null;
        }

        JSONObject wallet = new JSONObject();
        wallet.put("testnet", FormatsUtilGeneric.getInstance().isTestNet(bip44Wallet.getParams()) ? true : false);

        HD_Wallet bip44w = bip44Wallet.getHdWallet();
        wallet.put("seed", bip44w.getSeedHex());
        wallet.put("passphrase", bip44w.getPassphrase());
        wallet.put("fingerprint", Hex.toHexString(bip44w.getFingerprint()));

        JSONArray accts = new JSONArray();
        accts.put(exportBipWallet(bip44Wallet));
        wallet.put("accounts", accts);

        //
        // export BIP49 account for debug payload
        //
        BipWallet bip49Wallet = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP49);
        JSONArray bip49_account = new JSONArray();
        bip49_account.put(exportBipWallet(bip49Wallet));
        wallet.put("bip49_accounts", bip49_account);

        //
        // export BIP84 account for debug payload
        //
        BipWallet bip84Wallet = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP84);
        JSONArray bip84_account = new JSONArray();
        bip84_account.put(exportBipWallet(bip84Wallet));
        wallet.put("bip84_accounts", bip84_account);

        //
        // export Whirlpool accounts for debug payload
        //;
        JSONArray whirlpool_account = new JSONArray();

        BipWallet premixWallet = walletSupplier.getWallet(BIP_WALLET.PREMIX_BIP84);
        whirlpool_account.put(exportBipWallet(premixWallet));

        BipWallet postmixWallet = walletSupplier.getWallet(BIP_WALLET.POSTMIX_BIP84);
        whirlpool_account.put(exportBipWallet(postmixWallet));

        BipWallet badBankWallet = walletSupplier.getWallet(BIP_WALLET.BADBANK_BIP84);
        whirlpool_account.put(exportBipWallet(badBankWallet));
        wallet.put("whirlpool_account", whirlpool_account);

        //
        // export Atomic Swaps accounts for debug payload
        //;
        JSONArray swaps_account = new JSONArray();

        BipWallet swapsAsbWallet = walletSupplier.getWallet(BIP_WALLET.ASB_BIP84);
        swaps_account.put(exportBipWallet(swapsAsbWallet));

        BipWallet swapsDepositWallet = walletSupplier.getWallet(BIP_WALLET.SWAPS_DEPOSIT);
        swaps_account.put(exportBipWallet(swapsDepositWallet));

        BipWallet swapsRefundsWallet = walletSupplier.getWallet(BIP_WALLET.SWAPS_REFUNDS);
        swaps_account.put(exportBipWallet(swapsRefundsWallet));

        wallet.put("swaps_accounts", bip84_account);

        JSONObject meta = new JSONObject();

        JSONObject whirlpoolMeta = new JSONObject();
        if(!StringUtils.isEmpty(scode)) {
            whirlpoolMeta.put("scode", scode);
        }
        meta.put("whirlpool", whirlpoolMeta);

        // TODO zeroleak
        //meta.put("xpubreg44", PrefsUtil.getInstance(context).getValue(PrefsUtil.XPUB44REG, false));
        //meta.put("xpubreg49", PrefsUtil.getInstance(context).getValue(PrefsUtil.XPUB49REG, false));
        //meta.put("xpubreg84", PrefsUtil.getInstance(context).getValue(PrefsUtil.XPUB84REG, false));
        //meta.put("xpubprereg", PrefsUtil.getInstance(context).getValue(PrefsUtil.XPUBPREREG, false));
        //meta.put("xpubpostreg", PrefsUtil.getInstance(context).getValue(PrefsUtil.XPUBPOSTREG, false));
        //meta.put("paynym_claimed", PrefsUtil.getInstance(context).getValue(PrefsUtil.PAYNYM_CLAIMED, false));
        //meta.put("localIndexes", LocalReceiveIndexes.getInstance(context).toJSON());
        //meta.put("xpubpostxreg", PrefsUtil.getInstance(context).getValue(PrefsUtil.XPUBPOSTREG, false));

        if(pairingDojo != null)    {
            JSONObject dojoMeta = pairingDojo.toJson();
            meta.put("dojo", dojoMeta);
        }

        JSONObject obj = new JSONObject();
        obj.put("wallet", wallet);
        obj.put("meta", meta);
        String jsonString = obj.toString();
        return BackupPayload.parse(jsonString);
    }

    private JSONObject exportBipWallet(BipWallet bipWallet) {
        JSONObject obj = bipWallet.getHdAccount().toJSON(bipWallet.getDerivation().getPurpose());
        obj.put("receiveIdx", bipWallet.getIndexHandlerReceive().get());
        obj.put("changeIdx", bipWallet.getIndexHandlerChange().get());
        return obj;
    }

    public BackupEncrypted computeBackupEncrypted(WalletSupplier walletSupplier, String scode, PairingDojo pairingDojo, String passwordStr) throws Exception {
        // export
        BackupPayload backupPayload = computeBackupPayload(walletSupplier, scode, pairingDojo);
        if (backupPayload == null) {
            return null;
        }

        // encrypt
        return BackupEncrypted.encrypt(backupPayload, passwordStr);
    }

    public void writeBackup(WalletSupplier walletSupplier, String scode, PairingDojo pairingDojo, String passwordStr, File file) throws Exception {
        // compute
        BackupEncrypted backupEncrypted = computeBackupEncrypted(walletSupplier, scode, pairingDojo, passwordStr);
        String backupEncryptedStr = backupEncrypted.toJson().toString();

        // write
        SystemUtil.createFile(file);
        SystemUtil.safeWrite(file, backupEncryptedStr.getBytes());
    }

}