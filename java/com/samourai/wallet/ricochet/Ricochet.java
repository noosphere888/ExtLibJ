package com.samourai.wallet.ricochet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.util.JSONUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class Ricochet {
    private long nTimeLock;
    private int spend_account;
    private long spend_amount;
    private long change_amount;
    private long samourai_fee;
    private boolean samourai_fee_via_bip47;
    private long fee_per_hop;
    private long feeKB;
    private String destination;
    private long total_spend;
    private long total_miner_fee;
    private long total_vSize;
    private long total_weight;
    private Collection<MyTransactionOutPoint> spendFrom;
    private Collection<RicochetHop> hops;

    public Ricochet() {
        this.nTimeLock = 0;
        this.hops = new LinkedList<>();
    }

    @JsonIgnore
    public Collection<String> getTransactions() {
        return hops.stream().map(hop -> hop.getTx()).collect(Collectors.toList());
    }

    public long getnTimeLock() {
        return nTimeLock;
    }

    public void setnTimeLock(long nTimeLock) {
        this.nTimeLock = nTimeLock;
    }

    public int getSpend_account() {
        return spend_account;
    }

    public void setSpend_account(int spend_account) {
        this.spend_account = spend_account;
    }

    public long getSpend_amount() {
        return spend_amount;
    }

    public void setSpend_amount(long spend_amount) {
        this.spend_amount = spend_amount;
    }

    public long getChange_amount() {
        return change_amount;
    }

    public void setChange_amount(long change_amount) {
        this.change_amount = change_amount;
    }

    public long getSamourai_fee() {
        return samourai_fee;
    }

    public void setSamourai_fee(long samourai_fee) {
        this.samourai_fee = samourai_fee;
    }

    public boolean isSamourai_fee_via_bip47() {
        return samourai_fee_via_bip47;
    }

    public void setSamourai_fee_via_bip47(boolean samourai_fee_via_bip47) {
        this.samourai_fee_via_bip47 = samourai_fee_via_bip47;
    }

    public long getFee_per_hop() {
        return fee_per_hop;
    }

    public void setFee_per_hop(long fee_per_hop) {
        this.fee_per_hop = fee_per_hop;
    }

    public long getFeeKB() {
        return feeKB;
    }

    public void setFeeKB(long feeKB) {
        this.feeKB = feeKB;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Collection<RicochetHop> getHops() {
        return hops;
    }

    public void setHops(Collection<RicochetHop> hops) {
        this.hops = hops;
    }

    public void addHop(RicochetHop hop) {
        this.hops.add(hop);
    }

    public long getTotal_spend() {
        return total_spend;
    }

    public void setTotal_spend(long total_spend) {
        this.total_spend = total_spend;
    }

    public long getTotal_miner_fee() {
        return total_miner_fee;
    }

    public void setTotal_miner_fee(long total_miner_fee) {
        this.total_miner_fee = total_miner_fee;
    }

    public long getTotal_vSize() {
        return total_vSize;
    }

    public void setTotal_vSize(long total_vSize) {
        this.total_vSize = total_vSize;
    }

    public long getTotal_weight() {
        return total_weight;
    }

    public void setTotal_weight(long total_weight) {
        this.total_weight = total_weight;
    }

    @JsonIgnore
    public Collection<MyTransactionOutPoint> getSpendFrom() {
        return spendFrom;
    }

    public void setSpendFrom(Collection<MyTransactionOutPoint> spendFrom) {
        this.spendFrom = spendFrom;
    }

    public String toJsonString() throws Exception {
        return JSONUtils.getInstance().getObjectMapper().writeValueAsString(this);
    }
}
