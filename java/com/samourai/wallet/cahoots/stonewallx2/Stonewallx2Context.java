package com.samourai.wallet.cahoots.stonewallx2;

import com.samourai.wallet.cahoots.CahootsContext;
import com.samourai.wallet.cahoots.CahootsType;
import com.samourai.wallet.cahoots.CahootsTypeUser;
import com.samourai.wallet.cahoots.CahootsWallet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stonewallx2Context extends CahootsContext {
    private static final Logger log = LoggerFactory.getLogger(Stonewallx2Context.class);

    // only set for initiator, when sending to paynym
    // android will check STONEWALLx2.getPaynymDestination() to increment paynym counter after successfull broadcast
    private String paynymDestination;

    protected Stonewallx2Context(CahootsWallet cahootsWallet, CahootsTypeUser typeUser, int account, Long feePerB, Long amount, String address, String paynymDestination) {
        super(cahootsWallet, typeUser, CahootsType.STONEWALLX2, account, feePerB, amount, address);
        this.paynymDestination = paynymDestination;
    }

    public static Stonewallx2Context newInitiator(CahootsWallet cahootsWallet, int account, long feePerB, long amount, String address, String paynymDestination) {
        return new Stonewallx2Context(cahootsWallet, CahootsTypeUser.SENDER, account, feePerB, amount, address, paynymDestination);
    }

    public static Stonewallx2Context newCounterparty(CahootsWallet cahootsWallet, int account) {
        return new Stonewallx2Context(cahootsWallet, CahootsTypeUser.COUNTERPARTY, account, null,null, null, null);
    }

    public String getPaynymDestination() {
        return StringUtils.isEmpty(paynymDestination) ? null : paynymDestination;
    }
}
