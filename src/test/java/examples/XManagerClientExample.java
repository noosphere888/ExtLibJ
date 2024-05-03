package examples;

import com.samourai.wallet.httpClient.IHttpClient;
import com.samourai.wallet.xmanagerClient.XManagerClient;
import com.samourai.xmanager.protocol.XManagerEnv;
import com.samourai.xmanager.protocol.XManagerService;
import com.samourai.xmanager.protocol.rest.AddressIndexResponse;

public class XManagerClientExample {
  public void example() {
    // configuration
    boolean testnet = true;
    boolean onion = false;
    IHttpClient httpClient = null; // TODO provide AndroidHttpClient or CliHttpClient

    // instantiation
    XManagerClient xManagerClient = new XManagerClient(httpClient, testnet, onion);
    XManagerEnv xManagerEnv = XManagerEnv.get(testnet);

    // get address (or default when server unavailable)
    String address = xManagerClient.getAddressOrDefault(XManagerService.RICOCHET);

    // get address + index
    AddressIndexResponse addressIndexResponse =
        xManagerClient.getAddressIndexOrDefault(XManagerService.RICOCHET);
    System.out.println(
        "address=" + addressIndexResponse.address + ", index=" + addressIndexResponse.index);

    // validate address + index
    String addressToValidate = "...";
    int indexToValidate = 0;
    try {
      boolean valid =
          xManagerClient.verifyAddressIndexResponse(
              XManagerService.RICOCHET, addressToValidate, indexToValidate);
    } catch (Exception e) {
      // server not available
    }
  }
}
