package com.samourai.wallet.ricochet;

import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.hd.BipAddress;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.SendFactoryGeneric;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.send.provider.UtxoProvider;
import com.samourai.wallet.util.FeeUtil;
import com.samourai.wallet.constants.SamouraiAccountIndex;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.*;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

public class RicochetUtilGeneric {
    private static final Logger log = LoggerFactory.getLogger(RicochetUtilGeneric.class);

    public final static BigInteger samouraiFeeAmount = BigInteger.valueOf(100000L);

    public final static int defaultNbHops = 4;

    private static RicochetUtilGeneric instance = null;
    private static SendFactoryGeneric sendFactory = SendFactoryGeneric.getInstance();

    protected RicochetUtilGeneric() { ; }

    public static RicochetUtilGeneric getInstance() {
        if(instance == null) {
            instance = new RicochetUtilGeneric();
        }
        return instance;
    }

    public Ricochet ricochet(long spendAmount, String strDestination, RicochetConfig config) throws Exception {
        Ricochet ricochet = new Ricochet();

        long nTimeLock = 0L;
        boolean useTimeLock = config.isUseTimeLock();
        long latestBlock = config.getLatestBlock();
        if (useTimeLock && latestBlock > 0L) {
            nTimeLock = latestBlock;
            ricochet.setnTimeLock(latestBlock);
        }

        int nbHops = config.getNbHops();
        BigInteger biSamouraiFee = BigInteger.valueOf(samouraiFeeAmount.longValue() * ((nbHops - defaultNbHops) + 1));    // default 4 hops min. for base fee, each additional hop 0.001
        boolean samouraiFeeViaBIP47 = config.isSamouraiFeeViaBIP47();

        int spendAccount = SamouraiAccountIndex.find(config.getSpendAccount());
        ricochet.setSpend_account(spendAccount);
        ricochet.setSpend_amount(spendAmount);
        ricochet.setSamourai_fee(biSamouraiFee.longValue());
        ricochet.setSamourai_fee_via_bip47(samouraiFeeViaBIP47);
        ricochet.setFeeKB(FeeUtil.getInstance().toFeePerKB(config.getFeePerB()).longValue());
        ricochet.setDestination(strDestination);

        int nbOutputs = (samouraiFeeViaBIP47 ? 2 : 1);
        long minerFeePerHop = FeeUtil.getInstance().estimatedFeeSegwit(1, 0, 0, nbOutputs, 0, config.getFeePerB());
        ricochet.setFee_per_hop(minerFeePerHop);

        BigInteger biMinerFeePerHop = BigInteger.valueOf(minerFeePerHop);
        BigInteger biSpend = BigInteger.valueOf(spendAmount);
        BigInteger biHop0SpendAmount = biSpend.add(biSamouraiFee).add(biMinerFeePerHop.multiply(BigInteger.valueOf(nbHops)));

        // find hop0 utxos
        Pair<List<UTXO>, Long> pair = getHop0UTXO(biHop0SpendAmount.longValue(), config);
        List<UTXO> utxos = pair.getLeft();
        long hop0MinerFee = pair.getRight();

        // hop0 'leaves' wallet, change returned to wallet
        Collection<MyTransactionOutPoint> unspent = new ArrayList<MyTransactionOutPoint>();
        long totalValueSelected = UTXO.sumValue(utxos);
        for (UTXO u : utxos) {
            unspent.addAll(u.getOutpoints());
        }
        long changeAmount = totalValueSelected - (biHop0SpendAmount.longValue() + hop0MinerFee);
        ricochet.setChange_amount(changeAmount);
        ricochet.setSpendFrom(unspent);

        BipWallet bipWalletRicochet = config.getBipWalletRicochet();
        BipAddress bipDestinationAddress = bipWalletRicochet.getNextAddressReceive(true);
        String destinationAddress = bipDestinationAddress.getAddressString();
        Transaction txHop0 = getHop0Tx(unspent, biHop0SpendAmount.longValue(), changeAmount, destinationAddress, nTimeLock, config);
        if (log.isDebugEnabled()) {
            log.debug("+hop0: "+txHop0.toString());
        }

        UtxoProvider utxoProvider = config.getUtxoProvider();
        BipFormatSupplier bipFormatSupplier = utxoProvider.getBipFormatSupplier();
        ECKey prevKey = bipDestinationAddress.getHdAddress().getECKey();
        TransactionOutput prevDestinationOutput = null;
        for (TransactionOutput txOutput : txHop0.getOutputs()) {
            String address = bipFormatSupplier.getToAddress(txOutput);
            if (address.equals(destinationAddress)) {
                prevDestinationOutput = txOutput;
                break;
            }
        }

        BigInteger totalMinerFee = BigInteger.valueOf(txHop0.getFee().value);
        BigInteger totalVSize = BigInteger.valueOf(txHop0.getVirtualTransactionSize());
        BigInteger totalWeight = BigInteger.valueOf(txHop0.getWeight());

        RicochetHop hop0 = new RicochetHop();
        hop0.setSeq(0);
        hop0.setSpend_amount(biHop0SpendAmount.longValue());
        hop0.setFee(hop0MinerFee);
        hop0.setIndex(bipDestinationAddress.getHdAddress().getAddressIndex());
        hop0.setDestination(destinationAddress);
        hop0.setTx(new String(Hex.encode(txHop0.bitcoinSerialize())));
        hop0.setHash(txHop0.getHashAsString());
        if (useTimeLock) {
            hop0.setnTimeLock(nTimeLock);
        }
        ricochet.addHop(hop0);

        List<Pair<String, Long>> samouraiFeesBip47 = null;
        if (samouraiFeeViaBIP47) {
            samouraiFeesBip47 = computeSamouraiFeesBip47(config);
        }

        String prevTxHash = txHop0.getHash().toString();

        BigInteger remainingSamouraiFee = BigInteger.ZERO;
        long prevSpendValue = biHop0SpendAmount.longValue();
        if (!samouraiFeeViaBIP47) {
            prevSpendValue -= biSamouraiFee.longValue();
        } else {
            remainingSamouraiFee = samouraiFeeAmount;
        }
        int _hop = 0;
        for (int i = (nbHops - 1); i >= 0; i--) {
            _hop++;
            RicochetHop hop = new RicochetHop();
            BigInteger hopx = biSpend.add(biMinerFeePerHop.multiply(BigInteger.valueOf(i)));
            if (samouraiFeeViaBIP47) {
                Pair<String, Long> feePair = samouraiFeesBip47.get(_hop - 1);
                remainingSamouraiFee = remainingSamouraiFee.subtract(BigInteger.valueOf(feePair.getRight()));
                hopx = hopx.add(remainingSamouraiFee);
                hop.setSamourai_fee_address(feePair.getLeft());
                hop.setSamourai_fee_amount(feePair.getRight());
            }

            if (useTimeLock && latestBlock > 0L) {
                nTimeLock = latestBlock + _hop;
            }
            //                Log.d("RicochetUtilGeneric", "doing hop:" + _hop);

            ECKey nextKey = null;
            String nextDestinationAddress;
            if (_hop < nbHops) {
                BipAddress bipNextDestination = bipWalletRicochet.getNextAddressReceive(true);
                nextDestinationAddress = bipDestinationAddress.getAddressString();
                nextKey = bipDestinationAddress.getHdAddress().getECKey();
                hop.setIndex(bipNextDestination.getHdAddress().getAddressIndex());
            } else {
                nextDestinationAddress = strDestination;
            }
            Pair<String, Long> samouraiFeePairBip47 = (samouraiFeeViaBIP47 && ((_hop - 1) < nbHops) ? samouraiFeesBip47.get(_hop - 1) : null);

            MyTransactionOutPoint prevOutpoint = new MyTransactionOutPoint(prevDestinationOutput, destinationAddress, 0);
            Transaction txHop = getHopTx(prevOutpoint, prevKey, hopx.longValue(), nextDestinationAddress, samouraiFeePairBip47, nTimeLock, config);
            if (log.isDebugEnabled()) {
                log.debug("+hop"+_hop+": "+txHop.toString());
            }

            hop.setSeq(nbHops - i);
            hop.setSpend_amount(hopx.longValue());
            hop.setFee(minerFeePerHop);
            hop.setPrev_tx_hash(prevTxHash);
            hop.setPrev_tx_n(prevDestinationOutput.getIndex());
            hop.setPrev_spend_value(prevSpendValue);
            hop.setScript(Hex.toHexString(prevDestinationOutput.getScriptPubKey().getProgram()));
            hop.setTx(new String(Hex.encode(txHop.bitcoinSerialize())));
            hop.setHash(txHop.getHashAsString());
            if (useTimeLock) {
                hop.setnTimeLock(nTimeLock);
            }
            hop.setDestination(nextDestinationAddress);
            ricochet.addHop(hop);

            prevTxHash = txHop.getHash().toString();
            prevDestinationOutput = txHop.getOutput(0);
            prevKey = nextKey;
            prevSpendValue = hopx.longValue();
            totalMinerFee = totalMinerFee.add(BigInteger.valueOf(txHop.getFee().value));
            totalVSize = totalVSize.add(BigInteger.valueOf(txHop.getVirtualTransactionSize()));
            totalWeight = totalWeight.add(BigInteger.valueOf(txHop.getWeight()));
        }

        BigInteger totalAmount = biHop0SpendAmount.add(BigInteger.valueOf(hop0MinerFee));
        ricochet.setTotal_spend(totalAmount.longValue());
        ricochet.setTotal_miner_fee(totalMinerFee.longValue());
        ricochet.setTotal_vSize(totalVSize.longValue());
        ricochet.setTotal_weight(totalWeight.longValue());

        if (log.isDebugEnabled()) {
            log.debug("Ricochet: " + ricochet.toJsonString());
        }

        verify(ricochet);
        return ricochet;
    }

