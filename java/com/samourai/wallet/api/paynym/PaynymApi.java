package com.samourai.wallet.api.paynym;

import com.samourai.wallet.api.backend.IBackendClient;
import com.samourai.wallet.api.paynym.beans.*;
import com.samourai.wallet.bip47.BIP47UtilGeneric;
import com.samourai.wallet.bip47.rpc.BIP47Account;
import com.samourai.wallet.httpClient.HttpResponseException;
import com.samourai.wallet.httpClient.IHttpClient;
import com.samourai.wallet.util.JSONUtils;
import com.samourai.wallet.util.MessageSignUtilGeneric;
import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PaynymApi {
  private Logger log = LoggerFactory.getLogger(PaynymApi.class);

  private static final String URL_TOKEN = "/token";
  private static final String URL_CREATE = "/create";
  private static final String URL_CLAIM = "/claim";
  private static final String URL_ADD = "/nym/add";
  private static final String URL_NYM = "/nym";
  private static final String URL_FOLLOW = "/follow";
  private static final String URL_UNFOLLOW = "/unfollow";

  private IHttpClient httpClient;
  private String urlServer;
  private BIP47UtilGeneric bip47Util;

  public PaynymApi(IHttpClient httpClient, String urlServer, BIP47UtilGeneric bip47Util) {
    this.httpClient = httpClient;
    this.urlServer = urlServer;
    this.bip47Util = bip47Util;

    if (log.isDebugEnabled()) {
      log.debug("urlServer=" + urlServer);
    }
  }

  public Single<String> getToken(String paynymCode) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("getToken");
    }
    Map<String,String> headers = computeHeaders(null);
    String url = urlServer+URL_TOKEN;
    GetTokenRequest request = new GetTokenRequest(paynymCode);
    return httpClient.postJson(url, GetTokenResponse.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Single.error(responseError(throwable));
            })
            .map(
            responseOpt -> {
              GetTokenResponse response = responseOpt.get();
              if (StringUtils.isEmpty(response.token)) {
                throw new Exception("Invalid getToken response");
              }
              return response.token;
            }
    );
  }

  public Single<CreatePaynymResponse> createPaynym(String paynymCode) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("createPaynym");
    }
    Map<String,String> headers = computeHeaders(null);
    String url = urlServer+URL_CREATE;
    CreatePaynymRequest request = new CreatePaynymRequest(paynymCode);
    return httpClient.postJson(url, CreatePaynymResponse.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Single.error(responseError(throwable));
            })
            .map(responseOpt -> responseOpt.get());
  }

  public Single<ClaimPaynymResponse> claim(String paynymToken, BIP47Account bip47Account) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("claim");
    }
    Map<String,String> headers = computeHeaders(paynymToken);
    String url = urlServer+URL_CLAIM;
    String signature = computeSignature(bip47Account, paynymToken);
    ClaimPaynymRequest request = new ClaimPaynymRequest(signature);
    return httpClient.postJson(url, ClaimPaynymResponse.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Single.error(responseError(throwable));
            })
            .map(responseOpt -> responseOpt.get());
  }

  public Single<AddPaynymResponse> addPaynym(String paynymToken, BIP47Account bip47Account) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("addPaynym");
    }
    Map<String,String> headers = computeHeaders(paynymToken);
    String url = urlServer+URL_ADD;
    String nym = bip47Account.getPaymentCode().toString();
    String code = bip47Account.getPaymentCodeSamourai().toString();
    String signature = computeSignature(bip47Account, paynymToken);
    AddPaynymRequest request = new AddPaynymRequest(nym, code, signature);
    return httpClient.postJson(url, AddPaynymResponse.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Single.error(responseError(throwable));
            })
            .map(responseOpt -> responseOpt.get());
  }

  public Single<GetNymInfoResponse> getNymInfo(String paynymCode) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("getNym");
    }
    Map<String,String> headers = computeHeaders(null);
    String url = urlServer+URL_NYM;
    GetNymInfoRequest request = new GetNymInfoRequest(paynymCode);
    return httpClient.postJson(url, GetNymInfoResponse.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Single.error(responseError(throwable));
            })
            .map(responseOpt -> responseOpt.get());
  }

  public Single follow(String paynymToken, BIP47Account bip47Account, String paymentCodeTarget) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("follow");
    }
    Map<String,String> headers = computeHeaders(paynymToken);
    String url = urlServer+URL_FOLLOW;
    String signature = computeSignature(bip47Account, paynymToken);
    FollowPaynymRequest request = new FollowPaynymRequest(paymentCodeTarget, signature);
    return httpClient.postJson(url, Object.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Single.error(responseError(throwable));
            })
            .map(responseOpt -> responseOpt.get());
  }

  public Single unfollow(String paynymToken, BIP47Account bip47Account, String paymentCodeTarget) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("unfollow");
    }
    Map<String,String> headers = computeHeaders(paynymToken);
    String url = urlServer+URL_UNFOLLOW;
    String signature = computeSignature(bip47Account, paynymToken);
    UnfollowPaynymRequest request = new UnfollowPaynymRequest(paymentCodeTarget, signature);
    return httpClient.postJson(url, Object.class, headers, request)
            .onErrorResumeNext(throwable -> {
              return Single.error(responseError(throwable));
            })
            .map(responseOpt -> responseOpt.get());
  }

  protected String computeSignature(BIP47Account bip47Account, String payNymToken) {
    return MessageSignUtilGeneric.getInstance().signMessage(bip47Account.getNotificationAddress().getECKey(), payNymToken);
  }

  protected Throwable responseError(Throwable throwable) {
    if (throwable instanceof HttpResponseException) {
      // parse PaynymErrorResponse.message
      String responseBody = ((HttpResponseException) throwable).getResponseBody();
      try {
        PaynymErrorResponse paynymErrorResponse = JSONUtils.getInstance().getObjectMapper().readValue(responseBody, PaynymErrorResponse.class);
        return new Exception(paynymErrorResponse.message);
      } catch (Exception e) {
        // unexpected response
      }
    }
    return throwable;
  }

  protected Map<String,String> computeHeaders(String paynymToken) throws Exception {
    Map<String,String> headers = new HashMap<String, String>();
    if (paynymToken != null) {
      headers.put("auth-token", paynymToken);
    }
    return headers;
  }

  protected IBackendClient getHttpClient() {
    return httpClient;
  }

  public String getUrlServer() {
    return urlServer;
  }

}
