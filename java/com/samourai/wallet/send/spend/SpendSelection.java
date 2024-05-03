package com.samourai.wallet.send.spend;

import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.SendFactoryGeneric;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.send.beans.SpendError;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.beans.SpendTxSimple;
import com.samourai.wallet.send.beans.SpendType;
import com.samourai.wallet.send.exceptions.MakeTxException;
import com.samourai.wallet.send.exceptions.SignTxException;
import com.samourai.wallet.send.exceptions.SpendException;
import com.samourai.wallet.send.provider.UtxoKeyProvider;
import com.samourai.wallet.send.provider.UtxoProvider;
import com.samourai.wallet.constants.SamouraiAccount;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class SpendSelection {
    private static final Logger log = LoggerFactory.getLogger(SpendSelection.class);
    private BipFormatSupplier bipFormatSupplier;
    private SpendType spendType;
    private List<UTXO> selectedUTXO;

    public SpendSelection(BipFormatSupplier bipFormatSupplier, SpendType spendType) {
        this.bipFormatSupplier = bipFormatSupplier;
        this.spendType = spendType;
        this.selectedUTXO = new ArrayList<>();
    }

    protected BipFormatSupplier getBipFormatSupplier() {
        return bipFormatSupplier;
    }

    public SpendType getSpendType() {
        return spendType;
    }

    public void addSelectedUTXO(UTXO utxo) {
        selectedUTXO.add(utxo);
    }

    public Collection<MyTransactionOutPoint> getSpendFrom() {
        return UTXO.listOutpoints(selectedUTXO);
    }

    public long getTotalValueSelected() {
        return UTXO.sumValue(selectedUTXO);
    }

    public abstract SpendTx spendTx(long amount, String address, SamouraiAccount account, boolean rbfOptIn, NetworkParameters params, BigInteger feePerKb, UtxoProvider utxoProvider, long blockHeight) throws SpendException ;

    protected long computeChange(long amount, BigInteger fee) throws SpendException {
        long change = getTotalValueSelected() - (amount + fee.longValue());
        if (change > 0L && change < SamouraiWalletConst.bDust.longValue()) {
            log.warn("SpendError.DUST_CHANGE: change="+change);
            throw new SpendException(SpendError.DUST_CHANGE);
        }
        return change;
    }

    protected SpendTx computeSpendTx(long amount, boolean entireBalance, long minerFee, long change, Map<String, Long> receivers, boolean rbfOptIn, UtxoKeyProvider keyProvider, NetworkParameters params, long blockHeight) throws SpendException {
        // spend tx
        Transaction tx;
        try {
            tx = SendFactoryGeneric.getInstance().makeTransaction(receivers, getSpendFrom(), keyProvider.getBipFormatSupplier(), rbfOptIn, params, blockHeight);
        } catch (MakeTxException e) {
            log.error("MakeTxException", e);
            throw new SpendException(SpendError.MAKING);
        }
        try {
            tx = SendFactoryGeneric.getInstance().signTransaction(tx, keyProvider);
        } catch (SignTxException e) {
            log.error("spendTx failed", e);
            throw new SpendException(SpendError.SIGNING);
        }
        byte[] serialized = tx.bitcoinSerialize();

        // check fee
        if (minerFee != tx.getFee().value) {
            log.error("fee check failed: "+minerFee+" vs "+tx.getFee().value);
            throw new SpendException(SpendError.MAKING);
        }
        if ((tx.hasWitness() && (minerFee < tx.getVirtualTransactionSize())) || (!tx.hasWitness() && (minerFee < serialized.length))) {
            throw new SpendException(SpendError.INSUFFICIENT_FEE);
        }

        if (log.isDebugEnabled()) {
            log.debug("size:" + serialized.length);
            log.debug("vsize:" + tx.getVirtualTransactionSize());
            log.debug("fee:" + tx.getFee().value);
        }

        /*final RBFSpend rbf;
        if (rbfOptIn) {
            rbf = new RBFSpend();
            for (TransactionInput input : tx.getInputs()) {
                String _addr = TxUtil.getInstance().getToAddress(input.getConnectedOutput());
                AddressType addressType = AddressType.findByAddress(_addr, params);
                String path = APIFactory.getInstance(TxAnimUIActivity.this).getUnspentPaths().get(_addr);
                if (path != null) {
                    if (addressType == AddressType.SEGWIT_NATIVE || addressType == AddressType.SEGWIT_COMPAT) {
                        path += "/"+addressType.getPurpose();
                    }
                    rbf.addKey(input.getOutpoint().toString(), path);
                } else {
                    // TODO zeroleak paymentcodes
                    /*String pcode = BIP47Meta.getInstance().getPCode4Addr(_addr);
                    int idx = BIP47Meta.getInstance().getIdx4Addr(_addr);
                    rbf.addKey(input.getOutpoint().toString(), pcode + "/" + idx);*//*
                }
            }
        } else {
            rbf = null;
        }

        // TODO zeroleak strict mode
        /*
        final List<Integer> strictModeVouts = new ArrayList<Integer>();
        if (SendParams.getInstance().getDestAddress() != null && SendParams.getInstance().getDestAddress().compareTo("") != 0 &&
                PrefsUtil.getInstance(TxAnimUIActivity.this).getValue(PrefsUtil.STRICT_OUTPUTS, true) == true) {
            List<Integer> idxs = SendParams.getInstance().getSpendOutputIndex(tx);
            for(int i = 0; i < tx.getOutputs().size(); i++)   {
                if(!idxs.contains(i))   {
                    strictModeVouts.add(i);
                }
            }
        }*/

        return new SpendTxSimple(spendType, amount, entireBalance, minerFee, 0, change, getSpendFrom(), receivers, tx);
    }
}
