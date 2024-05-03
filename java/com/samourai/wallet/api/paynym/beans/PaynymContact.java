package com.samourai.wallet.api.paynym.beans;

public class PaynymContact {
    private String code;
    private String nymId;
    private String nymName;
    private boolean segwit;

    public PaynymContact(){}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNymId() {
        return nymId;
    }

    public void setNymId(String nymId) {
        this.nymId = nymId;
    }

    public String getNymName() {
        return nymName;
    }

    public void setNymName(String nymName) {
        this.nymName = nymName;
    }

    public boolean isSegwit() {
        return segwit;
    }

    public void setSegwit(boolean segwit) {
        this.segwit = segwit;
    }
}
