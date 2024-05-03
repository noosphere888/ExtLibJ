package com.samourai.wallet.cahoots;

import com.samourai.wallet.SamouraiWalletConst;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public abstract class AbstractCahoots2xService<T extends Cahoots2x, C extends CahootsContext> extends AbstractCahootsService<T,C> {
    private static final Logger log = LoggerFactory.getLogger(AbstractCahoots2xService.class);
    private static final long LOCK_TIME_LENIENCE = 2; // 2 blocks
    public AbstractCahoots2xService(CahootsType cahootsType, BipFormatSupplier bipFormatSupplier, NetworkParameters params) {
        super(cahootsType, bipFormatSupplier, params, TypeInteraction.TX_BROADCAST);
    }

    @Override
    public void verifyResponse(C cahootsContext, T cahoots, T request) throws Exception {
        super.verifyResponse(cahootsContext, cahoots, request);

        if (request != null) {
            // properties should never change once set
            if (cahoots.ts != request.ts) {
                throw new Exception("Invalid altered Cahoots ts");
            }
            if (cahoots.strID != request.strID) {
                throw new Exception("Invalid altered Cahoots strID");
            }
            if (!cahoots.strDestination.equals(request.strDestination)) {
                throw new Exception("Invalid altered Cahoots strDestination");
            }
            if (!Objects.equals(cahoots.strPayNymCollab, request.strPayNymCollab)) {
                throw new Exception("Invalid altered Cahoots strPayNymCollab");
            }
            if (!Objects.equals(cahoots.strPayNymInit, request.strPayNymInit)) {
                throw new Exception("Invalid altered Cahoots strPayNymInit");
            }
            if (cahoots.account != request.account) {
                throw new Exception("Invalid altered Cahoots account");
            }
            if (cahoots.cptyAccount != request.cptyAccount) {
                throw new Exception("Invalid altered Cahoots cptyAccount");
            }
            if (!Arrays.equals(cahoots.fingerprint, request.fingerprint)) {
                throw new Exception("Invalid altered Cahoots fingerprint");
            }
            if (!Arrays.equals(cahoots.fingerprintCollab, request.fingerprintCollab)) {
                throw new Exception("Invalid altered Cahoots fingerprintCollab");
            }
        }

        if (cahoots.getStep() >= 3) {
            // check fee
            if (cahoots.feeAmount > SamouraiWalletConst.MAX_ACCEPTABLE_FEES) {
                throw new Exception("Cahoots fee too high: " + cahoots.getTransaction().getFee().longValue());
            }
        }
    }

    protected void checkMaxSpendAmount(long verifiedSpendAmount, long feeAmount, C cahootsContext) throws Exception {
        long maxSpendAmount = computeMaxSpendAmount(feeAmount, cahootsContext);
        String prefix = "["+cahootsContext.getCahootsType()+"/"+cahootsContext.getTypeUser()+"] ";
        if (log.isDebugEnabled()) {
            log.debug(prefix+cahootsContext.getTypeUser()+" verifiedSpendAmount="+verifiedSpendAmount+", maxSpendAmount="+maxSpendAmount);
        }
        if (verifiedSpendAmount == 0) {
            throw new Exception(prefix+"Cahoots spendAmount verification failed");
        }
        if (verifiedSpendAmount > maxSpendAmount) {
            throw new Exception(prefix+"Cahoots verifiedSpendAmount mismatch: " + verifiedSpendAmount+" > "+maxSpendAmount);
        }
    }

    protected abstract long computeMaxSpendAmount(long minerFee, C cahootsContext) throws Exception;

    //
    // receiver
    //
    public T doStep3(T cahoots2, C cahootsContext) throws Exception {
        debug("BEGIN doStep3", cahoots2, cahootsContext);

        HashMap<String, ECKey> keyBag_A = computeKeyBag(cahoots2, cahootsContext.getInputs());

        T cahoots3 = (T)cahoots2.copy();
        checkLockTime(cahoots3, cahootsContext);
        cahoots3.doStep3(keyBag_A);

        // check verifiedSpendAmount
        long verifiedSpendAmount = computeSpendAmount(keyBag_A, cahoots3, cahootsContext);
        checkMaxSpendAmount(verifiedSpendAmount, cahoots3.getFeeAmount(), cahootsContext);

        debug("END doStep3", cahoots3, cahootsContext);
        return cahoots3;
    }

    //
    // sender
    //
    public T doStep4(T cahoots3, C cahootsContext) throws Exception {
        debug("BEGIN doStep4", cahoots3, cahootsContext);

        HashMap<String, ECKey> keyBag_B = computeKeyBag(cahoots3, cahootsContext.getInputs());

        T cahoots4 = (T)cahoots3.copy();
        checkLockTime(cahoots4, cahootsContext);
        cahoots4.doStep4(keyBag_B);

        // check verifiedSpendAmount
        long verifiedSpendAmount = computeSpendAmount(keyBag_B, cahoots4, cahootsContext);
        checkMaxSpendAmount(verifiedSpendAmount, cahoots4.getFeeAmount(), cahootsContext);

        // check fee
        checkFee(cahoots4);

        debug("END doStep4", cahoots4, cahootsContext);
        return cahoots4;
    }

    protected void checkLockTime(T cahoots, CahootsContext cahootsContext) throws Exception {
        long txLockTime = cahoots.getTransaction().getLockTime();
        long currentBlockHeight = cahootsContext.getCahootsWallet().getChainSupplier().getLatestBlock().height;
        if(cahootsContext.getCahootsType() != CahootsType.STOWAWAY) {
            if (txLockTime == 0 || Math.abs(txLockTime - currentBlockHeight) > LOCK_TIME_LENIENCE) { // maybe a block is found fast and users dont have exact same block heights, or the user is running custom code and is malicious
                throw new Exception("Locktime error: txLockTime " + txLockTime + ", vs currentBlockHeight " + currentBlockHeight);
            }
        }
    }

    public void debug(String info, T cahoots, C cahootsContext) {
        if (log.isDebugEnabled()) {
            log.debug("###### " +info+ " "+cahootsContext.getCahootsType()+"/"+cahootsContext.getTypeUser());
            log.debug(" * outpoints="+cahoots.getOutpoints());
            log.debug(" * tx="+cahoots.getTransaction());
        }
    }
}
