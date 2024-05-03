package com.samourai.wallet.util;

/**
 * App is shutting down.
 */
public class ShutdownException extends RuntimeException {
    public ShutdownException(Throwable cause) {
        super(cause);
    }
    public ShutdownException(String message) {
        super(message);
    }
}
