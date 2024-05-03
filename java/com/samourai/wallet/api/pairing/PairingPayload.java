package com.samourai.wallet.api.pairing;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PairingPayload {
    private static final Logger log = LoggerFactory.getLogger(PairingPayload.class);

    private PairingValue pairing;
    private PairingDojo dojo; // may be null

    public PairingPayload() {
        this.pairing = new PairingValue();
        this.dojo = null;
    }

    public PairingPayload(PairingType type, PairingVersion version, PairingNetwork network, String mnemonic, Boolean passphrase, PairingDojo dojo) {
        this.pairing = new PairingValue(type, version, network, mnemonic, passphrase);
        this.dojo = dojo;
    }

    protected void validate() throws Exception {
        if (pairing == null) {
            throw new Exception("Invalid pairing");
        }
        pairing.validate();
        if (dojo != null) {
            dojo.validate();
        }
    }

    public PairingValue getPairing() {
        return pairing;
    }

    public void setPairing(PairingValue pairing) {
        this.pairing = pairing;
    }

    public PairingDojo getDojo() {
        return dojo;
    }

    public void setDojo(PairingDojo dojo) {
        this.dojo = dojo;
    }

    public static class PairingValue {
        private PairingType type;
        private PairingVersion version;
        private PairingNetwork network;
        private String mnemonic;
        private Boolean passphrase; // NULL for V1

        public PairingValue() {

        }

        public PairingValue(PairingType type, PairingVersion version, PairingNetwork network, String mnemonic, Boolean passphrase) {
            this.type = type;
            this.version = version;
            this.network = network;
            this.mnemonic = mnemonic;
            this.passphrase = passphrase;
        }

        protected void validate() throws Exception {
            if (type == null) {
                throw new Exception("Invalid pairing.type");
            }

            if (version == null) {
                throw new Exception("Invalid pairing.version");
            }

            if (network == null) {
                throw new Exception("Invalid pairing.network");
            }

            if (StringUtils.isEmpty(mnemonic)) {
                throw new Exception("Invalid pairing.mnemonic");
            }
        }

        public PairingType getType() {
            return type;
        }

        public void setType(PairingType type) {
            this.type = type;
        }

        public PairingVersion getVersion() {
            return version;
        }

        public void setVersion(PairingVersion version) {
            this.version = version;
        }

        public PairingNetwork getNetwork() {
            return network;
        }

        public void setNetwork(PairingNetwork network) {
            this.network = network;
        }

        public String getMnemonic() {
            return mnemonic;
        }

        public void setMnemonic(String mnemonic) {
            this.mnemonic = mnemonic;
        }

        public Boolean getPassphrase() {
            return passphrase;
        }

        public void setPassphrase(Boolean passphrase) {
            this.passphrase = passphrase;
        }
    }
}
