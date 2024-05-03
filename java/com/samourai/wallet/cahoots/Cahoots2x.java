package com.samourai.wallet.cahoots;

import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.bip69.BIP69InputComparator;
import com.samourai.wallet.bip69.BIP69OutputComparator;
import com.samourai.wallet.cahoots.psbt.PSBT;
import com.samourai.wallet.chain.ChainSupplier;
import com.samourai.wallet.segwit.SegwitAddress;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.beans.SpendTxCahoots;
import com.samourai.wallet.send.exceptions.SpendException;
import com.samourai.wallet.send.provider.UtxoKeyProvider;
import com.samourai.wallet.util.RandomUtil;
import com.samourai.wallet.util.Z85;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

// shared payload for 2x Cahoots: Stonewallx2, Stowaway, Tx0x2
public abstract class Cahoots2x extends Cahoots {
    private static final Logger log = LoggerFactory.getLogger(Cahoots2x.class);

    // used by Sparrow
    protected static final String BLOCK_HEIGHT_PROPERTY = "com.sparrowwallet.blockHeight";
    protected static final long SEQUENCE_RBF_ENABLED = 4294967293L;

    protected long ts = -1L;
    protected String strID = null;
    protected PSBT psbt = null;
    protected long spendAmount = 0L;
    protected long feeAmount = 0L;
    protected HashMap<String,Long> outpoints = null;
    protected String strDestination = null;
    protected String strPayNymCollab = null;
    protected String strPayNymInit = null;
    protected int account = 0;
    protected int cptyAccount = 0;
    protected byte[] fingerprint = null;
    protected byte[] fingerprintCollab = null;

    public Cahoots2x()    {
        super();
        outpoints = new HashMap<String,Long>();
    }

    protected Cahoots2x(Cahoots2x c)    {
        super(c);
        this.ts = c.getTS();
        this.strID = c.getID();
        this.psbt = c.getPSBT();
        this.spendAmount = c.getSpendAmount();
        this.feeAmount = c.getFeeAmount();
        this.outpoints = c.getOutpoints();
        this.strDestination = c.strDestination;
        this.strPayNymCollab = c.strPayNymCollab;
        this.strPayNymInit = c.strPayNymInit;
        this.account = c.getAccount();
        this.cptyAccount = c.getCounterpartyAccount();
        this.fingerprint = c.getFingerprint();
        this.fingerprintCollab = c.getFingerprintCollab();
    }

    public Cahoots2x(int type, NetworkParameters params, long spendAmount, String strDestination, int account, byte[] fingerprint) {
        super(type, params);
        this.ts = System.currentTimeMillis() / 1000L;
        long randomLong = RandomUtil.getInstance().nextLong();
        this.strID = Hex.toHexString(Sha256Hash.hash(BigInteger.valueOf(randomLong).toByteArray()));
        this.spendAmount = spendAmount;
        this.outpoints = new HashMap<String, Long>();
        this.strDestination = strDestination;
        this.account = account;
        this.fingerprint = fingerprint;
    }

    public abstract Cahoots2x copy();

    public long getTS() { return ts; }

    public String getID() {
        return strID;
    }

    public PSBT getPSBT() {
        return psbt;
    }

    public void setPSBT(PSBT psbt) {
        this.psbt = psbt;
    }

    public Transaction getTransaction() {
        if (psbt == null) {
            return null;
        }
        return psbt.getTransaction();
    }

    @Override
    public long getSpendAmount() {
        return spendAmount;
    }

    public void setSpendAmount(long spendAmount) {
        this.spendAmount = spendAmount;
    }

    @Override
    public long getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(long fee)  {
        feeAmount = fee;
    }

    @Override
    public HashMap<String, Long> getOutpoints() {
        return outpoints;
    }

    public void setOutpoints(HashMap<String, Long> outpoints) {
        this.outpoints = outpoints;
    }

    @Override
    public String getDestination() {
        return strDestination;
    }

    public void setDestination(String strDestination) {
        this.strDestination = strDestination;
    }

    public String getPayNymCollab() {
        return strPayNymCollab;
    }

    public String getPayNymInit() {
        return strPayNymInit;
    }

    public int getAccount() {
        return account;
    }

    public void setCounterpartyAccount(int account) {
        this.cptyAccount = account;
    }

