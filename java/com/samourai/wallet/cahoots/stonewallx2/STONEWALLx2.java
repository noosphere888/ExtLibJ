package com.samourai.wallet.cahoots.stonewallx2;

import com.samourai.wallet.cahoots.Cahoots2x;
import com.samourai.wallet.cahoots.CahootsType;
import org.bitcoinj.core.NetworkParameters;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class STONEWALLx2 extends Cahoots2x {
    private static final Logger log = LoggerFactory.getLogger(STONEWALLx2.class);

    protected String strCollabChange = null;

    // set by initiator when spending to a paynym
    // we need this to increment paynym counter on android after broadcast
    protected String paynymDestination = null;

    private STONEWALLx2()    { ; }

    private STONEWALLx2(STONEWALLx2 c)    {
        super(c);
        this.strCollabChange = c.getCollabChange();
        this.paynymDestination = c.paynymDestination;
    }

    public STONEWALLx2(JSONObject obj)    {
        this.fromJSON(obj);
    }

    public STONEWALLx2(long spendAmount, String address, String paynymDestination, NetworkParameters params, int account, byte[] fingerprint)    {
        super(CahootsType.STONEWALLX2.getValue(), params, spendAmount, address, account, fingerprint);
        this.paynymDestination = paynymDestination;
    }

    @Override
    public STONEWALLx2 copy() {
        return new STONEWALLx2(this);
    }

    @Override
    protected JSONObject toJSONObjectCahoots() throws Exception {
        JSONObject obj = super.toJSONObjectCahoots();
        obj.put("collabChange", strCollabChange == null ? "" : strCollabChange);
        obj.put("destPaynym", paynymDestination == null ? "" : paynymDestination);
        return obj;
    }

    @Override
    protected void fromJSONObjectCahoots(JSONObject obj) throws Exception {
        super.fromJSONObjectCahoots(obj);
        if(obj.has("collabChange")) {
            this.strCollabChange = obj.getString("collabChange");
        }
        else    {
            this.strCollabChange = "";
        }
        this.paynymDestination = obj.getString("destPaynym");
    }

    public String getCollabChange() {
        return strCollabChange;
    }

    public void setCollabChange(String strCollabChange) {
        this.strCollabChange = strCollabChange;
    }

    @Override
    public String getPaynymDestination() {
        return paynymDestination;
    }
}