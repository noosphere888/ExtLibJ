package com.samourai.wallet.websocketClient;

public interface IWebsocketClientListener {
    void onClose(String reason);
    void onMessage(String msg);
    void onConnect();
}