    public int getCounterpartyAccount() {
        return cptyAccount;
    }

    public byte[] getFingerprint() {
        return fingerprint;
    }

    public byte[] getFingerprintCollab() {
        return fingerprintCollab;
    }

    public void setFingerprintCollab(byte[] fingerprint) {
        this.fingerprintCollab = fingerprint;
    }

    @Override
    protected JSONObject toJSONObjectCahoots() throws Exception {
        JSONObject obj = super.toJSONObjectCahoots();
        obj.put("ts", ts);
        obj.put("id", strID);
        obj.put("spend_amount", spendAmount);
        obj.put("fee_amount", feeAmount);
        JSONArray _outpoints = new JSONArray();
        for(String outpoint : outpoints.keySet())   {
            JSONObject entry = new JSONObject();
            entry.put("outpoint", outpoint);
            entry.put("value", outpoints.get(outpoint));
            _outpoints.put(entry);
        }
        obj.put("outpoints", _outpoints);
        obj.put("dest", strDestination == null ? "" : strDestination);
        obj.put("account", account);
        obj.put("cpty_account", cptyAccount);
        if(fingerprint != null)    {
            obj.put("fingerprint", Hex.toHexString(fingerprint));
        }
        if(fingerprintCollab != null)    {
            obj.put("fingerprint_collab", Hex.toHexString(fingerprintCollab));
        }
        obj.put("psbt", psbt == null ? "" : Z85.getInstance().encode(psbt.toGZIP()));
        return obj;
    }

    @Override
    protected void fromJSONObjectCahoots(JSONObject obj) throws Exception {
        super.fromJSONObjectCahoots(obj);

        if(obj.has("psbt") && obj.has("ts") && obj.has("id") && obj.has("spend_amount"))    {
            this.ts = obj.getLong("ts");
            this.strID = obj.getString("id");
            this.spendAmount = obj.getLong("spend_amount");
            this.feeAmount = obj.getLong("fee_amount");
            JSONArray _outpoints = obj.getJSONArray("outpoints");
            for(int i = 0; i < _outpoints.length(); i++)   {
                JSONObject entry = _outpoints.getJSONObject(i);
                outpoints.put(entry.getString("outpoint"), entry.getLong("value"));
            }
            this.strDestination = obj.getString("dest");
            if(obj.has("account"))    {
                this.account = obj.getInt("account");
            }
            else    {
                this.account = 0;
            }
            if(obj.has("cpty_account"))    {
                this.cptyAccount = obj.getInt("cpty_account");
            }
            else    {
                this.cptyAccount = 0;
            }
            if(obj.has("fingerprint"))    {
                fingerprint = Hex.decode(obj.getString("fingerprint"));
            }
            if(obj.has("fingerprint_collab"))    {
                fingerprintCollab = Hex.decode(obj.getString("fingerprint_collab"));
            }
            this.psbt = obj.getString("psbt").equals("") ? null : PSBT.fromBytes(Z85.getInstance().decode(obj.getString("psbt")), getParams());
        }
    }

    @Override
    public void signTx(HashMap<String,ECKey> keyBag) {

        Transaction transaction = psbt.getTransaction();
        if (log.isDebugEnabled()) {
            log.debug("signTx:" + transaction.toString());
        }

        for(int i = 0; i < transaction.getInputs().size(); i++)   {

            TransactionInput input = transaction.getInput(i);
            TransactionOutPoint outpoint = input.getOutpoint();
            if(keyBag.containsKey(outpoint.toString())) {

                if (log.isDebugEnabled()) {
                    log.debug("signTx outpoint:" + outpoint.toString());
                }

                ECKey key = keyBag.get(outpoint.toString());
                SegwitAddress segwitAddress = new SegwitAddress(key.getPubKey(), getParams());

                if (log.isDebugEnabled()) {
                    log.debug("signTx bech32:" + segwitAddress.getBech32AsString());
                }

                final Script redeemScript = segwitAddress.segwitRedeemScript();
                final Script scriptCode = redeemScript.scriptCode();

                long value = outpoints.get(outpoint.getHash().toString() + "-" + outpoint.getIndex());
                if (log.isDebugEnabled()) {
                    log.debug("signTx value:" + value);
                }

                TransactionSignature sig = transaction.calculateWitnessSignature(i, key, scriptCode, Coin.valueOf(value), Transaction.SigHash.ALL, false);
                final TransactionWitness witness = new TransactionWitness(2);
                witness.setPush(0, sig.encodeToBitcoin());
                witness.setPush(1, key.getPubKey());
                transaction.setWitness(i, witness);

            }

        }

        psbt.setTransaction(transaction);

    }

