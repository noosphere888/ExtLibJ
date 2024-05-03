package com.samourai.wallet.api.backend;

import com.samourai.wallet.api.backend.beans.*;
import com.samourai.wallet.api.backend.seenBackend.SeenResponse;
import com.samourai.wallet.segwit.SegwitAddress;
import com.samourai.wallet.send.SendFactoryGeneric;
import com.samourai.wallet.test.AbstractTest;
import com.samourai.wallet.util.TxUtil;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;

public class BackendApiTest extends AbstractTest {

  private static final String VPUB_1 =
          "vpub5SLqN2bLY4WeYBwMrtdanr5SfhRC7AyW1aEwbtVbt7t9y6kgBCS6ajVA4LL7Jy2iojpH1hjaiTMp5h4y9dG2dC64bAk9ZwuFCX6AvxFddaa";
  private static final String VPUB_2 =
          "vpub5b14oTd3mpWGzbxkqgaESn4Pq1MkbLbzvWZju8Y6LiqsN9JXX7ZzvdCp1qDDxLqeHGr6BUssz2yFmUDm5Fp9jTdz4madyxK6mwgsCvYdK5S";

  private ECKey inputKey = ECKey.fromPrivate(new BigInteger("45292090369707310635285627500870691371399357286012942906204494584441273561412"));
  private ECKey outputKey = ECKey.fromPrivate(new BigInteger("77292090369707310635285627500870691371399357286012942906204494584441273561412"));

  public BackendApiTest() throws Exception {
    super();
  }

  @Test
  public void initBip84() throws Exception {
    backendApi.initBip84(VPUB_1);
  }

  @Disabled
  @Test
  public void fetchAddress() throws Exception {
    String zpub = VPUB_1;
    MultiAddrResponse.Address address = backendApi.fetchAddress(zpub);
    assertAddressEquals(address, zpub, 63, 7, 0);
  }

  @Test
  public void fetchAddresses() throws Exception {
    String zpubs[] = {VPUB_1, VPUB_2};
    Map<String, MultiAddrResponse.Address> addresses = backendApi.fetchAddresses(zpubs);

    for (String zpub : zpubs) {
      Assertions.assertTrue(addresses.containsKey(zpub));
    }
    assertAddressEquals(addresses.get(VPUB_1), VPUB_1, 2, 0, 0);
    assertAddressEquals(addresses.get(VPUB_2), VPUB_2, 0, 0, 0);
  }

  @Test
  public void fetchUtxos() throws Exception {
    String zpub = VPUB_1;
    List<UnspentOutput> unspentOutputs = Arrays.asList(backendApi.fetchWallet(zpub).unspent_outputs);
    Assertions.assertEquals(0, unspentOutputs.size());
  }

  @Test
  public void fetchUtxosMulti() throws Exception {
    String[] zpubs = new String[] {VPUB_1, VPUB_2};
    List<UnspentOutput> unspentOutputs = Arrays.asList(backendApi.fetchWallet(zpubs).unspent_outputs);
    Assertions.assertEquals(0, unspentOutputs.size());
  }

  @Test
  public void fetchWallet_single() throws Exception {
    String[] zpubs = new String[] {VPUB_1};
    WalletResponse walletResponse = backendApi.fetchWallet(zpubs);

    Assertions.assertEquals(0, walletResponse.unspent_outputs.length);

    Map<String, WalletResponse.Address> addressesMap = walletResponse.getAddressesMap();
    assertAddressEquals(addressesMap.get(VPUB_1), VPUB_1, 2, 0, 0);

    Assertions.assertTrue(walletResponse.txs.length > 0);

    Assertions.assertNotNull(walletResponse.info.latest_block.hash);
    Assertions.assertTrue(walletResponse.info.latest_block.height > 0);
    Assertions.assertTrue(walletResponse.info.latest_block.time > 0);
    for (MinerFeeTarget minerFeeTarget : MinerFeeTarget.values()) {
      Assertions.assertTrue(walletResponse.info.fees.get(minerFeeTarget.getValue()) > 0);
    }
  }

