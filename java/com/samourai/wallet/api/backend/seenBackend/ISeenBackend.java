package com.samourai.wallet.api.backend.seenBackend;

import com.samourai.wallet.api.backend.IBackendClient;

import java.util.Collection;

public interface ISeenBackend {
    SeenResponse seen(Collection<String> addresses) throws Exception;
    boolean seen(String address) throws Exception;
    IBackendClient getHttpClient();
}
