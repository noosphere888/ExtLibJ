package com.samourai.wallet.ricochet;

import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.bip47.BIP47UtilGeneric;
import com.samourai.wallet.bip47.rpc.BIP47Account;
import com.samourai.wallet.bip47.rpc.PaymentCode;
import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.segwit.SegwitAddress;
import com.samourai.wallet.send.provider.UtxoProvider;
import com.samourai.wallet.constants.SamouraiAccount;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RicochetConfig {
    private static final Logger log = LoggerFactory.getLogger(RicochetConfig.class);

    private int feePerB;
    private int nbHops;

    private boolean samouraiFeeViaBIP47;
    private String samouraiFeeAddress; // used as bip47 fallback

    private boolean useTimeLock;
    private boolean rbfOptIn;
    private long latestBlock;

    private UtxoProvider utxoProvider;
    private BIP47UtilGeneric bip47Util;
    private BipWallet bipWalletRicochet;
    private BipWallet bipWalletChange;
    private SamouraiAccount spendAccount;

    private BIP47Account bip47Account;
    private int bip47WalletOutgoingIdx;

    public RicochetConfig(int feePerB, boolean samouraiFeeViaBIP47, String samouraiFeeAddress, boolean useTimeLock, boolean rbfOptIn, long latestBlock, UtxoProvider utxoProvider, BIP47UtilGeneric bip47Util, BipWallet bipWalletRicochet, BipWallet bipWalletChange, SamouraiAccount spendAccount, BIP47Account bip47Account, int bip47WalletOutgoingIdx) {
        this.feePerB = feePerB;
        this.nbHops = 4;
        this.samouraiFeeViaBIP47 = samouraiFeeViaBIP47;
        this.samouraiFeeAddress = samouraiFeeAddress;
        this.useTimeLock = useTimeLock;
        this.rbfOptIn = rbfOptIn;
        this.latestBlock = latestBlock;
        this.utxoProvider = utxoProvider;
        this.bip47Util = bip47Util;
        this.bipWalletRicochet = bipWalletRicochet;
        this.bipWalletChange = bipWalletChange;
        this.spendAccount = spendAccount;
        this.bip47Account = bip47Account;
        this.bip47WalletOutgoingIdx = bip47WalletOutgoingIdx;
    }

    public String getBip47NextFeeAddress() {
        String strAddress = samouraiFeeAddress; // fallback
        NetworkParameters params = bip47Account.getParams();
        try {
            PaymentCode pcode = new PaymentCode(SamouraiWalletConst.samouraiDonationPCode);
            SegwitAddress segwitAddress = bip47Util.getSendAddress(bip47Account, pcode, bip47WalletOutgoingIdx, params);
            // derive as bech32
            strAddress = segwitAddress.getBech32AsString();
            bip47WalletOutgoingIdx++;
        } catch (Exception e) {
            // fallback to defaultFeeAddress
            log.error("", e);
        }
        return strAddress;
    }

    public int getFeePerB() {
        return feePerB;
    }

    public int getNbHops() {
        return nbHops;
    }

    public void setNbHops(int nbHops) {
        this.nbHops = nbHops;
    }

    public boolean isSamouraiFeeViaBIP47() {
        return samouraiFeeViaBIP47;
    }

    public String getSamouraiFeeAddress() {
        return samouraiFeeAddress;
    }

    public boolean isUseTimeLock() {
        return useTimeLock;
    }

    public boolean isRbfOptIn() {
        return rbfOptIn;
    }

    public long getLatestBlock() {
        return latestBlock;
    }

    public UtxoProvider getUtxoProvider() {
        return utxoProvider;
    }

    public BipWallet getBipWalletRicochet() {
        return bipWalletRicochet;
    }

    public BipWallet getBipWalletChange() {
        return bipWalletChange;
    }

    public SamouraiAccount getSpendAccount() {
        return spendAccount;
    }
}
