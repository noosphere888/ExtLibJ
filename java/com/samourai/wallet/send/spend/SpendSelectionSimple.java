package com.samourai.wallet.send.spend;

import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.send.beans.SpendError;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.beans.SpendType;
import com.samourai.wallet.send.exceptions.SpendException;
import com.samourai.wallet.send.provider.UtxoProvider;
import com.samourai.wallet.util.FeeUtil;
import com.samourai.wallet.constants.SamouraiAccount;
import org.apache.commons.lang3.tuple.Triple;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

public class SpendSelectionSimple extends SpendSelection {
    private static final Logger log = LoggerFactory.getLogger(SpendSelectionSimple.class);
    private BipFormat changeFormat;
    private boolean entireBalance;

    protected SpendSelectionSimple(BipFormatSupplier bipFormatSupplier, Collection<UTXO> utxos, BipFormat changeFormat, boolean entireBalance) {
        super(bipFormatSupplier, SpendType.SIMPLE);

        for (UTXO utxo : utxos) {
            addSelectedUTXO(utxo);
        }
        this.changeFormat = changeFormat;
        this.entireBalance = entireBalance;
    }

    public static SpendSelectionSimple compute(Collection<UTXO> utxos, long amount, BipFormat changeFormat, BipFormatSupplier bipFormatSupplier, NetworkParameters params, BigInteger feePerKb) {
        // get smallest 1 UTXO > than spend + fee + dust
        SpendSelectionSimple spendSelection = computeSingleSmallestUtxo(utxos, amount, changeFormat, bipFormatSupplier, params, feePerKb);
        if (spendSelection != null) {
            if (log.isDebugEnabled()) {
                log.debug("SIMPLE spending smallest possible utxo");
            }
            return spendSelection;
        }

        // get largest UTXOs > than spend + fee + dust
        spendSelection = SpendSelectionSimple.computeMultipleLargestUtxos(utxos, amount, changeFormat, bipFormatSupplier, params, feePerKb);
        if (spendSelection != null) {
            if (log.isDebugEnabled()) {
                log.debug("SIMPLE spending multiple utxos");
            }
            return spendSelection;
        }
        return null;
    }

    private static SpendSelectionSimple computeSingleSmallestUtxo(Collection<UTXO> utxos, long amount, BipFormat changeFormat, BipFormatSupplier bipFormatSupplier, NetworkParameters params, BigInteger feePerKb) {
        // sort in ascending order by value
        List<UTXO> sortedUtxos = new ArrayList<>(utxos);
        Collections.sort(sortedUtxos, new UTXO.UTXOComparator());
        Collections.reverse(sortedUtxos);

        // get smallest 1 UTXO > than spend + fee + dust
        for (UTXO u : sortedUtxos) {
            Triple<Integer, Integer, Integer> outpointTypes = FeeUtil.getInstance().getOutpointCount(new Vector(u.getOutpoints()), params);
            if (u.getValue() >= (amount + SamouraiWalletConst.bDust.longValue() + FeeUtil.getInstance().estimatedFeeSegwit(outpointTypes.getLeft(), outpointTypes.getMiddle(), outpointTypes.getRight(), 2, 0, feePerKb).longValue())) {
                if (log.isDebugEnabled()) {
                    log.debug("spend type:" + SpendType.SIMPLE);
                    log.debug("single output");
                    log.debug("amount:" + amount);
                    log.debug("value selected:" + u.getValue());
                    log.debug("total value selected:" + u.getValue());
                    log.debug("nb inputs:" + u.getOutpoints().size());
                }
                return new SpendSelectionSimple(bipFormatSupplier, Arrays.asList(u), changeFormat, false);
            }
        }
        return null;
    }

