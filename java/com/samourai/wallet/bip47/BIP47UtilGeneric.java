package com.samourai.wallet.bip47;

import com.samourai.wallet.bip47.rpc.BIP47Account;
import com.samourai.wallet.bip47.rpc.NotSecp256k1Exception;
import com.samourai.wallet.bip47.rpc.PaymentAddress;
import com.samourai.wallet.bip47.rpc.PaymentCode;
import com.samourai.wallet.bip47.rpc.secretPoint.ISecretPointFactory;
import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.segwit.SegwitAddress;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bouncycastle.util.encoders.Hex;

public abstract class BIP47UtilGeneric {

    private static ISecretPointFactory secretPointFactory;
    private static boolean secretPointFactoryForced;

    protected BIP47UtilGeneric(ISecretPointFactory secretPointFactory, boolean secretPointFactoryForced) {
        if (BIP47UtilGeneric.secretPointFactory == null || !BIP47UtilGeneric.secretPointFactoryForced) {
            BIP47UtilGeneric.secretPointFactory = secretPointFactory;
        }
        if (secretPointFactoryForced) {
            // avoids Android impl getting overriden by Java impl
            BIP47UtilGeneric.secretPointFactoryForced = true;
        }
    }
    protected BIP47UtilGeneric(ISecretPointFactory secretPointFactory) {
        this(secretPointFactory, false);
    }

    public SegwitAddress getReceiveAddress(BIP47Account bip47Account, PaymentCode pcode, int idx, NetworkParameters params) throws Exception {
        HD_Address address = bip47Account.addressAt(idx);
        return getPaymentAddress(pcode, 0, address, params).getSegwitAddressReceive();
    }

    public String getReceivePubKey(BIP47Account bip47Account, PaymentCode pcode, int idx, NetworkParameters params) throws Exception {
        ECKey ecKey = getReceiveAddress(bip47Account, pcode, idx, params).getECKey();
        return Hex.toHexString(ecKey.getPubKey());
    }

    public SegwitAddress getSendAddress(BIP47Account bip47Account, PaymentCode pcode, int idx, NetworkParameters params) throws Exception {
        HD_Address address = bip47Account.getNotificationAddress();
        return getPaymentAddress(pcode, idx, address, params).getSegwitAddressSend();
    }

    public String getSendPubKey(BIP47Account bip47Account, PaymentCode pcode, int idx, NetworkParameters params) throws Exception {
        ECKey ecKey = getSendAddress(bip47Account, pcode, idx, params).getECKey();
        return Hex.toHexString(ecKey.getPubKey());
    }

    public byte[] getIncomingMask(BIP47Account bip47Account, byte[] pubkey, byte[] outPoint, NetworkParameters params) throws AddressFormatException, Exception    {

        HD_Address notifAddress = bip47Account.getNotificationAddress();
        DumpedPrivateKey dpk = new DumpedPrivateKey(params, notifAddress.getPrivateKeyString());
        ECKey inputKey = dpk.getKey();
        byte[] privkey = inputKey.getPrivKeyBytes();
        byte[] mask = com.samourai.wallet.bip47.rpc.PaymentCode.getMask(secretPointFactory.newSecretPoint(privkey, pubkey).ECDHSecretAsBytes(), outPoint);

        return mask;
    }

    public PaymentAddress getPaymentAddress(PaymentCode pcode, int idx, HD_Address address, NetworkParameters params) throws AddressFormatException, NotSecp256k1Exception {
        return getPaymentAddress(pcode, idx, address.getECKey(), params);
    }

    public PaymentAddress getPaymentAddress(PaymentCode pcode, int idx, ECKey addressPrivateKey, NetworkParameters params) throws AddressFormatException, NotSecp256k1Exception {
        DumpedPrivateKey dpk = new DumpedPrivateKey(params, addressPrivateKey.getPrivateKeyEncoded(params).toString());
        ECKey eckey = dpk.getKey();
        PaymentAddress paymentAddress = new PaymentAddress(pcode, idx, eckey.getPrivKeyBytes(), params, secretPointFactory);
        return paymentAddress;
    }

}