    protected void verify(Ricochet ricochet) {
        // TODO zl
    }

    protected List<Pair<String, Long>> computeSamouraiFeesBip47(RicochetConfig config) {
        List<Pair<String, Long>> samouraiFees = new ArrayList<Pair<String, Long>>();

        long baseVal = samouraiFeeAmount.longValue() / 4L;
        long totalVal = 0L;

        for (int i = 0; i < config.getNbHops(); i++) {
            long feeVal;
            if (i == 3) {
                feeVal = samouraiFeeAmount.longValue() - totalVal;
            } else {
                int val = computeSamouraiFeesBip47RandomValue();
                feeVal = baseVal + val;
                totalVal += feeVal;
            }

            //
            // put address here
            //
            String strAddress = config.getBip47NextFeeAddress();
            samouraiFees.add(Pair.of(strAddress, feeVal));
        }
        return samouraiFees;
    }

    protected int computeSamouraiFeesBip47RandomValue() {
        SecureRandom random = new SecureRandom();
        int val = random.nextInt((int)(samouraiFeeAmount.longValue() / 8L));
        int sign = random.nextInt(1);
        if (sign == 0) {
            val *= -1L;
        }
        return val;
    }

    protected Pair<List<UTXO>, Long> getHop0UTXO(long hop0SpendAmount, RicochetConfig config) throws Exception {
        List<UTXO> utxos = new LinkedList<>(config.getUtxoProvider().getUtxos(config.getSpendAccount()));

        final List<UTXO> selectedUTXO = new ArrayList<UTXO>();
        long totalValueSelected = 0L;
        long totalSpendAmount = 0L;
        int selected = 0;

        // sort in ascending order by value
        Collections.sort(utxos, new UTXO.UTXOComparator());
        Collections.reverse(utxos);

        long feePerB = config.getFeePerB();
        long minerFee = 0;
        for (UTXO u : utxos) {
            selectedUTXO.add(u);
            totalValueSelected += u.getValue();
            selected += u.getOutpoints().size();
//            Log.d("RicochetUtilGeneric", "selected:" + u.getValue());

            minerFee = FeeUtil.getInstance().estimatedFeeSegwit(selected, 0, 0, 3, 0, feePerB);
            totalSpendAmount = hop0SpendAmount + SamouraiWalletConst.bDust.longValue() + minerFee;

//            Log.d("RicochetUtilGeneric", "totalSpendAmount:" + totalSpendAmount);
//            Log.d("RicochetUtilGeneric", "totalValueSelected:" + totalValueSelected);
            if (totalValueSelected >= totalSpendAmount) {
//                Log.d("RicochetUtilGeneric", "breaking");
                break;
            }
        }

        if (selectedUTXO.size() < 1 || totalValueSelected < totalSpendAmount) {
            log.warn("Insufficient balance: totalValueSelected="+totalValueSelected+", totalSpendAmount="+totalSpendAmount);
            throw new Exception("Insufficient balance");
        }
        return Pair.of(selectedUTXO, minerFee);
    }

