package com.samourai.wallet.httpClient;

public interface IHttpClientService {
  IHttpClient getHttpClient(HttpUsage httpUsage);
  void changeIdentity(); // change Tor identity if any
  void stop();
}
