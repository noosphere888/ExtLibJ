package com.samourai.wallet.send.beans;

import com.samourai.wallet.api.backend.IPushTx;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.exceptions.SpendException;
import org.bitcoinj.core.TransactionOutPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class SpendTx {
    private static final Logger log = LoggerFactory.getLogger(SpendTx.class);
    private SpendType spendType;
    private long amount;
    private boolean entireBalance;
    private long minerFeeTotal;
    private long minerFeePaid;
    private long samouraiFee;
    private long change;
    private Collection<MyTransactionOutPoint> spendFrom;
    private Map<String, Long> receivers;
    private int virtualTransactionSize;
    private int weight;
    private String txid;

    public SpendTx(SpendType spendType, long amount, boolean entireBalance, long minerFeeTotal, long minerFeePaid, long samouraiFee, long change, Collection<MyTransactionOutPoint> spendFrom, Map<String, Long> receivers, int virtualTransactionSize, int weight, String txid) throws SpendException {
        this.spendType = spendType;
        this.amount = amount;
        this.entireBalance = entireBalance;
        this.minerFeeTotal = minerFeeTotal;
        this.minerFeePaid = minerFeePaid;
        this.samouraiFee = samouraiFee;
        this.spendFrom = spendFrom;
        this.receivers = receivers;
        this.change = change;
        this.virtualTransactionSize = virtualTransactionSize;
        this.weight = weight;
        this.txid = txid;

        // consistency check
        long sumSpendFrom = spendFrom.stream().mapToLong(o -> o.getValue().getValue()).sum();
        if((amount + samouraiFee + change + minerFeePaid) != sumSpendFrom){
            // should never happen
            log.error("inconsistency detected! (amount="+amount+" + samouraiFee="+samouraiFee+" + change="+change+" + minerFeePaid="+minerFeePaid+") != sumSpendFrom="+sumSpendFrom);
            throw new SpendException(SpendError.MAKING);
        }

        if(minerFeePaid > minerFeeTotal){
            // should never happen
            log.error("inconsistency detected! minerFeePaid="+minerFeePaid+" > minerFeeTotal="+minerFeeTotal);
            throw new SpendException(SpendError.MAKING);
        }
    }

    public SpendType getSpendType() {
        return spendType;
    }

    public long getAmount() {
        return amount;
    }

    public boolean isEntireBalance() {
        return entireBalance;
    }

    public long getMinerFeeTotal() {
        return minerFeeTotal;
    }

    public long getMinerFeePaid() {
        return minerFeePaid;
    }

    public long getSamouraiFee() {
        return samouraiFee;
    }

    public long getChange() {
        return change;
    }

    public Map<String, Long> getSpendTo() {
        return receivers;
    }

    public Collection<? extends TransactionOutPoint> getSpendFrom() {
        return spendFrom;
    }

    public Map<String, Long> getReceivers() {
        return receivers;
    }

    public int getVirtualTransactionSize() {
        return virtualTransactionSize;
    }

    public int getWeight() {
        return weight;
    }

    public String getTxid() {
        return txid;
    }

    public abstract void pushTx(IPushTx pushTx) throws Exception;
}
