package com.samourai.wallet.send.provider;

import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.bipWallet.WalletSupplier;
import com.samourai.wallet.bipWallet.WalletSupplierImpl;
import com.samourai.wallet.client.indexHandler.MemoryIndexHandlerSupplier;
import com.samourai.wallet.constants.BIP_WALLETS;
import com.samourai.wallet.constants.BIP_WALLET;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockUtxoProviderTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(MockUtxoProviderTest.class);

    @Test
    public void addUtxo_retroCompatibilityMode() throws Exception {
        utxoProvider.setRetroCompatibilityMode();
        BipWallet bipWallet = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP84);

        UTXO utxo1 = utxoProvider.addUtxo(bipWallet, 100000);
        assertUtxo(utxo1, 100000, "befa44c5a7c219c507d316c452af2202626986a17f8400e32b47927c4d0c3f3e", 1, 999, "tb1q4crk5fzlr7qcz0nsun67luk982mn4wtlyydvlh", "02hNvy9WddFQ{17<N@0j-x7E?XQK", "m/0/1");

        UTXO utxo2 = utxoProvider.addUtxo(bipWallet, 100000);
        assertUtxo(utxo2, 100000, "8a9181c630effdbe46a09a3f26ede268e579baf2addd3986614631decb019979", 2, 999, "tb1qfqd55aeuuhj6jl2v0v6ckudd7wecdv6ss9ands", "02d%yn{rzV</9V4DO{.3T[e(NVRQ", "m/0/2");
    }

    @Test
    public void addUtxo() throws Exception {
        BipWallet bipWallet = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP84);

        UTXO utxo1 = utxoProvider.addUtxo(bipWallet, 100000);
        assertUtxo(utxo1, 100000, "2b007b4c6aa6e9b9f191e434f3bf57005aa286e0aeee7e154bd627af917fafcb", 1, 999, "tb1q4crk5fzlr7qcz0nsun67luk982mn4wtlyydvlh", "02hNvy9WddFQ{17<N@0j-x7E?XQK", "m/0/1");

        UTXO utxo2 = utxoProvider.addUtxo(bipWallet, 100000);
        assertUtxo(utxo2, 100000, "0d13ddd55e910d38d168abf387e0b1438cf696576941a0982fd39d833545930f", 2, 999, "tb1qfqd55aeuuhj6jl2v0v6ckudd7wecdv6ss9ands", "02d%yn{rzV</9V4DO{.3T[e(NVRQ", "m/0/2");
    }

    @Test
    public void multi() throws Exception {
        // instanciate another MockUtxoProvider
        byte[] seed = hdWalletFactory.computeSeedFromWords(SEED_WORDS);
        HD_Wallet bip44w2 = hdWalletFactory.getBIP44(seed, SEED_PASSPHRASE+"FOO", params);
        WalletSupplier walletSupplier2 = new WalletSupplierImpl(bipFormatSupplier, new MemoryIndexHandlerSupplier(), bip44w2, BIP_WALLETS.WHIRLPOOL);
        MockUtxoProvider utxoProvider2 = new MockUtxoProvider(params, walletSupplier2);

        BipWallet bipWallet = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP84);
        UTXO utxo1 = utxoProvider.addUtxo(bipWallet, 100000);
        assertUtxo(utxo1, 100000, "2b007b4c6aa6e9b9f191e434f3bf57005aa286e0aeee7e154bd627af917fafcb", 1, 999, "tb1q4crk5fzlr7qcz0nsun67luk982mn4wtlyydvlh", "02hNvy9WddFQ{17<N@0j-x7E?XQK", "m/0/1");

        UTXO utxo2 = utxoProvider.addUtxo(bipWallet, 100000);
        assertUtxo(utxo2, 100000, "0d13ddd55e910d38d168abf387e0b1438cf696576941a0982fd39d833545930f", 2, 999, "tb1qfqd55aeuuhj6jl2v0v6ckudd7wecdv6ss9ands", "02d%yn{rzV</9V4DO{.3T[e(NVRQ", "m/0/2");

        // utxos for walletSupplier2 use a different namespace for txid & indexs
        BipWallet bipWallet2 = walletSupplier2.getWallet(BIP_WALLET.DEPOSIT_BIP84);
        UTXO utxo3 = utxoProvider2.addUtxo(bipWallet2, 100000);
        assertUtxo(utxo3, 100000, "39cfe074d0c394c6da282a61d83bc15d84b38581399cd627e8440cbe1d467a8b", 1, 999, "tb1qpvy6z7avyaqmta4dg8ze6257wx4djpx0enxefj", "02b!EP)J<*lacA*lb/xAO]H^-1Kv", "m/0/1");
    }

    private void assertUtxo(UTXO utxo, long value, String txid, int n, int confirmations, String address, String scriptBytesZ85, String path) {
        Assertions.assertEquals(path, utxo.getPath());
        Assertions.assertEquals(value, utxo.getValue());

        Assertions.assertEquals(1, utxo.getOutpoints().size());
        MyTransactionOutPoint outPoint = utxo.getOutpoints().get(0);
        Assertions.assertEquals(txid, outPoint.getTxHash().toString());
        Assertions.assertEquals(n, outPoint.getTxOutputN());
        Assertions.assertEquals(address, outPoint.getAddress());
        Assertions.assertEquals(scriptBytesZ85, z85.encode(outPoint.getScriptBytes()));
        Assertions.assertEquals(confirmations, outPoint.getConfirmations());
        Assertions.assertEquals(value, outPoint.getValue().getValue());
    }
}
