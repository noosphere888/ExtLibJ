package com.samourai.wallet.api.backend;

import com.samourai.wallet.api.backend.beans.UnspentOutput;

import java.util.Collection;

public interface ISweepBackend extends IPushTx {
    Collection<UnspentOutput> fetchAddressForSweep(String address) throws Exception;
}
