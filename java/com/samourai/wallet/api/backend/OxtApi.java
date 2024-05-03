package com.samourai.wallet.api.backend;

import com.samourai.wallet.api.backend.seenBackend.ISeenBackend;
import com.samourai.wallet.api.backend.seenBackend.SeenResponse;
import com.samourai.wallet.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OxtApi implements ISeenBackend {
  private static Logger log = LoggerFactory.getLogger(OxtApi.class);

  private static final String URL_OXT_MAINNET = "https://api.oxt.me";
  private static final String URL_SEEN = "/addresses/multi/seen?addresses=";

  private IBackendClient httpClient;
  private String urlBackend;

  public OxtApi(IBackendClient httpClient) {
    this(httpClient, URL_OXT_MAINNET);
  }

  public OxtApi(IBackendClient httpClient, String urlBackend) {
    this.httpClient = httpClient;
    this.urlBackend = urlBackend;
  }

  @Override
  public SeenResponse seen(Collection<String> addresses) throws Exception {
    String addressesStr = String.join(",", addresses);
    String url = computeAuthUrl(urlBackend + URL_SEEN + Util.encodeUrl(addressesStr));
    Map<String,String> headers = computeHeaders();
    Map<String,Boolean> seenResponse = (Map<String,Boolean>)getOxtData(httpClient.getJson(url, Map.class, headers));
    return new SeenResponse(seenResponse);
  }

  @Override
  public boolean seen(String address) throws Exception {
    return seen(Arrays.asList(address)).isSeen(address);
  }

  protected Object getOxtData(Map<String,Object> oxtResponse) throws Exception {
    String message = (String)oxtResponse.get("message");
    if (!StringUtils.isEmpty(message)) {
      throw new Exception("OXT error response: "+message);
    }
    List data = (List)oxtResponse.get("data");
    if (data == null || data.size() <= 0) {
      throw new Exception("Oxt error response: data is empty");
    }
    Object data0 = data.get(0);
    if (data0 == null) {
      throw new Exception("Oxt error response: data[0] is null");
    }
    return data0;
  }

  protected Map<String,String> computeHeaders() throws Exception {
    Map<String,String> headers = new HashMap<String, String>();
    // override if needed
    return headers;
  }

  protected String computeAuthUrl(String  url) throws Exception {
    // override for auth support
    return url;
  }

  @Override
  public IBackendClient getHttpClient() {
    return httpClient;
  }
}
