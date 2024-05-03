package com.samourai.wallet.bech32;

import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.wallet.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Bech32UtilGenericTest extends AbstractTest {
  private final Bech32UtilGeneric bech32Util = Bech32UtilGeneric.getInstance();

  @Test
  public void computeScriptPubKey() throws Exception {
    Assertions.assertEquals("02d6iJ6ppe[khil)k?ihOx%$yB5-", z85.encode(bech32Util.computeScriptPubKey(ADDRESS_BIP84, params)));

    try {
      z85.encode(bech32Util.computeScriptPubKey(null, params));
      Assertions.fail();
    } catch (Exception e) {
      Assertions.assertEquals("Bech32Segwit.decode() failed for address=null", e.getMessage());
    }

    try {
      z85.encode(bech32Util.computeScriptPubKey("foo", params));
      Assertions.fail();
    } catch (Exception e) {
      Assertions.assertEquals("Bech32Segwit.decode() failed for address=foo", e.getMessage());
    }
  }
}
