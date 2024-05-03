package com.samourai.wallet.send.spend;

import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.client.indexHandler.IIndexHandler;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.send.beans.SpendError;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.beans.SpendType;
import com.samourai.wallet.send.exceptions.SpendException;
import com.samourai.wallet.send.provider.UtxoProvider;
import com.samourai.wallet.constants.SamouraiAccount;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.TransactionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

public class SpendSelectionStonewall extends SpendSelection {
    private static final Logger log = LoggerFactory.getLogger(SpendSelectionStonewall.class);
    private static final StonewallUtil stonewallUtil = StonewallUtil.getInstance();
    private List<MyTransactionOutPoint> inputs;
    private List<TransactionOutput> outputs;

    protected SpendSelectionStonewall(BipFormatSupplier bipFormatSupplier, List<MyTransactionOutPoint> inputs, List<TransactionOutput> outputs) {
        super(bipFormatSupplier, SpendType.STONEWALL);
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public static SpendSelectionStonewall compute(UtxoProvider utxoProvider, BipFormat changeFormat, long amount, String address, SamouraiAccount account, BipFormat forcedChangeFormat, NetworkParameters params, BigInteger feePerKb, IIndexHandler changeIndexHandler) {
        // find inputs
        List<Collection<UTXO>> utxoSets = stonewallUtil.utxoSets(utxoProvider, changeFormat, account);
        Pair<List<UTXO>,List<UTXO>> utxosPair = stonewallUtil.stonewallInputs(utxoSets, changeFormat, amount, params, feePerKb);

        // STONEWALL spend
        int initialChangeIndex = changeIndexHandler.get();
        SpendSelectionStonewall spendSelection = stonewallUtil.stonewall(utxosPair.getLeft(), utxosPair.getRight(), BigInteger.valueOf(amount), address, account, utxoProvider, forcedChangeFormat, params, feePerKb);
        if (spendSelection == null) {
            // can't do stonewall => revert change index
            changeIndexHandler.set(initialChangeIndex, true);
            return null;
        }
        return spendSelection;
    }

    @Override
    public SpendTx spendTx(long amount, String address, SamouraiAccount account, boolean rbfOptIn, NetworkParameters params, BigInteger feePerKb, UtxoProvider utxoProvider, long blockHeight) throws SpendException {
        // select utxos for stonewall
        long inputAmount = 0L;
        long outputAmount = 0L;

        for (MyTransactionOutPoint outpoint : inputs) {
            UTXO u = new UTXO();
            List<MyTransactionOutPoint> outs = new ArrayList<MyTransactionOutPoint>();
            outs.add(outpoint);
            u.setOutpoints(outs);
            addSelectedUTXO(u);
            inputAmount += u.getValue();
        }

        Map<String, Long> receivers = new HashMap<>();
        for (TransactionOutput output : outputs) {
            try {
                String outputAddress = getBipFormatSupplier().getToAddress(output);
                if (receivers.containsKey(outputAddress)) {
                    // prevent erasing existing receiver
                    log.error("receiver already set");
                    throw new SpendException(SpendError.MAKING);
                }
                receivers.put(outputAddress, output.getValue().longValue());
                outputAmount += output.getValue().longValue();
            } catch (Exception e) {
                throw new SpendException(SpendError.BIP126_OUTPUT);
            }
        }

        BigInteger fee = BigInteger.valueOf(inputAmount - outputAmount);
        long change = computeChange(amount, fee);
        SpendTx spendTx = computeSpendTx(amount, false, fee.longValue(), change, receivers, rbfOptIn, utxoProvider, params, blockHeight);
        return spendTx;
    }
}
