package com.samourai.wallet.client;

import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.constants.BIP_WALLET;
import com.samourai.wallet.test.AbstractTest;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BipWalletTest extends AbstractTest {
  private BipWallet bipWalletDeposit84;
  private BipWallet bipWalletDeposit49;
  private BipWallet bipWalletDeposit44;
  private BipWallet bipWalletPremix;
  private BipWallet bipWalletPostmix84;
  private BipWallet bipWalletBadbank;

  public BipWalletTest() throws Exception {
    super();
  }

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();

    bipWalletDeposit84 = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP84);
    bipWalletDeposit49 = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP49);
    bipWalletDeposit44 = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP44);
    bipWalletPremix = walletSupplier.getWallet(BIP_WALLET.PREMIX_BIP84);
    bipWalletPostmix84 = walletSupplier.getWallet(BIP_WALLET.POSTMIX_BIP84);
    bipWalletBadbank = walletSupplier.getWallet(BIP_WALLET.BADBANK_BIP84);
  }

  @Test
  public void getAddressAt() throws Exception {
    Assertions.assertEquals(
        "tb1qp4jqz890g3u30meeks68aeqyf7tdaeycyc6hd0", bipWalletDeposit84.getAddressAt(0, 0).getAddressString());
    Assertions.assertEquals(
        "tb1q7uef0jnnj2dnzguz438aeejpqhjk7z45ngd4ww", bipWalletDeposit84.getAddressAt(0, 15).getAddressString());
    Assertions.assertEquals(
        "tb1q765gfuv0f4l83fqk0sl9vaeu8tjcuqtyrrduyv", bipWalletDeposit84.getAddressAt(1, 0).getAddressString());
    Assertions.assertEquals(
        "tb1q80vm5fqpr4pmnje0ftqkmhfmeztm4ua08kry00", bipWalletDeposit84.getAddressAt(1, 15).getAddressString());
  }

  @Test
  public void getNextAddress() throws Exception {
    // receive
    Assertions.assertEquals("m/84'/1'/0'/0/0", bipWalletDeposit84.getNextAddressReceive().getPathAddress());
    Assertions.assertEquals("m/84'/1'/0'/0/1", bipWalletDeposit84.getNextAddressReceive().getPathAddress());
    Assertions.assertEquals("m/84'/1'/0'/0/2", bipWalletDeposit84.getNextAddressReceive(true).getPathAddress());
    Assertions.assertEquals("m/84'/1'/0'/0/3", bipWalletDeposit84.getNextAddressReceive(false).getPathAddress());
    Assertions.assertEquals("m/84'/1'/0'/0/3", bipWalletDeposit84.getNextAddressReceive(false).getPathAddress());

    // change
    Assertions.assertEquals("m/84'/1'/0'/1/0", bipWalletDeposit84.getNextAddressChange().getPathAddress());
    Assertions.assertEquals("m/84'/1'/0'/1/1", bipWalletDeposit84.getNextAddressChange().getPathAddress());
    Assertions.assertEquals("m/84'/1'/0'/1/2", bipWalletDeposit84.getNextAddressChange(true).getPathAddress());
    Assertions.assertEquals("m/84'/1'/0'/1/3", bipWalletDeposit84.getNextAddressChange(false).getPathAddress());
    Assertions.assertEquals("m/84'/1'/0'/1/3", bipWalletDeposit84.getNextAddressChange(false).getPathAddress());

    // postmix
    Assertions.assertEquals("m/84'/1'/2147483646'/0/0", bipWalletPostmix84.getNextAddressReceive().getPathAddress());
    Assertions.assertEquals("m/84'/1'/2147483646'/0/1", bipWalletPostmix84.getNextAddressReceive().getPathAddress());
    Assertions.assertEquals("m/84'/1'/2147483646'/0/2", bipWalletPostmix84.getNextAddressReceive(true).getPathAddress());
    Assertions.assertEquals("m/84'/1'/2147483646'/0/3", bipWalletPostmix84.getNextAddressReceive(false).getPathAddress());
    Assertions.assertEquals("m/84'/1'/2147483646'/0/3", bipWalletPostmix84.getNextAddressReceive(false).getPathAddress());

    // postmix wallets use same counter for any bipFormat
    Assertions.assertEquals("m/84'/1'/2147483646'/0/3", bipWalletPostmix84.getNextAddressReceive(BIP_FORMAT.SEGWIT_COMPAT).getPathAddress());
    Assertions.assertEquals("m/84'/1'/2147483646'/0/4", bipWalletPostmix84.getNextAddressReceive(BIP_FORMAT.LEGACY).getPathAddress());
    Assertions.assertEquals("m/84'/1'/2147483646'/0/5", bipWalletPostmix84.getNextAddressReceive(BIP_FORMAT.SEGWIT_NATIVE).getPathAddress());
    Assertions.assertEquals("m/84'/1'/2147483646'/0/6", bipWalletPostmix84.getNextAddressReceive(BIP_FORMAT.SEGWIT_NATIVE, false).getPathAddress());
    Assertions.assertEquals("m/84'/1'/2147483646'/0/6", bipWalletPostmix84.getNextAddressReceive(BIP_FORMAT.SEGWIT_NATIVE, false).getPathAddress());

    Assertions.assertEquals("mnda9Ff5QkBdJcUw3yeTrvW2FgCMrzVpfd", bipWalletPostmix84.getNextAddressReceive(BIP_FORMAT.LEGACY, false).getAddressString());
    Assertions.assertEquals("2MsTFsbkSfgxTF5JWaFqk7UfNcYq3Jh52Dr", bipWalletPostmix84.getNextAddressReceive(BIP_FORMAT.SEGWIT_COMPAT, false).getAddressString());
    Assertions.assertEquals("tb1qfcy56p45asmfyahp29vt6ccc2zh5yndlyjk9h0", bipWalletPostmix84.getNextAddressReceive(BIP_FORMAT.SEGWIT_NATIVE, false).getAddressString());
  }

  @Test
  public void getBipPub() throws Exception {
    Assertions.assertEquals(
        "vpub5YEQpEDPAZWVTkmWASSHyaUMsae7uV9FnRrhZ3cqV6RFbBQx7wjVsUfLqSE3hgNY8WQixurkbWNkfV2sRE7LPfNKQh2t3s5une4QZthwdCu",
        bipWalletDeposit84.getBipPub());
  }

  @Test
  public void getXPub() throws Exception {
    Assertions.assertEquals(
            "tpubDCGZwoNuBCYuS9LbHLzdbfzjYe2fn7dKAHVSUPTkb1vuSfi7hUuiG3eT7tE1DzdcjhBF5SZk3vuu8EkcFUnbsaBpCyB2uDP7v3n774RGre9",
            bipWalletDeposit84.getXPub());
  }

  @Test
  public void derivationGetPath() throws Exception {
    Assertions.assertEquals("m/84'/1'/0", bipWalletDeposit84.getDerivation().getPathAccount(params));
    Assertions.assertEquals("m/84'/1'/0'/0", bipWalletDeposit84.getDerivation().getPathChain(0, params));
    Assertions.assertEquals("m/84'/1'/0'/1", bipWalletDeposit84.getDerivation().getPathChain(1, params));

    NetworkParameters mainnetParams = MainNetParams.get();
    Assertions.assertEquals("m/84'/0'/0", bipWalletDeposit84.getDerivation().getPathAccount(mainnetParams));

    Assertions.assertEquals("m/44'/1'/0", bipWalletDeposit44.getDerivation().getPathAccount(params));
    Assertions.assertEquals("m/44'/1'/0'/2", bipWalletDeposit44.getDerivation().getPathChain(2, params));

    Assertions.assertEquals("m/49'/1'/0", bipWalletDeposit49.getDerivation().getPathAccount(params));
    Assertions.assertEquals("m/49'/1'/0'/2", bipWalletDeposit49.getDerivation().getPathChain(2, params));

    Assertions.assertEquals("m/84'/1'/2147483644", bipWalletBadbank.getDerivation().getPathAccount(params));
    Assertions.assertEquals("m/84'/1'/2147483644'/2", bipWalletBadbank.getDerivation().getPathChain(2, params));

    Assertions.assertEquals("m/84'/1'/2147483645", bipWalletPremix.getDerivation().getPathAccount(params));
    Assertions.assertEquals("m/84'/1'/2147483645'/2", bipWalletPremix.getDerivation().getPathChain(2, params));

    Assertions.assertEquals("m/84'/1'/2147483646", bipWalletPostmix84.getDerivation().getPathAccount(params));
    Assertions.assertEquals("m/84'/1'/2147483646'/2", bipWalletPostmix84.getDerivation().getPathChain(2, params));
  }
}
