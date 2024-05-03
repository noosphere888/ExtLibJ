package com.samourai.wallet.bip47;

import com.samourai.wallet.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BIP47UtilGenericTest extends AbstractTest {

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void getNotificationAddress() {
        Assertions.assertEquals("mwkeyEBt55Jvg3TbqLKuL4BZ2SJd2VpZTM", bip47WalletInitiator.getAccount(0).getNotificationAddress().getAddressString());
        Assertions.assertEquals("mn1GbUVNJ7NbwdNKocBYW2ZtWXbfbdQrcH", bip47WalletInitiator.getAccount(1).getNotificationAddress().getAddressString());
    }

    @Test
    public void getPaymentCode() {
        Assertions.assertEquals("PM8TJXp19gCE6hQzqRi719FGJzF6AreRwvoQKLRnQ7dpgaakakFns22jHUqhtPQWmfevPQRCyfFbdDrKvrfw9oZv5PjaCerQMa3BKkPyUf9yN1CDR3w6", bip47WalletInitiator.getAccount(0).getPaymentCode().toString());
        Assertions.assertEquals("PM8TJhN7RsazMbzmSBzA8HQEZBcarpdbJiuPWPF6cBZFo7iP9VXHMb6LADUosCbHogYVn8LGxLZmKS4mCXgzTvJkJDyVFvXj1gPZXThCjB7nWoqwNg81", bip47WalletInitiator.getAccount(1).getPaymentCode().toString());
    }

    @Test
    public void getPaymentCodeSamourai() {
        Assertions.assertEquals("PM8TJXp19gCE6hQzqRi719FGJzF6AreRwvoQKLRnQ7dpgaakakFns22jHUqhtPQWmfevPQRCyfFbdDrKvrfw9oZv5PjaCerQMa3BKkPyUf9yN1M8Y8XN", bip47WalletInitiator.getAccount(0).getPaymentCodeSamourai().toString());
        Assertions.assertEquals("PM8TJhN7RsazMbzmSBzA8HQEZBcarpdbJiuPWPF6cBZFo7iP9VXHMb6LADUosCbHogYVn8LGxLZmKS4mCXgzTvJkJDyVFvXj1gPZXThCjB7nWp1UNdFD", bip47WalletInitiator.getAccount(1).getPaymentCodeSamourai().toString());
    }

    @Test
    public void getReceiveAddress() throws Exception {
        Assertions.assertEquals("tb1q4udyravjfu8yx2hdswvx0jvc5j7zhvulemg7jn", bip47Util.getReceiveAddress(bip47WalletInitiator.getAccount(0), paymentCodeCounterparty, 0, params).getBech32AsString());
        Assertions.assertEquals("tb1q9zsvn5h8747s75nlqxs6f78sxsytlryun438np", bip47Util.getReceiveAddress(bip47WalletInitiator.getAccount(0), paymentCodeCounterparty, 1, params).getBech32AsString());
   }

    @Test
    public void getSendAddress() throws Exception {
        Assertions.assertEquals("tb1q4udyravjfu8yx2hdswvx0jvc5j7zhvulemg7jn", bip47Util.getSendAddress(bip47WalletCounterparty.getAccount(0), paymentCodeInitiator, 0, params).getBech32AsString());
        Assertions.assertEquals("tb1q9zsvn5h8747s75nlqxs6f78sxsytlryun438np", bip47Util.getSendAddress(bip47WalletCounterparty.getAccount(0), paymentCodeInitiator, 1, params).getBech32AsString());
    }

    @Test
    public void getReceivePubKey() throws Exception {
        Assertions.assertEquals("027c2725acebc10cd4a0a7d9e07e5dfa371a25ed6d14f3a9deb602b55287ba36e2", bip47Util.getReceivePubKey(bip47WalletInitiator.getAccount(0), paymentCodeCounterparty, 0, params));
        Assertions.assertEquals("0283156c6a3e6724107a757e687ccef0b6241ac3baeef3422ede063a0797d04f5b", bip47Util.getReceivePubKey(bip47WalletInitiator.getAccount(0), paymentCodeCounterparty, 1, params));
    }

    @Test
    public void getSendPubKey() throws Exception {
        Assertions.assertEquals("027c2725acebc10cd4a0a7d9e07e5dfa371a25ed6d14f3a9deb602b55287ba36e2", bip47Util.getSendPubKey(bip47WalletCounterparty.getAccount(0), paymentCodeInitiator, 0, params));
        Assertions.assertEquals("0283156c6a3e6724107a757e687ccef0b6241ac3baeef3422ede063a0797d04f5b", bip47Util.getSendPubKey(bip47WalletCounterparty.getAccount(0), paymentCodeInitiator, 1, params));
    }

}
