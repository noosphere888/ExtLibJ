package com.samourai.wallet.fidelity.timelocks;

import java.util.List;
import java.util.ArrayList;

import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.hd.HD_WalletFactoryGeneric;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.TransactionWitness;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.crypto.TransactionSignature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.wallet.segwit.FidelityTimelockAddress;
import com.samourai.wallet.util.Util;

// test vectors taken from https://gist.github.com/chris-belcher/7257763cedcc014de2cd4239857cd36e
public class FidelityTimelocksTest {

    @Test
    public void testVectors() throws Exception  {

      // vectors by Belcher at: https://gist.github.com/chris-belcher/7257763cedcc014de2cd4239857cd36e

      NetworkParameters params = MainNetParams.get();

      HD_WalletFactoryGeneric hdWalletFactory = HD_WalletFactoryGeneric.getInstance();
      byte[] seed = hdWalletFactory.computeSeedFromWords("abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about");
      HD_Wallet hdw84 = hdWalletFactory.getBIP84(seed, "", params);

      List<String> pubkeys = new ArrayList<String>();
      List<String> privkeys = new ArrayList<String>();
      List<Integer> indexes = new ArrayList<Integer>();
      List<String> redeemScripts = new ArrayList<String>();
      List<String> scriptPubkeys = new ArrayList<String>();
      List<String> addresses = new ArrayList<String>();

      pubkeys.add("02a1b09f93073c63f205086440898141c0c3c6d24f69a18db608224bcf143fa011");
      privkeys.add("L2tQBEdhC48YLeEWNg3e4msk94iKfyVa9hdfzRwUERabZ53TfH3d");
      indexes.add(0);
      redeemScripts.add("0400e10b5eb1752102a1b09f93073c63f205086440898141c0c3c6d24f69a18db608224bcf143fa011ac");
      scriptPubkeys.add("0020bdee9515359fc9df912318523b4cd22f1c0b5410232dc943be73f9f4f07e39ad");
      addresses.add("bc1qhhhf29f4nlyalyfrrpfrknxj9uwqk4qsyvkujsa7w0ulfur78xkspsqn84");

      pubkeys.add("02599f6db8b33265a44200fef0be79c927398ed0b46c6a82fa6ddaa5be2714002d");
      privkeys.add("KxctaFBzetyc9KXeUr6jxESCZiCEXRuwnQMw7h7hroP6MqnWN6Pf");
      indexes.add(1);
      redeemScripts.add("0480bf345eb1752102599f6db8b33265a44200fef0be79c927398ed0b46c6a82fa6ddaa5be2714002dac");
      scriptPubkeys.add("0020b8f898643991608524ed04e0c6779f632a57f1ffa3a3a306cd81432c5533e9ae");
      addresses.add("bc1qhrufsepej9sg2f8dqnsvvaulvv490u0l5w36xpkds9pjc4fnaxhq7pcm4h");

      pubkeys.add("03ec8067418537bbb52d5d3e64e2868e67635c33cfeadeb9a46199f89ebfaab226");
      privkeys.add("L3SYqae23ZoDDcyEA8rRBK83h1MDqxaDG57imMc9FUx1J8o9anQe");
      indexes.add(240);
      redeemScripts.add("05807eaa8300b1752103ec8067418537bbb52d5d3e64e2868e67635c33cfeadeb9a46199f89ebfaab226ac");
      scriptPubkeys.add("0020e7de0ad2720ae1d6cc9b6ad91af57eb74646762cf594c91c18f6d5e7a873635a");
      addresses.add("bc1qul0q45njptsadnymdtv34at7karyva3v7k2vj8qc7m2702rnvddq0z20u5");

      pubkeys.add("0308c5751121b1ae5c973cdc7071312f6fc10ab864262f0cbd8134f056166e50f3");
      privkeys.add("L5Z9DDMnj5RZMyyPiQLCvN48Xt7GGmev6cjvJXD8uz5EqiY8trNJ");
      indexes.add(959);
      redeemScripts.add("0580785df400b175210308c5751121b1ae5c973cdc7071312f6fc10ab864262f0cbd8134f056166e50f3ac");
      scriptPubkeys.add("0020803268e042008737cf439748cbb5a4449e311da9aa64ae3ac56d84d059654f85");
      addresses.add("bc1qsqex3czzqzrn0n6rjayvhddygj0rz8df4fj2uwk9dkzdqkt9f7zs5c493u");

      for(int i = 0; i < pubkeys.size(); i++) {

        FidelityTimelockAddress faddress = new FidelityTimelockAddress(Util.hexToBytes(pubkeys.get(i)), params, indexes.get(i));

        String redeemScript = Util.bytesToHex(faddress.segwitRedeemScript().getProgram());
        byte[] scriptpubkey = faddress.segwitOutputScript().getProgram();
        String saddress = faddress.getTimelockAddressAsString();

        // test vectors
        Assertions.assertEquals(redeemScripts.get(i), Util.bytesToHex(faddress.segwitRedeemScript().getProgram()).toLowerCase());
        Assertions.assertEquals(scriptPubkeys.get(i), Util.bytesToHex(scriptpubkey).toLowerCase());
        Assertions.assertEquals(addresses.get(i), saddress);

        // path tests
        Assertions.assertEquals(pubkeys.get(i), Util.bytesToHex(hdw84.getAddressAt(0, 2, indexes.get(i)).getECKey().getPubKey()).toLowerCase());
        Assertions.assertEquals(privkeys.get(i), hdw84.getAddressAt(0, 2, indexes.get(i)).getECKey().getPrivateKeyAsWiF(params));
      }

      DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(params, privkeys.get(0));
      ECKey eckey = dpk.getKey();
      Assertions.assertEquals("16vmiGpY1rEaYnpGgtG7FZgr2uFCpeDgV6", eckey.toAddress(params).toString());
      // From test vectors:
      // Note that as signatures contains a random nonce, it might not be exactly the same when your code generates it
      // p2pkh address is the p2pkh address corresponding to the derived public key, it can be used to verify the message
      // signature in any wallet that supports Verify Message.
      //
      // This code asserts OK in our tests:
      //
      // String sig = eckey.signMessage("fidelity-bond-cert|020000000000000000000000000000000000000000000000000000000000000001|375");
      // Assertions.assertEquals("H2b/90XcKnIU/D1nSCPhk8OcxrHebMCr4Ok2d2yDnbKDTSThNsNKA64CT4v2kt+xA1JmGRG/dMnUUH1kKqCVSHo=", sign);
      eckey.verifyMessage("fidelity-bond-cert|020000000000000000000000000000000000000000000000000000000000000001|375", "H2b/90XcKnIU/D1nSCPhk8OcxrHebMCr4Ok2d2yDnbKDTSThNsNKA64CT4v2kt+xA1JmGRG/dMnUUH1kKqCVSHo=");

      String sig = eckey.signMessage("fidelity-bond-cert|020000000000000000000000000000000000000000000000000000000000000001|375");
      eckey.verifyMessage("fidelity-bond-cert|020000000000000000000000000000000000000000000000000000000000000001|375", sig);

    }

