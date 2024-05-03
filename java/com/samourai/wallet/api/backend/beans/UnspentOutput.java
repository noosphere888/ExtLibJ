package com.samourai.wallet.api.backend.beans;

import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.util.FormatsUtilGeneric;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.script.Script;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Collection;

public class UnspentOutput {
    private static final String PATH_SEPARATOR = "/";
    public String tx_hash;
    public int tx_output_n;
    public int tx_version;
    public long tx_locktime;
    public long value;
    public String script;
    public String addr;
    public int confirmations;
    public Xpub xpub;

    public UnspentOutput() {
    }

    public UnspentOutput(UnspentOutput copy) {
        this.tx_hash = copy.tx_hash;
        this.tx_output_n = copy.tx_output_n;
        this.tx_version = copy.tx_version;
        this.tx_locktime = copy.tx_locktime;
        this.value = copy.value;
        this.script = copy.script;
        this.addr = copy.addr;
        this.confirmations = copy.confirmations;
        this.xpub = copy.xpub;
    }

    public UnspentOutput(MyTransactionOutPoint outPoint, String path, String xpub) {
        this.tx_hash = outPoint.getTxHash().toString();
        this.tx_output_n = outPoint.getTxOutputN();
        this.tx_version = -1; // ignored
        this.tx_locktime = -1; // ignored
        this.value = outPoint.getValue().getValue();
        this.script = outPoint.getScriptBytes() != null ? Hex.toHexString(outPoint.getScriptBytes()) : null;
        this.addr = outPoint.getAddress();
        this.confirmations = outPoint.getConfirmations();
        this.xpub = new Xpub();
        this.xpub.path = path;
        this.xpub.m = xpub;
    }

    public boolean hasPath() {
        return xpub != null && !StringUtils.isEmpty(xpub.path);
    }

    public int computePathChainIndex() {
        try {
            return Integer.parseInt(getPath().split(PATH_SEPARATOR)[1]);
        } catch (Exception e) {
            throw new RuntimeException("computePathChainIndex failed for utxo path: "+getPath());
        }
    }

    public int computePathAddressIndex() {
        try {
            return Integer.parseInt(getPath().split(PATH_SEPARATOR)[2]);
        } catch (Exception e) {
            throw new RuntimeException("computePathAddressIndex failed for utxo path: "+getPath());
        }
    }

    public String getPath() {
        if (xpub == null) {
            return null;
        }
      return xpub.path;
    }

    public String getPathAddress(int purpose, int accountIndex, NetworkParameters params) {
        int coinType = FormatsUtilGeneric.getInstance().getCoinType(params);
        if (!hasPath()) {
            // bip47
            return HD_Address.getPathAddressBip47(purpose, coinType, accountIndex);
        }
        return HD_Address.getPathAddress(purpose, coinType, accountIndex, computePathChainIndex(), computePathAddressIndex());
    }

    public static String computePath(HD_Address hdAddress) {
        return computePath(hdAddress.getChainIndex(), hdAddress.getAddressIndex());
    }

    public static String computePath(int chainIndex, int addressIndex) {
        return "m"+PATH_SEPARATOR+chainIndex+PATH_SEPARATOR+addressIndex;
    }

    public MyTransactionOutPoint computeOutpoint(NetworkParameters params) {
        Sha256Hash sha256Hash = Sha256Hash.wrap(Hex.decode(tx_hash));
        // use MyTransactionOutPoint to forward scriptBytes + address
        return new MyTransactionOutPoint(params, sha256Hash, tx_output_n, BigInteger.valueOf(value), getScriptBytes(), addr, confirmations);
    }

    public TransactionInput computeSpendInput(NetworkParameters params) {
        return new TransactionInput(
                        params, null, new byte[] {}, computeOutpoint(params), Coin.valueOf(value));

    }

    public byte[] getScriptBytes() {
        return script != null ? Hex.decode(script) : null;
    }

    public Script computeScript() {
        return new Script(getScriptBytes());
    }

    public static long sumValue(Collection<UnspentOutput> utxos) {
        long sumValue = 0;
        for (UnspentOutput utxo : utxos) {
            sumValue += utxo.value;
        }
        return sumValue;
    }

    public static class Xpub {
      public String m;
      public String path;
    }

    @Override
    public String toString() {
      return getUtxoName()
          + " ("
          + value
          + " sats, "
          + confirmations
          + " confirmations"
          + ", path=" + (xpub != null && xpub.path != null ? xpub.path : "null")
          + ", xpub=" + (xpub != null && xpub.m != null ? xpub.m : "null")
          + ", address="
          + addr
          + ")";
    }

    public String getUtxoName() {
        return tx_hash+":"+tx_output_n;
    }
  }