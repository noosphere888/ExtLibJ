package com.samourai.wallet.bip47.rpc.obpp05;

import org.bitcoinj.core.ECKey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.samourai.wallet.bip47.rpc.obpp05.*;
import com.samourai.wallet.util.Util;

public class TestVectorsOBPP_RFC05 {

    // seed Alice: response seminar brave tip suit recall often sound stick owner lottery motion
    // seed Bob: reward upper indicate eight swift arch injury crystal super wrestle already dentist
    private static String xprivAlice = "xprv9z4XcXV3eLXcvuA72k9mcMAjFtqYoY8QsFLmc5G9bGnAAwJtr9CuMqME9BsaAhddpBsq2w2Xc6XLWmLG1RHfquXb4dy9yehambtmLT6iJuR";
    private static String xprivBob = "xprv9ycwhF2CmmqHd8Cxtyjv43M9iNbbzd9ktjTCNo2fnw2het4G6FcucAFTpb5GFnN8YvjAGruX3UEMNxdfQeYHm2UUcmCLppaAT6BvB7NJbBi";
    private static String xpubAlice = "xpub6D3t231wUi5v9PEa8mgmyV7Tovg3CzrGEUGNQTfm9cK93je3PgX9udfhzUDx29pkeeHQBPpTSHpAxnDgsf2XRbvLrmbCUQybjtHx8SUb3JB";
    private static String xpubBob = "xpub6CcJ6kZ6c9PaqcHS11GvRBHtGQS6Q5scFxNoBBSHMGZgXgPQdnwA9xZwfquC4rnLVH9ua1sNmL6uBLFHSYH2ZwFvsVHV1ELuM5pSDSA9pfK";

    private static String input = "";

