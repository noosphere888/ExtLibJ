package com.samourai.wallet.bip47.rpc;

import com.samourai.wallet.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Bip47PartnerImplTest extends AbstractTest {
    private Bip47PartnerImpl bip47PartnerInitiator;
    private Bip47PartnerImpl bip47PartnerCounterparty;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        bip47PartnerInitiator = new Bip47PartnerImpl(bip47AccountInitiator, paymentCodeCounterparty, true, cryptoUtil, bip47Util);
        bip47PartnerCounterparty = new Bip47PartnerImpl(bip47AccountCounterparty, paymentCodeInitiator, false, cryptoUtil, bip47Util);
    }

    @Test
    public void getSharedAddressBech32() {
        Assertions.assertEquals(bip47PartnerInitiator.getSharedAddressBech32(), bip47PartnerCounterparty.getSharedAddressBech32());
        Assertions.assertEquals("tb1qwsnjufu728l9p9ky68s7043f8zufkrq0pm0j73", bip47PartnerInitiator.getSharedAddressBech32());
    }
}
