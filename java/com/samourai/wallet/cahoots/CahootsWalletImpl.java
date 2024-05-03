package com.samourai.wallet.cahoots;

import com.samourai.wallet.bip47.rpc.BIP47Wallet;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.bipWallet.WalletSupplier;
import com.samourai.wallet.chain.ChainSupplier;
import com.samourai.wallet.constants.SamouraiAccountIndex;
import com.samourai.wallet.constants.SamouraiAccount;
import com.samourai.wallet.constants.BIP_WALLET;
import com.samourai.wallet.send.provider.CahootsUtxoProvider;

import java.util.List;

public class CahootsWalletImpl extends AbstractCahootsWallet {
    private WalletSupplier walletSupplier;
    private CahootsUtxoProvider utxoProvider;

    public CahootsWalletImpl(ChainSupplier chainSupplier, WalletSupplier walletSupplier, CahootsUtxoProvider utxoProvider, int bip47Account) {
        super(chainSupplier,
                walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP84).getHdWallet().getFingerprint(),
                new BIP47Wallet(walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP84).getHdWallet()).getAccount(bip47Account));
        this.walletSupplier = walletSupplier;
        this.utxoProvider = utxoProvider;
    }

    public CahootsWalletImpl(ChainSupplier chainSupplier, WalletSupplier walletSupplier, CahootsUtxoProvider utxoProvider) {
        this(chainSupplier, walletSupplier, utxoProvider, 0);
    }

    protected BipWallet getReceiveWallet(int account, BipFormat bipFormat) throws Exception {
        bipFormat = likeTypedBipFormat(bipFormat);
        SamouraiAccount samouraiAccount = SamouraiAccountIndex.find(account);
        return walletSupplier.getWallet(samouraiAccount, bipFormat);
    }

    @Override
    protected String doFetchAddressReceive(int account, boolean increment, BipFormat bipFormat) throws Exception {
        return getReceiveWallet(account, bipFormat).getNextAddressReceive(bipFormat, increment).getAddressString();
    }

    @Override
    protected String doFetchAddressChange(int account, boolean increment, BipFormat bipFormat) throws Exception {
        return getReceiveWallet(account, bipFormat).getNextAddressChange(bipFormat, increment).getAddressString();
    }

    @Override
    public List<CahootsUtxo> getUtxosWpkhByAccount(int account) {
        return utxoProvider.getUtxosWpkhByAccount(account);
    }
}