    @Test
    public void testTransaction() throws Exception  {

      NetworkParameters params = TestNet3Params.get();

      DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(params, "cSdzxBCNGiy87xh533V6fnpGkZ5GBMAYqTm5umGi381oMvV5QjAh");
      ECKey eckey = dpk.getKey();
      FidelityTimelockAddress faddress = new FidelityTimelockAddress(eckey, params, 2);
      Script redeemScript = faddress.segwitRedeemScript();

      String prevHash = "3cc94a320a2a9c94458a68eaa28b89ffa89d29c74735196a3a7f3f7827805982";
      long prevIdx = 1;
      long prevAmount = 100000000L;
      long spendAmount = 99999400L;

      String toAddress = "tb1pns3ykx8xew6a7vgas2v5v6c9ezc4aa42eagf9cjm37fqvmgaugnsnd02xw";

      Transaction tx = new Transaction(params);
      tx.setVersion(2);
      tx.setLockTime(faddress.getTimelock());   // nLocktime <= present time && >= CLTV timelock

      TransactionOutPoint outpoint = new TransactionOutPoint(params, prevIdx, Sha256Hash.wrap(prevHash));
      TransactionInput input = new TransactionInput(params, null, new byte[0], outpoint);
      input.setSequenceNumber(0xfffffffe);
      TransactionOutput output = Bech32UtilGeneric.getInstance().getTransactionOutput(toAddress, spendAmount, params);
      tx.addInput(input);
      tx.addOutput(output);

      TransactionSignature sig = tx.calculateWitnessSignature(0, eckey, redeemScript.getProgram(), Coin.valueOf(prevAmount), Transaction.SigHash.ALL, false);
      TransactionWitness witness = new TransactionWitness(2);
      witness.setPush(0, sig.encodeToBitcoin());
      witness.setPush(1, redeemScript.getProgram());
      tx.setWitness(0, witness);

      byte[] serialized = tx.bitcoinSerialize();
      Assertions.assertEquals(Util.bytesToHex(serialized).toUpperCase(), "0200000000010182598027783F7F3A6A193547C7299DA8FF898BA2EA688A45949C2A0A324AC93C0100000000FEFFFFFF01A8DEF505000000002251209C224B18E6CBB5DF311D8299466B05C8B15EF6AACF5092E25B8F92066D1DE22702473044022049873DB0370ECAF030E26FCA2EE9437BD5351DE0AE0A3154BC18EDFE897A0A2A02205CC98A20A1B463870D62547D56196F28C82C3F165BE16ABEDE4AFBF706CF07B4012A0400FB5A5EB1752103D665BB0F8CB7053C2A4DA40E1A1CCB83C62BB71BEB27E21EF70E5B22312329F7AC00FB5A5E");

    }

