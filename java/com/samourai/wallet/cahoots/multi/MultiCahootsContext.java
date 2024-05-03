package com.samourai.wallet.cahoots.multi;

import com.samourai.wallet.cahoots.CahootsContext;
import com.samourai.wallet.cahoots.stonewallx2.Stonewallx2Context;
import com.samourai.wallet.cahoots.stowaway.StowawayContext;
import com.samourai.wallet.cahoots.CahootsType;
import com.samourai.wallet.cahoots.CahootsTypeUser;
import com.samourai.wallet.cahoots.CahootsWallet;
import com.samourai.wallet.xmanagerClient.XManagerClient;

public class MultiCahootsContext extends CahootsContext {
    private Stonewallx2Context stonewallx2Context;
    private StowawayContext stowawayContext;
    private XManagerClient xManagerClient; // only needed for counterparty

    protected MultiCahootsContext(CahootsWallet cahootsWallet, CahootsTypeUser typeUser, int account, Long feePerB, Long amount, String address, String paynymDestination, XManagerClient xManagerClient) {
        super(cahootsWallet, typeUser, CahootsType.MULTI, account, feePerB, amount, address);
        this.stonewallx2Context = computeStonewallContext(paynymDestination);
        this.stowawayContext = computeStowawayContext();
        this.xManagerClient = xManagerClient;
    }

    public static MultiCahootsContext newInitiator(CahootsWallet cahootsWallet, int account, long feePerB, long amount, String address, String paynymDestination) {
        return new MultiCahootsContext(cahootsWallet, CahootsTypeUser.SENDER, account, feePerB, amount, address, paynymDestination, null);
    }

    public static MultiCahootsContext newCounterparty(CahootsWallet cahootsWallet, int account, XManagerClient xManagerClient) {
        return new MultiCahootsContext(cahootsWallet, CahootsTypeUser.COUNTERPARTY, account, null,null, null, null, xManagerClient);
    }

    private Stonewallx2Context computeStonewallContext(String paynymDestination) {
        if (getTypeUser().equals(CahootsTypeUser.COUNTERPARTY)) {
            return Stonewallx2Context.newCounterparty(getCahootsWallet(), getAccount());
        }
        return Stonewallx2Context.newInitiator(getCahootsWallet(), getAccount(), getFeePerB(), getAmount(), getAddress(), paynymDestination);
    }

    private StowawayContext computeStowawayContext() {
        if (getTypeUser().equals(CahootsTypeUser.COUNTERPARTY)) {
            return StowawayContext.newCounterpartyMulti(getCahootsWallet(), getAccount());
        }
        return StowawayContext.newInitiator(getCahootsWallet(), getAccount(), getFeePerB(), -1L);
    }

    public static long computeMultiCahootsFee(long amount) {
        long stowawayFee = (long)(amount * 0.035d);
        if(stowawayFee > 1000000) {
            stowawayFee = 1000000;
        }
        return stowawayFee;
    }

    public Stonewallx2Context getStonewallx2Context() {
        return stonewallx2Context;
    }

    public StowawayContext getStowawayContext() {
        return stowawayContext;
    }

    public XManagerClient getxManagerClient() {
        return xManagerClient;
    }
}
