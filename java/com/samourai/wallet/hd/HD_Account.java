package com.samourai.wallet.hd;

import com.samourai.wallet.util.FormatsUtilGeneric;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import org.json.JSONException;
import org.json.JSONObject;

public class HD_Account {

    protected DeterministicKey aKey = null;
    protected int mAID; // accountIndex

    private HD_Chain mReceive = null;
    private HD_Chain mChange = null;

    protected String strXPUB = null;
    protected String strYPUB = null;
    protected String strZPUB = null;

    protected NetworkParameters mParams = null;

    protected HD_Account() { ; }

    public HD_Account(NetworkParameters params, DeterministicKey mKey, int child) {

        mParams = params;
        mAID = child;

        // L0PRV & STDVx: private derivation.
        int childnum = child;
        childnum |= ChildNumber.HARDENED_BIT;
        aKey = HDKeyDerivation.deriveChildKey(mKey, childnum);

        strXPUB = aKey.serializePubB58(params);
        strYPUB = aKey.serializePubB58(params, 49);
        strZPUB = aKey.serializePubB58(params, 84);

        mReceive = new HD_Chain(mParams, aKey, mAID,true);
        mChange = new HD_Chain(mParams, aKey, mAID,false);

    }

    public HD_Account(NetworkParameters params, String xpub, int child) throws AddressFormatException {

        mParams = params;
        mAID = child;

        // assign master key to account key
        aKey = FormatsUtilGeneric.getInstance().createMasterPubKeyFromXPub(xpub);

        strXPUB = strYPUB = strZPUB = xpub;

        mReceive = new HD_Chain(mParams, aKey, mAID,true);
        mChange = new HD_Chain(mParams, aKey, mAID,false);

    }

    public String xpubstr() {

        return strXPUB;

    }

    public String ypubstr() {

        return strYPUB;

    }

    public String zpubstr() {

        return strZPUB;

    }

    public int getId() {
        return mAID;
    }

    public HD_Chain getReceive() {
        return mReceive;
    }

    public HD_Chain getChange() {
        return mChange;
    }

    public HD_Chain getChain(int idx) {
        return (idx == 0) ? mReceive : mChange;
    }

    public HD_Chain getChainAt(int idx) {
      return new HD_Chain(mParams, aKey, mAID, idx);
    }

    public JSONObject toJSON(int purpose) {
        try {
            JSONObject obj = new JSONObject();

            switch(purpose)    {
                case 49:
                    obj.put("ypub", ypubstr());
                    break;
                case 84:
                    obj.put("zpub", zpubstr());
                    break;
                // assume purpose == 44
                default:
                    obj.put("xpub", xpubstr());
                    break;
            }

            obj.put("receiveIdx", getReceive().getAddrIdx());
            obj.put("changeIdx", getChange().getAddrIdx());
            obj.put("id", mAID);

            return obj;
        }
        catch(JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public NetworkParameters getParams() {
        return mParams;
    }
}
