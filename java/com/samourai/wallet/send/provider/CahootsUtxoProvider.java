package com.samourai.wallet.send.provider;

import com.samourai.wallet.cahoots.CahootsUtxo;

import java.util.List;

public interface CahootsUtxoProvider {
    List<CahootsUtxo> getUtxosWpkhByAccount(int account);
}
