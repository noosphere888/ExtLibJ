package com.samourai.wallet.cahoots.multi;

import com.samourai.wallet.cahoots.CahootsContext;
import com.samourai.wallet.cahoots.manual.ManualCahootsMessage;
import com.samourai.wallet.api.backend.IPushTx;
import com.samourai.wallet.cahoots.Cahoots;
import com.samourai.wallet.cahoots.CahootsType;
import com.samourai.wallet.cahoots.psbt.PSBT;
import com.samourai.wallet.cahoots.stonewallx2.STONEWALLx2;
import com.samourai.wallet.cahoots.stowaway.Stowaway;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.exceptions.SpendException;
import com.samourai.wallet.send.provider.UtxoKeyProvider;
import com.samourai.wallet.util.TxUtil;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class MultiCahoots extends Cahoots {
    private static final Logger log = LoggerFactory.getLogger(MultiCahoots.class);

    protected Stowaway stowaway;
    protected STONEWALLx2 stonewallx2;

    private MultiCahoots()    { ; }

    public MultiCahoots(MultiCahoots multiCahoots)    {
        super(multiCahoots);
        this.stonewallx2 = multiCahoots.stonewallx2.copy();
        this.stowaway = multiCahoots.stowaway.copy();

        // keep stowaway unchanged once finished
        if (multiCahoots.stowaway.getStep() == ManualCahootsMessage.LAST_STEP) {
            this.stowaway.setStep(multiCahoots.stowaway.getStep());
        }
    }

    public MultiCahoots(JSONObject obj)    {
        this.fromJSON(obj);
    }

    // Stowaway
    public MultiCahoots(NetworkParameters params, Stowaway stowaway, STONEWALLx2 stonewallx2)    {
        super(CahootsType.MULTI.getValue(), params);
        this.stowaway = stowaway;
        this.stonewallx2 = stonewallx2;
    }

    @Override
    public String getPaynymDestination() {
        return stonewallx2.getPaynymDestination();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonObject = super.toJSON();
        jsonObject.put("stonewallx2", stonewallx2.toJSON());
        jsonObject.put("stowaway", stowaway.toJSON());
        return jsonObject;
    }

    @Override
    public void fromJSON(JSONObject cObj) {
        super.fromJSON(cObj);
        stonewallx2 = new STONEWALLx2(cObj.getJSONObject("stonewallx2"));
        stowaway = new Stowaway(cObj.getJSONObject("stowaway"));
    }

    @Override
    public void signTx(HashMap<String,ECKey> keyBag) {
        if(getStep() > 3) {
            stonewallx2.signTx(keyBag);
        } else {
            stowaway.signTx(keyBag);
        }
    }

    public Stowaway getStowaway() {
        return stowaway;
    }

    public void setStowaway(Stowaway stowaway) {
        this.stowaway = stowaway;
    }

    public STONEWALLx2 getStonewallx2() {
        return stonewallx2;
    }

    public void setStonewallx2(STONEWALLx2 stonewallx2) {
        this.stonewallx2 = stonewallx2;
    }

    public Transaction getStowawayTransaction() {
        return getStowaway().getTransaction();
    }

    public Transaction getStonewallTransaction() {
        return getStonewallx2().getTransaction();
    }

    @Override
    public long getFeeAmount() {
        return stonewallx2.getFeeAmount() + stowaway.getFeeAmount();
    }

    @Override
    public HashMap<String, Long> getOutpoints() {
        return stonewallx2.getOutpoints();
    }

    @Override
    public String getDestination() {
        return stonewallx2.getDestination();
    }

    @Override
    public long getSpendAmount() {
        return stonewallx2.getSpendAmount();
    }

    @Override
    public Transaction getTransaction() {
        return stonewallx2.getTransaction();
    }

    @Override
    public PSBT getPSBT() {
        return stonewallx2.getPSBT();
    }

    @Override
    public void pushTx(IPushTx pushTx) throws Exception {
        // push stonewallx2
        String stonewallHex = TxUtil.getInstance().getTxHex(getStonewallTransaction());
        pushTx.pushTx(stonewallHex);

        // push stowaway
        String stowawayHex = TxUtil.getInstance().getTxHex(getStowawayTransaction());
        pushTx.pushTx(stowawayHex);
    }

    @Override
    public SpendTx getSpendTx(CahootsContext cahootsContext, UtxoKeyProvider utxoKeyProvider) throws SpendException {
        // forward stonewallx2 SpendTx
        CahootsContext stonewallx2Context = ((MultiCahootsContext)cahootsContext).getStonewallx2Context();
        return stonewallx2.getSpendTx(stonewallx2Context, utxoKeyProvider);
    }
}