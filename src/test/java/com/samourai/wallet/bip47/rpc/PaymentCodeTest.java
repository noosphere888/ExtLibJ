package com.samourai.wallet.bip47.rpc;

import com.samourai.wallet.bip47.rpc.java.Bip47UtilJava;
import com.samourai.wallet.bip47.rpc.java.SecretPointJava;
import com.samourai.wallet.bip47.rpc.secretPoint.ISecretPoint;
import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.segwit.SegwitAddress;
import com.samourai.wallet.util.CryptoTestUtil;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.params.TestNet3Params;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

public class PaymentCodeTest {
    private static final NetworkParameters params = TestNet3Params.get();
    private static final Bip47UtilJava bip47Util = Bip47UtilJava.getInstance();
    private static final CryptoTestUtil cryptoTestUtil = CryptoTestUtil.getInstance();

    @Test
    public void testPaymentCode() throws Exception {
        BIP47Account bip47Account1 = cryptoTestUtil.generateBip47Wallet(params).getAccount(0);
        BIP47Account bip47Account2 = cryptoTestUtil.generateBip47Wallet(params).getAccount(0);

        PaymentCode paymentCode1 = bip47Account1.getPaymentCode();
        PaymentCode paymentCode2 = bip47Account2.getPaymentCode();

        int idx = 0;

        // calculate send addresses
        SegwitAddress sendAddress1 = bip47Util.getSendAddress(bip47Account1, paymentCode2, idx, params);
        SegwitAddress sendAddress2 = bip47Util.getSendAddress(bip47Account2, paymentCode1, idx, params);

        // calculate receive addresses
        SegwitAddress receiveAddress1 = bip47Util.getReceiveAddress(bip47Account1, paymentCode2, idx, params);
        SegwitAddress receiveAddress2 = bip47Util.getReceiveAddress(bip47Account2, paymentCode1, idx, params);

        // mutual confrontation should give same result
        Assertions.assertEquals(sendAddress1.getBech32AsString(), receiveAddress2.getBech32AsString());
        Assertions.assertEquals(receiveAddress1.getBech32AsString(), sendAddress2.getBech32AsString());
    }

    @Test
    public void testXorMask() throws Exception {
        SegwitAddress inputAddress = cryptoTestUtil.generateSegwitAddress(params);
        TransactionOutPoint inputOutPoint = cryptoTestUtil
            .generateTransactionOutPoint(inputAddress.getBech32AsString(), 999999, params);
        ECKey inputKey = inputAddress.getECKey();

        byte[] data = ByteBuffer.allocate(64).putInt(1234).array();

        ECKey secretWalletKey = new ECKey();

        // mask
        ISecretPoint secretPointMask = new SecretPointJava(inputKey.getPrivKeyBytes(), secretWalletKey.getPubKey());
        byte[] dataMasked = PaymentCode.xorMask(data, secretPointMask, inputOutPoint);

        // unmask
        ISecretPoint secretPointUnmask = new SecretPointJava(secretWalletKey.getPrivKeyBytes(), inputKey.getPubKey());
        byte[] dataUnmasked = PaymentCode.xorMask(dataMasked, secretPointUnmask, inputOutPoint);

        // verify
        Assertions.assertArrayEquals(data, dataUnmasked);
    }

    @Test
    public void testXorMaskClientServer() throws Exception {
        SegwitAddress inputAddress = cryptoTestUtil.generateSegwitAddress(params);
        TransactionOutPoint inputOutPoint = cryptoTestUtil
            .generateTransactionOutPoint(inputAddress.getBech32AsString(), 999999, params);
        ECKey inputKey = inputAddress.getECKey();

        byte[] data = ByteBuffer.allocate(64).putInt(1234).array();

        BIP47Wallet bip47Wallet = cryptoTestUtil.generateBip47Wallet(params);
        PaymentCode paymentCode = bip47Wallet.getAccount(0).getPaymentCode();

        // mask: client side
        HD_Address notifAddressCli = paymentCode.notificationAddress(params);
        ISecretPoint secretPointMask = new SecretPointJava(inputKey.getPrivKeyBytes(), notifAddressCli.getPubKey());
        byte[] dataMasked = PaymentCode.xorMask(data, secretPointMask, inputOutPoint);

        // unmask: server side
        HD_Address notifAddressServer = bip47Wallet.getAccount(0).getNotificationAddress();
        ISecretPoint secretPointUnmask = new SecretPointJava(notifAddressServer.getECKey().getPrivKeyBytes(), inputKey.getPubKey());
        byte[] dataUnmasked = PaymentCode.xorMask(dataMasked, secretPointUnmask, inputOutPoint);

        // verify
        Assertions.assertArrayEquals(data, dataUnmasked);
    }

    @Test
    public void invalidPaymentCodeException() throws Exception {
        // throw exception for invalid payment code
        Assertions.assertThrows(AddressFormatException.class,
            () -> {
                PaymentCode paymentCode1 = new PaymentCode("PM8TJdufVsRkwm8K4uLBUdPEzoZCsq2JCN47wQxcD");
            });
    }

    @Test
    public void equals() throws Exception {
        BIP47Wallet bip47Wallet1 = cryptoTestUtil.generateBip47Wallet(params);

        PaymentCode paymentCode1 = bip47Wallet1.getAccount(0).getPaymentCode();
        PaymentCode paymentCode2 = bip47Wallet1.getAccount(1).getPaymentCode();

        Assertions.assertEquals(paymentCode1, bip47Wallet1.getAccount(0).getPaymentCode());
        Assertions.assertNotEquals(paymentCode1, paymentCode2);
    }
}