  @Test
  public void fetchWallet_multi() throws Exception {
    String[] zpubs = new String[] {VPUB_1, VPUB_2};
    WalletResponse walletResponse = backendApi.fetchWallet(zpubs);

    Assertions.assertEquals(0, walletResponse.unspent_outputs.length);

    Map<String, WalletResponse.Address> addressesMap = walletResponse.getAddressesMap();
    assertAddressEquals(addressesMap.get(VPUB_1), VPUB_1, 2, 0, 0);
    assertAddressEquals(addressesMap.get(VPUB_2), VPUB_2, 0, 0, 0);

    Assertions.assertTrue(walletResponse.txs.length > 0);

    Assertions.assertNotNull(walletResponse.info.latest_block.hash);
    Assertions.assertTrue(walletResponse.info.latest_block.height > 0);
    Assertions.assertTrue(walletResponse.info.latest_block.time > 0);
    for (MinerFeeTarget minerFeeTarget : MinerFeeTarget.values()) {
      Assertions.assertTrue(walletResponse.info.fees.get(minerFeeTarget.getValue()) > 0);
    }
  }

  @Disabled // TODO
  @Test
  public void fetchAddressForSweep() throws Exception {
    String address = "tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4";
    Collection<UnspentOutput> unspentOutputs = backendApi.fetchAddressForSweep(address);

    Assertions.assertEquals(1, unspentOutputs.size());

    UnspentOutput unspentOutput = unspentOutputs.iterator().next();
    Assertions.assertEquals(address, unspentOutput.addr);
    Assertions.assertEquals(75000000, unspentOutput.value);
    Assertions.assertTrue(unspentOutput.confirmations >= 896868);
    Assertions.assertEquals("00142ecf8c3e5697f0513999ed3aa8ea9cd04d127355", Hex.toHexString(unspentOutput.getScriptBytes()));
    Assertions.assertEquals("d6efe01bbbc30022944c49d87e6f4e51bc091bf98197ca386bdb0c222bc56139", unspentOutput.tx_hash);
    Assertions.assertEquals(0, unspentOutput.tx_output_n);
  }

  @Test
  public void fetchXPub() throws Exception {
    XPubResponse xPubResponse = backendApi.fetchXPub(VPUB_1);

    Assertions.assertEquals(XPubResponse.Status.ok, xPubResponse.status);
    Assertions.assertEquals(1682434654, xPubResponse.data.created);
    Assertions.assertEquals("BIP84", xPubResponse.data.derivation);
    Assertions.assertTrue(xPubResponse.data.unused.external >= 2);
    Assertions.assertTrue(xPubResponse.data.unused.internal >= 0);
    Assertions.assertTrue(xPubResponse.data.balance >= 0);
  }

  @Test
  public void fetchTx() throws Exception {
    String txid = "0ba8c89afc51b65f133ac40131de7e170a41f87c5a4943502ff5705aae6341a8";
    TxDetail tx = backendApi.fetchTx(txid, true);

    Assertions.assertEquals(
            "0ba8c89afc51b65f133ac40131de7e170a41f87c5a4943502ff5705aae6341a8", tx.txid);
    Assertions.assertEquals(222, tx.size);
    Assertions.assertEquals(141, tx.vsize);
    Assertions.assertEquals(1, tx.version);
    Assertions.assertEquals(0, tx.locktime);

    Assertions.assertEquals(1, tx.inputs.length);
    Assertions.assertEquals(0, tx.inputs[0].n);
    Assertions.assertEquals(4294967295L, tx.inputs[0].seq);
    Assertions.assertEquals(
            "d60fae44ba8c728d43e7692c530b391eb393e298b169df3c09c150f79a66f1cc",
            tx.inputs[0].outpoint.txid);
    Assertions.assertEquals(1, tx.inputs[0].outpoint.vout);
    Assertions.assertEquals(238749293, tx.inputs[0].outpoint.value);
    Assertions.assertEquals(
            "0014ded4c3777ae40d686c981ee566a7021beda15ad1", tx.inputs[0].outpoint.scriptpubkey);

    Assertions.assertEquals(2, tx.outputs.length);
    Assertions.assertEquals(0, tx.outputs[0].n);
    Assertions.assertEquals(50000000, tx.outputs[0].value);
    Assertions.assertEquals(
            "001495df5bf26f2ae0307133ff6dc0a7d2e729872e89", tx.outputs[0].scriptpubkey);
    Assertions.assertEquals("witness_v0_keyhash", tx.outputs[0].type);
    Assertions.assertEquals("tb1qjh04hun09tsrqufnlakupf7juu5cwt5f87gh5u", tx.outputs[0].address);
  }

