package com.samourai.wallet.util;

import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.wallet.segwit.SegwitAddress;
import com.samourai.wallet.segwit.P2TRAddress;
import java.util.Arrays;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.TestNet3Params;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MessageSignUtilGenericTest {
    private static final MessageSignUtilGeneric messageSignUtil = MessageSignUtilGeneric.getInstance();
    private static final Bech32UtilGeneric bech32Util = Bech32UtilGeneric.getInstance();

    @Test
    public void verifySignedMessage() {
        TestNet3Params params = TestNet3Params.get();
        ECKey ecKey = new ECKey();
        String address = ecKey.toAddress(params).toString();
        String message = "hello foo";
        String signature = ecKey.signMessage(message);

        // TEST
        // valid
        Assertions.assertTrue(messageSignUtil.verifySignedMessage(address, message, signature, params));

        // wrong signature
        Assertions.assertFalse(messageSignUtil.verifySignedMessage(address, message, signature + "foo", params));

        // wrong message
        Assertions.assertFalse(messageSignUtil.verifySignedMessage(address, message + "foo", signature, params));

        // wrong pubkey
        ECKey ecKey2 = new ECKey();
        String address2 = ecKey2.toAddress(params).toString();
        Assertions.assertFalse(messageSignUtil.verifySignedMessage(address2, message, signature, params));
    }

    @Test
    public void verifyMessageSignatureBech32() {
        TestNet3Params params = TestNet3Params.get();
        ECKey ecKey = new ECKey();
        String address = bech32Util.toBech32(ecKey.getPubKey(), params);
        String message = "hello foo";
        String signature = ecKey.signMessage(message);

        // TEST
        // valid
        Assertions.assertTrue(messageSignUtil.verifySignedMessage(address, message, signature, params));

        // wrong signature
        Assertions.assertFalse(messageSignUtil.verifySignedMessage(address, message, signature + "foo", params));

        // wrong message
        Assertions.assertFalse(messageSignUtil.verifySignedMessage(address, message + "foo", signature, params));

        // wrong pubkey
        ECKey ecKey2 = new ECKey();
        String address2 = bech32Util.toBech32(ecKey2.getPubKey(), params);
        Assertions.assertFalse(messageSignUtil.verifySignedMessage(address2, message, signature, params));
    }

    @Test
    public void verifyMessageSignatureP2TR() {
        TestNet3Params params = TestNet3Params.get();
        ECKey ecKey = new ECKey();
		String address = null;
		try {
			P2TRAddress _address = new P2TRAddress(ecKey, params);
			address = _address.getP2TRAddressAsString();
        }
		catch(Exception e) {
			;
        }
        String message = "hello foo";
        String signature = ecKey.signMessage(message);

        // TEST
        // valid
        Assertions.assertTrue(messageSignUtil.verifySignedMessage(address, message, signature, params));

        // wrong signature
        Assertions.assertFalse(messageSignUtil.verifySignedMessage(address, message, signature + "foo", params));

        // wrong message
        Assertions.assertFalse(messageSignUtil.verifySignedMessage(address, message + "foo", signature, params));

        // wrong pubkey
        ECKey ecKey2 = new ECKey();
        String address2 = bech32Util.toBech32(ecKey2.getPubKey(), params);
        Assertions.assertFalse(messageSignUtil.verifySignedMessage(address2, message, signature, params));
    }

    @Test
    public void verifyMessageSignatureP2SH() {
        TestNet3Params params = TestNet3Params.get();
        ECKey ecKey = new ECKey();
		SegwitAddress _address = new SegwitAddress(ecKey, params, SegwitAddress.TYPE_P2SH_P2WPKH);
		String address = _address.getDefaultToAddressAsString();
        String message = "hello foo";
        String signature = ecKey.signMessage(message);

        // TEST
        // valid
        Assertions.assertTrue(messageSignUtil.verifySignedMessage(address, message, signature, params));

        // wrong signature
        Assertions.assertFalse(messageSignUtil.verifySignedMessage(address, message, signature + "foo", params));

        // wrong message
        Assertions.assertFalse(messageSignUtil.verifySignedMessage(address, message + "foo", signature, params));

        // wrong pubkey
        ECKey ecKey2 = new ECKey();
        String address2 = bech32Util.toBech32(ecKey2.getPubKey(), params);
        Assertions.assertFalse(messageSignUtil.verifySignedMessage(address2, message, signature, params));
    }

    @Test
    public void signedMessageToKey() {
        ECKey ecKey = new ECKey();
        String message = "hello foo";
        String signature = ecKey.signMessage(message);

        // valid
        Assertions.assertTrue(Arrays.equals(ecKey.getPubKey(), messageSignUtil.signedMessageToKey(message, signature).getPubKey()));

        // invalid
        Assertions.assertFalse(Arrays.equals(ecKey.getPubKey(), messageSignUtil.signedMessageToKey(message+"foo", signature).getPubKey()));

        // invalid
        Assertions.assertNull(messageSignUtil.signedMessageToKey(message, signature+"foo"));
    }
}
