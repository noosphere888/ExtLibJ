package com.samourai.wallet.websocketClient;

public interface IWebsocketClient {
  void connect(String url, IWebsocketClientListener listener) throws Exception;

  void send(String payload) throws Exception;

  void disconnect();
}