  @Test
  public void pushTx_failure() throws Exception {
    // spend coinbase -> P2WPKH
    Script outputScript = ScriptBuilder.createP2WPKHOutputScript(inputKey);
    Transaction txCoinbase = computeTxCoinbase(999999, outputScript);

    // spend tx
    TransactionOutput txOutput = txCoinbase.getOutput(0);
    Transaction tx = computeSpendTx(txOutput);

    // output #1
    SegwitAddress outputAddress = new SegwitAddress(outputKey, params);
    TransactionOutput transactionOutput = new TransactionOutput(params, null, Coin.valueOf(10000), outputAddress.getAddress());
    tx.addOutput(transactionOutput);

    // sign tx
    Map<String, ECKey> keyBag = new LinkedHashMap<>();
    keyBag.put(tx.getInput(0).getOutpoint().toString(), inputKey);
    SendFactoryGeneric.getInstance().signTransaction(tx, keyBag, bipFormatSupplier);
    tx.verify();

    Assertions.assertEquals("87d73800b3f117f666abf5fc6f41567cc43f88376d898c56784747aa7023e308", tx.getHashAsString());

    // push
    try {
      backendApi.pushTx(TxUtil.getInstance().getTxHex(tx));
      Assertions.assertTrue(false);
    } catch (BackendPushTxException e) {
      Assertions.assertEquals("bad-txns-inputs-missingorspent", e.getMessage());
      Assertions.assertEquals("bad-txns-inputs-missingorspent", e.getPushTxError());
    }
  }

  @Test
  public void pushTx_fail() throws Exception {
    // push
    try {
      String hex = "010000000001015f5e263ccc20a0d6b34d9b26eb8ac14ff7623b68dd55f8b2fb35d02e49cc9fcf0000000000ffffffff050000000000000000536a4c506ccfebfe774392f9b35bd2440e36370f1f904b7b0bab5bc4437b04748f02fa705bb14939be6d926a66fa65983ade03633d1fdb8c858e44f83ce8d625d5b27cc445663e665ecfd20d811a06061baad401881300000000000016001484a3fbbfdbd4166e5835e99b6ca7cd112c3c9c5559370000000000001600142c0038638b6ee7cd9a11653e1cf3a8fccf249fb8a687010000000000160014022ffa36b5b8dc9ee0a8ca4e6b51ec1745a70df0a68701000000000016001423d282ec3309b6de0b3f76f642150dbd54a8d4d402483045022100f48c654b60a90cc7298590da04e1bc63f90431554c73408064bcc1c5906b29d902205bdc9e3b081136aa1d9d56d3105f75a6308f5c2b38ac3126fbb98c14bc1b392a012103900b856c3780555390f1e70a68bdd3e341efc0110db071bdf190b76fe041657900000000";
      backendApi.pushTx(hex);
      Assertions.assertTrue(false);
    } catch (BackendPushTxException e) {
      Assertions.assertEquals("bad-txns-inputs-missingorspent", e.getMessage());
      Assertions.assertEquals("bad-txns-inputs-missingorspent", e.getPushTxError());
    }
  }

