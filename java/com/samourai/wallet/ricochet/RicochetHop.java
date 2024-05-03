package com.samourai.wallet.ricochet;

public class RicochetHop {
    private int seq;
    private long spend_amount;
    private long fee;
    private int index;
    private String destination;
    private String tx;
    private String hash;
    private long nTimeLock;

    private String prev_tx_hash;
    private int prev_tx_n;
    private long prev_spend_value;
    private String script;

    private String samourai_fee_address;
    private long samourai_fee_amount;

    public RicochetHop() {
        this.nTimeLock = 0;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public long getSpend_amount() {
        return spend_amount;
    }

    public void setSpend_amount(long spend_amount) {
        this.spend_amount = spend_amount;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTx() {
        return tx;
    }

    public void setTx(String tx) {
        this.tx = tx;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getnTimeLock() {
        return nTimeLock;
    }

    public void setnTimeLock(long nTimeLock) {
        this.nTimeLock = nTimeLock;
    }

    public String getPrev_tx_hash() {
        return prev_tx_hash;
    }

    public void setPrev_tx_hash(String prev_tx_hash) {
        this.prev_tx_hash = prev_tx_hash;
    }

    public int getPrev_tx_n() {
        return prev_tx_n;
    }

    public void setPrev_tx_n(int prev_tx_n) {
        this.prev_tx_n = prev_tx_n;
    }

    public long getPrev_spend_value() {
        return prev_spend_value;
    }

    public void setPrev_spend_value(long prev_spend_value) {
        this.prev_spend_value = prev_spend_value;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getSamourai_fee_address() {
        return samourai_fee_address;
    }

    public void setSamourai_fee_address(String samourai_fee_address) {
        this.samourai_fee_address = samourai_fee_address;
    }

    public long getSamourai_fee_amount() {
        return samourai_fee_amount;
    }

    public void setSamourai_fee_amount(long samourai_fee_amount) {
        this.samourai_fee_amount = samourai_fee_amount;
    }
}
