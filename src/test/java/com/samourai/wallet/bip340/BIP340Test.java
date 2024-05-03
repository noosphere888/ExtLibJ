package com.samourai.wallet.bip340;

import com.samourai.wallet.segwit.bech32.*;
import com.samourai.wallet.segwit.P2TRAddress;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;

import org.bouncycastle.util.encoders.Hex;

import org.apache.commons.lang3.tuple.Pair;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BIP340Test {

  @Test
  public void deriveP2TRAddress() throws Exception {

    //
    // https://github.com/bitcoin/bips/blob/master/bip-0086.mediawiki
    //
    ECKey ecKey = ECKey.fromPublicOnly(Hex.decode("03cc8a4bc64d897bddc5fbc2f670f7a8ba0b386779106cf1223c6fc5d7cd6fc115"));
    Point iPoint = BIP340Util.getInternalPubkey(ecKey);
    assert(Hex.toHexString(iPoint.toBytes()).equals("cc8a4bc64d897bddc5fbc2f670f7a8ba0b386779106cf1223c6fc5d7cd6fc115"));

    Point oPoint = BIP340Util.getTweakedPubKeyFromPoint(iPoint);
    assert(Point.isSecp256k1(oPoint.toBytes()));
    assert(Hex.toHexString(oPoint.toBytes()).equals("a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c"));

    String address = BIP340Util.getP2TRAddress(MainNetParams.get(), oPoint);
    assert(address.equals("bc1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqkedrcr"));

    Pair<Byte, byte[]> segp = null;
    segp = Bech32Segwit.decode(address.substring(0, 2), address);
    assert(Hex.toHexString(Bech32Segwit.getScriptPubkey(segp.getLeft(), segp.getRight())).equals("5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c"));

    ecKey = ECKey.fromPublicOnly(Hex.decode("0283dfe85a3151d2517290da461fe2815591ef69f2b18a2ce63f01697a8b313145"));
    iPoint = BIP340Util.getInternalPubkey(ecKey);
    assert(Hex.toHexString(iPoint.toBytes()).equals("83dfe85a3151d2517290da461fe2815591ef69f2b18a2ce63f01697a8b313145"));

    oPoint = BIP340Util.getTweakedPubKeyFromPoint(iPoint);
    assert(Point.isSecp256k1(oPoint.toBytes()));
    assert(Hex.toHexString(oPoint.toBytes()).equals("a82f29944d65b86ae6b5e5cc75e294ead6c59391a1edc5e016e3498c67fc7bbb"));

    address = BIP340Util.getP2TRAddress(MainNetParams.get(), oPoint);
    assert(address.equals("bc1p4qhjn9zdvkux4e44uhx8tc55attvtyu358kutcqkudyccelu0was9fqzwh"));

    segp = Bech32Segwit.decode(address.substring(0, 2), address);
    assert(Hex.toHexString(Bech32Segwit.getScriptPubkey(segp.getLeft(), segp.getRight())).equals("5120a82f29944d65b86ae6b5e5cc75e294ead6c59391a1edc5e016e3498c67fc7bbb"));

    ecKey = ECKey.fromPublicOnly(Hex.decode("02399f1b2f4393f29a18c937859c5dd8a77350103157eb880f02e8c08214277cef"));
    iPoint = BIP340Util.getInternalPubkey(ecKey);
    assert(Hex.toHexString(iPoint.toBytes()).equals("399f1b2f4393f29a18c937859c5dd8a77350103157eb880f02e8c08214277cef"));

    oPoint = BIP340Util.getTweakedPubKeyFromPoint(iPoint);
    assert(Point.isSecp256k1(oPoint.toBytes()));
    assert(Hex.toHexString(oPoint.toBytes()).equals("882d74e5d0572d5a816cef0041a96b6c1de832f6f9676d9605c44d5e9a97d3dc"));

    address = BIP340Util.getP2TRAddress(MainNetParams.get(), oPoint);
    assert(address.equals("bc1p3qkhfews2uk44qtvauqyr2ttdsw7svhkl9nkm9s9c3x4ax5h60wqwruhk7"));

    segp = Bech32Segwit.decode(address.substring(0, 2), address);
    assert(Hex.toHexString(Bech32Segwit.getScriptPubkey(segp.getLeft(), segp.getRight())).equals("5120882d74e5d0572d5a816cef0041a96b6c1de832f6f9676d9605c44d5e9a97d3dc"));
  }

  @Test
  public void _deriveP2TRAddress() throws Exception {

    //
    // https://github.com/bitcoin/bips/blob/master/bip-0086.mediawiki
    //
    ECKey ecKey = ECKey.fromPublicOnly(Hex.decode("03cc8a4bc64d897bddc5fbc2f670f7a8ba0b386779106cf1223c6fc5d7cd6fc115"));
    P2TRAddress paddress = new P2TRAddress(ecKey, MainNetParams.get());
    Point iPoint = paddress.getInternalPubKey();
    assert(Hex.toHexString(iPoint.toBytes()).equals("cc8a4bc64d897bddc5fbc2f670f7a8ba0b386779106cf1223c6fc5d7cd6fc115"));

    Point oPoint = paddress.getTweakedPubKeyFromPoint();
    assert(Point.isSecp256k1(oPoint.toBytes()));
    assert(Hex.toHexString(oPoint.toBytes()).equals("a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c"));

    String address = paddress.getDefaultToAddressAsString();
    assert(address.equals("bc1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqkedrcr"));

    Pair<Byte, byte[]> segp = null;
    segp = Bech32Segwit.decode(address.substring(0, 2), address);
    assert(Hex.toHexString(Bech32Segwit.getScriptPubkey(segp.getLeft(), segp.getRight())).equals("5120a60869f0dbcf1dc659c9cecbaf8050135ea9e8cdc487053f1dc6880949dc684c"));

    ecKey = ECKey.fromPublicOnly(Hex.decode("0283dfe85a3151d2517290da461fe2815591ef69f2b18a2ce63f01697a8b313145"));
    paddress = new P2TRAddress(ecKey, MainNetParams.get());
    iPoint = paddress.getInternalPubKey();
    assert(Hex.toHexString(iPoint.toBytes()).equals("83dfe85a3151d2517290da461fe2815591ef69f2b18a2ce63f01697a8b313145"));

    oPoint = paddress.getTweakedPubKeyFromPoint();
    assert(Point.isSecp256k1(oPoint.toBytes()));
    assert(Hex.toHexString(oPoint.toBytes()).equals("a82f29944d65b86ae6b5e5cc75e294ead6c59391a1edc5e016e3498c67fc7bbb"));

    address = paddress.getDefaultToAddressAsString();
    assert(address.equals("bc1p4qhjn9zdvkux4e44uhx8tc55attvtyu358kutcqkudyccelu0was9fqzwh"));

    segp = Bech32Segwit.decode(address.substring(0, 2), address);
    assert(Hex.toHexString(Bech32Segwit.getScriptPubkey(segp.getLeft(), segp.getRight())).equals("5120a82f29944d65b86ae6b5e5cc75e294ead6c59391a1edc5e016e3498c67fc7bbb"));

    ecKey = ECKey.fromPublicOnly(Hex.decode("02399f1b2f4393f29a18c937859c5dd8a77350103157eb880f02e8c08214277cef"));
    paddress = new P2TRAddress(ecKey, MainNetParams.get());
    iPoint = paddress.getInternalPubKey();
    assert(Hex.toHexString(iPoint.toBytes()).equals("399f1b2f4393f29a18c937859c5dd8a77350103157eb880f02e8c08214277cef"));

    oPoint = paddress.getTweakedPubKeyFromPoint();
    assert(Point.isSecp256k1(oPoint.toBytes()));
    assert(Hex.toHexString(oPoint.toBytes()).equals("882d74e5d0572d5a816cef0041a96b6c1de832f6f9676d9605c44d5e9a97d3dc"));

    address = paddress.getDefaultToAddressAsString();
    assert(address.equals("bc1p3qkhfews2uk44qtvauqyr2ttdsw7svhkl9nkm9s9c3x4ax5h60wqwruhk7"));

    segp = Bech32Segwit.decode(address.substring(0, 2), address);
    assert(Hex.toHexString(Bech32Segwit.getScriptPubkey(segp.getLeft(), segp.getRight())).equals("5120882d74e5d0572d5a816cef0041a96b6c1de832f6f9676d9605c44d5e9a97d3dc"));
  }

  @Test
  public void invalidPubKey() throws Exception {

    //
    // https://suredbits.com/taproot-funds-burned-on-the-bitcoin-blockchain/
    //
    assert(!Point.isSecp256k1(Hex.decode("658204033e46a1fa8cceb84013cfe2d376ca72d5f595319497b95b08aa64a970")));

  }

  @Test
  public void publicKeyTweakingTest() throws Exception {
    ECKey key = ECKey.fromPublicOnly(Hex.decode("02d6889cb081036e0faefa3a35157ad71086b123b2b144b649798b494c300a961d"));
    String p2TRAddress = BIP340Util.getP2TRAddress(MainNetParams.get(), key, true);
    assert("bc1p2wsldez5mud2yam29q22wgfh9439spgduvct83k3pm50fcxa5dps59h4z5".equals(p2TRAddress));
  }

  @Test
  public void _publicKeyTweakingTest() throws Exception {
    ECKey key = ECKey.fromPublicOnly(Hex.decode("02d6889cb081036e0faefa3a35157ad71086b123b2b144b649798b494c300a961d"));
    P2TRAddress paddress = new P2TRAddress(key, MainNetParams.get(), true);
    assert("bc1p2wsldez5mud2yam29q22wgfh9439spgduvct83k3pm50fcxa5dps59h4z5".equals(paddress.getDefaultToAddressAsString()));
  }

  // test vectors: https://github.com/bitcoin/bips/blob/master/bip-0341/wallet-test-vectors.json
  @Test
  public void privateKeyTweakingTest() throws Exception {
    ECKey key = ECKey.fromPrivate(Hex.decode("77863416be0d0665e517e1c375fd6f75839544eca553675ef7fdf4949518ebaa"));
    byte[] merkleRoot = Hex.decode("ab179431c28d3b68fb798957faf5497d69c883c6fb1e1cd9f81483d87bac90cc");
    ECKey tweakedPrivateKey = BIP340Util.getTweakedPrivKey(key, merkleRoot);
    String p2TRAddress = BIP340Util.getP2TRAddress(MainNetParams.get(), tweakedPrivateKey, false);
    assert("bc1pwl3s54fzmk0cjnpl3w9af39je7pv5ldg504x5guk2hpecpg2kgsqaqstjq".equals(p2TRAddress));
    assert("ec18ce6af99f43815db543f47b8af5ff5df3b2cb7315c955aa4a86e8143d2bf5".equals(Hex.toHexString(tweakedPrivateKey.getPrivKeyBytes())));
  }

  @Test
  public void _privateKeyTweakingTest() throws Exception {
    ECKey key = ECKey.fromPrivate(Hex.decode("77863416be0d0665e517e1c375fd6f75839544eca553675ef7fdf4949518ebaa"));
    byte[] merkleRoot = Hex.decode("ab179431c28d3b68fb798957faf5497d69c883c6fb1e1cd9f81483d87bac90cc");
    P2TRAddress paddress = new P2TRAddress(key, MainNetParams.get());
    ECKey tweakedPrivateKey = paddress.getTweakedPrivKey(merkleRoot);
    P2TRAddress taddress = new P2TRAddress(tweakedPrivateKey, MainNetParams.get(), false);
    assert("bc1pwl3s54fzmk0cjnpl3w9af39je7pv5ldg504x5guk2hpecpg2kgsqaqstjq".equals(taddress.getDefaultToAddressAsString()));
    assert("ec18ce6af99f43815db543f47b8af5ff5df3b2cb7315c955aa4a86e8143d2bf5".equals(Hex.toHexString(tweakedPrivateKey.getPrivKeyBytes())));
  }

  @Test
  public void privateKeyTweakingTest2() throws Exception {
    ECKey key = ECKey.fromPrivate(Hex.decode("415cfe9c15d9cea27d8104d5517c06e9de48e2f986b695e4f5ffebf230e725d8"));
    byte[] merkleRoot = Hex.decode("2f6b2c5397b6d68ca18e09a3f05161668ffe93a988582d55c6f07bd5b3329def");
    ECKey tweakedPrivateKey = BIP340Util.getTweakedPrivKey(key, merkleRoot);
    String p2TRAddress = BIP340Util.getP2TRAddress(MainNetParams.get(), tweakedPrivateKey, false);
    assert("bc1pw5tf7sqp4f50zka7629jrr036znzew70zxyvvej3zrpf8jg8hqcssyuewe".equals(p2TRAddress));
    assert("241c14f2639d0d7139282aa6abde28dd8a067baa9d633e4e7230287ec2d02901".equals(Hex.toHexString(tweakedPrivateKey.getPrivKeyBytes())));
  }

  @Test
  public void _privateKeyTweakingTest2() throws Exception {
    ECKey key = ECKey.fromPrivate(Hex.decode("415cfe9c15d9cea27d8104d5517c06e9de48e2f986b695e4f5ffebf230e725d8"));
    byte[] merkleRoot = Hex.decode("2f6b2c5397b6d68ca18e09a3f05161668ffe93a988582d55c6f07bd5b3329def");
    P2TRAddress paddress = new P2TRAddress(key, MainNetParams.get());
    ECKey tweakedPrivateKey = paddress.getTweakedPrivKey(merkleRoot);
    P2TRAddress taddress = new P2TRAddress(tweakedPrivateKey, MainNetParams.get(), false);
    assert("bc1pw5tf7sqp4f50zka7629jrr036znzew70zxyvvej3zrpf8jg8hqcssyuewe".equals(taddress.getDefaultToAddressAsString()));
    assert("241c14f2639d0d7139282aa6abde28dd8a067baa9d633e4e7230287ec2d02901".equals(Hex.toHexString(tweakedPrivateKey.getPrivKeyBytes())));
  }

  @Test
  public void privateKeyTweakingTestNoMerkleRoot() throws Exception {
    ECKey key = ECKey.fromPrivate(Hex.decode("6b973d88838f27366ed61c9ad6367663045cb456e28335c109e30717ae0c6baa"));
    byte[] merkleRoot = null;
    ECKey tweakedPrivateKey = BIP340Util.getTweakedPrivKey(key, merkleRoot);
    String p2TRAddress = BIP340Util.getP2TRAddress(MainNetParams.get(), tweakedPrivateKey, false);
    assert("bc1p2wsldez5mud2yam29q22wgfh9439spgduvct83k3pm50fcxa5dps59h4z5".equals(p2TRAddress));
    assert("2405b971772ad26915c8dcdf10f238753a9b837e5f8e6a86fd7c0cce5b7296d9".equals(Hex.toHexString(tweakedPrivateKey.getPrivKeyBytes())));
  }

  @Test
  public void _privateKeyTweakingTestNoMerkleRoot() throws Exception {
    ECKey key = ECKey.fromPrivate(Hex.decode("6b973d88838f27366ed61c9ad6367663045cb456e28335c109e30717ae0c6baa"));
    byte[] merkleRoot = null;
    P2TRAddress paddress = new P2TRAddress(key, MainNetParams.get());
    ECKey tweakedPrivateKey = paddress.getTweakedPrivKey(merkleRoot);
    P2TRAddress taddress = new P2TRAddress(tweakedPrivateKey, MainNetParams.get(), false);
    assert("bc1p2wsldez5mud2yam29q22wgfh9439spgduvct83k3pm50fcxa5dps59h4z5".equals(taddress.getDefaultToAddressAsString()));
    assert("2405b971772ad26915c8dcdf10f238753a9b837e5f8e6a86fd7c0cce5b7296d9".equals(Hex.toHexString(tweakedPrivateKey.getPrivKeyBytes())));
  }

  @Test
  public void tweakVsNonTweakTest() throws IOException {
    ECKey key = ECKey.fromPrivate(Hex.decode("6b973d88838f27366ed61c9ad6367663045cb456e28335c109e30717ae0c6baa"));
    byte[] pubKey = Hex.decode("02d6889cb081036e0faefa3a35157ad71086b123b2b144b649798b494c300a961d"); // the pub key to the above key
    Point pubKeyPoint = BIP340Util.getInternalPubkey(ECKey.fromPublicOnly(pubKey));
    Point tweakedPubKeyPoint = BIP340Util.getTweakedPubKeyFromPoint(pubKeyPoint);
    byte[] tweakedPubKey = tweakedPubKeyPoint.toBytes();
    ECKey tweakedPrivKey = BIP340Util.getTweakedPrivKey(key, null);
    assert(Hex.toHexString(tweakedPubKey).equals(Hex.toHexString(BIP340Util.getInternalPubkey(tweakedPrivKey).toBytes())));
  }

  @Test
  public void _tweakVsNonTweakTest() throws Exception {
    ECKey key = ECKey.fromPrivate(Hex.decode("6b973d88838f27366ed61c9ad6367663045cb456e28335c109e30717ae0c6baa"));
    byte[] pubKey = Hex.decode("02d6889cb081036e0faefa3a35157ad71086b123b2b144b649798b494c300a961d"); // the pub key to the above key
    P2TRAddress paddress = new P2TRAddress(key, MainNetParams.get());
    byte[] tweakedPubKey = paddress.getTweakedPubKeyFromPoint().toBytes();
    P2TRAddress vaddress = new P2TRAddress(key, MainNetParams.get());
    ECKey tweakedPrivateKey = vaddress.getTweakedPrivKey(null);
    P2TRAddress taddress = new P2TRAddress(tweakedPrivateKey, MainNetParams.get());
    assert(Hex.toHexString(tweakedPubKey).equals(Hex.toHexString(taddress.getInternalPubKey().toBytes())));
  }

}
