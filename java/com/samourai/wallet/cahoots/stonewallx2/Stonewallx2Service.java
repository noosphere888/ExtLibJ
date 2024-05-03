package com.samourai.wallet.cahoots.stonewallx2;

import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.cahoots.*;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.util.FeeUtil;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.wallet.util.RandomUtil;
import com.samourai.wallet.util.TxUtil;
import com.samourai.wallet.xmanagerClient.XManagerClient;
import com.samourai.xmanager.protocol.XManagerService;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Stonewallx2Service extends AbstractCahoots2xService<STONEWALLx2, Stonewallx2Context> {
    private static final Logger log = LoggerFactory.getLogger(Stonewallx2Service.class);

    public Stonewallx2Service(BipFormatSupplier bipFormatSupplier, NetworkParameters params) {
        super(CahootsType.STONEWALLX2, bipFormatSupplier, params);
    }

    @Override
    public void verifyResponse(Stonewallx2Context cahootsContext, STONEWALLx2 cahoots, STONEWALLx2 request) throws Exception {
        super.verifyResponse(cahootsContext, cahoots, request);

        if (request != null) {
            // properties should never change once set
            if (!Objects.equals(cahoots.strCollabChange, request.strCollabChange)) {
                throw new Exception("Invalid altered Cahoots strCollabChange");
            }
            if (!StringUtils.equals(cahoots.paynymDestination, request.paynymDestination)) {
                throw new Exception("Invalid altered Cahoots strPayNymDestination");
            }
        }
    }

    @Override
    public STONEWALLx2 startInitiator(Stonewallx2Context cahootsContext) throws Exception {
        CahootsWallet cahootsWallet = cahootsContext.getCahootsWallet();
        long amount = cahootsContext.getAmount();
        String address = cahootsContext.getAddress();
        if (amount <= 0) {
            throw new Exception("Invalid amount");
        }
        if (StringUtils.isEmpty(address)) {
            throw new Exception("Invalid address");
        }

        String paynymDestination = cahootsContext.getPaynymDestination();
        byte[] fingerprint = cahootsWallet.getFingerprint();
        int account = cahootsContext.getAccount();
        STONEWALLx2 stonewall0 = doSTONEWALLx2_0(amount, address, paynymDestination, account, fingerprint);
        if (log.isDebugEnabled()) {
            log.debug("# STONEWALLx2 INITIATOR => step="+stonewall0.getStep());
        }
        return stonewall0;
    }

    @Override
    public STONEWALLx2 startCollaborator(Stonewallx2Context cahootsContext, STONEWALLx2 stonewall0) throws Exception {
        STONEWALLx2 stonewall1 = doSTONEWALLx2_1(stonewall0, cahootsContext);
        if (log.isDebugEnabled()) {
            log.debug("# STONEWALLx2 COUNTERPARTY => step="+stonewall1.getStep());
        }
        return stonewall1;
    }

    @Override
    public STONEWALLx2 reply(Stonewallx2Context cahootsContext, STONEWALLx2 stonewall) throws Exception {
        int step = stonewall.getStep();
        if (log.isDebugEnabled()) {
            log.debug("# STONEWALLx2 "+cahootsContext.getTypeUser()+" <= step="+step);
        }
        STONEWALLx2 payload;
        switch (step) {
            case 1:
                payload = doSTONEWALLx2_2(stonewall, cahootsContext);
                break;
            case 2:
                payload = doStep3(stonewall, cahootsContext);
                break;
            case 3:
                payload = doStep4(stonewall, cahootsContext);
                break;
            default:
                throw new Exception("Unrecognized #Cahoots step");
        }
        if (payload == null) {
            throw new Exception("Cannot compose #Cahoots");
        }
        if (log.isDebugEnabled()) {
            log.debug("# STONEWALLx2 "+cahootsContext.getTypeUser()+" => step="+payload.getStep());
        }
        return payload;
    }

    //
    // sender
    //
    private STONEWALLx2 doSTONEWALLx2_0(long spendAmount, String address, String paynymDestination, int account, byte[] fingerprint) {
        //
        //
        // step0: B sends spend amount to A,  creates step0
        //
        //
        STONEWALLx2 stonewall0 = new STONEWALLx2(spendAmount, address, paynymDestination, params, account, fingerprint);
        return stonewall0;
    }

    protected List<CahootsUtxo> selectUtxos1(Cahoots2x stonewall0, List<CahootsUtxo> utxos, List<String> seenTxs) throws Exception {
        RandomUtil.getInstance().shuffle(utxos);

        if (log.isDebugEnabled()) {
            log.debug("BIP84 utxos:" + utxos.size());
        }

        List<CahootsUtxo> selectedUTXO = new ArrayList<CahootsUtxo>();
        long totalContributedAmount = 0L;
        for (int step = 0; step < 3; step++) {

            if (stonewall0.getCounterpartyAccount() == 0) {
                step = 2;
            }

            selectedUTXO = new ArrayList<CahootsUtxo>();
            totalContributedAmount = 0L;
            for (CahootsUtxo utxo : utxos) {

                switch (step) {
                    case 0:
                        if (utxo.getPath() != null && utxo.getPath().length() > 3 && utxo.getPath().charAt(2) != '0') {
                            continue;
                        }
                        break;
                    case 1:
                        if (utxo.getPath() != null && utxo.getPath().length() > 3 && utxo.getPath().charAt(2) != '1') {
                            continue;
                        }
                        break;
                    default:
                        break;
                }

                MyTransactionOutPoint outpoint = utxo.getOutpoint();
                if (!seenTxs.contains(outpoint.getHash().toString())) {
                    seenTxs.add(outpoint.getHash().toString());

                    selectedUTXO.add(utxo);
                    totalContributedAmount += utxo.getValue();
                    if (log.isDebugEnabled()) {
                        log.debug("BIP84 selected utxo: " + utxo);
                    }
                }

                if (stonewall0.isContributedAmountSufficient(totalContributedAmount)) {
                    break;
                }
            }
            if (stonewall0.isContributedAmountSufficient(totalContributedAmount)) {
                break;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(selectedUTXO.size()+" selected utxos, totalContributedAmount="+totalContributedAmount+", requiredAmount="+stonewall0.computeRequiredAmount());
        }
        if (!stonewall0.isContributedAmountSufficient(totalContributedAmount)) {
            throw new Exception("Cannot compose #Cahoots: insufficient wallet balance");
        }
        return selectedUTXO;
    }

    private String getContributorMixAddress(CahootsWallet cahootsWallet, STONEWALLx2 stonewall0, boolean increment, BipFormat bipFormat) throws Exception {
        String receiveAddress = cahootsWallet.fetchAddressChange(stonewall0.getCounterpartyAccount(), increment, bipFormat);
        if (receiveAddress.equalsIgnoreCase(stonewall0.getDestination())) {
            receiveAddress = cahootsWallet.fetchAddressChange(stonewall0.getCounterpartyAccount(), increment, bipFormat);
        }
        return receiveAddress;
    }

    public STONEWALLx2 doSTONEWALLx2_1_Multi(STONEWALLx2 stonewall0, Stonewallx2Context cahootsContext, List<String> seenTxs, XManagerClient xManagerClient, long threshold) throws Exception {
        Coin thresholdAsCoin = Coin.valueOf(threshold);
        int account = cahootsContext.getAccount();
        CahootsWallet cahootsWallet = cahootsContext.getCahootsWallet();
        Coin balance = CahootsUtxo.sumValue(cahootsWallet.getUtxosWpkhByAccount(account));
        boolean isBech32Destination = FormatsUtilGeneric.getInstance().isValidBech32(stonewall0.getDestination());
        if(balance.isGreaterThan(thresholdAsCoin) && isBech32Destination && xManagerClient != null) {
            // mix to external
            String xmAddress = xManagerClient.getAddressOrDefault(XManagerService.STONEWALL, 3);
            if(!xmAddress.equals(XManagerService.STONEWALL.getDefaultAddress(params == TestNet3Params.get()))) {
                log.info("EXTRACTING FUNDS TO EXTERNAL WALLET > " + xmAddress);
                TransactionOutput mixOutput = computeTxOutput(xmAddress, stonewall0.getSpendAmount(), cahootsContext);
                if (log.isDebugEnabled()) {
                    log.debug("+output (CounterParty Mix) = " + xmAddress);
                }
                return doSTONEWALLx2_1(mixOutput, stonewall0, cahootsContext, seenTxs);
            }
        }

        // regular STONEWALLx2
        return doSTONEWALLx2_1(stonewall0, cahootsContext);
    }

    private STONEWALLx2 doSTONEWALLx2_1(STONEWALLx2 stonewall0, Stonewallx2Context cahootsContext) throws Exception {
        stonewall0.setCounterpartyAccount(cahootsContext.getAccount());
        CahootsWallet cahootsWallet = cahootsContext.getCahootsWallet();
        byte[] fingerprint = cahootsWallet.getFingerprint();
        stonewall0.setFingerprintCollab(fingerprint);

        List<String> seenTxs = new ArrayList<>();
        // contributor mix output: like-typed with destination
        BipFormat bipFormatDestination = getBipFormatSupplier().findByAddress(stonewall0.getDestination(), params);
        log.debug("BIP FORMAT:: " + bipFormatDestination.getId());
        String receiveAddress = getContributorMixAddress(cahootsWallet, stonewall0, true, bipFormatDestination);
        TransactionOutput mixOutput = computeTxOutput(receiveAddress, stonewall0.getSpendAmount(), cahootsContext);
        if (log.isDebugEnabled()) {
            log.debug("+output (CounterParty Mix) = " + receiveAddress);
        }
        return doSTONEWALLx2_1(mixOutput, stonewall0, cahootsContext, seenTxs);
    }

    private STONEWALLx2 doSTONEWALLx2_1(TransactionOutput mixOutput, STONEWALLx2 stonewall0, Stonewallx2Context cahootsContext, List<String> seenTxs) throws Exception {
        debug("BEGIN doSTONEWALLx2_1", stonewall0, cahootsContext);

        int account = cahootsContext.getAccount();
        CahootsWallet cahootsWallet = cahootsContext.getCahootsWallet();

        //
        //
        // step1: A utxos -> B (take largest that cover amount)
        //
        //

        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(account);
        List<CahootsUtxo> selectedUTXO = selectUtxos1(stonewall0, utxos, seenTxs);
        List<TransactionInput> inputsA = cahootsContext.addInputs(selectedUTXO);
        long contributedAmount = CahootsUtxo.sumValue(selectedUTXO).getValue();

        List<TransactionOutput> outputsA = new LinkedList<>();
        outputsA.add(mixOutput);

        // contributor change output
        String changeAddress = cahootsWallet.fetchAddressChange(stonewall0.getCounterpartyAccount(), true, BIP_FORMAT.SEGWIT_NATIVE);
        if (log.isDebugEnabled()) {
            log.debug("+output (CounterParty change) = " + changeAddress);
        }
        TransactionOutput output_A1 = computeTxOutput(changeAddress, contributedAmount - stonewall0.getSpendAmount(), cahootsContext);
        outputsA.add(output_A1);
        stonewall0.setCollabChange(changeAddress);

        STONEWALLx2 stonewall1 = stonewall0.copy();
        stonewall1.doStep1(inputsA, outputsA, cahootsWallet.getChainSupplier());

        debug("END doSTONEWALLx2_1", stonewall1, cahootsContext);
        return stonewall1;
    }

    //
    // sender
    //
    private STONEWALLx2 doSTONEWALLx2_2(STONEWALLx2 stonewall1, Stonewallx2Context cahootsContext) throws Exception {
        return doSTONEWALLx2_2(stonewall1, cahootsContext, new ArrayList<>());
    }
    public STONEWALLx2 doSTONEWALLx2_2(STONEWALLx2 stonewall1, Stonewallx2Context cahootsContext, List<String> seenTxs) throws Exception {
        debug("BEGIN doSTONEWALLx2_2", stonewall1, cahootsContext);

        Transaction transaction = stonewall1.getTransaction();
        if (log.isDebugEnabled()) {
            log.debug("step2 tx:" + Hex.toHexString(transaction.bitcoinSerialize()));
            log.debug("step2 tx:" + transaction);
        }
        int nbIncomingInputs = transaction.getInputs().size();

        CahootsWallet cahootsWallet = cahootsContext.getCahootsWallet();
        List<CahootsUtxo> utxos = cahootsWallet.getUtxosWpkhByAccount(stonewall1.getAccount());
        RandomUtil.getInstance().shuffle(utxos);

        if (log.isDebugEnabled()) {
            log.debug("BIP84 utxos:" + utxos.size());
        }

        for (TransactionInput input : transaction.getInputs()) {
            if (!seenTxs.contains(input.getOutpoint().getHash().toString())) {
                seenTxs.add(input.getOutpoint().getHash().toString());
            }
        }

        long feePerB = cahootsContext.getFeePerB();
        String destination = stonewall1.getDestination();

        List<CahootsUtxo> selectedUTXO = new ArrayList<CahootsUtxo>();
        long totalSelectedAmount = 0L;
        int nbTotalSelectedOutPoints = 0;
        for (int step = 0; step < 3; step++) {

            if (stonewall1.getCounterpartyAccount() == 0) {
                step = 2;
            }

            List<String> _seenTxs = seenTxs;
            selectedUTXO = new ArrayList<CahootsUtxo>();
            totalSelectedAmount = 0;
            nbTotalSelectedOutPoints = 0;
            for (CahootsUtxo utxo : utxos) {

                switch (step) {
                    case 0:
                        if (utxo.getPath() != null && utxo.getPath().length() > 3 && utxo.getPath().charAt(2) != '0') {
                            continue;
                        }
                        break;
                    case 1:
                        if (utxo.getPath() != null && utxo.getPath().length() > 3 && utxo.getPath().charAt(2) != '1') {
                            continue;
                        }
                        break;
                    default:
                        break;
                }

                if (!_seenTxs.contains(utxo.getOutpoint().getHash().toString())) {
                    _seenTxs.add(utxo.getOutpoint().getHash().toString());

                    selectedUTXO.add(utxo);
                    totalSelectedAmount += utxo.getValue();
                    nbTotalSelectedOutPoints ++;
                    if (log.isDebugEnabled()) {
                        log.debug("BIP84 selected utxo: " + utxo);
                    }
                }

                if (stonewall1.isContributedAmountSufficient(totalSelectedAmount, estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, destination, feePerB))) {
                    break;
                }
            }
            if (stonewall1.isContributedAmountSufficient(totalSelectedAmount, estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, destination, feePerB))) {
                break;
            }
        }
        long fee = estimatedFee(nbTotalSelectedOutPoints, nbIncomingInputs, destination, feePerB);
        if (log.isDebugEnabled()) {
            log.debug("fee:" + fee);
            log.debug(selectedUTXO.size()+" selected utxos, totalContributedAmount="+totalSelectedAmount+", requiredAmount="+stonewall1.computeRequiredAmount(fee));
        }
        if (!stonewall1.isContributedAmountSufficient(totalSelectedAmount, fee)) {
            throw new Exception("Cannot compose #Cahoots: insufficient wallet balance");
        }

        if (fee % 2L != 0) {
            fee++;
        }
        if (log.isDebugEnabled()) {
            log.debug("fee pair:" + fee);
        }
        stonewall1.setFeeAmount(fee);

        if (log.isDebugEnabled()) {
            log.debug("destination:" + stonewall1.getDestination());
        }

        if (transaction.getOutputs() == null || transaction.getOutputs().size() != 2) {
            log.error("outputs: " + transaction.getOutputs().size());
            log.error("tx:" + transaction.toString());
            throw new Exception("Cannot compose #Cahoots: invalid tx outputs count");
        }

        // keep track of minerFeePaid
        long minerFeePaid = fee / 2L;
        cahootsContext.setMinerFeePaid(minerFeePaid); // sender & counterparty pay half minerFee

        // update counterparty change output to pay half of fees
        TransactionOutput counterpartyChangeOutput = TxUtil.getInstance().findOutputByAddress(transaction, stonewall1.getCollabChange(), getBipFormatSupplier());
        if (counterpartyChangeOutput == null) {
            throw new Exception("Cannot compose #Cahoots: counterpartyChangeOutput not found");
        }
        Coin _value = Coin.valueOf(counterpartyChangeOutput.getValue().longValue() - minerFeePaid);
        if (log.isDebugEnabled()) {
            log.debug("output value post fee:" + _value);
        }
        counterpartyChangeOutput.setValue(_value);
        stonewall1.getPSBT().setTransaction(transaction);

        //
        //
        // step2: B verif, utxos -> A (take smallest that cover amount)
        //
        //

        List<TransactionInput> inputsB = cahootsContext.addInputs(selectedUTXO);

        // spender change output
        List<TransactionOutput> outputsB = new LinkedList<>();
        String changeAddress = cahootsWallet.fetchAddressChange(stonewall1.getAccount(), true, BIP_FORMAT.SEGWIT_NATIVE);
        if (log.isDebugEnabled()) {
            log.debug("+output (Spender change) = " + changeAddress);
        }
        TransactionOutput output_B0 = computeTxOutput(changeAddress, (totalSelectedAmount - stonewall1.getSpendAmount()) - minerFeePaid, cahootsContext);
        outputsB.add(output_B0);

        // destination output
        TransactionOutput destOutput = getBipFormatSupplier().getTransactionOutput(stonewall1.getDestination(), stonewall1.getSpendAmount(), params);
        transaction.addOutput(destOutput);

        STONEWALLx2 stonewall2 = stonewall1.copy();
        if(stonewall1.getTransaction().getLockTime() == 0) {
            throw new Exception("Locktime error: Please update."); // safety check
        } else {
            stonewall2.doStep2(inputsB, outputsB);
        }

        debug("END doSTONEWALLx2_2",stonewall2, cahootsContext);
        return stonewall2;
    }

    //
    // counterparty
    //
    @Override
    public STONEWALLx2 doStep3(STONEWALLx2 cahoots2, Stonewallx2Context cahootsContext) throws Exception {
        STONEWALLx2 cahoots3 = super.doStep3(cahoots2, cahootsContext);

        // keep track of minerFeePaid
        long minerFeePaid = cahoots3.getFeeAmount() / 2L;
        cahootsContext.setMinerFeePaid(minerFeePaid); // sender & counterparty pay half minerFee
        return cahoots3;
    }

    private long estimatedFee(int nbTotalSelectedOutPoints, int nbIncomingInputs, String destination, long feePerB) {
        int outputsNonP2WSH_P2TR = 4;
        int outputsP2WSH_P2TR = 0;
        if (FormatsUtilGeneric.getInstance().isValidP2WSH_P2TR(destination)) {
            // destination is P2TR or P2WSH, contributor mix output is SEGWIT_NATIVE (no like-typed output for P2TR)
            outputsNonP2WSH_P2TR--;
            outputsP2WSH_P2TR++;
        }
        return FeeUtil.getInstance().estimatedFeeSegwit(0, 0, nbTotalSelectedOutPoints + nbIncomingInputs, outputsNonP2WSH_P2TR, outputsP2WSH_P2TR, 0, feePerB);
    }

    @Override
    protected long computeMaxSpendAmount(long minerFee, Stonewallx2Context cahootsContext) throws Exception {
        // shares minerFee
        long maxSpendAmount;
        long sharedMinerFee = minerFee / 2;
        String prefix = "["+cahootsContext.getCahootsType()+"/"+cahootsContext.getTypeUser()+"] ";
        switch (cahootsContext.getTypeUser()) {
            case SENDER:
                // spends amount + sharedMinerFee
                maxSpendAmount = cahootsContext.getAmount()+sharedMinerFee;
                if (log.isDebugEnabled()) {
                    log.debug(prefix+"maxSpendAmount = "+maxSpendAmount+": amount="+cahootsContext.getAmount()+" + sharedMinerFee="+sharedMinerFee);
                }
                break;
            case COUNTERPARTY:
                // shares minerFee
                maxSpendAmount = sharedMinerFee;
                if (log.isDebugEnabled()) {
                    log.debug(prefix+"maxSpendAmount = "+maxSpendAmount+": sharedMinerFee="+sharedMinerFee);
                }
                break;
            default:
                throw new Exception("Unknown typeUser");
        }
        return maxSpendAmount;
    }

    public static final Coin THRESHOLD = Coin.valueOf(200000000);
}