    protected Transaction getHop0Tx(Collection<MyTransactionOutPoint> unspent, long spendAmount, long changeAmount, String destination, long nTimeLock, RicochetConfig config) throws Exception {

//        Log.d("RicochetUtilGeneric", "spendAmount:" + spendAmount);
//        Log.d("RicochetUtilGeneric", "fee:" + fee);
//        Log.d("RicochetUtilGeneric", "totalValueSelected:" + totalValueSelected);

//        Log.d("RicochetUtilGeneric", "changeAmount:" + changeAmount);
        HashMap<String, BigInteger> receivers = new HashMap<String, BigInteger>();

        if (changeAmount > 0L) {
            String change_address = config.getBipWalletChange().getNextAddressChange(true).getAddressString();
            receivers.put(change_address, BigInteger.valueOf(changeAmount));
        }

        if (config.isSamouraiFeeViaBIP47()) {
            // Samourai fee paid in the hops
            receivers.put(destination, BigInteger.valueOf(spendAmount));
        } else {
            receivers.put(config.getSamouraiFeeAddress(), samouraiFeeAmount);
            receivers.put(destination, BigInteger.valueOf(spendAmount - samouraiFeeAmount.longValue()));
        }

        UtxoProvider utxoProvider = config.getUtxoProvider();
        BipFormatSupplier bipFormatSupplier = utxoProvider.getBipFormatSupplier();
        NetworkParameters params = config.getBipWalletChange().getParams();
        Transaction tx = sendFactory.makeTransaction(unspent, receivers, bipFormatSupplier, config.isRbfOptIn(), params, config.getLatestBlock());
        if (nTimeLock > 0L) {
            tx.setLockTime(nTimeLock);
        }
        tx = sendFactory.signTransaction(tx, utxoProvider);
        return tx;
    }

