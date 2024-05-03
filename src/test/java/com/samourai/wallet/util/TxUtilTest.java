package com.samourai.wallet.util;

import com.samourai.wallet.segwit.SegwitAddress;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class TxUtilTest {
    private static final TxUtil txUtil = TxUtil.getInstance();
    private static final NetworkParameters params = TestNet3Params.get();
    private ECKey inputKey = ECKey.fromPrivate(new BigInteger("45292090369707310635285627500870691371399357286012942906204494584441273561412"));
    private ECKey outputKey = ECKey.fromPrivate(new BigInteger("77292090369707310635285627500870691371399357286012942906204494584441273561412"));

    private Transaction computeTxCoinbase(long value, Script outputScript) {
        Transaction tx = new Transaction(params);

        // add output
        tx.addOutput(Coin.valueOf(value), outputScript);

        // add input: coinbase
        int txCounter = 1;
        TransactionInput input =
                new TransactionInput(
                        params, tx, new byte[] {(byte) txCounter, (byte) (txCounter++ >> 8)});
        tx.addInput(input);

        tx.verify();
        return tx;
    }

    private Transaction computeSpendTx(ECKey inputKey, Script inputScript, TransactionOutPoint inputOutPoint, TransactionOutput transactionOutput) {
        Transaction tx = new Transaction(params);

        if (transactionOutput == null) {
            // add dummy output
            SegwitAddress outputAddress = new SegwitAddress(outputKey, params);
            transactionOutput = new TransactionOutput(params, null, inputOutPoint.getValue(),
                    outputAddress.getAddress());
            tx.addOutput(transactionOutput);
        }

        // add input
        tx.addSignedInput(inputOutPoint, inputScript, inputKey);

        tx.verify();
        return tx;
    }

    private void doTestFindInputPubkeyAndVerifySignInput(final TransactionOutput linkedOutput, ECKey inputKey, boolean amountSigned) throws Exception {
        // spend linkedOutput
        TransactionOutPoint inputOutPoint = linkedOutput.getOutPointFor();
        inputOutPoint.setValue(linkedOutput.getValue());
        Script inputScript = linkedOutput.getScriptPubKey(); // equivaut à outputScript
        Transaction tx = computeSpendTx(inputKey, inputScript, inputOutPoint, null);

        Callback<byte[]> fetchInputOutpointScriptBytes = () -> linkedOutput.getScriptBytes();

        // TEST findInputPubkey
        byte[] pubkey = txUtil.findInputPubkey(tx, 0, fetchInputOutpointScriptBytes);
        Assertions.assertArrayEquals(inputKey.getPubKey(), pubkey);

        // TEST verifySignInput: valid signature
        txUtil.verifySignInput(tx, 0, linkedOutput.getValue().getValue(), linkedOutput.getScriptBytes());

        // verifySignInput: wrong scriptBytes
        try {
            Script dummyScript = ScriptBuilder.createP2WPKHOutputScript(new ECKey());

            txUtil.verifySignInput(tx, 0, linkedOutput.getValue().getValue(),
                    dummyScript.getProgram()); // should raise an exception
            Assertions.assertTrue(false);
        } catch(Exception e) {
            // ok
        }

        // verifySignInput: wrong amount
        try {
            txUtil.verifySignInput(tx, 0, linkedOutput.getValue().getValue() - 1,
                    linkedOutput.getScriptBytes()); // should raise an exception
            if (amountSigned) {
                Assertions.assertTrue(false, "segwit should not verify signature for invalid amount");
            }
        } catch(Exception e) {
            if (!amountSigned) {
                Assertions.assertTrue(false, "non-segwit should verify signature even for invalid amount");
            }
        }
    }

    @Test
    public void testP2WPKH() throws Exception {
        SegwitAddress inputAddress = new SegwitAddress(inputKey, params);
        ECKey inputKey = inputAddress.getECKey();

        // spend coinbase -> P2WPKH
        Script outputScript = ScriptBuilder.createP2WPKHOutputScript(inputKey);
        Transaction tx = computeTxCoinbase(999999, outputScript);

        // test
        TransactionOutput txOutput = tx.getOutput(0);
        doTestFindInputPubkeyAndVerifySignInput(txOutput, inputKey, true);

        Assertions.assertEquals("047b5dfd753abf764cf9a137e1c9edf8134b4d978718ac722bb30f668a3c1484", tx.getHashAsString());
    }

    @Test
    public void testP2SHP2WPKH() throws Exception {
        SegwitAddress inputAddress = new SegwitAddress(inputKey, params);
        ECKey inputKey = inputAddress.getECKey();
        long value = 999999;

        // spend coinbase -> P2SHP2WPKH
        Script ouputScriptP2WPKH = ScriptBuilder.createP2WPKHOutputScript(inputKey);
        Script outputScript = ScriptBuilder.createP2SHOutputScript(ouputScriptP2WPKH);
        Transaction tx = computeTxCoinbase(value, outputScript);

        // test
        TransactionOutput txOutput = tx.getOutput(0);
        doTestFindInputPubkeyAndVerifySignInput(txOutput, inputKey, true);

        Assertions.assertEquals("fb785733c06564588e1975bbaad36ad5179219c0a326cf3dff779f913540fd96", tx.getHashAsString());
    }

    @Test
    public void testP2PKH() throws Exception {
        // spend coinbase -> P2PKH
        Address inputAddressP2PKH = new Address(params, inputKey.getPubKeyHash());
        Script outputScript = ScriptBuilder.createOutputScript(inputAddressP2PKH);
        Transaction tx = computeTxCoinbase(999999, outputScript);

        // test
        TransactionOutput txOutput = tx.getOutput(0);
        doTestFindInputPubkeyAndVerifySignInput(txOutput, inputKey, false);

        Assertions.assertEquals("e972a7d2aed41a56f2869b218ed8f7e5665824b6b09d02b8958821624fb257e1", tx.getHashAsString());
    }

    @Test
    public void testP2PK() throws Exception {
        // spend coinbase -> P2PK
        Script outputScript = ScriptBuilder.createOutputScript(inputKey);
        Transaction tx = computeTxCoinbase(999999, outputScript);

        // test
        TransactionOutput txOutput = tx.getOutput(0);
        doTestFindInputPubkeyAndVerifySignInput(txOutput, inputKey, false);

        Assertions.assertEquals("e3d2845a77dfd83e571e62633ba353347f1f7eeb5c6f57c5781ee66ffeada9b7", tx.getHashAsString());
    }

    @Test
    public void getTxHex() throws Exception {
        // spend coinbase -> P2PK
        Script outputScript = ScriptBuilder.createOutputScript(inputKey);
        Transaction txCoinbase = computeTxCoinbase(999999, outputScript);
        TransactionOutput txOutput = txCoinbase.getOutput(0);

        TransactionOutPoint inputOutPoint = txOutput.getOutPointFor();
        inputOutPoint.setValue(txOutput.getValue());
        Script inputScript = txOutput.getScriptPubKey();
        Transaction tx = computeSpendTx(inputKey, inputScript, inputOutPoint, null);

        // test
        String txHex = txUtil.getTxHex(tx);
        Assertions.assertEquals("0100000001b7a9adfe6fe61e78c5576f5ceb7e1f7f3453a33b63621e573ed8df775a84d2e30000000049483045022100d9f002ad1e648f1658f20605efb33eb3375a3d4eceecc9d6415034622d60cbe302204bcfdc452a5fa4aa43ce0af6459e64ac674a51b8773d8b00cd29d7d33d323d2501ffffffff013f420f000000000017a91430a3a154ab9b649fc4f57dff2ac8ec3a400c825b8700000000", txHex);
    }

    @Test
    public void fromTxHex() throws Exception {
        Transaction tx = TxUtil.getInstance().fromTxHex(params, "0100000001b7a9adfe6fe61e78c5576f5ceb7e1f7f3453a33b63621e573ed8df775a84d2e30000000049483045022100d9f002ad1e648f1658f20605efb33eb3375a3d4eceecc9d6415034622d60cbe302204bcfdc452a5fa4aa43ce0af6459e64ac674a51b8773d8b00cd29d7d33d323d2501ffffffff013f420f000000000017a91430a3a154ab9b649fc4f57dff2ac8ec3a400c825b8700000000");
        Assertions.assertEquals("067e0b9e37a9164e4d54bba02a9173ab629341fd96b98893d2ceff04db351f5b", tx.getHashAsString());
    }

    @Test
    public void findInputIndex() throws Exception {
        Transaction tx = TxUtil.getInstance().fromTxHex(params, "0100000001b7a9adfe6fe61e78c5576f5ceb7e1f7f3453a33b63621e573ed8df775a84d2e30000000049483045022100d9f002ad1e648f1658f20605efb33eb3375a3d4eceecc9d6415034622d60cbe302204bcfdc452a5fa4aa43ce0af6459e64ac674a51b8773d8b00cd29d7d33d323d2501ffffffff013f420f000000000017a91430a3a154ab9b649fc4f57dff2ac8ec3a400c825b8700000000");

        // test
        Integer index = txUtil.findInputIndex(tx, "e3d2845a77dfd83e571e62633ba353347f1f7eeb5c6f57c5781ee66ffeada9b7", 0);
        Assertions.assertEquals(0, index);

        index = txUtil.findInputIndex(tx, "foo", 0);
        Assertions.assertEquals(null, index);
    }
}
