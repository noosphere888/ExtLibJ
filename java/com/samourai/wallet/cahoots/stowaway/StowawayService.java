package com.samourai.wallet.cahoots.stowaway;

import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.cahoots.*;
import com.samourai.wallet.constants.SamouraiAccountIndex;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.util.FeeUtil;
import com.samourai.wallet.util.RandomUtil;
import org.bitcoinj.core.*;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class StowawayService extends AbstractCahoots2xService<Stowaway, StowawayContext> {
    private static final Logger log = LoggerFactory.getLogger(StowawayService.class);

    public StowawayService(BipFormatSupplier bipFormatSupplier, NetworkParameters params) {
        super(CahootsType.STOWAWAY, bipFormatSupplier, params);
    }

    @Override
    public Stowaway startInitiator(StowawayContext cahootsContext) throws Exception {
        CahootsWallet cahootsWallet = cahootsContext.getCahootsWallet();
        long amount = cahootsContext.getAmount();
        int account = cahootsContext.getAccount();

        byte[] fingerprint = cahootsWallet.getFingerprint();
        Stowaway stowaway0 = doStowaway0(amount, account, fingerprint);
        if (log.isDebugEnabled()) {
            log.debug("# Stowaway INITIATOR => step="+stowaway0.getStep());
        }
        return stowaway0;
    }

    @Override
    public Stowaway startCollaborator(StowawayContext cahootsContext, Stowaway stowaway0) throws Exception {
        if (stowaway0.getSpendAmount() <= 0) {
            // this check used to be the initiator portion, but with the introduction of MultiCahoots, it remains -1 until the Stonewallx2 finishes, so we can get an accurate amount, so the check is here now.
            throw new Exception("Invalid amount");
        }
        Stowaway stowaway1 = doStowaway1(stowaway0, cahootsContext, new ArrayList<>());
        if (log.isDebugEnabled()) {
            log.debug("# Stowaway COUNTERPARTY => step="+stowaway1.getStep());
        }
        return stowaway1;
    }

    @Override
    public Stowaway reply(StowawayContext cahootsContext, Stowaway stowaway) throws Exception {
        int step = stowaway.getStep();
        if (log.isDebugEnabled()) {
            log.debug("# Stowaway "+cahootsContext.getTypeUser()+" <= step="+step);
        }
        Stowaway payload;
        switch (step) {
            case 1:
                List<String> seenTxs = new ArrayList<>();
                for(TransactionInput input : stowaway.getTransaction().getInputs()) {
                    seenTxs.add(input.getOutpoint().getHash().toString());
                }
                payload = doStowaway2(stowaway, cahootsContext, seenTxs);
                break;
            case 2:
                payload = doStep3(stowaway, cahootsContext);
                break;
            case 3:
                payload = doStep4(stowaway, cahootsContext);
                break;
            default:
                throw new Exception("Unrecognized #Cahoots step");
        }
        if (payload == null) {
            throw new Exception("Cannot compose #Cahoots");
        }
        if (log.isDebugEnabled()) {
            log.debug("# Stowaway "+cahootsContext.getTypeUser()+" => step="+payload.getStep());
        }
        return payload;
    }

    //
    // sender
    //
    public Stowaway doStowaway0(long spendAmount, int account, byte[] fingerprint) {
        //
        //
        // step0: B sends spend amount to A,  creates step0
        //
        //
        Stowaway stowaway0 = new Stowaway(spendAmount, params, account, fingerprint);
        return stowaway0;
    }

    //
    // receiver
    //
    public Stowaway doStowaway1(Stowaway stowaway0, StowawayContext cahootsContext, List<String> seenTxs) throws Exception {
        CahootsWallet cahootsWallet = cahootsContext.getCahootsWallet();
        int account = cahootsContext.getAccount();
        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(account);
        int receiveAccount = SamouraiAccountIndex.DEPOSIT; //counterparty should always receive Stowaway to DEPOSIT
        return doStowaway1(stowaway0, cahootsContext, utxos, receiveAccount, seenTxs);
    }

    public Stowaway doStowaway1(Stowaway stowaway0, StowawayContext cahootsContext, List<CahootsUtxo> utxos, int receiveAccount, List<String> seenTxs) throws Exception {
        debug("BEGIN doStowaway1", stowaway0, cahootsContext);

        CahootsWallet cahootsWallet = cahootsContext.getCahootsWallet();
        byte[] fingerprint = cahootsWallet.getFingerprint();
        stowaway0.setFingerprintCollab(fingerprint);

        //
        //
        // step1: A utxos -> B (take largest that cover amount)
        //
        //

        List<CahootsUtxo> selectedUTXO = selectUtxos1(stowaway0, utxos, seenTxs);
        List<TransactionInput> inputsA = cahootsContext.addInputs(selectedUTXO);

        // receive output
        String receiveAddress = cahootsWallet.fetchAddressReceive(receiveAccount, true, BIP_FORMAT.SEGWIT_NATIVE);
        if (log.isDebugEnabled()) {
            log.debug("+output (CounterParty receive) = "+receiveAddress);
        }
        List<TransactionOutput> outputsA = new LinkedList<>();
        TransactionOutput output_A0 = computeTxOutput(receiveAddress, stowaway0.getSpendAmount(), cahootsContext);
        outputsA.add(output_A0);

        stowaway0.setDestination(receiveAddress);
        stowaway0.setCounterpartyAccount(cahootsContext.getAccount());

        Stowaway stowaway1 = stowaway0.copy();
        stowaway1.doStep1(inputsA, outputsA, null);

        debug("END doStowaway1", stowaway1, cahootsContext);
        return stowaway1;
    }

    protected List<CahootsUtxo> selectUtxos1(Cahoots2x stowaway0, List<CahootsUtxo> utxos, List<String> seenTxs) throws Exception {
        // sort in ascending order by value
        Collections.sort(utxos, new UTXO.UTXOComparator());
        if (log.isDebugEnabled()) {
            log.debug("BIP84 utxos:" + utxos.size());
        }

        List<String> _seenTxs = seenTxs;
        List<CahootsUtxo> selectedUTXO = new ArrayList<CahootsUtxo>();
        long totalContributedAmount = 0L;
        List<CahootsUtxo> highUTXO = new ArrayList<CahootsUtxo>();
        for (CahootsUtxo utxo : utxos) {
            if (utxo.getValue() > stowaway0.getSpendAmount() + SamouraiWalletConst.bDust.longValue()) {
                if (!_seenTxs.contains(utxo.getOutpoint().getHash().toString())) {
                    _seenTxs.add(utxo.getOutpoint().getHash().toString());
                    highUTXO.add(utxo);
                }
            }
        }
        if(highUTXO.size() > 0)    {
            // select a single high utxo randomly
            CahootsUtxo utxo = highUTXO.get(RandomUtil.getInstance().nextInt(highUTXO.size()));
            if (log.isDebugEnabled()) {
                log.debug("BIP84 selected random utxo: " + utxo);
            }
            selectedUTXO.add(utxo);
            totalContributedAmount = utxo.getValue();
        }
        if (selectedUTXO.size() == 0) {
            // select multiple utxos
            for (CahootsUtxo utxo : utxos) {
                if (!_seenTxs.contains(utxo.getOutpoint().getHash().toString())) {
                    _seenTxs.add(utxo.getOutpoint().getHash().toString());
                    selectedUTXO.add(utxo);
                    totalContributedAmount += utxo.getValue();
                    if (log.isDebugEnabled()) {
                        log.debug("BIP84 selected utxo: " + utxo);
                    }
                    if (stowaway0.isContributedAmountSufficient(totalContributedAmount)) {
                        break;
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(selectedUTXO.size()+" selected utxos, totalContributedAmount="+totalContributedAmount+", requiredAmount="+stowaway0.computeRequiredAmount());
        }
        if (!stowaway0.isContributedAmountSufficient(totalContributedAmount)) {
            throw new Exception("Cannot compose #Cahoots: insufficient wallet balance");
        }
        return selectedUTXO;
    }

    //
    // sender
    //
    public Stowaway doStowaway2(Stowaway stowaway1, StowawayContext cahootsContext, List<String> seenTxs) throws Exception {
        debug("BEGIN doStowaway2", stowaway1, cahootsContext);

        if (log.isDebugEnabled()) {
            log.debug("sender account (2):" + stowaway1.getAccount());
        }

        Transaction transaction = stowaway1.getTransaction();
        if (log.isDebugEnabled()) {
            log.debug("step2 tx:" + Hex.toHexString(transaction.bitcoinSerialize()));
        }
        int nbIncomingInputs = transaction.getInputs().size();

        CahootsWallet cahootsWallet = cahootsContext.getCahootsWallet();
        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(stowaway1.getAccount());
        // sort in ascending order by value
        Collections.sort(utxos, new UTXO.UTXOComparator());
        Collections.reverse(utxos);

        if (log.isDebugEnabled()) {
            log.debug("BIP84 utxos:" + utxos.size());
        }

        List<String> _seenTxs = seenTxs;
        List<CahootsUtxo> selectedUTXO = new ArrayList<CahootsUtxo>();
        int nbTotalSelectedOutPoints = 0;
        long totalSelectedAmount = 0L;
        List<CahootsUtxo> lowUTXO = new ArrayList<CahootsUtxo>();
        for (CahootsUtxo utxo : utxos) {
            if(utxo.getValue() < stowaway1.getSpendAmount())    {
                if (!_seenTxs.contains(utxo.getOutpoint().getHash().toString())) {
                    _seenTxs.add(utxo.getOutpoint().getHash().toString());
                    lowUTXO.add(utxo);
                }
            }
        }

        long feePerB = cahootsContext.getFeePerB();

        List<List<CahootsUtxo>> listOfLists = new ArrayList<List<CahootsUtxo>>();
        RandomUtil.getInstance().shuffle(lowUTXO);
        listOfLists.add(lowUTXO);
        listOfLists.add(utxos);
        for(List<CahootsUtxo> list : listOfLists)   {

            _seenTxs = seenTxs;
            selectedUTXO.clear();
            totalSelectedAmount = 0L;
            nbTotalSelectedOutPoints = 0;

            for (CahootsUtxo utxo : list) {
                if (!_seenTxs.contains(utxo.getOutpoint().getHash().toString())) {
                    _seenTxs.add(utxo.getOutpoint().getHash().toString());
                    selectedUTXO.add(utxo);
                    totalSelectedAmount += utxo.getValue();
                    if (log.isDebugEnabled()) {
                        log.debug("BIP84 selected utxo: " + utxo);
                    }
                    nbTotalSelectedOutPoints ++;
                }
                if (stowaway1.isContributedAmountSufficient(totalSelectedAmount, estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, feePerB))) {

                    // discard "extra" utxo, if any
                    List<CahootsUtxo> _selectedUTXO = new ArrayList<CahootsUtxo>();
                    Collections.reverse(selectedUTXO);
                    int _nbTotalSelectedOutPoints = 0;
                    long _totalSelectedAmount = 0L;
                    for (CahootsUtxo utxoSel : selectedUTXO) {
                        _selectedUTXO.add(utxoSel);
                        _totalSelectedAmount += utxoSel.getValue();
                        if (log.isDebugEnabled()) {
                            log.debug("BIP84 post selected utxo: " + utxoSel);
                        }
                        _nbTotalSelectedOutPoints ++;
                        if (stowaway1.isContributedAmountSufficient(_totalSelectedAmount, estimatedFee(_nbTotalSelectedOutPoints, nbIncomingInputs, feePerB))) {
                            selectedUTXO.clear();
                            selectedUTXO.addAll(_selectedUTXO);
                            totalSelectedAmount = _totalSelectedAmount;
                            nbTotalSelectedOutPoints = _nbTotalSelectedOutPoints;
                            break;
                        }
                    }

                    break;
                }
            }
            if (stowaway1.isContributedAmountSufficient(totalSelectedAmount, estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, feePerB))) {
                break;
            }
        }

        long fee = estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, feePerB);
        if (log.isDebugEnabled()) {
            log.debug("fee:" + fee);
            log.debug(selectedUTXO.size()+" selected utxos, totalContributedAmount="+totalSelectedAmount+", requiredAmount="+stowaway1.computeRequiredAmount(fee));
        }
        if (!stowaway1.isContributedAmountSufficient(totalSelectedAmount, fee)) {
            throw new Exception("Cannot compose #Cahoots: insufficient wallet balance");
        }

        //
        //
        // step2: B verif, utxos -> A (take smallest that cover amount)
        //
        //

        List<TransactionInput> inputsB = cahootsContext.addInputs(selectedUTXO);

        List<TransactionOutput> outputsB = new LinkedList<>();

        // tx: modify receive output: spendAmount + sum(counterparty inputs)
        long counterpartyContributedAmount = 0L;
        for(Long value : stowaway1.getOutpoints().values())   {
            counterpartyContributedAmount += value;
        }
        long spendAmount = transaction.getOutput(0).getValue().longValue();
        TransactionOutput spendOutput = transaction.getOutput(0);
        spendOutput.setValue(Coin.valueOf(spendAmount + counterpartyContributedAmount));
        outputsB.add(spendOutput);
        stowaway1.getTransaction().clearOutputs(); // replace receive output by the new one

        // keep track of minerFeePaid
        cahootsContext.setMinerFeePaid(fee); // sender pays all minerFee

        // change output
        String changeAddress = cahootsWallet.fetchAddressChange(stowaway1.getAccount(), true, BIP_FORMAT.SEGWIT_NATIVE);
        if (log.isDebugEnabled()) {
            log.debug("+output (sender change) = "+changeAddress);
        }
        TransactionOutput output_B0 = computeTxOutput(changeAddress, (totalSelectedAmount - stowaway1.getSpendAmount()) - fee, cahootsContext);
        outputsB.add(output_B0);

        Stowaway stowaway2 = stowaway1.copy();
        stowaway2.doStep2(inputsB, outputsB);
        stowaway2.setFeeAmount(fee);
        debug("END doStowaway2", stowaway2, cahootsContext);
        return stowaway2;
    }

    //
    // counterparty
    //
    @Override
    public Stowaway doStep3(Stowaway cahoots2, StowawayContext cahootsContext) throws Exception {
        Stowaway cahoots3 = super.doStep3(cahoots2, cahootsContext);

        // keep track of minerFeePaid
        cahootsContext.setMinerFeePaid(0); // counterparty pays no minerFee
        return cahoots3;
    }

    private long estimatedFee(int nbTotalSelectedOutPoints, int nbIncomingInputs, long feePerB) {
        return FeeUtil.getInstance().estimatedFeeSegwit(0, 0, nbTotalSelectedOutPoints + nbIncomingInputs, 2, 0, feePerB);
    }

    @Override
    protected long computeMaxSpendAmount(long minerFee, StowawayContext cahootsContext) throws Exception {
        long maxSpendAmount;
        String prefix = "["+cahootsContext.getCahootsType()+"/"+cahootsContext.getTypeUser()+"] ";
        switch (cahootsContext.getTypeUser()) {
            case SENDER:
                // spends amount + minerFee
                maxSpendAmount = cahootsContext.getAmount()+minerFee;
                if (log.isDebugEnabled()) {
                    log.debug(prefix+"maxSpendAmount = "+maxSpendAmount+": amount="+cahootsContext.getAmount()+" + minerFee="+minerFee);
                }
                break;
            case COUNTERPARTY:
                // receives money (<0)
                maxSpendAmount = 0;
                if (log.isDebugEnabled()) {
                    log.debug(prefix+"maxSpendAmount = 0 (receives money)");
                }
                break;
            default:
                throw new Exception("Unknown typeUser");
        }
        return maxSpendAmount;
    }
}