    @Test
    public void test0() throws Exception  {

      PaymentCode pccodeV3Alice = new PaymentCode(xpubAlice);
      pccodeV3Alice.setXprv(xprivAlice);
      PaymentCode pccodeV3Bob = new PaymentCode(xpubBob);
      pccodeV3Bob.setXprv(xprivBob);

      ECKey eckeyAlice = null;
      byte[] _A = new byte[33];
      byte[] _F = new byte[33];
      byte[] _G = new byte[33];

      ECKey eckeyBob = null;

      {
          Assertions.assertEquals("PD1jTsa1rjnbMMLVbj5cg2c8KkFY32KWtPRqVVpSBkv1jf8zjHJVu", pccodeV3Alice.toString());

          //
          // change address
          //
          BigInteger privkey = new BigInteger(1, Hex.decode("872313fe1beb41a9e1ae19c0def97591e5c204387b64b85f4077078b232906d0"));
          eckeyAlice = ECKey.fromPrivate(privkey, true);

          byte[] k = pccodeV3Alice.getNotifPrivkey();
          Assertions.assertEquals("030a5280a538fe5a134b77d96f5cd9d050c11021d86e8b4cf327f064a7c76b0db4", Hex.toHexString(ECKey.fromPrivate(k, true).getPubKey()));
          Assertions.assertEquals("7167db816df3e03b4f4df749dd1c1cf5b9a81ae0ce0b2f4dc5d8b75aea4e77e0", Hex.toHexString(k));

      }

      {

          Assertions.assertEquals("PD1jFsimY3DQUe7qGtx3z8BohTaT6r4kwJMCYXwp7uY8z6BSaFrpM", pccodeV3Bob.toString());

          //
          // change address
          //
          BigInteger privkey = new BigInteger(1, Hex.decode("0fb05a28df58b2add0d01eb491962b79092239e4d9396442eed83144b6541f4c"));
          eckeyBob = ECKey.fromPrivate(privkey, true);

          byte[] k = pccodeV3Bob.getNotifPrivkey();
          Assertions.assertEquals("023aeb8ccc72ff375de289993b87cc98c8f621ecca1e62b3747a76952bf71b7efe", Hex.toHexString(ECKey.fromPrivate(k, true).getPubKey()));
          Assertions.assertEquals("6850fcb45313e30f941f91d49bbad21260161c9ea7ed4a322930176db945f0bd", Hex.toHexString(k));

      }

      //
      // Alice
      //
      {

          Assertions.assertEquals("0383b5e54776628baacee0cbb66b4db31aa95176dba1f62cabf0415103d0fdbda6", Hex.toHexString(eckeyAlice.getPubKey()));
          System.arraycopy(eckeyAlice.getPubKey(), 0, _A, 0, _A.length);

          // v3
          byte[] F = pccodeV3Alice.getIdentifierV3();
          Assertions.assertEquals("0205be1671949473c1b252db7aff98a8704841ad7cd19596f9d64ed81bd3e58bc8", Hex.toHexString(F));
          System.arraycopy(F, 0, _F, 0, _F.length);
          // v2
          byte[] identifierAlice = pccodeV3Alice.getIdentifierV2();
          Assertions.assertEquals("02f84e63e94e70678ab8367ef91711259fc98885be92479afec1a6a656e2245636", Hex.toHexString(identifierAlice));
          // v1
          identifierAlice = pccodeV3Alice.getIdentifierV1();
          Assertions.assertEquals("0277a215775bbacf7e0d325154a093e8d9f69c19f45b08d00114b214cc24b134f9", Hex.toHexString(identifierAlice));

          byte[] G = pccodeV3Alice.blind(eckeyAlice, pccodeV3Bob);
          Assertions.assertEquals("0292d97c287932848852890ded442311623e32ebfeba12e2020b41c2fbe12f3812", Hex.toHexString(G));
          System.arraycopy(G, 0, _G, 0, _G.length);

      }

      List<String> pubkeys = new ArrayList<String>();
      int limit = 10;

      {

          for(int idx = 0; idx < limit; idx++)   {
              try {
                  PaymentAddress paymentAddress = new PaymentAddress(pccodeV3Bob, idx, pccodeV3Alice.getNotifPrivkey(), PaymentAddress.BTC_TESTNET_COIN);
                  pubkeys.add(Hex.toHexString(paymentAddress.getSendAddress().getPubKey()));
              }
              catch(Exception e) {
                  ;
              }
          }

          Assertions.assertEquals("03dc41458b939d966a0e141281c2a7c5faf184dc43bc26160f0ffc3c583600c9b6", pubkeys.get(0));
          Assertions.assertEquals("02513de274f78ce0c8cb827f25aae2ade941ac9d482002fc04ef60d580c5403afd", pubkeys.get(1));
          Assertions.assertEquals("033cf4391b3e7daad0220b572d796ac0711e93e2ef389119d3ec0bed2debf0472a", pubkeys.get(2));
          Assertions.assertEquals("02de639a0d80bc8b6976e71e5242b7f0ba5e9f8f6b317c0a180884424600bcaafc", pubkeys.get(3));
          Assertions.assertEquals("036b08a58e0d664505c95e2e0ceaa87e34c82cc6ed91a94980fc631967cc8d931f", pubkeys.get(4));
          Assertions.assertEquals("029d5dae4c27c59a9c207a1beafab9f1b8bef93e19b8bbd7614dae37e8f7c0210c", pubkeys.get(5));
          Assertions.assertEquals("03003668a8915ba65adb9ff8cfcce7f8d5aae2655a210e1e863eda6cb41dd5e1d2", pubkeys.get(6));
          Assertions.assertEquals("03a857d0bef97a0e5ffb1911e7cd13ced1bdce9c2a6a838dd5bdd8e805f44b8cc9", pubkeys.get(7));
          Assertions.assertEquals("02bcfcdc2e7fbdaebf1fa69a74ccd219c919981353433538ff98979c252609c564", pubkeys.get(8));
          Assertions.assertEquals("029dccbb87fec52713f90afbbef3e78dddef4dfa6858bbf2a5fd2fcd2582a5cf7f", pubkeys.get(9));

      }

      {

          for(int idx = 0; idx < limit; idx++)   {
            PaymentAddress paymentAddress = new PaymentAddress(pccodeV3Alice, 0, pccodeV3Bob.derivePrivkey(idx), PaymentAddress.BTC_TESTNET_COIN);
            ECKey k = ECKey.fromPrivate(paymentAddress.getReceiveAddress().getPrivKeyBytes());
            Assertions.assertEquals(pubkeys.get(idx), Hex.toHexString(k.getPubKey()));
          }

          pubkeys.clear();
          for(int idx = 0; idx < limit; idx++)   {
            PaymentAddress paymentAddress = new PaymentAddress(pccodeV3Alice, idx, pccodeV3Bob.getNotifPrivkey(), PaymentAddress.BTC_MAINNET_COIN);
            pubkeys.add(Hex.toHexString(paymentAddress.getSendAddress().getPubKey()));
          }

          Assertions.assertEquals("024edba30e70855e7846e850982f2eb3aefe33b292cc9a744604367de14cc018b8", pubkeys.get(0));
          Assertions.assertEquals("03a769eb57ce38dc3f7d80c4464bc61b02153a8e881c472d6d3e99b1d8fe53100c", pubkeys.get(1));
          Assertions.assertEquals("038f8e84682fb78ec6fdf3560020df035e144ce60bb9b09dd99b606d130140bd2c", pubkeys.get(2));
          Assertions.assertEquals("0210964b717a97430e9ca206bf84e1b0834385a03af3c749d60ad632d31e511954", pubkeys.get(3));
          Assertions.assertEquals("03b24c25099596f0984e4eedcc6147d1faff269a79f919e5d42414ea0691749174", pubkeys.get(4));
          Assertions.assertEquals("0285b4cda5356a7333510fac98fc27da4df8a3fcf6f50df594fbe6013e78d64114", pubkeys.get(5));
          Assertions.assertEquals("02a6946888b559db413f94a6de3aa974d4c22d881f132f753297baef510219327c", pubkeys.get(6));
          Assertions.assertEquals("02ab944a2509a27b9b9f569736e6cb45cb1c900627573a01bb9dffe38131103a12", pubkeys.get(7));
          Assertions.assertEquals("0276442e645c3f5e412b60ffac771a67b0ef1b652b18f18d101e9c6f70365cd183", pubkeys.get(8));
          Assertions.assertEquals("039386636f65cbc72a70bacc0f43ee17862ead8d37941e72b630f37e048ef2d405", pubkeys.get(9));

          for(int idx = 0; idx < limit; idx++)   {
            PaymentAddress paymentAddress = new PaymentAddress(pccodeV3Bob, 0, pccodeV3Alice.derivePrivkey(idx), PaymentAddress.BTC_MAINNET_COIN);
            ECKey k = ECKey.fromPrivate(paymentAddress.getReceiveAddress().getPrivKeyBytes());
            Assertions.assertEquals(pubkeys.get(idx), Hex.toHexString(k.getPubKey()));
          }

      }

      //
      // Bob
      //
      {

          Assertions.assertEquals("0389087b9573ccc7efc5252a8a7c93d349d9b3dd882724c818e5369cbff0647d35", Hex.toHexString(eckeyBob.getPubKey()));

          // v3
          byte[] F = pccodeV3Bob.getIdentifierV3();
          Assertions.assertEquals("02ce75616fcd80345bca54dabd279b155f960c57260378455b872269221de231b6", Hex.toHexString(F));
          System.arraycopy(F, 0, _F, 0, _F.length);
          // v2
          byte[] identifierBob = pccodeV3Bob.getIdentifierV2();
          Assertions.assertEquals("02a6806034129abafba1511019991cca9bd8bededb1580bdc4fe0eb905dec8da2d", Hex.toHexString(identifierBob));
          // v1
          identifierBob = pccodeV3Bob.getIdentifierV1();
          Assertions.assertEquals("024e299b083f610d5ab6e7e241089f185cf222deb9a790eacf01a72930c90d2261", Hex.toHexString(identifierBob));

          SecretPoint secretPoint = new SecretPoint(eckeyBob.getPrivKeyBytes(), pccodeV3Alice.getNotifPubkey());
          byte[] secretPointX = secretPoint.ECDHSecretAsBytes();
          Assertions.assertEquals("030a5280a538fe5a134b77d96f5cd9d050c11021d86e8b4cf327f064a7c76b0db4", Hex.toHexString(pccodeV3Alice.getNotifPubkey()));
          Assertions.assertEquals("96d39fc4abc1138c7dc862d84d7617434fa4fa525d4334fa81f9c905aefa643b", Hex.toHexString(secretPointX));

          byte[] blindFactor = Util.getHMAC(secretPointX, eckeyBob.getPubKey());
          Assertions.assertEquals("e29add66d59cce8998499e27d13101ce66af67a14c7410fc77b3d9b172244669f376417d1a72c068bb004ac01907752d14a48a52d6c82d6cd86b8483d37713b9", Hex.toHexString(blindFactor));
          byte[] G = new byte[33];
          G[0] = pccodeV3Bob.getPayload()[1];
          byte[] bf = new byte[32];
          byte[] pc = new byte[32];
          System.arraycopy(blindFactor, 0, bf, 0, bf.length);
          byte[] pl = pccodeV3Bob.getPayload();
          Assertions.assertEquals("03029d125e1cb89e5a1a108192643ee25370c2e75c192b10aac18de8d5a09b5f48d5", Hex.toHexString(pl));
          System.arraycopy(pl, 2, pc, 0, pc.length);
          Assertions.assertEquals("7f88837a6d02949388c80c43efd352bea4483bb86764ba3dfa5b0c11e97b0ebc", Hex.toHexString(Util.xor(bf, pc)));
          System.arraycopy(Util.xor(bf, pc), 0, G, 1, Util.xor(bf, pc).length);
          Assertions.assertEquals("027f88837a6d02949388c80c43efd352bea4483bb86764ba3dfa5b0c11e97b0ebc", Hex.toHexString(G));

      }

      //
      // unblinding
      //
      {
          byte[] pl = pccodeV3Bob.unblind(_A, _G);
          Assertions.assertEquals("PD1jTsa1rjnbMMLVbj5cg2c8KkFY32KWtPRqVVpSBkv1jf8zjHJVu", new PaymentCode(pl).toString());
      }

    }

    @Test
    public void fromV1() throws Exception  {

      PaymentCode pcodeV3 = PaymentCode.fromV1("PM8TJTLJbPRGxSbc8EJi42Wrr6QbNSaSSVJ5Y3E4pbCYiTHUskHg13935Ubb7q8tx9GVbh2UuRnBc3WSyJHhUrw8KhprKnn9eDznYGieTzFcwQRya4GA");
      Assertions.assertEquals("PD1jTsa1rjnbMMLVbj5cg2c8KkFY32KWtPRqVVpSBkv1jf8zjHJVu", pcodeV3.toString());

      pcodeV3 = PaymentCode.fromV1("PM8TJS2JxQ5ztXUpBBRnpTbcUXbUHy2T1abfrb3KkAAtMEGNbey4oumH7Hc578WgQJhPjBxteQ5GHHToTYHE3A1w6p7tU6KSoFmWBVbFGjKPisZDbP97");
      Assertions.assertEquals("PD1jFsimY3DQUe7qGtx3z8BohTaT6r4kwJMCYXwp7uY8z6BSaFrpM", pcodeV3.toString());

    }

}
