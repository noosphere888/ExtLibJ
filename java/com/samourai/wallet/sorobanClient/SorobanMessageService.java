package com.samourai.wallet.sorobanClient;

public abstract class SorobanMessageService<M extends SorobanMessage, C extends SorobanContext> {
    public abstract M parse(String payload) throws Exception;

    public abstract SorobanReply reply(C sorobanContext, M message) throws Exception;
}
