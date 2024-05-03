package com.samourai.wallet.crypto;

import com.samourai.wallet.crypto.impl.ECDHKeySet;
import org.bitcoinj.core.ECKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;


public class CryptoUtilTest {
  private CryptoUtil cryptoUtil = CryptoUtil.getInstance(new BouncyCastleProvider());

  private ECKey keySender = ECKey.fromPrivate(new BigInteger("45292090369707310635285627500870691371399357286012942906204494584441273561412"));
  private ECKey keyReceiver = ECKey.fromPrivate(new BigInteger("15746563759639979716567498468827168096964808533808569971664267146066160930713"));

  @Test
  public void getSharedSecret() throws Exception {
    ECDHKeySet ecdhKeySet = cryptoUtil.getSharedSecret(keySender, keyReceiver);
    Assertions.assertEquals("uV6eEsBiCfd+G5SLlpglmy38B0xHyTMl0NWlzSdbHxA=", Base64.toBase64String(ecdhKeySet.masterKey));
    Assertions.assertEquals("gosl2XGC3TJDfoW7d0olcK3OrR+MtJabPEYI7C5miW0=", Base64.toBase64String(ecdhKeySet.encryptionKey));
    Assertions.assertEquals("0oeJi1vpoH3MGzbNTfvrrmA2QrDFIT1XyiIciAMWeCI=", Base64.toBase64String(ecdhKeySet.hmacKey));
  }

  @Test
  public void decrypt() throws Exception {
    String data = "all all all all all all all all all all all all";
    byte[] encrypted = Base64.decode("2+IUANM7zrR/fyw+bBoPPFNXbJetSo5MKVtcZR48o46yEkoByLyxqhkwr0yz+lDb4eT5IgeriUYCgxN6+Yq9nHFC3k4hM/6j+++v5FrWwFQNkFz32Opf1aDUKmpL2WzC/ALY7FVEwB2Mh1J8j7t/L1j1AC3WhWVpJMvZ1H638w==");
    ECDHKeySet ecdhKeySet = cryptoUtil.getSharedSecret(keySender, keyReceiver);
    Assertions.assertEquals(data, cryptoUtil.decryptString(encrypted, ecdhKeySet));
  }

  @Test
  public void encrypt() throws Exception {
    String data = "all all all all all all all all all all all all";

    // encrypt
    ECDHKeySet ecdhKeySet1 = cryptoUtil.getSharedSecret(keySender, keyReceiver);
    byte[] encrypted = cryptoUtil.encrypt(data, ecdhKeySet1);

    // verify
    ECDHKeySet ecdhKeySet2 = cryptoUtil.getSharedSecret(keyReceiver, keySender);
    String decrypted = cryptoUtil.decryptString(encrypted, ecdhKeySet2);

    Assertions.assertEquals(data, decrypted);
  }

  @Test
  public void createSignature() throws Exception {
    byte[] data = "all all all all all all all all all all all all".getBytes();

    // create signature
    byte[] signature = cryptoUtil.createSignature(keySender, data);

    // verify
    Assertions.assertTrue(cryptoUtil.verifySignature(keySender, data, signature));
    Assertions.assertFalse(cryptoUtil.verifySignature(keySender, "wrong data".getBytes(), signature));
    Assertions.assertFalse(cryptoUtil.verifySignature(keySender, data, "wrong signature".getBytes()));
    Assertions.assertFalse(cryptoUtil.verifySignature(new ECKey(), data, signature));
  }

  @Test
  public void verifySignature() throws Exception {
      byte[] data = "all all all all all all all all all all all all".getBytes();
      byte[] signature = Base64.decode("MEQCIHpTPcLRu4L5VN3eZkceM/kT+8xRBkmLXBx5lp9gufiaAiB8mlghCtv4P5yS8MerigNp6d0YK37fHSkclGqOuGmUbg==");

      Assertions.assertTrue(cryptoUtil.verifySignature(keySender, data, signature));
      Assertions.assertFalse(cryptoUtil.verifySignature(keySender, "wrong data".getBytes(), signature));
      Assertions.assertFalse(cryptoUtil.verifySignature(keySender, data, "wrong signature".getBytes()));
  }
}
