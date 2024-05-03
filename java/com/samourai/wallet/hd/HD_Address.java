package com.samourai.wallet.hd;

import com.samourai.wallet.segwit.SegwitAddress;
import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.wallet.util.FormatsUtilGeneric;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.json.JSONException;
import org.json.JSONObject;

public class HD_Address {

    private int accountIndex;
    private int chainIndex;
    private int mChildNum; // addressIndex
    private String strPath = null;
    private ECKey ecKey = null;
    private byte[] mPubKey = null;

    private NetworkParameters mParams = null;

    private HD_Address() { ; }

    public HD_Address(NetworkParameters params, DeterministicKey cKey, int accountIndex, int chainIndex, int child) {

        mParams = params;
        this.accountIndex = accountIndex;
        this.chainIndex = chainIndex;
        mChildNum = child;

        DeterministicKey dk = HDKeyDerivation.deriveChildKey(cKey, new ChildNumber(mChildNum, false));
        if(dk.hasPrivKey())    {
            ecKey = new ECKey(dk.getPrivKeyBytes(), dk.getPubKey());
        }
        else    {
            ecKey = ECKey.fromPublicOnly(dk.getPubKey());
        }
        long now = Utils.now().getTime() / 1000;
        ecKey.setCreationTimeSeconds(now);

        mPubKey = ecKey.getPubKey();

        strPath = dk.getPath().toString();
    }

    public String getAddressString() {
        return ecKey.toAddress(mParams).toString();
    }

    public String getAddressStringSegwitCompat() {
        return new SegwitAddress(getPubKey(), mParams).getAddressAsString();
    }

    public String getAddressStringSegwitNative() {
        return Bech32UtilGeneric.getInstance().toBech32(getPubKey(), mParams);
    }

    public String getPrivateKeyString() {

        if(ecKey.hasPrivKey()) {
            return ecKey.getPrivateKeyEncoded(mParams).toString();
        }
        else    {
            return null;
        }

    }

    public int getAccountIndex() {
        return accountIndex;
    }

    public int getChainIndex() {
        return chainIndex;
    }

    public int getAddressIndex() {
        return mChildNum;
    }

    public byte[] getPubKey() {
        return mPubKey;
    }

    public Address getAddress() {
        return ecKey.toAddress(mParams);
    }

    public ECKey getECKey() {
        return ecKey;
    }

    public NetworkParameters getParams() {
        return mParams;
    }

    public JSONObject toJSON() {
        try {
            JSONObject obj = new JSONObject();

            obj.put("path", strPath);
            obj.put("address", getAddressString());

            return obj;
        }
        catch(JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getPathAddress(int purpose) {
        int coinType = FormatsUtilGeneric.getInstance().getCoinType(mParams);
        return getPathAddress(purpose, coinType, accountIndex, chainIndex, mChildNum);
    }

    public static String getPathAccount(int purpose, int coinType, int account) {
        return "m/"+purpose+"'/"+coinType+"'/"+account;
    }

    public static String getPathChain(int purpose, int coinType, int account, int chain) {
        return getPathAccount(purpose, coinType, account)+"'/"+chain;
    }

    public static String getPathAddress(int purpose, int coinType, int account, int chain, int address) {
        return getPathChain(purpose, coinType, account, chain)+"/"+address;
    }

    public static String getPathAddressBip47(int purpose, int coinType, int account) {
        return getPathAccount(purpose, coinType, account)+"'/bip47/bip47";
    }
}
