package com.samourai.wallet.util;

public interface CallbackWithArg<P,R> {
  R apply(P arg) throws Exception;
}
