package com.samourai.wallet.util;

import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.hd.HD_Chain;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.constants.WALLET_INDEX;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class AddressFactoryGeneric {
    private Logger log = LoggerFactory.getLogger(AddressFactoryGeneric.class);

    public static final int LOOKAHEAD_GAP = 20;
    public static final int RECEIVE_CHAIN = 0;
    public static final int CHANGE_CHAIN = 1;

    private HD_Wallet bip44Wallet;
    private HD_Wallet bip49Wallet;
    private HD_Wallet bip84Wallet;
    private NetworkParameters params;
    private Map<WALLET_INDEX,Integer> highestIdxMap = null; // set from backend by APIFactory
    private Map<WALLET_INDEX,Integer> walletIdxMap = null; // mirrored with HDWallets instances

    public AddressFactoryGeneric() {
        reset();
    }

    public void reset() {
        reset(null, null, null, null);
    }

    public void reset(HD_Wallet bip44Wallet, HD_Wallet bip49Wallet, HD_Wallet bip84Wallet, NetworkParameters params) {
        if (log.isDebugEnabled()) {
            log.debug("reset");
        }
        if (bip44Wallet == null) {
            if (log.isDebugEnabled()) {
                log.debug("reset: bip44Wallet=null");
            }
        }
        if (bip49Wallet == null) {
            if (log.isDebugEnabled()) {
                log.debug("reset: bip49Wallet=null");
            }
        }
        if (bip84Wallet == null) {
            if (log.isDebugEnabled()) {
                log.debug("reset: bip84Wallet=null");
            }
        }
        this.bip44Wallet = bip44Wallet;
        this.bip49Wallet = bip49Wallet;
        this.bip84Wallet = bip84Wallet;
        this.params = params;

        highestIdxMap = initMap();
        walletIdxMap = initMap();
    }

    private static Map<WALLET_INDEX,Integer> initMap() {
        Map<WALLET_INDEX,Integer> map = new LinkedHashMap<>();
        for (WALLET_INDEX walletIndex : WALLET_INDEX.values()) {
            map.put(walletIndex, 0);
        }
        return map;
    }

    public int getIndex(WALLET_INDEX walletIndex) {
        int idx = 0;
        try {
            // max of {highestIdx, walletIdx}
            int highestIdx = highestIdxMap.get(walletIndex);
            int walletIdx = getWalletIdx(walletIndex);
            idx = Math.max(highestIdx, walletIdx);
            return idx;
        } catch (Exception e) {
            log.error("", e);
        }
        return idx;
    }

    public int getIndexAndIncrement(WALLET_INDEX walletIndex) {
        // get current index
        int idx = getIndex(walletIndex);

        // increment
        setWalletIdx(walletIndex, idx+1, false); // no decrement
        return idx;
    }

    public void increment(WALLET_INDEX walletIndex) {
        int idx = getIndex(walletIndex);
        setWalletIdx(walletIndex, idx+1, false); // no decrement
    }

    public Pair<Integer, String> getAddress(WALLET_INDEX walletIndex) {
        BipFormat bipFormat = walletIndex.getBipWallet().getBipFormatDefault();
        return getAddress(walletIndex, bipFormat, false);
    }

    public Pair<Integer, String> getAddress(WALLET_INDEX walletIndex, BipFormat bipFormat) {
        return getAddress(walletIndex, bipFormat, false);
    }

    public Pair<Integer, String> getAddressAndIncrement(WALLET_INDEX walletIndex) {
        BipFormat bipFormat = walletIndex.getBipWallet().getBipFormatDefault();
        return getAddress(walletIndex, bipFormat, true);
    }

    public Pair<Integer, String> getAddressAndIncrement(WALLET_INDEX walletIndex, BipFormat bipFormat) {
        return getAddress(walletIndex, bipFormat, true);
    }

    protected Pair<Integer, String> getAddress(WALLET_INDEX walletIndex, BipFormat forcedFormat, boolean increment)	{
        int idx = increment ? getIndexAndIncrement(walletIndex) : getIndex(walletIndex);
        HD_Chain hdChain = getHdCHain(walletIndex);
        if (hdChain == null) {
            // may happen on wallet startup
            throw new RuntimeException("getAddress("+walletIndex+") failed: wallet is null");
        }
        HD_Address hdAddress = hdChain.getAddressAt(idx);
        String addr = forcedFormat.getAddressString(hdAddress);
        return Pair.of(idx, addr);
    }

    public HD_Address get(int accountIdx, int chain, int idx)	{

        HD_Address addr = bip44Wallet.getAccount(accountIdx).getChain(chain).getAddressAt(idx);

        return addr;
    }

    public HD_Wallet getHdWallet(BipFormat bipFormat) {
        HD_Wallet hdWallet = null;
        if (bipFormat == BIP_FORMAT.LEGACY) {
            hdWallet = bip44Wallet;
        } else if (bipFormat == BIP_FORMAT.SEGWIT_COMPAT) {
            hdWallet = bip49Wallet;
        } else if (bipFormat == BIP_FORMAT.SEGWIT_NATIVE) {
            hdWallet = bip84Wallet;
        }
        if (hdWallet == null) {
            // may happen on wallet startup
            log.warn("wallet is NULL for "+bipFormat.getId());
            return null;
        }
        return hdWallet;
    }

    protected HD_Chain getHdCHain(WALLET_INDEX walletIndex) {
        int account = walletIndex.getBipWallet().getBipDerivation().getAccountIndex();
        int chain = walletIndex.getChainIndex();
        BipFormat bipFormat = walletIndex.getBipWallet().getBipFormatDefault();
        HD_Wallet hdWallet = getHdWallet(bipFormat);
        if (hdWallet == null) {
            // may happen on wallet startup
            return null;
        }
        return hdWallet.getAccount(account).getChain(chain);
    }

    protected int getWalletIdx(WALLET_INDEX walletIndex) {
        int idx = 0;
        try {
            // max of {hdIdx, walletIdx} (they should be mirrored)
            int hdIdx = getHdIdx(walletIndex);
            int walletIdx = walletIdxMap.get(walletIndex);
            idx = Math.max(hdIdx, walletIdx);
            return idx;
        } catch (Exception e) {
            log.error("", e);
        }
        return idx;
    }

    boolean canIncrement(WALLET_INDEX walletIndex, int value) {
        if (WALLET_INDEX.PREMIX_RECEIVE.equals(walletIndex) || WALLET_INDEX.POSTMIX_RECEIVE.equals(walletIndex)) {
            // disable lookahead gap limit for PREMIX to allow large tx0
            // disable lookahead gap limit for POSTMIX to prevent REJECTED_OUTPUT
            return true;
        }
        int highestIdx = highestIdxMap.get(walletIndex);
        boolean canInc = ((value - highestIdx) < LOOKAHEAD_GAP);
        return canInc;
    }

    // should only be set by AndroidWalletStateIndexHandler
    public void setWalletIdx(WALLET_INDEX walletIndex, int value, boolean allowDecrement) {
        int walletIdx = getWalletIdx(walletIndex);
        if (walletIdx < value) {
            // INCREMENT

            // check lookahead gap
            if (canIncrement(walletIndex, value)) {
                // increment
                if (log.isDebugEnabled()) {
                    int oldValue = walletIdxMap.get(walletIndex);
                    log.debug("set "+walletIndex+".walletIdx "+oldValue+" -> "+value);
                }
                walletIdxMap.put(walletIndex, value);

                // apply to HdWallet
                setHdIdx(walletIndex, value, false); // no decrement
            } else {
                log.warn("Cannot increment "+walletIndex+": walletIdx="+walletIdx+", valueToSet="+value);
            }
        } else if (walletIdx > value && allowDecrement) {
            // DECREMENT
            if (log.isDebugEnabled()) {
                int oldValue = walletIdxMap.get(walletIndex);
                log.debug("set "+walletIndex+".walletIdx "+oldValue+" -> "+value);
            }
            walletIdxMap.put(walletIndex, value);

            // apply to HDWallet
            setHdIdx(walletIndex, value, true); // allow decrement
        }
    }

    protected int getHdIdx(WALLET_INDEX walletIndex) {
        int idx = 0;
        try {
            HD_Chain hdChain = getHdCHain(walletIndex);
            if (hdChain != null) {
                idx = hdChain.getAddrIdx();
            } else {
                // may happen on wallet startup
                log.warn("getHdIdx("+walletIndex+") failed: wallet is null");
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return idx;
    }

    // should only be set by setWalletIdx() to mirror HDWallets instances with walletIdx
    protected void setHdIdx(WALLET_INDEX walletIndex, int value, boolean allowDecrement) {
        try {
            // set hdIdx
            HD_Chain hdChain = getHdCHain(walletIndex);
            if (hdChain != null) {
                int hdIdx = hdChain.getAddrIdx();
                if (allowDecrement || hdIdx < value) {
                    if (log.isDebugEnabled()) {
                        int oldValue = hdChain.getAddrIdx();
                        log.debug("set "+walletIndex+".hdIdx "+oldValue+" -> "+value);
                    }
                    hdChain.setAddrIdx(value);
                }
            } else {
                // this may happen on startup
                log.warn("cannot setHdIdx("+walletIndex+"): wallet is null");
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    // should only be set by APIFactory!
    public void setHighestIdx(WALLET_INDEX walletIndex, int value) {
        // set highestIdx
        if (log.isDebugEnabled()) {
            int oldValue = highestIdxMap.get(walletIndex);
            log.debug("set "+walletIndex+".highestIdx "+oldValue+" -> "+value);
        }
        highestIdxMap.put(walletIndex, value);

        // update walletIdx
        setWalletIdx(walletIndex, value, false);
    }

    public String debugIndex(WALLET_INDEX walletIndex) {
        int highestIdx = highestIdxMap.get(walletIndex);
        int walletIdx = walletIdxMap.get(walletIndex);
        int hdIdx = getHdIdx(walletIndex);
        int index = getIndex(walletIndex);
        String debugStr = highestIdx+" ; "+walletIdx+" ; "+hdIdx+" => "+index;
        if (log.isDebugEnabled()) {
            log.debug(walletIndex+": "+debugStr);
        }
        return debugStr;
    }
}
