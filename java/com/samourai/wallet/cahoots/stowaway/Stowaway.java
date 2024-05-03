package com.samourai.wallet.cahoots.stowaway;

import com.samourai.wallet.cahoots.Cahoots2x;
import com.samourai.wallet.cahoots.CahootsType;
import org.bitcoinj.core.NetworkParameters;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stowaway extends Cahoots2x {
    private static final Logger log = LoggerFactory.getLogger(Stowaway.class);

    private Stowaway()    { ; }

    private Stowaway(Stowaway stowaway)    {
        super(stowaway);
    }

    public Stowaway(JSONObject obj)    {
        this.fromJSON(obj);
    }

    public Stowaway(long spendAmount, NetworkParameters params, int account, byte[] fingerprint)    {
        super(CahootsType.STOWAWAY.getValue(), params, spendAmount, null, account, fingerprint);
    }

    @Override
    public Stowaway copy() {
        return new Stowaway(this);
    }
}
