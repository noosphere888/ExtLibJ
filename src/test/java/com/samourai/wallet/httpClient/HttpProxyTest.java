package com.samourai.wallet.httpClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpProxyTest {

  @Test
  public void testValidate() throws Exception {
    // valid
    Assertions.assertTrue(HttpProxy.validate("http://localhost:8080"));
    Assertions.assertTrue(HttpProxy.validate("socks://localhost:9050"));

    // invalid
    Assertions.assertFalse(HttpProxy.validate("foo://localhost:9050")); // invalid protocol
    Assertions.assertFalse(HttpProxy.validate("http://localhost")); // missing port
    Assertions.assertFalse(HttpProxy.validate("localhost:8080")); // missing protocol
  }
}
