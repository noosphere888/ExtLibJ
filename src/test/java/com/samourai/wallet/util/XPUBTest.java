package com.samourai.wallet.util;

import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.hd.HD_WalletFactoryGeneric;
import com.samourai.wallet.hd.Purpose;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XPUBTest {
  private static final HD_WalletFactoryGeneric hdWalletFactory = HD_WalletFactoryGeneric.getInstance();
  private static final String SEED_WORDS = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";
  private static final String SEED_PASSPHRASE = "test";

  @Test
  public void test_testnet() throws Exception {
    HD_Wallet hdWallet = hdWalletFactory.restoreWallet(SEED_WORDS, SEED_PASSPHRASE, TestNet3Params.get());
    int depth = 3;
    int fingerprint = 1391939217;
    boolean testnet = true;

    // ACCOUNT 0
    int account = 0;
    String xpub0 = hdWallet.getAccount(account).xpubstr();
    String chainCode = "1705EF67672E5C90DEDC1AB00FEAF509D7B8F281B0B2DED403C85EC3875F3C07";
    String pubKey = "0291A772442C3D991DD0832795667F462A01E31ABB5619209F03C3D566DF44DF49";
    doTest(xpub0, chainCode, account, depth, fingerprint, pubKey, XPUB.MAGIC_TPUB, Purpose.PURPOSE_44, testnet, "m/44'/1'/0'/1/2");

    String ypub0 = hdWallet.getAccount(0).ypubstr();
    doTest(ypub0, chainCode, account, depth, fingerprint, pubKey, XPUB.MAGIC_UPUB, Purpose.PURPOSE_49, testnet, "m/49'/1'/0'/1/2");

    String zpub0 = hdWallet.getAccount(0).zpubstr();
    doTest(zpub0, chainCode, account, depth, fingerprint, pubKey, XPUB.MAGIC_VPUB, Purpose.PURPOSE_84, testnet, "m/84'/1'/0'/1/2");

    // ACCOUNT 1
    account = 1;
    String xpub1 = hdWallet.getAccount(account).xpubstr();
    chainCode = "32CF9C119AF3A50D796AD90CFB7FFA194033BFDBC834745C29E08B19C1531BBA";
    pubKey = "028B13817EFA1F96D134C6F337820209A20BF73BBABF5FBF232B224B48B64048C7";
    doTest(xpub1, chainCode, account, depth, fingerprint, pubKey, XPUB.MAGIC_TPUB, Purpose.PURPOSE_44, testnet, "m/44'/1'/1'/1/2");

    String ypub1 = hdWallet.getAccount(1).ypubstr();
    doTest(ypub1, chainCode, account, depth, fingerprint, pubKey, XPUB.MAGIC_UPUB, Purpose.PURPOSE_49, testnet, "m/49'/1'/1'/1/2");

    String zpub1 = hdWallet.getAccount(1).zpubstr();
    doTest(zpub1, chainCode, account, depth, fingerprint, pubKey, XPUB.MAGIC_VPUB, Purpose.PURPOSE_84, testnet, "m/84'/1'/1'/1/2");
  }

  @Test
  public void test_mainnet() throws Exception {
    HD_Wallet hdWallet = hdWalletFactory.restoreWallet(SEED_WORDS, SEED_PASSPHRASE, MainNetParams.get());
    int depth = 3;
    int fingerprint = 1285451822;
    boolean testnet = false;

    // ACCOUNT 0
    int account = 0;
    String xpub0 = hdWallet.getAccount(account).xpubstr();
    String chainCode = "321B5C8814D484AF6BF52104F2378011CDCE8AB21C63014ECB87B8E6AB095D5B";
    String pubKey = "021B5629D25CB88A27D0DD12627409FA66D00035D8889DC06EA95A6A83F0EE9AF2";
    doTest(xpub0, chainCode, account, depth, fingerprint, pubKey, XPUB.MAGIC_XPUB, Purpose.PURPOSE_44, testnet, "m/44'/0'/0'/1/2");

    String ypub0 = hdWallet.getAccount(0).ypubstr();
    doTest(ypub0, chainCode, account, depth, fingerprint, pubKey, XPUB.MAGIC_YPUB, Purpose.PURPOSE_49, testnet, "m/49'/0'/0'/1/2");

    String zpub0 = hdWallet.getAccount(0).zpubstr();
    doTest(zpub0, chainCode, account, depth, fingerprint, pubKey, XPUB.MAGIC_ZPUB, Purpose.PURPOSE_84, testnet, "m/84'/0'/0'/1/2");

    // ACCOUNT 1
    account = 1;
    String xpub1 = hdWallet.getAccount(account).xpubstr();
    chainCode = "B8BF2776817595944204F1B2D5374622EC6CFEFE30AC7423AE03087D19AC0BE3";
    pubKey = "02659C8B9B0596CB8FAED6E9E3C549FF895A857143004351C119FD066218B30776";
    doTest(xpub1, chainCode, account, depth, fingerprint, pubKey, XPUB.MAGIC_XPUB, Purpose.PURPOSE_44, testnet, "m/44'/0'/1'/1/2");

    String ypub1 = hdWallet.getAccount(1).ypubstr();
    doTest(ypub1, chainCode, account, depth, fingerprint, pubKey, XPUB.MAGIC_YPUB, Purpose.PURPOSE_49, testnet, "m/49'/0'/1'/1/2");

    String zpub1 = hdWallet.getAccount(1).zpubstr();
    doTest(zpub1, chainCode, account, depth, fingerprint, pubKey, XPUB.MAGIC_ZPUB, Purpose.PURPOSE_84, testnet, "m/84'/0'/1'/1/2");
  }

  private void doTest(String pub, String chainCode, int account, int depth, int fingerprint, String pubkey, int version, int purpose, boolean testnet, String path_1_2) {
    // decode
    XPUB xpub = new XPUB(pub);
    xpub.decode();
    Assertions.assertEquals(chainCode, Util.bytesToHex(xpub.getChain()));
    Assertions.assertEquals(account, xpub.getAccount());
    Assertions.assertEquals(depth, xpub.getDepth());
    Assertions.assertEquals(fingerprint, xpub.getFingerprint());
    Assertions.assertEquals(pubkey, Util.bytesToHex(xpub.getPubkey()));
    Assertions.assertEquals(version, xpub.getVersion());
    Assertions.assertEquals(purpose, xpub.getPurpose());
    Assertions.assertEquals(testnet, xpub.isTestnet());

    // getPathAddress
    Assertions.assertEquals(path_1_2, xpub.getPathAddress(1,2));

    // makeXPUB
    String newPub = XPUB.makeXPUB(version, (byte)depth, fingerprint, xpub.getChild(), Util.hexToBytes(chainCode), Util.hexToBytes(pubkey));
    Assertions.assertEquals(pub, newPub);
  }
}
