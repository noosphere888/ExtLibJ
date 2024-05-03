package com.samourai.wallet.send;

import com.samourai.wallet.api.backend.beans.UnspentOutput;
import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import org.bouncycastle.util.encoders.Hex;

import java.util.*;
import java.util.stream.Collectors;

//import org.apache.commons.lang3.tuple.Pair;

public class UTXO {

    private String path = null;
    private String xpub = null;

    private List<MyTransactionOutPoint> outpoints = null;

    public UTXO() {
        this(new ArrayList<>(), null, null);
    }

    public UTXO(String path, String xpub) {
        this(new ArrayList<>(), path, xpub);
    }

    public UTXO(List<MyTransactionOutPoint> outpoints, String path, String xpub) {
        this.outpoints = outpoints;
        this.path = path;
        this.xpub = xpub;
    }

    public Collection<UnspentOutput> toUnspentOutputs() {
        List<UnspentOutput> unspentOutputs = new LinkedList<>();
        for (MyTransactionOutPoint outPoint : outpoints) {
            unspentOutputs.add(new UnspentOutput(outPoint, path, xpub));
        }
        return unspentOutputs;
    }

    public List<MyTransactionOutPoint> getOutpoints() {
        return outpoints;
    }

    public void setOutpoints(List<MyTransactionOutPoint> outpoints) {
        this.outpoints = outpoints;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getXpub() {
        return xpub;
    }

    public void setXpub(String xpub) {
        this.xpub = xpub;
    }

    public long getValue() {

        long value = 0L;

        for (MyTransactionOutPoint out : outpoints) {
            value += out.getValue().longValue();
        }

        return value;
    }

    public static long sumValue(Collection<UTXO> utxos) {
        long sum = 0L;
        for (UTXO utxo : utxos) {
            sum += utxo.getValue();
        }
        return sum;
    }

    // sorts in descending order by amount
    public static class UTXOComparator implements Comparator<UTXO> {

        public int compare(UTXO o1, UTXO o2) {

            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;

            if (o1.getValue() > o2.getValue()) {
                return BEFORE;
            } else if (o1.getValue() < o2.getValue()) {
                return AFTER;
            } else {
                return EQUAL;
            }

        }

    }

    // sorts in descending order by amount
    public static class OutpointComparator implements Comparator<MyTransactionOutPoint> {

        public int compare(MyTransactionOutPoint o1, MyTransactionOutPoint o2) {

            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;

            if (o1.getValue().longValue() > o2.getValue().longValue()) {
                return BEFORE;
            } else if (o1.getValue().longValue() < o2.getValue().longValue()) {
                return AFTER;
            } else {
                return EQUAL;
            }

        }

    }

    public static List<MyTransactionOutPoint> listOutpoints(Collection<UTXO> utxos) {
        List<MyTransactionOutPoint> outPoints = new LinkedList<>();
        for (UTXO utxo : utxos) {
            outPoints.addAll(utxo.getOutpoints());
        }
        return outPoints;
    }

    public static int countOutpoints(Collection<UTXO> utxos) {
        int ret = 0;
        for (UTXO utxo : utxos) {
            ret += utxo.getOutpoints().size();
        }
        return ret;
    }

    // used by Android
    public static List<UTXO> filterUtxosWpkh(List<UTXO> utxos) {
        return utxos.stream().filter(utxo -> {
            // filter wpkh
            String script = Hex.toHexString(utxo.getOutpoints().get(0).getScriptBytes());
            return Bech32UtilGeneric.getInstance().isP2WPKHScript(script);
        }).collect(Collectors.toList());
    }

}
