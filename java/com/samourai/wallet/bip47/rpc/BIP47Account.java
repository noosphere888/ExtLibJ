package com.samourai.wallet.bip47.rpc;

import com.samourai.wallet.hd.HD_Account;
import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.util.FormatsUtilGeneric;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.nio.ByteBuffer;

/**
 *
 * BIP47Account.java : an account in a BIP47 wallet
 *
 */
public class BIP47Account extends HD_Account {

    private PaymentCode paymentCode = null;

    /**
     * Constructor for account.
     *
     * @param NetworkParameters params
     * @param DeterministicKey mwey deterministic key for this account
     * @param int child id within the wallet for this account
     *
     */
    public BIP47Account(NetworkParameters params, DeterministicKey wKey, int child) {
        super(params, wKey, child);
        paymentCode = new PaymentCode(createPaymentCodeFromAccountKey());
    }

    /**
     * Constructor for watch-only account.
     *
     * @param NetworkParameters params
     * @param String data XPUB or payment code for this account
     *
     */
    public BIP47Account(NetworkParameters params, String data) throws AddressFormatException {

        mParams = params;
        mAID = -1;

        // assign master key to account key
        if(FormatsUtilGeneric.getInstance().isValidPaymentCode(data))  {
            aKey = createMasterPubKeyFromPaymentCode(data);
            paymentCode = new PaymentCode(data);
        }
        else if(FormatsUtilGeneric.getInstance().isValidXpub(data))  {
            aKey = FormatsUtilGeneric.getInstance().createMasterPubKeyFromXPub(data);
            strXPUB = data;
            paymentCode = new PaymentCode(createPaymentCodeFromAccountKey());
        }
        else    {
            ;
        }

    }

    /**
     * Return notification address.
     *
     * @return Address
     *
     */
    public HD_Address getNotificationAddress() {
        return addressAt(0);
    }

    /**
     * Return address at idx.
     *
     * @param int idx
     * @return Address
     *
     */
    public HD_Address addressAt(int idx) {
        return new HD_Address(mParams, aKey, mAID,0, idx);
    }

    private String createPaymentCodeFromAccountKey() {

        PaymentCode pcode = new PaymentCode(aKey.getPubKey(), aKey.getChainCode());

        return pcode.toString();

    }

    /**
     * Return payment code string for this account.
     *
     * @return String
     *
     */
    public PaymentCode getPaymentCode() {
        return paymentCode;
    }

    public PaymentCode getPaymentCodeSamourai() {
        return new PaymentCode(getPaymentCode().makePaymentCodeSamourai());
    }

    /**
     * Restore watch-only account deterministic public key from payment code.
     *
     * @return DeterministicKey
     *
     */
    private DeterministicKey createMasterPubKeyFromPaymentCode(String payment_code_str) throws AddressFormatException {

        byte[] paymentCodeBytes = Base58.decodeChecked(payment_code_str);

        ByteBuffer bb = ByteBuffer.wrap(paymentCodeBytes);
        if(bb.get() != 0x47)   {
            throw new AddressFormatException("invalid payment code version");
        }

        byte[] chain = new byte[32];
        byte[] pub = new byte[33];
        // type:
        bb.get();
        // features:
        bb.get();

        bb.get(pub);
        bb.get(chain);

        return HDKeyDerivation.createMasterPubKeyFromBytes(pub, chain);
    }

}