    public boolean isContributedAmountSufficient(long totalContributedAmount) {
        return isContributedAmountSufficient(totalContributedAmount, null);
    }

    public boolean isContributedAmountSufficient(long totalContributedAmount, Long estimatedFee) {
        return totalContributedAmount >= computeRequiredAmount(estimatedFee);
    }

    public long computeRequiredAmount() {
        return computeRequiredAmount(null);
    }

    public long computeRequiredAmount(Long estimatedFee) {
        long requiredAmount = getSpendAmount() + SamouraiWalletConst.bDust.longValue();
        if (estimatedFee != null) {
            requiredAmount += estimatedFee;
        }
        return requiredAmount;
    }

    //
    // counterparty
    //
    public void doStep1(List<TransactionInput> inputs, List<TransactionOutput> outputs, ChainSupplier chainSupplier) throws Exception    {
        if(this.getStep() != 0 || this.getSpendAmount() == 0L)   {
            throw new Exception("Invalid step/amount");
        }
        if(outputs == null)    {
            throw new Exception("Invalid outputs");
        }

        Transaction transaction = new Transaction(params);
        transaction.setVersion(2);
        appendTx(inputs, outputs, transaction, chainSupplier);

        this.setStep(1);
    }

    //
    // sender
    //
    public void doStep2(List<TransactionInput> inputs, List<TransactionOutput> outputs) throws Exception    {
        Transaction transaction = psbt.getTransaction();
        appendTx(inputs, outputs, transaction, null); // no need to give chain supplier, psbt should have the lock time

        this.setStep(2);
    }

    //
    // counterparty
    //
    public void doStep3(HashMap<String,ECKey> keyBag)    {
        Transaction transaction = this.getTransaction();

        // sort inputs
        List<TransactionInput> inputs = new ArrayList<TransactionInput>();
        inputs.addAll(transaction.getInputs());
        Collections.sort(inputs, new BIP69InputComparator());
        transaction.clearInputs();
        for(TransactionInput input : inputs)    {
            transaction.addInput(input);
        }

        // sort outputs
        List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
        outputs.addAll(transaction.getOutputs());
        Collections.sort(outputs, new BIP69OutputComparator());
        transaction.clearOutputs();
        for(TransactionOutput output : outputs)    {
            transaction.addOutput(output);
        }

        psbt = new PSBT(transaction);

        signTx(keyBag);

        this.setStep(3);
    }

    //
    // sender
    //
    public void doStep4(HashMap<String,ECKey> keyBag)    {
        signTx(keyBag);

        this.setStep(4);
    }

    protected void appendTx(List<TransactionInput> inputs, List<TransactionOutput> outputs, Transaction transaction, ChainSupplier chainSupplier) {
        // append inputs
        for(TransactionInput input : inputs)   {
            input.setSequenceNumber(SEQUENCE_RBF_ENABLED);
            transaction.addInput(input);
            outpoints.put(input.getOutpoint().getHash().toString() + "-" + input.getOutpoint().getIndex(), input.getValue().longValue());
        }

        // append outputs
        for(TransactionOutput output : outputs)   {
            transaction.addOutput(output);
        }

        // used by Sparrow
        String strBlockHeight = System.getProperty(BLOCK_HEIGHT_PROPERTY);
        if(strBlockHeight != null) {
            transaction.setLockTime(Long.parseLong(strBlockHeight));
        } else if(this.psbt == null && getType() != CahootsType.STOWAWAY.getValue()) {
            if(chainSupplier != null) {
                long height = chainSupplier.getLatestBlock().height;
                transaction.setLockTime(height);
            }
        }

        // update psbt
        this.psbt = new PSBT(transaction);
    }

    @Override
    public SpendTx getSpendTx(CahootsContext cahootsContext, UtxoKeyProvider utxoKeyProvider) throws SpendException {
        return new SpendTxCahoots(this, cahootsContext, utxoKeyProvider);
    }
}
