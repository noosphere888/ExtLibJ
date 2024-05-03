package com.samourai.wallet.bipFormat;

import com.samourai.wallet.bip340.BIP340Util;
import com.samourai.wallet.bip340.Schnorr;
import com.samourai.wallet.hd.HD_Account;
import com.samourai.wallet.segwit.SegwitAddress;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.security.SecureRandom;

public class BIP_FORMAT {
    public static final BipFormat LEGACY = new BipFormatImpl("LEGACY", "Original (P2PKH)") {
        @Override
        public String getBipPub(HD_Account hdAccount) {
            return hdAccount.xpubstr();
        }

        @Override
        public String getToAddress(ECKey ecKey, NetworkParameters params) {
            return ecKey.toAddress(params).toString();
        }

        @Override
        public void sign(Transaction tx, int inputIndex, ECKey key) throws Exception {
            TransactionInput txInput = tx.getInput(inputIndex);
            TransactionOutput connectedOutput = txInput.getOutpoint().getConnectedOutput();
            Script scriptPubKey = connectedOutput.getScriptPubKey();

            TransactionSignature signature;
            if(key != null && (key.hasPrivKey() || key.isEncrypted())) {
                byte[] scriptBytes = connectedOutput.getScriptBytes();
                signature = tx.calculateSignature(inputIndex, key, scriptBytes, Transaction.SigHash.ALL, false);
            }
            else {
                signature = TransactionSignature.dummy();   // watch only ?
            }

            if(scriptPubKey.isSentToAddress()) {
                txInput.setScriptSig(ScriptBuilder.createInputScript(signature, key));
            }
            else if(scriptPubKey.isSentToRawPubKey()) {
                txInput.setScriptSig(ScriptBuilder.createInputScript(signature));
            }
            else {
                throw new RuntimeException("Unknown script type: " + scriptPubKey);
            }
        }
    };

    public static final BipFormat SEGWIT_COMPAT = new BipFormatImpl("SEGWIT_COMPAT", "Segwit compatible (P2SH_P2WPKH)") {
        @Override
        public String getBipPub(HD_Account hdAccount) {
            return hdAccount.ypubstr();
        }

        @Override
        public String getToAddress(ECKey ecKey, NetworkParameters params) {
            return new SegwitAddress(ecKey, params).getAddressAsString();
        }

        @Override
        public void sign(Transaction tx, int inputIndex, ECKey key) throws Exception {
            TransactionInput txInput = tx.getInput(inputIndex);
            TransactionOutput connectedOutput = txInput.getOutpoint().getConnectedOutput();
            Script scriptPubKey = connectedOutput.getScriptPubKey();
            Coin value = txInput.getValue();

            SegwitAddress segwitAddress = new SegwitAddress(key.getPubKey(), tx.getParams());
            final Script redeemScript = segwitAddress.segwitRedeemScript();

            TransactionSignature sig =
                    tx.calculateWitnessSignature(
                            inputIndex,
                            key,
                            redeemScript.scriptCode(),
                            value,
                            Transaction.SigHash.ALL,
                            false);
            final TransactionWitness witness = new TransactionWitness(2);
            witness.setPush(0, sig.encodeToBitcoin());
            witness.setPush(1, key.getPubKey());
            tx.setWitness(inputIndex, witness);

            // P2SH
            final ScriptBuilder sigScript = new ScriptBuilder();
            sigScript.data(redeemScript.getProgram());
            txInput.setScriptSig(sigScript.build());
            tx.getInput(inputIndex).getScriptSig().correctlySpends(tx, inputIndex, scriptPubKey, value, Script.ALL_VERIFY_FLAGS);
        }
    };

    public static final BipFormat SEGWIT_NATIVE = new BipFormatImpl("SEGWIT_NATIVE", "Segwit native (P2WPKH)") {
        @Override
        public String getBipPub(HD_Account hdAccount) {
            return hdAccount.zpubstr();
        }

        @Override
        public String getToAddress(ECKey ecKey, NetworkParameters params) {
            return new SegwitAddress(ecKey, params).getBech32AsString();
        }

        @Override
        public void sign(Transaction tx, int inputIndex, ECKey key) throws Exception {
            TransactionInput txInput = tx.getInput(inputIndex);
            Coin value = txInput.getValue();

            SegwitAddress segwitAddress = new SegwitAddress(key.getPubKey(), tx.getParams());
            final Script redeemScript = segwitAddress.segwitRedeemScript();

            TransactionSignature sig =
                    tx.calculateWitnessSignature(
                            inputIndex,
                            key,
                            redeemScript.scriptCode(),
                            value,
                            Transaction.SigHash.ALL,
                            false);
            final TransactionWitness witness = new TransactionWitness(2);
            witness.setPush(0, sig.encodeToBitcoin());
            witness.setPush(1, key.getPubKey());
            tx.setWitness(inputIndex, witness);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BipFormat) {
                return ((BipFormat) obj).getId().equals(getId());
            }
            return super.equals(obj);
        }
    };

    public static final BipFormat TAPROOT = new BipFormatImpl("TAPROOT", "Taproot (P2TR)") {
        @Override
        public String getBipPub(HD_Account hdAccount) {
            return hdAccount.zpubstr();
        }

        @Override
        public String getToAddress(ECKey ecKey, NetworkParameters params) {
            return BIP340Util.getP2TRAddress(params, ecKey, true);
        }

        @Override
        public void sign(Transaction tx, int inputIndex, ECKey key) throws Exception {
            ECKey tweakedKey = BIP340Util.getTweakedPrivKey(key, null);
            int nbInputs = tx.getInputs().size();
            byte[][] inputScriptPubKeys = new byte[nbInputs][];
            Coin[] inputValues = new Coin[nbInputs];

            for(int i=0; i<nbInputs; i++) {
                TransactionInput input = tx.getInput(i);
                inputScriptPubKeys[i] = input.getConnectedOutput().getScriptPubKey().getProgram();
                inputValues[i] = input.getValue();
            }

            Sha256Hash sigHash = tx.hashForTaprootWitnessSignature(inputIndex, Transaction.SigHash.ALL, inputScriptPubKeys, inputValues, false);
            byte[] sig = Schnorr.sign(sigHash.getBytes(), tweakedKey.getPrivKeyBytes(), new SecureRandom().generateSeed(32));

            final TransactionWitness witness = TransactionWitness.createTaprootWitness(sig, Transaction.SigHash.ALL, false);
            tx.setWitness(inputIndex, witness);
        }
    };

    public static final BipFormatSupplier PROVIDER = new BipFormatSupplierImpl();
}
