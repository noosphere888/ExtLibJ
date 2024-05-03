package com.samourai.wallet.timelocks;

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
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.crypto.TransactionSignature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.wallet.segwit.TimelockAddress;
import com.samourai.wallet.util.Util;

public class TimelocksTest {

    @Test
    public void testTransaction() throws Exception  {

      NetworkParameters params = TestNet3Params.get();

      DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(params, "cSdzxBCNGiy87xh533V6fnpGkZ5GBMAYqTm5umGi381oMvV5QjAh");
      ECKey eckey = dpk.getKey();
      TimelockAddress taddress = new TimelockAddress(eckey, params, 1668336636L);
      Script redeemScript = taddress.segwitRedeemScript();

      String prevHash = "d570a38001357240e9957999d8e7e55304eaa0a0d6d3b02d1b1e72684f89e833";
      long prevIdx = 1;
      long prevAmount = 125000L;
      long spendAmount = 124440L;

      String toAddress = "tb1pns3ykx8xew6a7vgas2v5v6c9ezc4aa42eagf9cjm37fqvmgaugnsnd02xw";

      Transaction tx = new Transaction(params);
      tx.setVersion(2);
      tx.setLockTime(taddress.getTimelock());   // nLocktime <= present time && >= CLTV timelock

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
      Assertions.assertEquals(Util.bytesToHex(serialized).toUpperCase(), "0200000000010133E8894F68721E1B2DB0D3D6A0A0EA0453E5E7D8997995E94072350180A370D50100000000FEFFFFFF0118E60100000000002251209C224B18E6CBB5DF311D8299466B05C8B15EF6AACF5092E25B8F92066D1DE22702483045022100A78EDA60344C9D1C6CB856697A7776C1222A70F8BF098E74B384C68B8FB95D0A0220349B940F3876402D1601E7A548404E8FA7DF1770CF9C636C784D56F9B4FF982C012A04FCCB7063B1752103D665BB0F8CB7053C2A4DA40E1A1CCB83C62BB71BEB27E21EF70E5B22312329F7ACFCCB7063");

    }

    @Test
    public void testTransactionFuture() throws Exception  {

      NetworkParameters params = TestNet3Params.get();

      DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(params, "cQWyG7JbyfQU7MzYEhvczDs3A5ScBKv1tSwangdJKmhEoa9fWtvk");
      ECKey eckey = dpk.getKey();
      TimelockAddress taddress = new TimelockAddress(eckey, params, 4099766400L);
      Script redeemScript = taddress.segwitRedeemScript();

      String prevHash = "4d7a0ef04f7395f30d35e231b6a831aa09aeed56b28d943db130500b185b6c04";
      long prevIdx = 1;
      long prevAmount = 100000L;
      long spendAmount = 99440L;

      String toAddress = "tb1pns3ykx8xew6a7vgas2v5v6c9ezc4aa42eagf9cjm37fqvmgaugnsnd02xw";

      Transaction tx = new Transaction(params);
      tx.setVersion(2);
      tx.setLockTime(taddress.getTimelock());   // nLocktime <= present time && >= CLTV timelock

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
      Assertions.assertEquals(Util.bytesToHex(serialized).toUpperCase(), "02000000000101046C5B180B5030B13D948DB256EDAE09AA31A8B631E2350DF395734FF00E7A4D0100000000FEFFFFFF0170840100000000002251209C224B18E6CBB5DF311D8299466B05C8B15EF6AACF5092E25B8F92066D1DE22702483045022100825DF4F56DADEBDE7FAFDC24367480665E542148698C44F2EB8029D9F65FFE55022063534B7C86B3B424E0ADC7DEEE67938E0529D9C534E1384BD93A966712606D98012B0580785DF400B175210263D37F7CE5260D6409CB22CFDDB7442A6E588A7F3BC0F6C53C31F42BF1E78CC5AC80785DF4");

    }

}