    private static SpendSelectionSimple computeMultipleLargestUtxos(Collection<UTXO> utxos, long amount, BipFormat changeFormat, BipFormatSupplier bipFormatSupplier, NetworkParameters params, BigInteger feePerKb) {
        // sort in descending order by value
        List<UTXO> sortedUtxos = new ArrayList<>(utxos);
        Collections.sort(sortedUtxos, new UTXO.UTXOComparator());
        int selected = 0;
        int p2pkh = 0;
        int p2sh_p2wpkh = 0;
        int p2wpkh = 0;

        // get largest UTXOs > than spend + fee + dust
        long totalValueSelected = 0L;
        List<UTXO> selectedUTXO = new ArrayList<UTXO>();
        for (UTXO u : sortedUtxos) {

            selectedUTXO.add(u);
            totalValueSelected += u.getValue();
            selected += u.getOutpoints().size();

//                            Log.d("SendActivity", "value selected:" + u.getValue());
//                            Log.d("SendActivity", "total value selected/threshold:" + totalValueSelected + "/" + (amount + SamouraiWallet.bDust.longValue() + FeeUtil.getInstance().estimatedFee(selected, 2).longValue()));

            Triple<Integer, Integer, Integer> outpointTypes = FeeUtil.getInstance().getOutpointCount(new Vector<MyTransactionOutPoint>(u.getOutpoints()), params);
            p2pkh += outpointTypes.getLeft();
            p2sh_p2wpkh += outpointTypes.getMiddle();
            p2wpkh += outpointTypes.getRight();
            if (totalValueSelected >= (amount + SamouraiWalletConst.bDust.longValue() + FeeUtil.getInstance().estimatedFeeSegwit(p2pkh, p2sh_p2wpkh, p2wpkh, 2, 0, feePerKb).longValue())) {
                if (log.isDebugEnabled()) {
                    log.debug("spend type:" + SpendType.SIMPLE);
                    log.debug("multiple outputs");
                    log.debug("amount:" + amount);
                    log.debug("total value selected:" + totalValueSelected);
                    log.debug("nb inputs:" + selected);
                }
                return new SpendSelectionSimple(bipFormatSupplier, selectedUTXO, changeFormat, false);
            }
        }
        return null;
    }

    @Override
    public SpendTx spendTx(long amount, String address, SamouraiAccount account, boolean rbfOptIn, NetworkParameters params, BigInteger feePerKb, UtxoProvider utxoProvider, long blockHeight) throws SpendException {
        Collection<MyTransactionOutPoint> outpoints = getSpendFrom();
        Triple<Integer, Integer, Integer> outpointTypes = FeeUtil.getInstance().getOutpointCount(new Vector(outpoints), params);
        BigInteger fee;
        long change;
        Map<String, Long> receivers = new HashMap<>();
        if (amount == getTotalValueSelected()) {
            // NO CHANGE = 1 output, spending entire balance

            // estimate fee
            fee = FeeUtil.getInstance().estimatedFeeSegwit(outpointTypes.getLeft(), outpointTypes.getMiddle(), outpointTypes.getRight(), 1, 0, feePerKb);

            // adjust amount
            amount -= fee.longValue();

            // add recipient output
            if (receivers.containsKey(address)) {
                // prevent erasing existing receiver
                log.error("receiver already set");
                throw new SpendException(SpendError.MAKING);
            }
            receivers.put(address, amount);
            change = 0;
        } else{
            // WITH CHANGE = 2 outputs

            // estimate fee
            fee = FeeUtil.getInstance().estimatedFeeSegwit(outpointTypes.getLeft(), outpointTypes.getMiddle(), outpointTypes.getRight(), 2, 0, feePerKb);

            // compute change
            change = computeChange(amount, fee);

            // add recipient output
            if (receivers.containsKey(address)) {
                // prevent erasing existing receiver
                log.error("receiver already set");
                throw new SpendException(SpendError.MAKING);
            }
            receivers.put(address, amount);

            // add change output
            String changeAddress = utxoProvider.getNextAddressChange(account, changeFormat, true);
            if (changeAddress.equals(address)) {
                // prevent erasing existing receiver
                log.error("address and changeAddress are identical");
                throw new SpendException(SpendError.MAKING);
            }
            receivers.put(changeAddress, change);
        }

        //
        // fee sanity check
        //
        // TODO zeroleak restoreChangeIndexes?
        SpendTx spendTx = computeSpendTx(amount, entireBalance, fee.longValue(), change, receivers, rbfOptIn, utxoProvider, params, blockHeight);
        return spendTx;
    }

    public boolean isEntireBalance() {
        return entireBalance;
    }
}
