package com.samourai.wallet.hd;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class HD_WalletFactoryGenericTest {
    private static final Logger log = LoggerFactory.getLogger(HD_WalletFactoryGenericTest.class);
    private HD_WalletFactoryGeneric hdWalletFactory = HD_WalletFactoryGeneric.getInstance();

    @Test
    public void restoreWallet_words() throws Exception {
        NetworkParameters params = MainNetParams.get();
        HD_Wallet hdWallet;
        String passphrase = "TREZOR";

        // https://github.com/trezor/python-mnemonic/blob/master/vectors.json

        hdWallet = hdWalletFactory.restoreWallet("abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about", passphrase, params);
        Assertions.assertEquals("00000000000000000000000000000000", hdWallet.getSeedHex());
        Assertions.assertEquals(passphrase, hdWallet.getPassphrase());
        Assertions.assertEquals(new BigInteger("98271371290466655911529653016182302339914803975553744677741258513646170498272"), hdWallet.mRoot.getPrivKey());

        hdWallet = hdWalletFactory.restoreWallet("legal winner thank year wave sausage worth useful legal winner thank yellow", passphrase, params);
        Assertions.assertEquals("7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f", hdWallet.getSeedHex());
        Assertions.assertEquals(passphrase, hdWallet.getPassphrase());
        Assertions.assertEquals(new BigInteger("12662160214494740211391410586234565047416291165663256642181573049330307149082"), hdWallet.mRoot.getPrivKey());

        hdWallet = hdWalletFactory.restoreWallet("letter advice cage absurd amount doctor acoustic avoid letter advice cage above", passphrase, params);
        Assertions.assertEquals("80808080808080808080808080808080", hdWallet.getSeedHex());
        Assertions.assertEquals(passphrase, hdWallet.getPassphrase());
        Assertions.assertEquals(new BigInteger("63978822330767128043945360358261997375960365869251665953739239749472592461997"), hdWallet.mRoot.getPrivKey());

        hdWallet = hdWalletFactory.restoreWallet("void come effort suffer camp survey warrior heavy shoot primary clutch crush open amazing screen patrol group space point ten exist slush involve unfold", passphrase, params);
        Assertions.assertEquals("f585c11aec520db57dd353c69554b21a89b20fb0650966fa0a9d6f74fd989d8f", hdWallet.getSeedHex());
        Assertions.assertEquals(passphrase, hdWallet.getPassphrase());
        Assertions.assertEquals(new BigInteger("11902135336207312062323482070347138053483447968293492732980096496846405108646"), hdWallet.mRoot.getPrivKey());
    }

    @Test
    public void restoreWallet_hex() throws Exception {
        NetworkParameters params = MainNetParams.get();
        HD_Wallet hdWallet;
        String passphrase = "TREZOR";

        hdWallet = hdWalletFactory.restoreWallet("064f8f0bebfa2f65db003b56bc911535614f2764799bc89091398c1aed82e884", passphrase, params);
        Assertions.assertEquals("064f8f0bebfa2f65db003b56bc911535614f2764799bc89091398c1aed82e884", hdWallet.getSeedHex());
        Assertions.assertEquals(passphrase, hdWallet.getPassphrase());
        Assertions.assertEquals(new BigInteger("86537261523959021197231152630513431638710381086905592664083437282970228049122"), hdWallet.mRoot.getPrivKey());
    }

    @Test
    public void computeSeedFromWords() throws Exception {
        byte[] mSeed;

        // https://github.com/trezor/python-mnemonic/blob/master/vectors.json

        mSeed = hdWalletFactory.computeSeedFromWords("abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about");
        Assertions.assertEquals("00000000000000000000000000000000", org.bouncycastle.util.encoders.Hex.toHexString(mSeed));

        mSeed = hdWalletFactory.computeSeedFromWords("legal winner thank year wave sausage worth useful legal winner thank yellow");
        Assertions.assertEquals("7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f", org.bouncycastle.util.encoders.Hex.toHexString(mSeed));

        mSeed = hdWalletFactory.computeSeedFromWords("letter advice cage absurd amount doctor acoustic avoid letter advice cage above");
        Assertions.assertEquals("80808080808080808080808080808080", org.bouncycastle.util.encoders.Hex.toHexString(mSeed));

        mSeed = hdWalletFactory.computeSeedFromWords("void come effort suffer camp survey warrior heavy shoot primary clutch crush open amazing screen patrol group space point ten exist slush involve unfold");
        Assertions.assertEquals("f585c11aec520db57dd353c69554b21a89b20fb0650966fa0a9d6f74fd989d8f", org.bouncycastle.util.encoders.Hex.toHexString(mSeed));
    }

    @Test
    public void newWallet() throws Exception {
        NetworkParameters params = MainNetParams.get();
        String passphrase = "test";

        HD_Wallet hdw1 = hdWalletFactory.newWallet(passphrase, params);
        verifyNewWallet(hdw1, passphrase);

        HD_Wallet hdw2 = hdWalletFactory.newWallet(passphrase, params);
        verifyNewWallet(hdw2, passphrase);

        Assertions.assertNotEquals(hdw1.getSeedHex(), hdw2.getSeedHex());
        Assertions.assertNotEquals(hdw1.getMnemonic(), hdw2.getMnemonic());
    }

    @Test
    public void generateWallet() throws Exception {
        NetworkParameters params = TestNet3Params.get();
        HD_Wallet hdw1 = hdWalletFactory.generateWallet(44, params);
        Assertions.assertTrue(hdw1.getPassphrase().length() >= 15 && hdw1.getPassphrase().length() < 30);
        Assertions.assertTrue(hdw1.getMnemonic().split(" ").length == 24);
    }

    private void verifyNewWallet(HD_Wallet hdw, String passphrase) {
        if (log.isDebugEnabled()) {
            log.debug("verifyNewWallet: "+hdw.getMnemonic());
        }
        Assertions.assertEquals(12, hdw.getMnemonic().split(" ").length);
        Assertions.assertEquals(passphrase, hdw.getPassphrase());
    }
}