  @Test
  public void pushTx_addressReuse() throws Exception {
    // spend coinbase -> P2WPKH
    Script outputScript = ScriptBuilder.createP2WPKHOutputScript(inputKey);
    Transaction txCoinbase = computeTxCoinbase(999999, outputScript);

    // spend tx
    TransactionOutput txOutput = txCoinbase.getOutput(0);
    Transaction tx = computeSpendTx(txOutput);

    // output #1
    SegwitAddress outputAddress = new SegwitAddress(outputKey, params);
    TransactionOutput transactionOutput = new TransactionOutput(params, null, Coin.valueOf(10000), outputAddress.getAddress());
    tx.addOutput(transactionOutput);

    // output #2
    outputAddress = new SegwitAddress(outputKey, params);
    transactionOutput = new TransactionOutput(params, null, Coin.valueOf(20000), outputAddress.getAddress());
    tx.addOutput(transactionOutput);

    // sign tx
    Map<String, ECKey> keyBag = new LinkedHashMap<>();
    keyBag.put(tx.getInput(0).getOutpoint().toString(), inputKey);
    SendFactoryGeneric.getInstance().signTransaction(tx, keyBag, bipFormatSupplier);
    tx.verify();

    Assertions.assertEquals("d0be0bbc23f9309a23609e5680dda236c87c826acc44a6f6a164489e23e897cf", tx.getHashAsString());

    // push
    try {
      backendApi.pushTx(TxUtil.getInstance().getTxHex(tx), Arrays.asList(0,1));
      Assertions.assertTrue(false);
    } catch (BackendPushTxException e) {
      Assertions.assertEquals("address-reuse", e.getMessage());
      Assertions.assertEquals("address-reuse", e.getPushTxError());
      Assertions.assertArrayEquals(new Integer[]{1}, e.getVoutsAddressReuse().toArray());
    }
  }

  @Test
  public void seen() throws Exception {
    String ADDRESS1 = "tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4";
    String ADDRESS2 = "msbDHK6mikUQC8pUFfvsxa4q9vmt2qLvxC";
    String ADDRESS3 = "mjDfJ4jYvbuZ6YYCWoo8wmy2BQ89xsWFfh";
    String ADDRESS4 = "2N8gUTVdadexoewmcRE3tcgJ81kbksT4DH7";
    String ADDRESS5 = "tb1pqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesf3hn0c";

    // single
    Assertions.assertTrue(backendApi.seen(ADDRESS1));
    Assertions.assertFalse(backendApi.seen(ADDRESS2));
    Assertions.assertTrue(backendApi.seen(ADDRESS3));
    Assertions.assertTrue(backendApi.seen(ADDRESS4));
    Assertions.assertTrue(backendApi.seen(ADDRESS5));
    Assertions.assertFalse(backendApi.seen("unknown"));

    // multi
    SeenResponse seenResponse = backendApi.seen(Arrays.asList(ADDRESS1, ADDRESS2, ADDRESS3, ADDRESS4, ADDRESS5));
    Assertions.assertTrue(seenResponse.isSeen(ADDRESS1));
    Assertions.assertFalse(seenResponse.isSeen(ADDRESS2));
    Assertions.assertTrue(seenResponse.isSeen(ADDRESS3));
    Assertions.assertTrue(seenResponse.isSeen(ADDRESS4));
    Assertions.assertTrue(seenResponse.isSeen(ADDRESS5));
    Assertions.assertFalse(seenResponse.isSeen("unknown"));
  }

  private void assertAddressEquals(
          MultiAddrResponse.Address address,
          String zpub,
          int accountIndex,
          int changeIndex,
          int finalBalance) {
    Assertions.assertEquals(accountIndex, address.account_index);
    Assertions.assertEquals(changeIndex, address.change_index);
    Assertions.assertEquals(finalBalance, address.final_balance);
    Assertions.assertEquals(zpub, address.address);
  }

  private void assertAddressEquals(
          WalletResponse.Address address,
          String zpub,
          int accountIndex,
          int changeIndex,
          int finalBalance) {
    Assertions.assertEquals(accountIndex, address.account_index);
    Assertions.assertEquals(changeIndex, address.change_index);
    Assertions.assertEquals(finalBalance, address.final_balance);
    Assertions.assertEquals(zpub, address.address);
  }
}