    @Test
    public void testTransactionFuture() throws Exception  {

      NetworkParameters params = TestNet3Params.get();

      DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(params, "cSCbi8sSbZXaMN5PgvCx9agw3garQL8XT1BoqHRturr5K4tndXUi");
      ECKey eckey = dpk.getKey();
      FidelityTimelockAddress faddress = new FidelityTimelockAddress(eckey, params, 959);
      Script redeemScript = faddress.segwitRedeemScript();

      String prevHash = "249ad1298554be84dcf4d02cef39a18449c5af3390d2f44f4e765361f87e392b";
      long prevIdx = 1;
      long prevAmount = 100000L;
      long spendAmount = 99430L;

      String toAddress = "tb1pns3ykx8xew6a7vgas2v5v6c9ezc4aa42eagf9cjm37fqvmgaugnsnd02xw";

      Transaction tx = new Transaction(params);
      tx.setVersion(2);
      tx.setLockTime(faddress.getTimelock());   // nLocktime <= present time && >= CLTV timelock

      TransactionOutPoint outpoint = new TransactionOutPoint(params, prevIdx, Sha256Hash.wrap(prevHash));
      TransactionInput input = new TransactionInput(params, null, new byte[0], outpoint);
      input.setSequenceNumber(0xfffffffe);
      TransactionOutput output = Bech32UtilGeneric.getInstance().getTransactionOutput(toAddress, spendAmount, params);
      tx.addInput(input);
      tx.addOutput(output);

      TransactionSignature sig = tx.calculateWitnessSignature(0, eckey, redeemScript.getProgram(), Coin.valueOf(prevAmount), Transaction.SigHash.ALL, false);
      TransactionWitness witness = new TransactionWitness(2);
      witness.setPush(0, sig.encodeToBitcoin());
      witness.setPush(1, redeemScript.getProgram());
      tx.setWitness(0, witness);

      byte[] serialized = tx.bitcoinSerialize();
      Assertions.assertEquals(Util.bytesToHex(serialized).toUpperCase(), "020000000001012B397EF86153764E4FF4D29033AFC54984A139EF2CD0F4DC84BE548529D19A240100000000FEFFFFFF0166840100000000002251209C224B18E6CBB5DF311D8299466B05C8B15EF6AACF5092E25B8F92066D1DE22702473044022076F1CC512D984C6118732682C508836B9C377DE5DBBEC20AE2EF3277C0A8BBB602206F8950521A2BF09CFE5865F19E601BACE5A3726CF753AB178C7D4623C3C74283012B0580785DF400B1752103A64668F579E1A7126191B2536572426569624E8ED7910CDCFBA1B2179A7CB26FAC80785DF4");

    }

}