    protected Transaction getHopTx(MyTransactionOutPoint prevOutPoint, ECKey prevKey, long spendAmount, String destination, Pair<String, Long> samouraiFeePairBip47, long nTimeLock, RicochetConfig config) throws Exception {
        UtxoProvider utxoProvider = config.getUtxoProvider();
        BipFormatSupplier bipFormatSupplier = utxoProvider.getBipFormatSupplier();
        NetworkParameters params = prevOutPoint.getParams();
        TransactionOutput txOutputDestination = bipFormatSupplier.getTransactionOutput(destination, spendAmount, params);

        Transaction tx = new Transaction(params);
        tx.setVersion(2);
        if (nTimeLock > 0L) {
            tx.setLockTime(nTimeLock);
        }
        tx.addOutput(txOutputDestination);

        if (samouraiFeePairBip47 != null) {
            String samouraiFeeAddress = samouraiFeePairBip47.getLeft();
            long samouraiFeeAmount = samouraiFeePairBip47.getRight();
            TransactionOutput samouraiFeeOutput = bipFormatSupplier.getTransactionOutput(samouraiFeeAddress, samouraiFeeAmount, params);
            tx.addOutput(samouraiFeeOutput);
        }

//        Log.d("RicochetUtilGeneric", "spending from:" + p2wpkh.getBech32AsString());
//        Log.d("RicochetUtilGeneric", "pubkey:" + Hex.toHexString(ecKey.getPubKey()));

        TransactionInput txInput = prevOutPoint.computeSpendInput();
        if (config.isRbfOptIn()) {
            txInput.setSequenceNumber(SamouraiWalletConst.RBF_SEQUENCE_VAL.longValue());
        }
        tx.addInput(txInput);

        Map<String, ECKey> keyBag = new LinkedHashMap<>();
        keyBag.put(txInput.getOutpoint().toString(), prevKey);

        sendFactory.signTransaction(tx, keyBag, bipFormatSupplier);
        assert (0 == tx.getInput(0).getScriptBytes().length);
//        Log.d("RicochetUtilGeneric", "script sig length:" + tx.getInput(0).getScriptBytes().length);

        return tx;
    }

}
