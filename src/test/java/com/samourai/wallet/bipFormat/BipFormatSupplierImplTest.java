package com.samourai.wallet.bipFormat;

import com.samourai.wallet.test.AbstractTest;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.MainNetParams;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BipFormatSupplierImplTest extends AbstractTest {

  private BipFormatSupplierImpl bipFormatSupplier;
  private NetworkParameters mainNetParams = MainNetParams.get();

  public BipFormatSupplierImplTest() throws Exception {
    bipFormatSupplier = (BipFormatSupplierImpl)BIP_FORMAT.PROVIDER;
  }

  @Test
  public void findByAddress() throws Exception {
    Assertions.assertEquals(BIP_FORMAT.LEGACY, bipFormatSupplier.findByAddress("mn9QhsFiX2eEXtF6zrGn5N49iS8BHXFjBt", params));
    Assertions.assertEquals(BIP_FORMAT.LEGACY, bipFormatSupplier.findByAddress("17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem", mainNetParams));

    Assertions.assertEquals(BIP_FORMAT.SEGWIT_COMPAT, bipFormatSupplier.findByAddress("2N8hwP1WmJrFF5QWABn38y63uYLhnJYJYTF", params));
    Assertions.assertEquals(BIP_FORMAT.SEGWIT_COMPAT, bipFormatSupplier.findByAddress("3EktnHQD7RiAE6uzMj2ZifT9YgRrkSgzQX", mainNetParams));

    Assertions.assertEquals(BIP_FORMAT.SEGWIT_NATIVE, bipFormatSupplier.findByAddress("tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4", params));
    Assertions.assertEquals(BIP_FORMAT.SEGWIT_NATIVE, bipFormatSupplier.findByAddress("tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7", params));
    Assertions.assertEquals(BIP_FORMAT.SEGWIT_NATIVE, bipFormatSupplier.findByAddress("tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy", params));
    Assertions.assertEquals(BIP_FORMAT.SEGWIT_NATIVE, bipFormatSupplier.findByAddress("bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4", mainNetParams));
    Assertions.assertEquals(BIP_FORMAT.SEGWIT_NATIVE, bipFormatSupplier.findByAddress("bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq", mainNetParams));
    Assertions.assertEquals(BIP_FORMAT.SEGWIT_NATIVE, bipFormatSupplier.findByAddress("bc1qc7slrfxkknqcq2jevvvkdgvrt8080852dfjewde450xdlk4ugp7szw5tk9", mainNetParams));

    Assertions.assertEquals(BIP_FORMAT.TAPROOT, bipFormatSupplier.findByAddress("tb1pqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesf3hn0c", params));
    Assertions.assertEquals(BIP_FORMAT.TAPROOT, bipFormatSupplier.findByAddress("bc1p8denc9m4sqe9hluasrvxkkdqgkydrk5ctxre5nkk4qwdvefn0sdsc6eqxe", mainNetParams));
  }

  @Test
  public void getList() throws Exception {
    Assertions.assertEquals(4, bipFormatSupplier.getList().size());
  }

  @Test
  public void findById() throws Exception {
    Assertions.assertEquals(BIP_FORMAT.LEGACY, bipFormatSupplier.findById(BIP_FORMAT.LEGACY.getId()));
    Assertions.assertEquals(BIP_FORMAT.SEGWIT_COMPAT, bipFormatSupplier.findById(BIP_FORMAT.SEGWIT_COMPAT.getId()));
    Assertions.assertEquals(BIP_FORMAT.SEGWIT_NATIVE, bipFormatSupplier.findById(BIP_FORMAT.SEGWIT_NATIVE.getId()));
    Assertions.assertEquals(BIP_FORMAT.TAPROOT, bipFormatSupplier.findById(BIP_FORMAT.TAPROOT.getId()));
  }

  @Test
  public void getTransactionOutput_and_getToAddress() throws Exception {
    // LEGACY
    String address = "mn9QhsFiX2eEXtF6zrGn5N49iS8BHXFjBt";
    TransactionOutput txOutput = bipFormatSupplier.getTransactionOutput(address, 4000, params);
    Assertions.assertEquals(address, bipFormatSupplier.getToAddress(txOutput));

    address = "17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem";
    txOutput = bipFormatSupplier.getTransactionOutput(address, 4000, mainNetParams);
    Assertions.assertEquals(address, bipFormatSupplier.getToAddress(txOutput));

    // SEGWIT_COMPAT
    address = "2N8hwP1WmJrFF5QWABn38y63uYLhnJYJYTF";
    txOutput = bipFormatSupplier.getTransactionOutput(address, 4000, params);
    Assertions.assertEquals(address, bipFormatSupplier.getToAddress(txOutput));

    address = "3EktnHQD7RiAE6uzMj2ZifT9YgRrkSgzQX";
    txOutput = bipFormatSupplier.getTransactionOutput(address, 4000, mainNetParams);
    Assertions.assertEquals(address, bipFormatSupplier.getToAddress(txOutput));

    // SEGWIT_NATIVE
    address = "tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4";
    txOutput = bipFormatSupplier.getTransactionOutput(address, 4000, params);
    Assertions.assertEquals(address, bipFormatSupplier.getToAddress(txOutput));

    address = "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4";
    txOutput = bipFormatSupplier.getTransactionOutput(address, 4000, mainNetParams);
    Assertions.assertEquals(address, bipFormatSupplier.getToAddress(txOutput));

    // P2TR
    address = "tb1pqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesf3hn0c";
    txOutput = bipFormatSupplier.getTransactionOutput(address, 4000, params);
    Assertions.assertEquals(address, bipFormatSupplier.getToAddress(txOutput));

    address = "bc1p8denc9m4sqe9hluasrvxkkdqgkydrk5ctxre5nkk4qwdvefn0sdsc6eqxe";
    txOutput = bipFormatSupplier.getTransactionOutput(address, 4000, mainNetParams);
    Assertions.assertEquals(address, bipFormatSupplier.getToAddress(txOutput));
  }

  @Test
  public void getToAddress() throws Exception {
    ECKey ecKey = ECKey.fromPublicOnly(Hex.decode("03cc8a4bc64d897bddc5fbc2f670f7a8ba0b386779106cf1223c6fc5d7cd6fc115"));

    // LEGACY
    String address = BIP_FORMAT.LEGACY.getToAddress(ecKey, params);
    Assertions.assertEquals("n3PFj7J96N3uTKZfcRYHggxfd1eAKgDLjX", address);

    address = BIP_FORMAT.LEGACY.getToAddress(ecKey, mainNetParams);
    Assertions.assertEquals("1NsJS4DAHLcegD63trZurmkLm23TRAujXd", address);

    // SEGWIT_COMPAT
    address = BIP_FORMAT.SEGWIT_COMPAT.getToAddress(ecKey, params);
    Assertions.assertEquals("2N1Mdnn2XCeoyxkmVYWpKhCLfamgeXJmrYV", address);

    address = BIP_FORMAT.SEGWIT_COMPAT.getToAddress(ecKey, mainNetParams);
    Assertions.assertEquals("39oRj36VbCJdky8wsPCT5FMQNRUUekuoUr", address);

    // SEGWIT_NATIVE
    address = BIP_FORMAT.SEGWIT_NATIVE.getToAddress(ecKey, params);
    Assertions.assertEquals("tb1qalwlmdxd2ggue4290ekzxl9tetg56neev9pwqa", address);

    address = BIP_FORMAT.SEGWIT_NATIVE.getToAddress(ecKey, mainNetParams);
    Assertions.assertEquals("bc1qalwlmdxd2ggue4290ekzxl9tetg56neexr6amw", address);

    // P2TR
    address = BIP_FORMAT.TAPROOT.getToAddress(ecKey, params);
    Assertions.assertEquals("tb1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqp3mvzv", address);

    address = BIP_FORMAT.TAPROOT.getToAddress(ecKey, mainNetParams);
    Assertions.assertEquals("bc1p5cyxnuxmeuwuvkwfem96lqzszd02n6xdcjrs20cac6yqjjwudpxqkedrcr", address);
  }
}
