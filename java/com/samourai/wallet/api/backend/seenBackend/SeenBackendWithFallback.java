package com.samourai.wallet.api.backend.seenBackend;

import com.samourai.wallet.api.backend.IBackendClient;
import com.samourai.wallet.api.backend.OxtApi;
import com.samourai.wallet.util.FormatsUtilGeneric;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

public class SeenBackendWithFallback implements ISeenBackend {
    private static Logger log = LoggerFactory.getLogger(SeenBackendWithFallback.class);

    private ISeenBackend mainBackend;
    private ISeenBackend fallbackBackend;

    public SeenBackendWithFallback(ISeenBackend mainBackend, ISeenBackend fallbackBackend) {
        this.mainBackend = mainBackend;
        this.fallbackBackend = fallbackBackend;
    }

    public static ISeenBackend withOxt(ISeenBackend mainBackend, NetworkParameters params) {
        if (FormatsUtilGeneric.getInstance().isTestNet(params)) {
            return mainBackend; // disable OXT for testnet
        }
        return new SeenBackendWithFallback(mainBackend, new OxtApi(mainBackend.getHttpClient()));
    }

    @Override
    public SeenResponse seen(Collection<String> addresses) throws Exception {
        try {
            return mainBackend.seen(addresses);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("seenBackend not available, retrying with fallbackBackend...", e);
            }
        }
        return fallbackBackend.seen(addresses);
    }

    @Override
    public boolean seen(String address) throws Exception {
        return seen(Arrays.asList(address)).isSeen(address);
    }

    @Override
    public IBackendClient getHttpClient() {
        return mainBackend.getHttpClient();
    }
}
