package com.samourai.wallet.util;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FormatsUtilGenericTest {
  private static final FormatsUtilGeneric formatsUtil = FormatsUtilGeneric.getInstance();

  private static final String VPUB_1 =
          "vpub5SLqN2bLY4WeYBwMrtdanr5SfhRC7AyW1aEwbtVbt7t9y6kgBCS6ajVA4LL7Jy2iojpH1hjaiTMp5h4y9dG2dC64bAk9ZwuFCX6AvxFddaa";
  private static final String VPUB_2 =
          "vpub5b14oTd3mpWGzbxkqgaESn4Pq1MkbLbzvWZju8Y6LiqsN9JXX7ZzvdCp1qDDxLqeHGr6BUssz2yFmUDm5Fp9jTdz4madyxK6mwgsCvYdK5S";
  private static final String XPUB_1 =
          "xpub6C8aSUjB7fwH6CSpS5AjRh1sPwfmrZKNNrfye5rkijhFpSfiKeSNT2CpVLuDzQiipdYAmmyi4eLXritVhYjfBfeEWJPXUrUEEHrcgnEH7wX";
  private static final String XPUB_2 =
          "xpub6DUQ2PuGdPGVK74fqMpFw7UxQa2wLcv8JcWEV7mjNgiuiv4NjgsxukpDfd6xaeuU87oEGx16k3w1XhCs4mmK8GybS6n9W5hvAvCtyxB9nLV";
  private static final String TPUB_1 =
          "tpubD6NzVbkrYhZ4WaWSyoBvQwbpLkojyoTZPRsgXELWz3Popb3qkjcJyJUGLnL4qHHoQvao8ESaAstxYSnhyswJ76uZPStJRJCTKvosUCJZL5B";
  private static final String TPUB_2 =
          "tpubDF3Dw2nZnTYgxzXqxb8a4samW4kJTy64JNCUpUP1SeMXDdbh6ekDKCBvJHDBUf6itTccJ1asSTWQEDwVuWVRDNTUs3inqJcJuMQZk8EysmY";
  private static final String UPUB_1 =
          "upub57Wa4MvRPNyAgtkF2XqxakywVjGkAYz16TiipVbiW7WGuzwSvYGXxfq238NXK4NoQ6hUGE92Fo1GCQTQRvr1pxQTiq3iz35kvo2XYU7ZfFa";
  private static final String UPUB_2 =
          "upub5GAoVnx8d8xo9Jme1KncEgxtf3DJeicW1Q3X7jeCxiTzK3VJGTQSJZYfzdFdxSBisdjHS1HKXNchtBcCMZQ8wDxPCRtDQ3VcWDdDpJ4zA7C";
  private static final String ZPUB_1 =
          "zpub6qo73p51R32Ennq46njyqsCsjsxfjoJND5iRCseXUkT1veJApxmVh9X6XkpPzE2ZdumnGjApyy3ddJ7d8wZgn91SEynNeg7CmjyuTuXfNQ6";
  private static final String YPUB_1 =
          "ypub6Wxqk9Q6GMUkwVdwGRxMdn7NZupDoBJsHyCCRUke6k58sYUwaJbw55rxWYrozKNeEGeyXFaGXJh5k1W4RF9fyuKqNe5x4mHiW1vG5JX2Wmv";
  private static final String XPRV = "xprv9ycwhF2CmmqHd8Cxtyjv43M9iNbbzd9ktjTCNo2fnw2het4G6FcucAFTpb5GFnN8YvjAGruX3UEMNxdfQeYHm2UUcmCLppaAT6BvB7NJbBi";
  private static final String BTC_ADDRESS_P2TR = "tb1pqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesf3hn0c";
  private static final String BTC_ADDRESS_BECH32 = "bc1qgzpm6qefpaxqzyl428xztm49duxxv8uys9f8hu";
  private static final String BTC_ADDRESS_P2SH = "3ANptnTAD8eQNRz9gVmNPZcf1d2LyFJJZz";
  private static final String BTC_ADDRESS_P2PKH = "1FWQiwK27EnGXb6BiBMRLJvunJQZZPMcGd";
  private static final String BTC_URI = "bitcoin:3JK2dFmWA58A3kukgw1yybotStGAFaV6Sg";
  private static final String BTC_URI_ADDRESS = "BitcoinURI['address'='3JK2dFmWA58A3kukgw1yybotStGAFaV6Sg']";
  private static final String BTC_URI_LABEL = "bitcoin:3JK2dFmWA58A3kukgw1yybotStGAFaV6Sg?label=Coin-Guides";
  private static final String BTC_URI_LABEL_ADDRESS =
          "BitcoinURI['label'='Coin-Guides','address'='3JK2dFmWA58A3kukgw1yybotStGAFaV6Sg']";
  private static final String BTC_URI_1 = "bitcoin:3JK2dFmWA58A3kukgw1yybotStGAFaV6Sg?amount=1.00000000&label=Coin-Guides";
  private static final String BTC_URI_1_MES =
          "bitcoin:3JK2dFmWA58A3kukgw1yybotStGAFaV6Sg?amount=1.00000000&label=Coin-Guides&message=Donation%20for%20Coin%20Guides";
  private static final String BTC_AMOUNT_0 = "0.0000";
  private static final String BTC_AMOUNT_1 = "100000000";
  private static final String BTC_AMOUNT_2 = "200000000";
  private static final String BIP47_OP_RETURN =
          "010002b85034fb08a8bfefd22848238257b252721454bbbfba2c3667f168837ea2cdad671af9f659" +
          "04632e2dcc0c6ad314e11d53fc82fa4c4ea27a4a14eccecc478fee00000000000000000000000000";



  private static final NetworkParameters params = MainNetParams.get();

  @Test
  public void isValidXpubOrZpub() throws Exception {
    Assertions.assertTrue(formatsUtil.isValidXpub(VPUB_1));
    Assertions.assertTrue(formatsUtil.isValidXpub(VPUB_2));
    Assertions.assertTrue(formatsUtil.isValidXpub(XPUB_1));
    Assertions.assertTrue(formatsUtil.isValidXpub(XPUB_2));
    Assertions.assertTrue(formatsUtil.isValidXpubOrZpub(ZPUB_1, params));

    Assertions.assertFalse(formatsUtil.isValidXpub("xpubfoo"));
    Assertions.assertFalse(formatsUtil.isValidXpub("vpubfoo"));
    Assertions.assertFalse(formatsUtil.isValidXpub(XPUB_1+"foo"));
    Assertions.assertFalse(formatsUtil.isValidXpub(XPUB_2+"foo"));
    Assertions.assertFalse(formatsUtil.isValidXpub(XPUB_1+"foo"));
    Assertions.assertFalse(formatsUtil.isValidXpubOrZpub(ZPUB_1+"foo", params));
  }

  @Test
  public void xlatXpub() throws Exception {
    Assertions.assertEquals(TPUB_1, formatsUtil.xlatXpub(VPUB_1, true));
    Assertions.assertEquals(TPUB_1, formatsUtil.xlatXpub(VPUB_1, false));
    Assertions.assertEquals(VPUB_1, formatsUtil.xlatXpub(TPUB_1, true));
    Assertions.assertEquals(UPUB_1, formatsUtil.xlatXpub(TPUB_1, false));
    Assertions.assertEquals(TPUB_1, formatsUtil.xlatXpub(UPUB_1, true));
    Assertions.assertEquals(TPUB_1, formatsUtil.xlatXpub(UPUB_1, false));

    Assertions.assertEquals(TPUB_2, formatsUtil.xlatXpub(VPUB_2, true));
    Assertions.assertEquals(TPUB_2, formatsUtil.xlatXpub(VPUB_2, false));
    Assertions.assertEquals(VPUB_2, formatsUtil.xlatXpub(TPUB_2, true));
    Assertions.assertEquals(UPUB_2, formatsUtil.xlatXpub(TPUB_2, false));
    Assertions.assertEquals(TPUB_2, formatsUtil.xlatXpub(UPUB_2, true));
    Assertions.assertEquals(TPUB_2, formatsUtil.xlatXpub(UPUB_2, false));

    Assertions.assertEquals(ZPUB_1, formatsUtil.xlatXpub(XPUB_1, true));
    Assertions.assertEquals(YPUB_1, formatsUtil.xlatXpub(XPUB_1, false));
    Assertions.assertEquals(XPUB_1, formatsUtil.xlatXpub(ZPUB_1, true));
    Assertions.assertEquals(XPUB_1, formatsUtil.xlatXpub(ZPUB_1, false));
    Assertions.assertEquals(XPUB_1, formatsUtil.xlatXpub(YPUB_1, true));
    Assertions.assertEquals(XPUB_1, formatsUtil.xlatXpub(YPUB_1, false));
  }

  @Test
  public void validateBitcoinAddress() throws Exception {
    Assertions.assertEquals(BTC_ADDRESS_BECH32, formatsUtil.validateBitcoinAddress(BTC_ADDRESS_BECH32, params));
    Assertions.assertEquals(BTC_ADDRESS_P2SH, formatsUtil.validateBitcoinAddress(BTC_ADDRESS_P2SH, params));
    Assertions.assertEquals(BTC_ADDRESS_P2PKH, formatsUtil.validateBitcoinAddress(BTC_ADDRESS_P2PKH, params));
    // failing Assertions.assertEquals(BTC_ADDRESS_P2TR, formatsUtil.validateBitcoinAddress(BTC_ADDRESS_P2TR, params));

    Assertions.assertNotEquals(BTC_ADDRESS_P2SH, formatsUtil.validateBitcoinAddress(BTC_ADDRESS_P2SH+"foo", params));
    Assertions.assertNotEquals(BTC_ADDRESS_P2PKH, formatsUtil.validateBitcoinAddress(BTC_ADDRESS_P2PKH +"foo", params));
    Assertions.assertNotEquals(BTC_ADDRESS_BECH32, formatsUtil.validateBitcoinAddress(BTC_ADDRESS_BECH32+"foo", params));
    Assertions.assertNotEquals(BTC_ADDRESS_P2TR, formatsUtil.validateBitcoinAddress(BTC_ADDRESS_P2TR+"foo", params));
  }

  @Test
  public void isBitcoinUri() throws Exception {
    Assertions.assertTrue(formatsUtil.isBitcoinUri(BTC_URI));
    Assertions.assertFalse(formatsUtil.isBitcoinUri(BTC_URI+"foo"));
  }

  @Test
  public void getBitcoinUri() throws Exception {
    Assertions.assertEquals(BTC_URI_ADDRESS, formatsUtil.getBitcoinUri(BTC_URI));
    Assertions.assertEquals(BTC_URI_LABEL_ADDRESS, formatsUtil.getBitcoinUri(BTC_URI_LABEL));

    Assertions.assertNotEquals(BTC_URI_ADDRESS, formatsUtil.getBitcoinUri(BTC_URI+"foo"));
    Assertions.assertNotEquals(BTC_URI_LABEL_ADDRESS, formatsUtil.getBitcoinUri(BTC_URI_LABEL+"foo"));
  }

  @Test
  public void getBitcoinAmount() throws Exception {
    Assertions.assertEquals(BTC_AMOUNT_0, formatsUtil.getBitcoinAmount(BTC_URI));
    Assertions.assertEquals(BTC_AMOUNT_1, formatsUtil.getBitcoinAmount(BTC_URI_1));
    Assertions.assertEquals(BTC_AMOUNT_1, formatsUtil.getBitcoinAmount(BTC_URI_1_MES));

    Assertions.assertNotEquals(BTC_AMOUNT_2, formatsUtil.getBitcoinAmount(BTC_URI));
    Assertions.assertNotEquals(BTC_AMOUNT_2, formatsUtil.getBitcoinAmount(BTC_URI_1));
    Assertions.assertNotEquals(BTC_AMOUNT_2, formatsUtil.getBitcoinAmount(BTC_URI_1_MES));
  }

  @Test
  public void isValidBech32() throws Exception {
    Assertions.assertTrue(formatsUtil.isValidBech32(BTC_ADDRESS_BECH32));
    Assertions.assertTrue(formatsUtil.isValidP2TR(BTC_ADDRESS_P2TR));

    Assertions.assertFalse(formatsUtil.isValidBech32(BTC_ADDRESS_P2SH));
    Assertions.assertFalse(formatsUtil.isValidBech32(BTC_ADDRESS_P2PKH));
  }

  @Test
  public void isValidP2TR() throws Exception {
    Assertions.assertTrue(formatsUtil.isValidP2TR(BTC_ADDRESS_P2TR));

    Assertions.assertFalse(formatsUtil.isValidP2TR(BTC_ADDRESS_BECH32));
    Assertions.assertFalse(formatsUtil.isValidP2TR(BTC_ADDRESS_P2SH));
    Assertions.assertFalse(formatsUtil.isValidP2TR(BTC_ADDRESS_P2PKH));
  }

  @Test
  public void isValidP2SH() throws Exception {
    Assertions.assertTrue(formatsUtil.isValidP2SH(BTC_ADDRESS_P2SH, MainNetParams.get()));

    Assertions.assertFalse(formatsUtil.isValidP2SH(BTC_ADDRESS_BECH32, MainNetParams.get()));
    Assertions.assertFalse(formatsUtil.isValidP2SH(BTC_ADDRESS_P2TR, TestNet3Params.get()));
    Assertions.assertFalse(formatsUtil.isValidP2SH(BTC_ADDRESS_P2PKH, MainNetParams.get()));
  }

  @Test
  public void isValidXprv() throws Exception {
    Assertions.assertTrue(formatsUtil.isValidXprv(XPRV));
    Assertions.assertFalse(formatsUtil.isValidXprv(XPUB_1));
  }

  @Test
  public void isValidBIP47OpReturn() throws Exception {
    Assertions.assertTrue(formatsUtil.isValidBIP47OpReturn(BIP47_OP_RETURN));
    Assertions.assertFalse(formatsUtil.isValidBIP47OpReturn("123456"));
  }
}
