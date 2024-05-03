package com.samourai.wallet.util;

import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.hd.HD_WalletFactoryGeneric;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;

public class TestUtil {
    private static final NetworkParameters params = TestNet3Params.get();
    private static final HD_WalletFactoryGeneric hdWalletFactory = HD_WalletFactoryGeneric.getInstance();

    public static HD_Wallet computeBip84wallet(String seedWords, String passphrase) throws Exception {
        byte[] seed = hdWalletFactory.computeSeedFromWords(seedWords);
        HD_Wallet bip84w = hdWalletFactory.getBIP84(seed, passphrase, params);
        return bip84w;
    }
}
