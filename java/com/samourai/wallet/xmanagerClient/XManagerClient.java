package com.samourai.wallet.xmanagerClient;

import com.samourai.wallet.httpClient.IHttpClient;
import com.samourai.wallet.util.AsyncUtil;
import com.samourai.wallet.util.FormatsUtilGeneric;
import com.samourai.xmanager.protocol.XManagerEnv;
import com.samourai.xmanager.protocol.XManagerProtocol;
import com.samourai.xmanager.protocol.XManagerService;
import com.samourai.xmanager.protocol.rest.*;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class XManagerClient {
  private static final Logger log = LoggerFactory.getLogger(XManagerClient.class);
  private static final XManagerProtocol protocol = XManagerProtocol.getInstance();
  private static final FormatsUtilGeneric formatUtils = FormatsUtilGeneric.getInstance();
  private static final AsyncUtil asyncUtil = AsyncUtil.getInstance();

  private String serverUrl;
  private boolean testnet;
  private IHttpClient httpClient;

  public XManagerClient(IHttpClient httpClient, boolean testnet, boolean onion) {
    this(httpClient, testnet, XManagerEnv.get(testnet).getUrl(onion));
  }

  public XManagerClient(IHttpClient httpClient, boolean testnet, String serverUrl) {
    this.serverUrl = serverUrl;
    this.testnet = testnet;
    this.httpClient = httpClient;
  }

  public Single<Optional<AddressResponse>> getAddressResponse(XManagerService service) {
    String url = protocol.getUrlAddress(serverUrl);
    AddressRequest request = new AddressRequest(service.name());
    return httpClient.postJson(url, AddressResponse.class, null, request);
  }

  public String getAddressOrDefault(XManagerService service) {
    String address = null;
    try {
      Single<Optional<AddressResponse>> responseObservable = getAddressResponse(service);
      address = asyncUtil.blockingGet(responseObservable).get().address;
    } catch (Exception e) {
      log.error("getAddressResponse(" + service.name() + ") failed", e);
    }
    if (address == null || !formatUtils.isValidBech32(address)) {
      log.error(
          "getAddressResponse("
              + service.name()
              + "): invalid response (address="
              + (address != null ? address : "null")
              + ") => using default address");
      address = service.getDefaultAddress(testnet);
    }
    return address;
  }

  public String getAddressOrDefault(XManagerService service, int attempts) {
    String defaultAddress = service.getDefaultAddress(testnet);
    String receiveAddress = defaultAddress;
    int tries = attempts;
    while (tries > 0 && receiveAddress.equals(defaultAddress)) {
      receiveAddress = getAddressOrDefault(service);
      tries--;
    }
    return receiveAddress;
  }

  public Single<Optional<AddressIndexResponse>> getAddressIndexResponse(
      XManagerService service) {
    String url = protocol.getUrlAddressIndex(serverUrl);
    AddressIndexRequest request = new AddressIndexRequest(service.name());
    return httpClient.postJson(url, AddressIndexResponse.class, null, request);
  }

  public AddressIndexResponse getAddressIndexOrDefault(XManagerService service) {
    AddressIndexResponse response = null;
    try {
      Single<Optional<AddressIndexResponse>> responseObservable =
          getAddressIndexResponse(service);
      response = asyncUtil.blockingGet(responseObservable).get();
    } catch (Exception e) {
      log.error("getAddressIndexResponse(" + service.name() + ") failed", e);
    }
    if (response == null || !formatUtils.isValidBech32(response.address) || response.index < 0) {
      String addressStr = response != null && response.address != null ? response.address : "null";
      String indexStr = response != null ? Integer.toString(response.index) : "null";
      log.error(
          "getAddressIndexResponse("
              + service.name()
              + "): invalid response (address="
              + addressStr
              + " index="
              + indexStr
              + ") => using default address");
      String defaultAaddress = service.getDefaultAddress(testnet);
      response = new AddressIndexResponse(defaultAaddress, 0);
    }
    return response;
  }

  public Single<Optional<VerifyAddressIndexResponse>> verifyAddressIndexResponseAsync(
      XManagerService service, String address, int index) {
    String url = protocol.getUrlVerifyAddressIndex(serverUrl);
    VerifyAddressIndexRequest request =
        new VerifyAddressIndexRequest(service.name(), address, index);
    return httpClient.postJson(url, VerifyAddressIndexResponse.class, null, request);
  }

  public boolean verifyAddressIndexResponse(XManagerService service, String address, int index)
      throws Exception {
    try {
      Single<Optional<VerifyAddressIndexResponse>> responseObservable =
          verifyAddressIndexResponseAsync(service, address, index);
      return asyncUtil.blockingGet(responseObservable).get().valid;
    } catch (Exception e) {
      log.error("verifyAddressIndexResponse(" + service.name() + ") failed", e);
      throw e;
    }
  }
}
