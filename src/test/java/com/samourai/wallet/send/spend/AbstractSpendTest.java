package com.samourai.wallet.send.spend;

import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.constants.BIP_WALLET;
import com.samourai.wallet.hd.Chain;
import com.samourai.wallet.test.AbstractTest;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractSpendTest extends AbstractTest {
    protected BipWallet depositWallet44;
    protected BipWallet depositWallet49;
    protected BipWallet depositWallet84;

    protected static String[] ADDRESS_CHANGE_44;
    protected static String[] ADDRESS_CHANGE_49;
    protected static String[] ADDRESS_CHANGE_84;

    public AbstractSpendTest() throws Exception {
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        depositWallet44 = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP44);
        depositWallet49 = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP49);
        depositWallet84 = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP84);

        ADDRESS_CHANGE_44 = new String[4];
        for (int i = 0; i < 4; i++) {
            ADDRESS_CHANGE_44[i] = depositWallet44.getAddressAt(Chain.CHANGE.getIndex(), i).getAddressString();
        }

        ADDRESS_CHANGE_49 = new String[4];
        for (int i = 0; i < 4; i++) {
            ADDRESS_CHANGE_49[i] = depositWallet49.getAddressAt(Chain.CHANGE.getIndex(), i).getAddressString();
        }

        ADDRESS_CHANGE_84 = new String[4];
        for (int i = 0; i < 4; i++) {
            ADDRESS_CHANGE_84[i] = depositWallet84.getAddressAt(Chain.CHANGE.getIndex(), i).getAddressString();
        }
    }
}
