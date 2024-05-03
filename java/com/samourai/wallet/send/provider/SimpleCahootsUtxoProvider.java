package com.samourai.wallet.send.provider;

import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.cahoots.CahootsUtxo;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.constants.SamouraiAccountIndex;
import com.samourai.wallet.constants.SamouraiAccount;

import java.util.Collection;
import java.util.List;

public class SimpleCahootsUtxoProvider implements CahootsUtxoProvider {
    private UtxoProvider utxoProvider;

    public SimpleCahootsUtxoProvider(UtxoProvider utxoProvider) {
        this.utxoProvider = utxoProvider;
    }

    @Override
    public List<CahootsUtxo> getUtxosWpkhByAccount(int account) {
        SamouraiAccount samouraiAccount = SamouraiAccountIndex.find(account);
        Collection<UTXO> utxos = utxoProvider.getUtxos(samouraiAccount, BIP_FORMAT.SEGWIT_NATIVE);
        return CahootsUtxo.toCahootsUtxos(utxos, utxoProvider);
    }
}
