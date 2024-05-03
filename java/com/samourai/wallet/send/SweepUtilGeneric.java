package com.samourai.wallet.send;

import com.samourai.wallet.api.backend.ISweepBackend;
import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.send.beans.SweepPreview;
import com.samourai.wallet.util.FeeUtil;
import com.samourai.wallet.util.PrivKeyReader;
import com.samourai.wallet.util.TxUtil;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SweepUtilGeneric {
    private static Logger log = LoggerFactory.getLogger(SweepUtilGeneric.class);
    private static final long MIN_FEE_PER_B = 1L;

    private static SweepUtilGeneric instance = null;
    private static FeeUtil feeUtil = FeeUtil.getInstance();
    private static SendFactoryGeneric sendFactory = SendFactoryGeneric.getInstance();

    protected SweepUtilGeneric() { ; }

    public static SweepUtilGeneric getInstance() {

        if(instance == null)    {
            instance = new SweepUtilGeneric();
        }

        return instance;
    }

    public Collection<SweepPreview> sweepPreviews(final PrivKeyReader privKeyReader, long feePerB, ISweepBackend sweepBackend) throws Exception {
        Collection<BipFormat> bipFormats = Arrays.asList(BIP_FORMAT.LEGACY, BIP_FORMAT.SEGWIT_COMPAT, BIP_FORMAT.SEGWIT_NATIVE, BIP_FORMAT.TAPROOT);
        return sweepPreviews(privKeyReader, feePerB, sweepBackend, bipFormats);
    }

    public Collection<SweepPreview> sweepPreviews(final PrivKeyReader privKeyReader, long feePerB, ISweepBackend sweepBackend, Collection<BipFormat> bipFormats) throws Exception {
        // try each bipFormat
        Collection<SweepPreview> sweepPreviews = new LinkedList<>();
        for (BipFormat bipFormat : bipFormats) {
            SweepPreview sweepPreview = sweepPreview(privKeyReader, feePerB, sweepBackend, bipFormat);
            if (sweepPreview != null) {
                sweepPreviews.add(sweepPreview);
            }
        }
        return sweepPreviews;
    }

    public SweepPreview sweepPreview(final PrivKeyReader privKeyReader, long feePerB, ISweepBackend sweepBackend, BipFormat bipFormat) throws Exception {
        // check private key
        if(privKeyReader == null || privKeyReader.getKey() == null || !privKeyReader.getKey().hasPrivKey())    {
            if (log.isDebugEnabled()) {
                if (privKeyReader == null) {
                    log.debug("privKeyReader=null");
                }
                else if (privKeyReader.getKey() == null) {
                    log.debug("privKeyReader.getKey()=null");
                }
                else if (!privKeyReader.getKey().hasPrivKey()) {
                    log.debug("privKeyReader.getKey().hasPrivKey()=null");
                }
            }
            throw new Exception("Cannot recognize private key");
        }

        // apply min fee
        feePerB = Math.max(feePerB, MIN_FEE_PER_B);
        NetworkParameters params = privKeyReader.getParams();

        // find utxo
        final String address = bipFormat.getToAddress(privKeyReader.getKey(), params);
        if (address == null) {
            if (log.isDebugEnabled()) {
                log.debug("findUtxoToSweep: no address found for "+bipFormat);
            }
            return null;
        }
        Collection<UnspentOutput> unspentOutputs = sweepBackend.fetchAddressForSweep(address);
        if (unspentOutputs.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("findUtxoToSweep: no utxo found for "+bipFormat+" "+address);
            }
            return null;
        }

        // found utxo to sweep
        if (log.isDebugEnabled()) {
            log.debug("findUtxoToSweep: found "+unspentOutputs.size()+" utxos for "+bipFormat+" "+address);
        }
        long totalValue = UnspentOutput.sumValue(unspentOutputs);
        long fee = computeFee(bipFormat, unspentOutputs, feePerB);
        long amount = totalValue - fee;

        SweepPreview sweepPreview = new SweepPreview(amount, address, bipFormat, fee, unspentOutputs, privKeyReader.getKey(), params);
        return sweepPreview;
    }

    protected long computeFee(BipFormat bipFormat, Collection<UnspentOutput> unspentOutputs, long feePerB) {
        int inputsP2PKH = 0;
        int inputsP2WPKH = 0;
        int inputsP2SH_P2WPKH = 0;

        if(bipFormat == BIP_FORMAT.SEGWIT_COMPAT)    {
            inputsP2SH_P2WPKH = unspentOutputs.size();
        }
        else if(bipFormat == BIP_FORMAT.SEGWIT_NATIVE)    {
            inputsP2WPKH = unspentOutputs.size();
        }
        else {
            inputsP2PKH = unspentOutputs.size();
        }
        return feeUtil.estimatedFeeSegwit(inputsP2PKH, inputsP2SH_P2WPKH, inputsP2WPKH, 1, 0, feePerB);
    }

    public String sweep(SweepPreview sweepPreview, String receive_address, ISweepBackend sweepBackend, BipFormatSupplier bipFormatSupplier, boolean rbfOptIn, long blockHeight) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("sweeping to "+receive_address+": "+sweepPreview);
        }
        NetworkParameters params = sweepPreview.getParams();
        final Map<String, Long> receivers = new LinkedHashMap<>();
        receivers.put(receive_address, sweepPreview.getAmount());
        Collection<MyTransactionOutPoint> outpoints = sweepPreview.getUtxos().stream()
                .map(unspentOutput -> unspentOutput.computeOutpoint(params)).collect(Collectors.toList());

        Transaction tx = sendFactory.makeTransaction(receivers, outpoints, bipFormatSupplier, rbfOptIn, params, blockHeight);
        try {
            tx = sendFactory.signTransactionForSweep(tx, sweepPreview.getPrivKey(), params);
            if (log.isDebugEnabled()) {
                log.debug("tx size:" + tx.bitcoinSerialize().length);
            }
            final String hexTx = TxUtil.getInstance().getTxHex(tx);

            try {
                String txid = sweepBackend.pushTx(hexTx);
                log.info("sweep success: "+txid);
                return txid;
            } catch (Exception e) {
                throw new Exception("pushTx:" + e.getMessage());
            }
        } catch (Exception e) {
            throw new Exception("signTx:" + e.getMessage());
        }
    }

}
