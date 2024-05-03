package com.samourai.wallet.cahoots;

import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.send.provider.UtxoKeyProvider;
import com.samourai.wallet.util.TxUtil;
import org.bitcoinj.core.Coin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CahootsUtxo extends UTXO {
    private static final Logger log = LoggerFactory.getLogger(CahootsUtxo.class);

    private MyTransactionOutPoint outpoint;
    private byte[] key;

    public CahootsUtxo(MyTransactionOutPoint cahootsOutpoint, String path, String xpub, byte[] key) {
        super(new LinkedList<>(Arrays.asList(new MyTransactionOutPoint[]{cahootsOutpoint})), path, xpub);
        this.outpoint = cahootsOutpoint;
        this.key = key;
    }

    public MyTransactionOutPoint getOutpoint() {
        return outpoint;
    }

    public byte[] getKey() {
        return key;
    }

    public static Coin sumValue(List<CahootsUtxo> utxos) {
        Coin balance = Coin.ZERO;
        for(CahootsUtxo cahootsUtxo : utxos) {
            balance = balance.add(cahootsUtxo.getOutpoint().getValue());
        }
        return balance;
    }

    public static List<CahootsUtxo> toCahootsUtxos(Collection<UTXO> utxos, UtxoKeyProvider keyProvider) {
        return utxos.stream().map(utxo -> {
            MyTransactionOutPoint outPoint = utxo.getOutpoints().get(0); // TODO
            try {
                byte[] key = keyProvider._getPrivKey(outPoint.getHash().toString(), (int) outPoint.getIndex());
                return new CahootsUtxo(outPoint, utxo.getPath(), utxo.getXpub(), key);
            } catch (Exception e) {
                log.warn("Skipping CahootsUtxo: "+outPoint+": key not found");
                return null;
            }
        }).filter(utxo -> utxo != null).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "CahootsUtxo{" +
                "utxo=" + outpoint.toString() +
                ", value=" + outpoint.getValue() +
                ", address="+outpoint.getAddress() +
                ", path="+getPath()+
                ", xpub="+getXpub()+
                '}';
    }
}
