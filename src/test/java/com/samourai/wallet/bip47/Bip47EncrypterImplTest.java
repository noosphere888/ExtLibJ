package com.samourai.wallet.bip47;

import com.samourai.wallet.bip47.rpc.Bip47EncrypterImpl;
import com.samourai.wallet.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Bip47EncrypterImplTest extends AbstractTest {
    private Bip47EncrypterImpl bip47EncrypterInitiator;
    private Bip47EncrypterImpl bip47EncrypterCounterparty;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        bip47EncrypterInitiator = new Bip47EncrypterImpl(bip47AccountInitiator, cryptoUtil, bip47Util);
        bip47EncrypterCounterparty = new Bip47EncrypterImpl(bip47AccountCounterparty, cryptoUtil, bip47Util);
    }

    @Test
    public void getPaymentCode() {
        String pCode = bip47EncrypterInitiator.getPaymentCode().toString();
        Assertions.assertEquals("PM8TJXp19gCE6hQzqRi719FGJzF6AreRwvoQKLRnQ7dpgaakakFns22jHUqhtPQWmfevPQRCyfFbdDrKvrfw9oZv5PjaCerQMa3BKkPyUf9yN1CDR3w6", pCode);
        Assertions.assertEquals(bip47Wallet.getAccount(0).getPaymentCode().toString(), pCode);
    }

    @Test
    public void getPaymentAddress() throws Exception {
        String receiveAddress = bip47EncrypterInitiator.getSharedPaymentAddress(paymentCodeCounterparty)
                .getSegwitAddressReceive().getBech32AsString();
        Assertions.assertEquals("tb1q4udyravjfu8yx2hdswvx0jvc5j7zhvulemg7jn", receiveAddress);
        Assertions.assertEquals(bip47Util.getReceiveAddress(bip47AccountInitiator, paymentCodeCounterparty, 0, params).getBech32AsString(), receiveAddress);

        String sendAddress = bip47EncrypterCounterparty.getSharedPaymentAddress(paymentCodeInitiator)
                .getSegwitAddressSend().getBech32AsString();
        Assertions.assertEquals("tb1q4udyravjfu8yx2hdswvx0jvc5j7zhvulemg7jn", sendAddress);
        Assertions.assertEquals(bip47Util.getSendAddress(bip47AccountCounterparty, paymentCodeInitiator, 0, params).getBech32AsString(), sendAddress);
    }

    @Test
    public void sign() throws Exception {
        String message = "hello Soroban";
        String signature = bip47EncrypterInitiator.sign(message);
        Assertions.assertEquals("IP2vGNSVHJNu1MjUKM7NVajc0omP1OQvX/8i3GgEX6oufI/E+q5sR4ouD6B55BznUE1TSxmnlGlzzB5WnXJAKH4=", signature);
    }

    @Test
    public void verifySignature_success() throws Exception {
        String message = "hello Soroban";
        String signature = "IP2vGNSVHJNu1MjUKM7NVajc0omP1OQvX/8i3GgEX6oufI/E+q5sR4ouD6B55BznUE1TSxmnlGlzzB5WnXJAKH4=";
        Assertions.assertTrue(bip47EncrypterCounterparty.verifySignature(message, signature, paymentCodeInitiator));

        Assertions.assertFalse(bip47EncrypterCounterparty.verifySignature(message+"altered", signature, paymentCodeInitiator));
        Assertions.assertFalse(bip47EncrypterCounterparty.verifySignature(message, signature+"altered", paymentCodeInitiator));
        Assertions.assertFalse(bip47EncrypterCounterparty.verifySignature(message, signature, paymentCodeCounterparty));
    }
}
