package com.samourai.wallet.api.backend;

import com.samourai.wallet.api.backend.beans.*;
import com.samourai.wallet.api.backend.seenBackend.ISeenBackend;
import com.samourai.wallet.api.backend.seenBackend.SeenResponse;
import com.samourai.wallet.api.backend.websocket.BackendWsApi;
import com.samourai.wallet.httpClient.HttpResponseException;
import com.samourai.wallet.util.JSONUtils;
import com.samourai.wallet.util.Util;
import com.samourai.wallet.util.oauth.OAuthApi;
import com.samourai.wallet.util.oauth.OAuthManager;
import com.samourai.wallet.util.oauth.OAuthManagerJava;
import com.samourai.wallet.websocketClient.IWebsocketClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BackendApi implements ISweepBackend, ISeenBackend {
  private static Logger log = LoggerFactory.getLogger(BackendApi.class);

  private static final String URL_UNSPENT = "/unspent?active=";
  private static final String URL_MULTIADDR = "/multiaddr?active=";
  private static final String URL_WALLET = "/wallet?active=";
  private static final String URL_XPUB = "/xpub/";
  private static final String URL_TXS = "/txs?active=";
  private static final String URL_TX = "/tx/";
  private static final String URL_INIT_BIP84 = "/xpub";
  private static final String URL_MINER_FEES = "/fees";
  private static final String URL_SEEN = "/seen?addresses=";
  private static final String URL_PUSHTX = "/pushtx/";
  private static final String ZPUB_SEPARATOR = "%7C";

  private IBackendClient httpClient;
  private String urlBackend;
  private OAuthManager oAuthManager; // may be null

  protected BackendApi(IBackendClient httpClient, String urlBackend, OAuthManager oAuthManager) {
    this.httpClient = httpClient;
    this.urlBackend = urlBackend;

    this.oAuthManager = oAuthManager;
    if (log.isDebugEnabled()) {
      String oAuthStr = oAuthManager != null ? "yes" : "no";
      log.debug("urlBackend=" + urlBackend + ", oAuth=" + oAuthStr);
    }
  }

  public static BackendApi newBackendApiDojo(IBackendClient httpClient, String urlBackend, String dojoApiKey) {
    // configure OAuth
    OAuthApi backendOAuthApi = new BackendOAuthApi(httpClient, urlBackend);
    OAuthManager oAuthManager = new OAuthManagerJava(dojoApiKey, backendOAuthApi);

    // configure Samourai/Dojo backend
    return new BackendApi(httpClient, urlBackend, oAuthManager);
  }

  public static BackendApi newBackendApiSamourai(IBackendClient httpClient, String urlBackend) {
    return new BackendApi(httpClient, urlBackend, null);
  }

  public BackendWsApi newBackendWsApi(IWebsocketClient wsClient) {
    return new BackendWsApi(wsClient, urlBackend, oAuthManager);
  }

  private String computeZpubStr(String[] zpubs) {
    String zpubStr = StringUtils.join(zpubs,ZPUB_SEPARATOR);
    return zpubStr;
  }

  public List<UnspentOutput> fetchUtxos(String... zpubs) throws Exception {
    String zpubStr = computeZpubStr(zpubs);
    String url = computeAuthUrl(urlBackend + URL_UNSPENT + zpubStr);
    if (log.isDebugEnabled()) {
      log.debug("fetchUtxos");
    }
    Map<String,String> headers = computeHeaders();
    UnspentResponse unspentResponse = httpClient.getJson(url, UnspentResponse.class, headers);
    List<UnspentOutput> unspentOutputs =
            new ArrayList<UnspentOutput>();
    if (unspentResponse.unspent_outputs != null) {
      unspentOutputs = Arrays.asList(unspentResponse.unspent_outputs);
    }
    return unspentOutputs;
  }

  public Map<String,MultiAddrResponse.Address> fetchAddresses(String... zpubs) throws Exception {
    String zpubStr = computeZpubStr(zpubs);
    String url = computeAuthUrl(urlBackend + URL_MULTIADDR + zpubStr);
    if (log.isDebugEnabled()) {
      log.debug("fetchAddress");
    }
    Map<String,String> headers = computeHeaders();
    MultiAddrResponse multiAddrResponse = httpClient.getJson(url, MultiAddrResponse.class, headers);
    Map<String,MultiAddrResponse.Address> addressesByZpub = new LinkedHashMap<String, MultiAddrResponse.Address>();
    if (multiAddrResponse.addresses != null) {
      for (MultiAddrResponse.Address address : multiAddrResponse.addresses) {
        addressesByZpub.put(address.address, address);
      }
    }
    return addressesByZpub;
  }

  public MultiAddrResponse.Address fetchAddress(String zpub) throws Exception {
    Collection<MultiAddrResponse.Address> addresses = fetchAddresses(new String[]{zpub}).values();
    if (addresses.size() != 1) {
      throw new Exception("Address count=" + addresses.size());
    }
    MultiAddrResponse.Address address = addresses.iterator().next();

    if (log.isDebugEnabled()) {
      log.debug(
              "fetchAddress "
                      + zpub
                      + ": account_index="
                      + address.account_index
                      + ", change_index="
                      + address.change_index);
    }
    return address;
  }

  public TxsResponse fetchTxs(String[] zpubs, int page, int count) throws Exception {
    String zpubStr = computeZpubStr(zpubs);

    String url = computeAuthUrl(urlBackend + URL_TXS + zpubStr+"&page="+page+"&count="+count);
    if (log.isDebugEnabled()) {
      log.debug("fetchTxs");
    }
    Map<String,String> headers = computeHeaders();
    return httpClient.getJson(url, TxsResponse.class, headers);
  }

  public TxDetail fetchTx(String txid, boolean fees) throws Exception {
    String url = computeAuthUrl(urlBackend + URL_TX + txid + (fees ? "?fees=1" : ""));
    if (log.isDebugEnabled()) {
      log.debug("fetchTx: "+txid);
    }
    Map<String,String> headers = computeHeaders();
    return httpClient.getJson(url, TxDetail.class, headers);
  }

  public WalletResponse fetchWallet(String... zpubs) throws Exception {
    String zpubStr = computeZpubStr(zpubs);
    String url = computeAuthUrl(urlBackend + URL_WALLET + zpubStr);
    if (log.isDebugEnabled()) {
      log.debug("fetchWallet");
    }
    Map<String,String> headers = computeHeaders();
    // use async to avoid Jetty's buffer exceeded exception on large responses
    WalletResponse walletResponse = httpClient.getJson(url, WalletResponse.class, headers, true);
    return walletResponse;
  }

  @Override
  public Collection<UnspentOutput> fetchAddressForSweep(String address) throws Exception {
    WalletResponse walletResponse = fetchWallet(address);
    return Arrays.asList(walletResponse.unspent_outputs);
  }

  public XPubResponse fetchXPub(String xpub) throws Exception {
    String url = computeAuthUrl(urlBackend + URL_XPUB + xpub);
    if (log.isDebugEnabled()) {
      log.debug("fetchXpub");
    }
    Map<String,String> headers = computeHeaders();
    XPubResponse response = httpClient.getJson(url, XPubResponse.class, headers);
    if (response.status != XPubResponse.Status.ok) {
      throw new Exception("fetchXPub failed: "+response.error);
    }
    return response;
  }

  public void initBip84(String xpub) throws Exception {
    String url = computeAuthUrl(urlBackend + URL_INIT_BIP84);
    if (log.isDebugEnabled()) {
      log.debug("initBip84");
    }
    Map<String,String> headers = computeHeaders();
    Map<String, String> postBody = new HashMap<String, String>();
    postBody.put("xpub", xpub);
    postBody.put("type", "new");
    postBody.put("segwit", "bip84");
    httpClient.postUrlEncoded(url, Void.class, headers, postBody);
  }

  public MinerFee fetchMinerFee() throws Exception {
    String url = computeAuthUrl(urlBackend + URL_MINER_FEES);
    Map<String,String> headers = computeHeaders();
    Map<String, Integer> feeResponse = httpClient.getJson(url, Map.class, headers);
    if (feeResponse == null) {
      throw new Exception("Invalid miner fee response from server");
    }
    return new MinerFee(feeResponse);
  }

  @Override
  public SeenResponse seen(Collection<String> addresses) throws Exception {
    String addressesStr = String.join("|", addresses);
    String url = computeAuthUrl(urlBackend + URL_SEEN + Util.encodeUrl(addressesStr));
    Map<String,String> headers = computeHeaders();
    Map<String, Boolean> seenResponse = httpClient.getJson(url, Map.class, headers);
    if (seenResponse == null) {
      throw new Exception("Invalid seen response from server");
    }
    return new SeenResponse(seenResponse);
  }

  @Override
  public boolean seen(String address) throws Exception {
    return seen(Arrays.asList(address)).isSeen(address);
  }

  public String pushTx(String txHex) throws Exception {
    return pushTx(txHex, null);
  }

  @Override
  public String pushTx(String txHex, Collection<Integer> strictModeVouts) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("pushTx... " + txHex);
    } else {
      log.info("pushTx...");
    }
    String url = computeAuthUrl(urlBackend + URL_PUSHTX);
    Map<String,String> headers = computeHeaders();
    Map<String, String> postBody = new HashMap<String, String>();
    postBody.put("tx", txHex);

    if(strictModeVouts != null && !strictModeVouts.isEmpty()) {
      String strStrictVouts = StringUtils.join(strictModeVouts,"|");
      postBody.put("strict_mode_vouts", strStrictVouts);
    }

    try {
      BackendPushTxResponse backendPushTxResponse = httpClient.postUrlEncoded(url, BackendPushTxResponse.class, headers, postBody);
      checkPushTxResponse(backendPushTxResponse);
      String txid = backendPushTxResponse.data;
      if (log.isDebugEnabled()) {
        log.debug("pushTx success: "+txid);
      }
      return txid;
    } catch (HttpResponseException e) {
      // parse pushTxResponse
      String responseBody = e.getResponseBody();
      BackendPushTxResponse backendPushTxResponse = null;
      try {
        backendPushTxResponse = JSONUtils.getInstance().getObjectMapper().readValue(responseBody, BackendPushTxResponse.class);
      } catch(Exception ee) {
        log.error("Not a PushTxResponse: "+responseBody);
      }
      if (backendPushTxResponse != null) {
        checkPushTxResponse(backendPushTxResponse); // throw PushTxException
      }
      throw e;
    }
  }

  // used by Android
  public static void checkPushTxResponse(BackendPushTxResponse backendPushTxResponse) throws Exception {
    if (backendPushTxResponse.status == BackendPushTxResponse.PushTxStatus.ok) {
      // success
      return;
    }
    log.error("pushTx failed: "+ backendPushTxResponse.toString());

    // address reuse
    if (backendPushTxResponse.isErrorAddressReuse()) {
      Collection<Integer> adressReuseOutputIndexs = backendPushTxResponse.getAdressReuseOutputIndexs();
      throw new BackendPushTxException("address-reuse", adressReuseOutputIndexs);
    }

    // other error
    throw new BackendPushTxException(backendPushTxResponse.error.toString());
  }

  public boolean testConnectivity() {
    try {
      fetchMinerFee();
      return true;
    } catch (Exception e) {
      log.error("", e);
      return false;
    }
  }

  protected Map<String,String> computeHeaders() throws Exception {
    Map<String,String> headers = new HashMap<String, String>();
    if (oAuthManager != null) {
      // add auth token
      headers.put("Authorization", "Bearer " + oAuthManager.getOAuthAccessToken());
    }
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

  public String getUrlBackend() {
    return urlBackend;
  }

}
