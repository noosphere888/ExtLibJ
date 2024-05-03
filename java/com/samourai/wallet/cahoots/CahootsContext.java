package com.samourai.wallet.cahoots;

import com.samourai.wallet.cahoots.stonewallx2.Stonewallx2Context;
import com.samourai.wallet.cahoots.stowaway.StowawayContext;
import com.samourai.wallet.sorobanClient.SorobanContext;
import com.samourai.wallet.cahoots.multi.MultiCahootsContext;
import org.bitcoinj.core.TransactionInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class CahootsContext implements SorobanContext {
    private static final Logger log = LoggerFactory.getLogger(CahootsContext.class);

    private CahootsWallet cahootsWallet;
    private CahootsTypeUser typeUser;
    private CahootsType cahootsType;
    private int account;
    private Long feePerB; // only set for initiator
    private Long amount; // only set for initiator
    private String address; // only set for initiator
    private Set<String> outputAddresses; // keep track of our own change addresses outputs
    private List<CahootsUtxo> inputs; // keep track of our own inputs
    private long samouraiFee; // keep track of samourai fee
    private long minerFeePaid; // keep track of paid minerFee (lower or equals cahoots.fee)

    protected CahootsContext(CahootsWallet cahootsWallet, CahootsTypeUser typeUser, CahootsType cahootsType, int account, Long feePerB, Long amount, String address) {
        this.cahootsWallet = cahootsWallet;
        this.typeUser = typeUser;
        this.cahootsType = cahootsType;
        this.account = account;
        this.feePerB = feePerB;
        this.amount = amount;
        this.address = address;
        this.outputAddresses = new LinkedHashSet<>();
        this.inputs = new LinkedList<>();
        this.samouraiFee = 0;
        this.minerFeePaid = 0;
    }

    public static CahootsContext newCounterparty(CahootsWallet cahootsWallet, CahootsType cahootsType, int account) throws Exception {
        CahootsContext cahootsContext = null;
        switch (cahootsType) {
            case MULTI:
                // MULTI counterparty is reserved to SAAS backend via newCounterpartyMultiCahoots()
                throw new RuntimeException("MULTI counterparty is reserved to SAAS backend");

            case STONEWALLX2:
                cahootsContext = Stonewallx2Context.newCounterparty(cahootsWallet, account);
                break;

            case STOWAWAY:
                cahootsContext = StowawayContext.newCounterparty(cahootsWallet, account);
                break;

            default:
                throw new Exception("Unknown Cahoots type");
        }
        return cahootsContext;
    }

    public static CahootsContext newInitiator(CahootsWallet cahootsWallet, CahootsType cahootsType, int account, long feePerB, long amount, String address, String paynymDestination) throws Exception {
        switch (cahootsType) {
            case STONEWALLX2:
                return Stonewallx2Context.newInitiator(
                                cahootsWallet, account, feePerB, amount, address, paynymDestination);

            case STOWAWAY:
                return StowawayContext.newInitiator(cahootsWallet, account, feePerB, amount);

            case MULTI:
                return MultiCahootsContext.newInitiator(cahootsWallet, account, feePerB, amount, address, paynymDestination);

            default:
                throw new Exception("Unknown CahootsType");
        }
    }

    public CahootsWallet getCahootsWallet() {
        return cahootsWallet;
    }

    public CahootsTypeUser getTypeUser() {
        return typeUser;
    }

    public CahootsType getCahootsType() {
        return cahootsType;
    }

    public int getAccount() {
        return account;
    }

    public Long getFeePerB() {
        return feePerB;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public Set<String> getOutputAddresses() {
        return outputAddresses;
    }

    public void addOutputAddress(String address) {
        outputAddresses.add(address);
    }

    public void addInput(CahootsUtxo cahootsUtxo) {
        inputs.add(cahootsUtxo);
    }

    public List<TransactionInput> addInputs(List<CahootsUtxo> inputs) {
        List<TransactionInput> inputsA = new LinkedList<>();

        for (CahootsUtxo utxo : inputs) {
            TransactionInput input = utxo.getOutpoint().computeSpendInput();
            inputsA.add(input);
            addInput(utxo);
        }
        return inputsA;
    }

    public List<CahootsUtxo> getInputs() {
        return inputs;
    }

    public long getSamouraiFee() {
        return samouraiFee;
    }

    public void setSamouraiFee(long samouraiFee) {
        this.samouraiFee = samouraiFee;
    }

    public long getMinerFeePaid() {
        return minerFeePaid;
    }

    public void setMinerFeePaid(long minerFeePaid) {
        this.minerFeePaid = minerFeePaid;
    }
}
