package com.samourai.wallet.send.spend;

import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.client.indexHandler.IIndexHandler;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.send.beans.SpendError;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.exceptions.SpendException;
import com.samourai.wallet.send.provider.UtxoProvider;
import com.samourai.wallet.util.FeeUtil;
import com.samourai.wallet.constants.SamouraiAccount;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpendBuilder {
    private static final Logger log = LoggerFactory.getLogger(SpendBuilder.class);

    private UtxoProvider utxoProvider;

    public SpendBuilder(UtxoProvider utxoProvider) {
        this.utxoProvider = utxoProvider;
    }

    // forcedChangeType may be null
    public SpendTx preview(BipWallet spendWallet, BipWallet changeWallet, String address, long amount, boolean stonewall, boolean rbfOptIn, BigInteger feePerKb, BipFormat forcedChangeFormat, List<MyTransactionOutPoint> preselectedInputs, long blockHeight) throws Exception {
        SamouraiAccount account = spendWallet.getAccount();
        NetworkParameters params = spendWallet.getParams();
        SpendSelection spendSelection = computeSpendSelection(spendWallet, changeWallet, address, amount, stonewall, feePerKb, forcedChangeFormat, preselectedInputs);

        SpendTx spendTx = spendSelection.spendTx(amount, address, account, rbfOptIn, params, feePerKb, utxoProvider, blockHeight);
        if (spendTx != null) {
            if (log.isDebugEnabled()) {
                log.debug("spend type:" + spendSelection.getSpendType());
                log.debug("amount:" + amount);
                log.debug("total value selected:" + spendSelection.getTotalValueSelected());
                log.debug("minerFeeTotal:" + spendTx.getMinerFeeTotal());
                log.debug("nb inputs:" + spendTx.getSpendFrom().size());
            }
        }
        return spendTx;
    }

    private SpendSelection computeSpendSelection(BipWallet spendWallet, BipWallet changeWallet, String address, long amount, boolean stonewall, BigInteger feePerKb, BipFormat forcedChangeFormat, List<MyTransactionOutPoint> preselectedInputs) throws SpendException {
        SamouraiAccount account = spendWallet.getAccount();
        NetworkParameters params = spendWallet.getParams();
        BipFormat changeFormat = computeAddressFormat(forcedChangeFormat, address, utxoProvider.getBipFormatSupplier(), params);

        Collection<UTXO> allUtxos = utxoProvider.getUtxos(account);
        SpendSelection spendSelection = null;
        if (amount > 0 && UTXO.sumValue(allUtxos) == amount) {
            // spend entire balance (can only be simple spend, fees will be deducted later)
            if (log.isDebugEnabled()) {
                log.debug("SIMPLE spending (entire balance)");
            }
            spendSelection = new SpendSelectionSimple(utxoProvider.getBipFormatSupplier(), allUtxos, changeFormat, true);
        }
        else if (preselectedInputs != null && !preselectedInputs.isEmpty()) {
            // spend preselected UTXOs
            spendSelection = computeSpendSelectionForUtxosPreselected(amount, changeFormat, preselectedInputs, feePerKb, params);
        } else {
            // spend best UTXOs
            spendSelection = computeSpendSelectionForUtxosAvailable(spendWallet, changeWallet, address, stonewall, amount, changeFormat, feePerKb, forcedChangeFormat);
        }
        return spendSelection;
    }

    // if possible, get UTXO by input 'type': p2pkh, p2sh-p2wpkh or p2wpkh, else get all UTXO
    private Collection<UTXO> findUtxosAvailableForAddressFormat(long amount, SamouraiAccount account, BipFormat addressFormat, BigInteger feePerKb, NetworkParameters params) throws SpendException {
        // TODO filter-out do-not-spends
        /*
        //Filtering out do not spends
        for (String key : postmix.keySet()) {
            UTXO u = new UTXO();
            for (MyTransactionOutPoint out : postmix.get(key).getOutpoints()) {
                if (!BlockedUTXO.getInstance().contains(out.getTxHash().toString(), out.getTxOutputN())) {
                    u.getOutpoints().add(out);
                    u.setPath(postmix.get(key).getPath());
                }
            }
            if (u.getOutpoints().size() > 0) {
                utxos.add(u);
            }
        }*/

        // spend by addressType
        Collection<UTXO> utxosByAddressFormat = utxoProvider.getUtxos(account, addressFormat);
        long neededAmount = computeNeededAmount(UTXO.listOutpoints(utxosByAddressFormat), amount, feePerKb, params);
        long availableBalance = UTXO.sumValue(utxosByAddressFormat);
        if (availableBalance >= neededAmount) {
            return utxosByAddressFormat;
        }

        // do not mix AddressTypes for postmix
        if (account == SamouraiAccount.POSTMIX) {
            log.warn("InsufficientFundsException: amount="+amount+", neededAmount="+neededAmount+", availableBalance="+availableBalance+" (POSTMIX/"+addressFormat+")");
            throw new SpendException(SpendError.INSUFFICIENT_FUNDS);
        }

        // fallback by mixed type
        Collection<UTXO> allUtxos = utxoProvider.getUtxos(account);
        neededAmount = computeNeededAmount(UTXO.listOutpoints(allUtxos), amount, feePerKb, params);
        availableBalance = UTXO.sumValue(allUtxos);
        if (availableBalance >= neededAmount) {
            return allUtxos;
        }
        log.warn("InsufficientFundsException: amount="+amount+", neededAmount="+neededAmount+", availableBalance="+availableBalance+" ("+account+"/all)");
        throw new SpendException(SpendError.INSUFFICIENT_FUNDS);
    }

    private SpendSelection computeSpendSelectionForUtxosPreselected(long amount, BipFormat changeFormat, Collection<MyTransactionOutPoint> preselectedInputs, BigInteger feePerKb, NetworkParameters params) throws SpendException {
        // use all preselected UTXOs
        Collection<UTXO> utxos = new ArrayList<>();
        for (MyTransactionOutPoint outPoint : preselectedInputs) {
            UTXO u = new UTXO();
            List<MyTransactionOutPoint> outs = new ArrayList<>();
            outs.add(outPoint);
            u.setOutpoints(outs);
            utxos.add(u);
        }

        // check balance
        long neededAmount = computeNeededAmount(preselectedInputs, amount, feePerKb, params);
        long preselectedBalance = UTXO.sumValue(utxos);
        if (neededAmount > preselectedBalance) {
            log.warn("InsufficientFundsException: amount="+amount+", neededAmount="+neededAmount+", preselectedBalance="+preselectedBalance);
            throw new SpendException(SpendError.INSUFFICIENT_FUNDS);
        }

        return new SpendSelectionSimple(utxoProvider.getBipFormatSupplier(), utxos, changeFormat, false);
    }

    private SpendSelection computeSpendSelectionForUtxosAvailable(BipWallet spendWallet, BipWallet changeWallet, String address, boolean stonewall, long amount, BipFormat changeFormat, BigInteger feePerKb, BipFormat forcedChangeFormat) throws SpendException {
        SamouraiAccount account = spendWallet.getAccount();
        NetworkParameters params = spendWallet.getParams();

        // get all UTXO (throws SpendException on insufficient balance)
        Collection<UTXO> utxos = findUtxosAvailableForAddressFormat(amount, account, changeFormat, feePerKb, params);

        // stonewall spend
        if (stonewall) {
            IIndexHandler changeIndexHandler = changeWallet.getIndexHandlerChange();
            SpendSelection spendSelection = SpendSelectionStonewall.compute(utxoProvider, changeFormat, amount, address, account, forcedChangeFormat, params, feePerKb, changeIndexHandler);
            if (spendSelection != null) {
                return spendSelection;
            }
        }

        // simple spend (less than balance)
        BipFormatSupplier bipFormatSupplier = utxoProvider.getBipFormatSupplier();
        SpendSelection spendSelection = SpendSelectionSimple.compute(utxos, amount, changeFormat, bipFormatSupplier, params, feePerKb);
        if (spendSelection != null) {
            return spendSelection;
        }

        // no selection found
        throw new SpendException(SpendError.MAKING);
    }

    private static long computeNeededAmount(long fee, long amount) {
        return fee + amount + SamouraiWalletConst.bDust.longValue();
    }

    public static long computeNeededAmount(Collection<MyTransactionOutPoint> outPoints, long amount, BigInteger feePerKb, NetworkParameters params) {
        long fee = FeeUtil.getInstance().estimatedFeeSegwit(outPoints, 4, 0, feePerKb, params).longValue();
        return computeNeededAmount(fee, amount);
    }

    public static BipFormat computeAddressFormat(BipFormat forcedChangeFormat, String address, BipFormatSupplier bipFormatSupplier, NetworkParameters params) {
        if (forcedChangeFormat != null) {
            return forcedChangeFormat;
        }
        return bipFormatSupplier.findByAddress(address, params);
    }
}
