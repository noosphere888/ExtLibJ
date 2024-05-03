package com.samourai.wallet.sorobanClient;

public interface SorobanPayloadable extends SorobanReply {
    String toPayload() throws Exception;
}
