package com.samourai.wallet.test;

import com.samourai.http.client.JettyHttpClient;
import com.samourai.wallet.api.backend.BackendApi;
import com.samourai.wallet.api.backend.BackendServer;
import com.samourai.wallet.api.backend.beans.WalletResponse;
import com.samourai.wallet.bip47.BIP47UtilGeneric;
import com.samourai.wallet.bip47.rpc.BIP47Account;
import com.samourai.wallet.bip47.rpc.BIP47Wallet;
import com.samourai.wallet.bip47.rpc.PaymentCode;
import com.samourai.wallet.bip47.rpc.java.Bip47UtilJava;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.bipWallet.WalletSupplierImpl;
import com.samourai.wallet.chain.ChainSupplier;
import com.samourai.wallet.client.indexHandler.MemoryIndexHandlerSupplier;
import com.samourai.wallet.constants.BIP_WALLETS;
import com.samourai.wallet.constants.SamouraiNetwork;
import com.samourai.wallet.crypto.CryptoUtil;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.hd.HD_WalletFactoryGeneric;
import com.samourai.wallet.httpClient.HttpUsage;
import com.samourai.wallet.httpClient.IHttpClient;
import com.samourai.wallet.payload.PayloadUtilGeneric;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.beans.SpendType;
import com.samourai.wallet.send.provider.MockUtxoProvider;
import com.samourai.wallet.util.*;
import com.samourai.wallet.xmanagerClient.XManagerClient;
import com.samourai.xmanager.protocol.XManagerService;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AbstractTest {
  protected static final Logger log = LoggerFactory.getLogger(AbstractTest.class);

  protected static final String SEED_WORDS = "all all all all all all all all all all all all";
  protected static final String SEED_PASSPHRASE = "whirlpool";

  protected static final String ADDRESS_BIP44 = "muimRQFJKMJM1pTminJxiD5HrPgSu257tX";
  protected static final String ADDRESS_BIP49 = "2Mww8dCYPUpKHofjgcXcBCEGmniw9CoaiD2";
  protected static final String ADDRESS_BIP84 = "tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4";
  protected static final String ADDRESS_P2TR = "tb1pqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesf3hn0c";
  protected static final String ADDRESS_XMANAGER = "tb1q6m3urxjc8j2l8fltqj93jarmzn0975nnxuymnx";

  protected NetworkParameters params = TestNet3Params.get();
  protected SamouraiNetwork samouraiNetwork = SamouraiNetwork.TESTNET;
  protected HD_WalletFactoryGeneric hdWalletFactory = HD_WalletFactoryGeneric.getInstance();
  protected IHttpClient httpClient;
  protected BipFormatSupplier bipFormatSupplier = BIP_FORMAT.PROVIDER;
  protected CryptoTestUtil cryptoTestUtil = CryptoTestUtil.getInstance();
  protected CryptoUtil cryptoUtil = CryptoUtil.getInstanceJava();
  protected AsyncUtil asyncUtil = AsyncUtil.getInstance();
  protected ThreadUtil threadUtil = ThreadUtil.getInstance();

  protected ChainSupplier mockChainSupplier = () -> {
    WalletResponse.InfoBlock infoBlock = new WalletResponse.InfoBlock();
    infoBlock.height = 1234;
    return infoBlock;
  };
  protected PayloadUtilGeneric payloadUtil = PayloadUtilGeneric.getInstance();
  protected WalletSupplierImpl walletSupplier;
  protected HD_Wallet bip44w;
  protected BIP47Wallet bip47Wallet;

  protected BIP47Wallet bip47WalletInitiator;
  protected BIP47Account bip47AccountInitiator;
  protected BIP47Wallet bip47WalletCounterparty;
  protected BIP47Account bip47AccountCounterparty;
  protected PaymentCode paymentCodeInitiator;
  protected PaymentCode paymentCodeCounterparty;

  protected BackendApi backendApi;
  protected MockUtxoProvider utxoProvider;
  protected XManagerClient xManagerClient;
  protected BIP47UtilGeneric bip47Util = Bip47UtilJava.getInstance();
  protected MockPushTx pushTx = new MockPushTx(params);
  protected Z85 z85 = Z85.getInstance();

  public AbstractTest() {
    try {
      setUp();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  public void setUp() throws Exception {
    RandomUtil._setTestMode(true);
    httpClient = new JettyHttpClient(10000, HttpUsage.BACKEND);

    byte[] seed = hdWalletFactory.computeSeedFromWords(SEED_WORDS);
    bip44w = hdWalletFactory.getBIP44(seed, SEED_PASSPHRASE, params);
    bip47Wallet = new BIP47Wallet(bip44w);

    bip47WalletInitiator = bip47Wallet;
    bip47WalletCounterparty = new BIP47Wallet(hdWalletFactory.getBIP44(seed, SEED_PASSPHRASE+"counterparty", params));
    bip47AccountInitiator = bip47WalletInitiator.getAccount(0);
    bip47AccountCounterparty = bip47WalletCounterparty.getAccount(0);
    paymentCodeInitiator = bip47AccountInitiator.getPaymentCode();
    paymentCodeCounterparty = bip47AccountCounterparty.getPaymentCode();

    walletSupplier = new WalletSupplierImpl(bipFormatSupplier, new MemoryIndexHandlerSupplier(), bip44w, BIP_WALLETS.SWAPS);
    utxoProvider = new MockUtxoProvider(bip44w.getParams(), walletSupplier);
    xManagerClient = new XManagerClient(httpClient, true, false) {
      @Override
      public String getAddressOrDefault(XManagerService service) {
        return ADDRESS_XMANAGER; // mock for reproductible tests
      }
    };

    backendApi = computeBackendApi(params);

    setLogLevel(Level.DEBUG);
  }

  protected static void setLogLevel(Level logLevel) {
    LogbackUtils.setLogLevel("com.samourai", logLevel.toString());

    LogbackUtils.setLogLevel("com.samourai.whirlpool.client", logLevel.toString());
    LogbackUtils.setLogLevel("com.samourai.whirlpool.client.mix.dialog", logLevel.toString());
    LogbackUtils.setLogLevel("com.samourai.stomp.client", logLevel.toString());
    LogbackUtils.setLogLevel("com.samourai.wallet.util.FeeUtil", logLevel.toString());

    LogbackUtils.setLogLevel("com.samourai.whirlpool.client.utils", logLevel.toString());
    LogbackUtils.setLogLevel("com.samourai.whirlpool.client.wallet", logLevel.toString());

    LogbackUtils.setLogLevel("com.samourai.wallet.cahoots", logLevel.toString());
    LogbackUtils.setLogLevel("com.samourai.wallet.cahoots.stowaway", logLevel.toString());
    LogbackUtils.setLogLevel("com.samourai.wallet.cahoots.stonewallx2", logLevel.toString());
    LogbackUtils.setLogLevel("com.samourai.wallet.sorobanClient", logLevel.toString());
    LogbackUtils.setLogLevel("com.samourai.soroban.client", logLevel.toString());
    LogbackUtils.setLogLevel("com.samourai.soroban.client.rpc", logLevel.toString());
    LogbackUtils.setLogLevel("com.samourai.soroban.client.dialog", logLevel.toString());
    LogbackUtils.setLogLevel("com.samourai.soroban.client.meeting", logLevel.toString());

    LogbackUtils.setLogLevel(
            "com.samourai.whirlpool.client.wallet.orchestrator", logLevel.toString());

    // skip noisy logs
    LogbackUtils.setLogLevel("org.bitcoinj", org.slf4j.event.Level.ERROR.toString());
    LogbackUtils.setLogLevel(
            "org.bitcoin", org.slf4j.event.Level.WARN.toString()); // "no wallycore"
    LogbackUtils.setLogLevel("org.eclipse.jetty", org.slf4j.event.Level.INFO.toString());
  }

  protected BackendApi computeBackendApi(NetworkParameters params) {
    boolean testnet = FormatsUtilGeneric.getInstance().isTestNet(params);
    return BackendApi.newBackendApiSamourai(httpClient, BackendServer.get(testnet).getBackendUrl(false));
  }

  protected Transaction computeTxCoinbase(long value, Script outputScript) {
    Transaction tx = new Transaction(params);

    // add output
    tx.addOutput(Coin.valueOf(value), outputScript);

    // add input: coinbase
    int txCounter = 1;
    TransactionInput input =
            new TransactionInput(
                    params, tx, new byte[] {(byte) txCounter, (byte) (txCounter++ >> 8)});
    tx.addInput(input);

    tx.verify();
    return tx;
  }

  protected Transaction computeSpendTx(TransactionOutput txOutput) {
    // spend coinbase
    TransactionOutPoint inputOutPoint = txOutput.getOutPointFor();
    inputOutPoint.setValue(txOutput.getValue());

    Transaction tx = new Transaction(params);

    // add input
    TransactionInput txInput = new TransactionInput(params, null, new byte[0], inputOutPoint, inputOutPoint.getValue());
    tx.addInput(txInput);
    return tx;
  }

  protected void verifyTx(Transaction tx, String txid, String raw, Map<String,Long> outputsExpected) throws Exception {
    if (log.isDebugEnabled()) {
      log.debug(tx.toString());
    }

    Map<String,Long> outputsActuals = new LinkedHashMap<>();
    for (TransactionOutput txOutput : tx.getOutputs()) {
      if (!txOutput.getScriptPubKey().isOpReturn()) {
        String address = bipFormatSupplier.getToAddress(txOutput);
        outputsActuals.put(address, txOutput.getValue().getValue());
      }
    }
    // sort by value ASC to comply with UTXOComparator
    outputsActuals = sortMapOutputs(outputsActuals);
    outputsExpected = sortMapOutputs(outputsExpected);
    if (log.isDebugEnabled()) {
      log.debug("outputsActuals: "+outputsActuals);
    }
    Assertions.assertEquals(outputsExpected, outputsActuals);

    Assertions.assertEquals(txid, tx.getHashAsString());
    Assertions.assertEquals(raw, TxUtil.getInstance().getTxHex(tx));
  }

  protected Map<String,Long> sortMapOutputs(Map<String,Long> map) {
    return map.entrySet().stream().sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  protected void verifySpendTx(SpendTx spendTx, SpendType spendType, Collection<com.samourai.wallet.send.UTXO> utxos, long minerFeeTotal, long minerFeePaid, long samouraiFee, long amount, boolean entireBalanceExpected, long change) throws Exception {
    Assertions.assertEquals(spendType, spendTx.getSpendType());
    assertEquals(utxos, spendTx.getSpendFrom());
    Assertions.assertEquals(minerFeeTotal, spendTx.getMinerFeeTotal());
    Assertions.assertEquals(minerFeePaid, spendTx.getMinerFeePaid());
    Assertions.assertEquals(samouraiFee, spendTx.getSamouraiFee());
    Assertions.assertEquals(amount, spendTx.getAmount());
    Assertions.assertEquals(entireBalanceExpected, spendTx.isEntireBalance());
    Assertions.assertEquals(change, spendTx.getChange());
  }

  protected void assertEquals(Collection<UTXO> utxos1, Collection<? extends TransactionOutPoint> utxos2) {
    Function<TransactionOutPoint,String> outPointToString = outPoint -> {
      return outPoint.getHash().toString()+"-"+outPoint.getIndex();
    };
    Collection<String> utxos1Str = UTXO.listOutpoints(utxos1).stream().map(outPointToString).collect(Collectors.toList());
    Collection<String> utxos2Str = utxos2.stream().map(outPointToString).collect(Collectors.toList());
    Assertions.assertEquals(utxos1Str, utxos2Str);
  }
}
