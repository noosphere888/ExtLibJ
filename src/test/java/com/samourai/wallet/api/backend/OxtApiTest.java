package com.samourai.wallet.api.backend;

import com.samourai.wallet.api.backend.beans.HttpException;
import com.samourai.wallet.api.backend.seenBackend.ISeenBackend;
import com.samourai.wallet.api.backend.seenBackend.SeenBackendWithFallback;
import com.samourai.wallet.api.backend.seenBackend.SeenResponse;
import com.samourai.wallet.test.AbstractTest;
import org.bitcoinj.params.MainNetParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class OxtApiTest extends AbstractTest {
  private OxtApi oxtApi;

  public OxtApiTest() throws Exception {
    super();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    oxtApi = new OxtApi(httpClient);
  }

  @Test
  public void seen() throws Exception {
    String ADDRESS1 = "1Nv54Mqt5C9Yj8vMe8uQXa5LbTa3tKLBL3";
    String ADDRESS2 = "14fzVjPe3MhTqCiYXRTj5jqkQ3YbvUeHQJ";
    String ADDRESS3 = "bc1pwn8qezw8hd59tkcthu8repq88prcvg89yw4deqj9u3q8j5hr7e0qene308";
    String ADDRESS4 = "156pZUx3rnSePoVjePp73MBTHAffyySLs5";
    String ADDRESS5 = "14acNoSPmNYTx2eGDACWTAJsr9VYjC7sQw";

    // single
    Assertions.assertTrue(oxtApi.seen(ADDRESS1));
    Assertions.assertTrue(oxtApi.seen(ADDRESS2));
    Assertions.assertTrue(oxtApi.seen(ADDRESS3));
    Assertions.assertFalse(oxtApi.seen(ADDRESS4));
    Assertions.assertFalse(oxtApi.seen(ADDRESS5));
    Assertions.assertFalse(oxtApi.seen("unknown"));

    // multi
    SeenResponse seenResponse = oxtApi.seen(Arrays.asList(ADDRESS1, ADDRESS2, ADDRESS3, ADDRESS4, ADDRESS5));
    Assertions.assertTrue(seenResponse.isSeen(ADDRESS1));
    Assertions.assertTrue(seenResponse.isSeen(ADDRESS2));
    Assertions.assertTrue(seenResponse.isSeen(ADDRESS3));
    Assertions.assertFalse(seenResponse.isSeen(ADDRESS4));
    Assertions.assertFalse(seenResponse.isSeen(ADDRESS5));
    Assertions.assertFalse(seenResponse.isSeen("unknown"));
  }

  @Test
  public void withOxtFallback() throws Exception {
    ISeenBackend failingBackend = new BackendApi(httpClient, "http://127.0.0.1/invalid", null);
    ISeenBackend seenBackend = SeenBackendWithFallback.withOxt(failingBackend, MainNetParams.get());

    String ADDRESS1 = "1Nv54Mqt5C9Yj8vMe8uQXa5LbTa3tKLBL3";

    // failingBackend fails
    Assertions.assertThrows(HttpException.class, () -> {
      failingBackend.seen(ADDRESS1);
    });

    // backend with fallback works through OXT
    Assertions.assertTrue(seenBackend.seen(ADDRESS1));
  }
}
