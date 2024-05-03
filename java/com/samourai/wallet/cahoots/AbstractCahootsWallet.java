package com.samourai.wallet.cahoots;

import com.samourai.wallet.bip47.rpc.BIP47Account;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.chain.ChainSupplier;
import com.samourai.wallet.hd.BipAddress;

import java.util.List;

public abstract class AbstractCahootsWallet implements CahootsWallet {
    private ChainSupplier chainSupplier;
    private byte[] fingerprint;
    private BIP47Account bip47Account;

    public AbstractCahootsWallet(ChainSupplier chainSupplier, byte[] fingerprint, BIP47Account bip47Account) {
        this.chainSupplier = chainSupplier;
        this.fingerprint = fingerprint;
        this.bip47Account = bip47Account;
    }

    protected BipFormat likeTypedBipFormat(BipFormat bipFormat) {
        if (bipFormat == BIP_FORMAT.TAPROOT) {
            // like-typed output is not implemented for TAPROOT => handle TAPROOT mix output as SEGWIT_NATIVE
            return BIP_FORMAT.SEGWIT_NATIVE;
        }
        return bipFormat;
    }

    public abstract List<CahootsUtxo> getUtxosWpkhByAccount(int account);
    protected abstract String doFetchAddressReceive(int account, boolean increment, BipFormat bipFormat) throws Exception;
    protected abstract String doFetchAddressChange(int account, boolean increment, BipFormat bipFormat) throws Exception;

    @Override
    public String fetchAddressReceive(int account, boolean increment, BipFormat bipFormat) throws Exception {
        bipFormat = likeTypedBipFormat(bipFormat);
        return doFetchAddressReceive(account, increment, bipFormat);
    }

    @Override
    public String fetchAddressChange(int account, boolean increment, BipFormat bipFormat) throws Exception {
        bipFormat = likeTypedBipFormat(bipFormat);
        return doFetchAddressChange(account, increment, bipFormat);
    }

    @Override
    public ChainSupplier getChainSupplier() {
        return chainSupplier;
    }

    @Override
    public byte[] getFingerprint() {
        return fingerprint;
    }

    @Override
    public BIP47Account getBip47Account() {
        return bip47Account;
    }
}
