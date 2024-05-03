package com.samourai.wallet.segwit;

import java.math.BigInteger;

import com.samourai.wallet.segwit.bech32.Bech32Segwit;
import com.samourai.wallet.util.Util;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;

import org.apache.commons.lang3.ArrayUtils;

public class TimelockAddress extends SegwitAddress {

    protected long timelock = 0L;

    public TimelockAddress(byte[] pubkey, NetworkParameters params, long timelock) throws Exception {
        super(pubkey, params, TYPE_P2WSH);
        this.timelock = timelock;
    }

    public TimelockAddress(ECKey ecKey, NetworkParameters params, long timelock) throws Exception {
        super(ecKey, params);
        this.DEFAULT_TO = TYPE_P2WSH;
        this.timelock = timelock;
    }

    public TimelockAddress(byte[] pubkey, NetworkParameters params) throws Exception {
        super(pubkey, params, TYPE_P2WSH);
    }

    public TimelockAddress(ECKey ecKey, NetworkParameters params) throws Exception {
        super(ecKey, params);
        this.DEFAULT_TO = TYPE_P2WSH;
    }

    public String getTimelockAddressAsString()    {

        String address = null;

        try {
            address = Bech32Segwit.encode(params instanceof TestNet3Params ? "tb" : "bc", (byte)0x00, Util.sha256(this.segwitRedeemScript().getProgram()));
        }
        catch(Exception e) {
            ;
        }

        return address;
    }

    public long getTimelock()  {

      return timelock;

    }

    public String getDefaultToAddressAsString()  {

      return getTimelockAddressAsString();

    }

    public Script segwitOutputScript() throws java.security.NoSuchAlgorithmException  {

        byte[] hash = Util.sha256(this.segwitRedeemScript().getProgram());
        byte[] buf = new byte[2 + hash.length];
        buf[0] = (byte)0x00;
        buf[1] = (byte)0x20;
        System.arraycopy(hash, 0, buf, 2, hash.length);

        return new Script(buf);
    }

    public Script segwitRedeemScript()    {
        //
        // <timelock> OP_CHECKLOCKTIMEVERIFY OP_DROP <derived_key> OP_CHECKSIG
        //
        byte[] lock = getTimelockAsByteArray();
        byte[] locklen = new byte[1];
        locklen[0] = (byte)lock.length;
        byte[] pubkey = this.ecKey.getPubKey();
        byte[] buf = new byte[1 + lock.length + 3 + pubkey.length + 1];
        System.arraycopy(locklen, 0, buf, 0, locklen.length);
        System.arraycopy(lock, 0, buf, locklen.length, lock.length);
        buf[locklen.length + lock.length] = (byte)0xb1;
        buf[locklen.length + lock.length + 1] = (byte)0x75;
        buf[locklen.length + lock.length + 2] = (byte)0x21;
        System.arraycopy(pubkey, 0, buf, locklen.length + lock.length + 3, pubkey.length);
        buf[locklen.length + lock.length + 3 + pubkey.length] = (byte)0xac;

        return new Script(buf);
    }

    protected byte[] getTimelockAsByteArray() {

      BigInteger biTimelock = BigInteger.valueOf(timelock);
      byte[] lock = new byte[biTimelock.toByteArray().length];
      System.arraycopy(biTimelock.toByteArray(), 0, lock, 0, biTimelock.toByteArray().length);

      ArrayUtils.reverse(lock);

      return lock;
    }

}
