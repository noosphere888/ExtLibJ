package com.samourai.wallet.util;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.bip47.rpc.BIP47Wallet;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.hd.HD_WalletFactoryGeneric;
import com.samourai.wallet.segwit.SegwitAddress;
import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.SendFactoryGeneric;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

public class CryptoTestUtil {
    protected static final Logger log = LoggerFactory.getLogger(CryptoTestUtil.class);
    private static final HD_WalletFactoryGeneric hdWalletFactory = HD_WalletFactoryGeneric.getInstance();
    private static final NetworkParameters params = TestNet3Params.get();
    private static final ECKey ecKey = ECKey.fromPrivate(new BigInteger("45292090369707310635285627500870691371399357286012942906204494584441273561412"));
    private static final BipFormatSupplier bipFormatSupplier = BIP_FORMAT.PROVIDER;

    private CryptoTestUtil() {}

    private static CryptoTestUtil instance = null;
    public static CryptoTestUtil getInstance() {
        if(instance == null) {
            instance = new CryptoTestUtil();
        }
        return instance;
    }

    public BIP47Wallet generateBip47Wallet(NetworkParameters networkParameters) throws Exception {
        HD_Wallet bip44Wallet = hdWalletFactory.generateWallet(44, networkParameters);
        BIP47Wallet bip47Wallet = new BIP47Wallet(bip44Wallet);
        return bip47Wallet;
    }

    public SegwitAddress generateSegwitAddress(NetworkParameters params) {
        SegwitAddress segwitAddress = new SegwitAddress(ecKey, params);
        return segwitAddress;
    }

    public TransactionOutPoint generateTransactionOutPoint(String toAddress, long amount, NetworkParameters params) throws Exception {
        // generate transaction with bitcoinj
        Transaction transaction = new Transaction(params);

        // add output
        TransactionOutput transactionOutput =
            Bech32UtilGeneric.getInstance().getTransactionOutput(toAddress, amount, params);
        transaction.addOutput(transactionOutput);

        // add coinbase input
        int txCounter = 1;
        TransactionInput transactionInput =
            new TransactionInput(
                params, transaction, new byte[] {(byte) txCounter, (byte) (txCounter++ >> 8)});
        transaction.addInput(transactionInput);

        TransactionOutPoint transactionOutPoint = transactionOutput.getOutPointFor();
        transactionOutPoint.setValue(Coin.valueOf(amount));
        return transactionOutPoint;
    }

    public Transaction generateTx(int inputsP2PKH,
                                         int inputsP2SHP2WPKH,
                                         int inputsP2WPKH,
                                        int outputsNonP2WSH_P2TR,
                                        int outputsP2WSH_P2TR,
                                         int outputsOpReturn) throws Exception {
        Transaction tx = new Transaction(params);
        List<ECKey> privKeys = new LinkedList<>();

        // inputs
        for (int i=0; i<inputsP2PKH; i++) {
            BipFormat bipFormat = BIP_FORMAT.LEGACY;
            appendInput(tx, bipFormat, privKeys);
        }
        for (int i=0; i<inputsP2SHP2WPKH; i++) {
            BipFormat bipFormat = BIP_FORMAT.SEGWIT_COMPAT;
            appendInput(tx, bipFormat, privKeys);
        }
        for (int i=0; i<inputsP2WPKH; i++) {
            BipFormat bipFormat = BIP_FORMAT.SEGWIT_NATIVE;
            appendInput(tx, bipFormat, privKeys);
        }

        // outputs
        for (int i=0; i<outputsNonP2WSH_P2TR; i++) {
            String address = generateSegwitAddress(params).getBech32AsString();
            TransactionOutput txOut = bipFormatSupplier.getTransactionOutput(address, 100, params);
            tx.addOutput(txOut);
        }
        for (int i=0; i<outputsP2WSH_P2TR; i++) {
            String address = "tb1pqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesf3hn0c";
            TransactionOutput txOut = bipFormatSupplier.getTransactionOutput(address, 100, params);
            tx.addOutput(txOut);
        }
        for (int i=0; i<outputsOpReturn; i++) {
            byte[] payload = RandomUtil.getInstance().nextBytes(80);
            Script op_returnOutputScript =
                    new ScriptBuilder().op(ScriptOpCodes.OP_RETURN).data(payload).build();
            TransactionOutput txOut =
                    new TransactionOutput(params, null, Coin.valueOf(0L), op_returnOutputScript.getProgram());
            tx.addOutput(txOut);
        }

        // sign
        for (int i=0; i<tx.getInputs().size(); i++) {
            ECKey privKey = privKeys.get(i);
            SendFactoryGeneric.getInstance().signInput(privKey, tx, i, bipFormatSupplier);
        }

        tx.verify();
        return tx;
    }

    private void appendInput(Transaction tx, BipFormat bipFormat, List<ECKey> privKeys) throws Exception {
        ECKey privKey = new ECKey();
        privKeys.add(privKey);

        long value = 10000;
        String address = bipFormat.getToAddress(privKey, params);
        String inputHash = "598cbf9f11ab9a1a5e788dbd11a7cf970089cec43e04fc073eb91c0a5717fd0e";
        byte[] scriptBytes = bipFormatSupplier.getTransactionOutput(address, value, params).getScriptBytes();
        MyTransactionOutPoint outPoint = new MyTransactionOutPoint(params, new Sha256Hash(inputHash), tx.getInputs().size(), BigInteger.valueOf(value), scriptBytes, address, 9999);
        String path = "path...";
        String xpub = "xpub...";
        UnspentOutput utxo = new UnspentOutput(outPoint, path, xpub);
        TransactionInput input = utxo.computeSpendInput(params);
        tx.addInput(input);
    }

}
