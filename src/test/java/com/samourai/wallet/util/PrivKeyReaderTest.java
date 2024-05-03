package com.samourai.wallet.util;

import com.samourai.wallet.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class PrivKeyReaderTest extends AbstractTest {

  @Test
  public void hex() throws Exception {
    String pk = "064f8f0bebfa2f65db003b56bc911535614f2764799bc89091398c1aed82e884";
    PrivKeyReader privKeyReader = new PrivKeyReader(pk, params);

    Assertions.assertEquals(PrivKeyReader.HEX_UNCOMPRESSED, privKeyReader.getFormat());
    Assertions.assertEquals("064f8f0bebfa2f65db003b56bc911535614f2764799bc89091398c1aed82e884", privKeyReader.getKey().getPrivateKeyAsHex());
    Assertions.assertEquals("91dhN38UTmqGtd3zG1GnDdnyivAP5LnWJQyyj7V7pqthirHAj4X", privKeyReader.getKey().getPrivateKeyAsWiF(params));
    Assertions.assertEquals(false, privKeyReader.getKey().isCompressed());
    Assertions.assertEquals(new BigInteger("2854445280755403823944422649848886010716442579975080723501674454330739189892"), privKeyReader.getKey().getPrivKey());
  }

  @Test
  public void wif_uncompressed() throws Exception {
    String pk = "91dhN38UTmqGtd3zG1GnDdnyivAP5LnWJQyyj7V7pqthirHAj4X";
    PrivKeyReader privKeyReader = new PrivKeyReader(pk, params);

    Assertions.assertEquals(PrivKeyReader.WIF_UNCOMPRESSED, privKeyReader.getFormat());
    Assertions.assertEquals("064f8f0bebfa2f65db003b56bc911535614f2764799bc89091398c1aed82e884", privKeyReader.getKey().getPrivateKeyAsHex());
    Assertions.assertEquals("91dhN38UTmqGtd3zG1GnDdnyivAP5LnWJQyyj7V7pqthirHAj4X", privKeyReader.getKey().getPrivateKeyAsWiF(params));
    Assertions.assertEquals(false, privKeyReader.getKey().isCompressed());
    Assertions.assertEquals(new BigInteger("2854445280755403823944422649848886010716442579975080723501674454330739189892"), privKeyReader.getKey().getPrivKey());
  }

  @Test
  public void wif_compressed() throws Exception {
    String pk = "cMny9rPzDAt58r8BjECeamPwN1eQSAKrKrrVNsd78AoCjcWxuVym";
    PrivKeyReader privKeyReader = new PrivKeyReader(pk, params);

    Assertions.assertEquals(PrivKeyReader.WIF_COMPRESSED, privKeyReader.getFormat());
    Assertions.assertEquals("064f8f0bebfa2f65db003b56bc911535614f2764799bc89091398c1aed82e884", privKeyReader.getKey().getPrivateKeyAsHex());
    Assertions.assertEquals("cMny9rPzDAt58r8BjECeamPwN1eQSAKrKrrVNsd78AoCjcWxuVym", privKeyReader.getKey().getPrivateKeyAsWiF(params));
    Assertions.assertEquals(true, privKeyReader.getKey().isCompressed());
    Assertions.assertEquals(new BigInteger("2854445280755403823944422649848886010716442579975080723501674454330739189892"), privKeyReader.getKey().getPrivKey());
  }
}
