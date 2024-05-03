package com.samourai.wallet.util;

import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.wallet.segwit.SegwitAddress;
import com.samourai.wallet.segwit.P2TRAddress;
import java.security.SignatureException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

public class MessageSignUtilGeneric {

    private static MessageSignUtilGeneric instance = null;

    private MessageSignUtilGeneric() { ; }

    public static MessageSignUtilGeneric getInstance() {

        if(instance == null) {
            instance = new MessageSignUtilGeneric();
        }

        return instance;
    }

    public boolean verifySignedMessage(String address, String strMessage, String strSignature, NetworkParameters params) {

        if(address == null || strMessage == null || strSignature == null)    {
            return false;
        }

        ECKey ecKey = signedMessageToKey(strMessage, strSignature);
        if(ecKey != null)   {
            String toAddress = null;
            if(FormatsUtilGeneric.getInstance().isValidP2TR(address)) {
				try {
					P2TRAddress _address = new P2TRAddress(ecKey, params);
					toAddress = _address.getP2TRAddressAsString();
		            return toAddress.equalsIgnoreCase(address);
	            }
				catch(Exception e) {
					return false;
	            }
            }
            else if(FormatsUtilGeneric.getInstance().isValidBech32(address)) {
                toAddress = Bech32UtilGeneric.getInstance().toBech32(ecKey.getPubKey(), params);
	            return toAddress.equalsIgnoreCase(address);
            }
            else if(FormatsUtilGeneric.getInstance().isValidP2SH(address, params)) {
				SegwitAddress _address = new SegwitAddress(ecKey, params, SegwitAddress.TYPE_P2SH_P2WPKH);
				toAddress = _address.getDefaultToAddressAsString();
	            return toAddress.equals(address);
            }
			else {
                toAddress = ecKey.toAddress(params).toString();
	            return toAddress.equals(address);
            }
        }
		
		return false;
    }

    public String signMessage(ECKey key, String strMessage) {

        if(key == null || strMessage == null || !key.hasPrivKey())    {
            return null;
        }

        return key.signMessage(strMessage);
    }

    public String signMessageArmored(ECKey key, String strMessage, NetworkParameters params) {

        String sig = signMessage(key, strMessage);
        String ret = null;

        if(sig != null)    {
            ret = "-----BEGIN BITCOIN SIGNED MESSAGE-----\n";
            ret += strMessage;
            ret += "\n";
            ret += "-----BEGIN BITCOIN SIGNATURE-----\n";
            ret += "Version: Bitcoin-qt (1.0)\n";
            ret += "Address: " + key.toAddress(params).toString() + "\n\n";
            ret += sig;
            ret += "\n";
            ret += "-----END BITCOIN SIGNATURE-----\n";
        }

        return ret;
    }

    public ECKey signedMessageToKey(String strMessage, String strSignature) {

        if(strMessage == null || strSignature == null)    {
            return null;
        }

        try {
            return ECKey.signedMessageToKey(strMessage, strSignature);
        } catch(SignatureException e) {
            return null;
        }
    }

}
