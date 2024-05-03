package com.samourai.wallet.hd;

import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.bipWallet.BipDerivation;

public class BipAddress {
    private HD_Address hdAddress;
    private BipDerivation bipDerivation;
    private BipFormat bipFormat;

    public BipAddress(HD_Address hdAddress, BipDerivation bipDerivation, BipFormat bipFormat) {
        this.hdAddress = hdAddress;
        this.bipDerivation = bipDerivation;
        this.bipFormat = bipFormat;
    }

    public HD_Address getHdAddress() {
        return hdAddress;
    }

    public BipFormat getBipFormat() {
        return bipFormat;
    }

    public String getAddressString() {
        return bipFormat.getAddressString(hdAddress);
    }

    public String getPathAddress() {
        return bipDerivation.getPathAddress(hdAddress);
    }

    @Override
    public String toString() {
        return "BipAddress{" +
                "address=" + getAddressString() +
                ", path=" + getPathAddress() +
                ", bipFormat=" + bipFormat +
                '}';
    }
}
