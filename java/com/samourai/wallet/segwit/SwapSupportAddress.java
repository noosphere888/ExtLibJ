package com.samourai.wallet.segwit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

import com.samourai.wallet.segwit.bech32.Bech32Segwit;
import com.samourai.wallet.util.Util;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;

import org.bouncycastle.util.encoders.Hex;

import org.apache.commons.lang3.ArrayUtils;

public class SwapSupportAddress extends SegwitAddress {
	
	public static final long SEQUENCE_LOCK   = 0xffffffff;
	public static final long SEQUENCE_CANCEL = 0x0c;
	public static final long SEQUENCE_REFUND = 0xffffffff;
	public static final long SEQUENCE_PUNISH = 0x06;

    protected ECKey ecKey1 = null;

    public SwapSupportAddress(byte[] pubkey0, byte[] pubkey1, NetworkParameters params) throws Exception {
        super(pubkey0, params, TYPE_P2WSH);
        ecKey1 = ECKey.fromPublicOnly(pubkey1);
    }

    public SwapSupportAddress(ECKey ecKey0, ECKey ecKey1, NetworkParameters params) throws Exception {
        super(ecKey0, params);
        this.DEFAULT_TO = TYPE_P2WSH;
        this.ecKey1 = ecKey1;
    }

    public String getSwapSupportAddressAsString()    {

        String address = null;

        try {
            address = Bech32Segwit.encode(params instanceof TestNet3Params ? "tb" : "bc", (byte)0x00, Util.sha256(redeemScript().getProgram()));
        }
        catch(Exception e) {
            ;
        }

        return address;
    }

    public String getDefaultToAddressAsString()  {

      return getSwapSupportAddressAsString();

    }

    public Script outputScript() throws NoSuchAlgorithmException    {

        byte[] hash = Util.sha256(redeemScript().getProgram());
        byte[] buf = new byte[2 + hash.length];
        buf[0] = (byte)0x00;
        buf[1] = (byte)0x20;
        System.arraycopy(hash, 0, buf, 2, hash.length);

        return new Script(buf);
    }

    public Script redeemScript()  {
        //
        // Redeem script: <33 byte push> OP_CHECKSIGVERIFY <33 byte push> OP_CHECKSIG
        //
        byte[] buf = new byte[1 + ecKey.getPubKey().length + 1 + ecKey1.getPubKey().length + 2];
        buf[0] = (byte)0x21;
        System.arraycopy(ecKey.getPubKey(), 0, buf, 1, ecKey.getPubKey().length);
        buf[1 + ecKey.getPubKey().length] = (byte)0xad;
        buf[1 + ecKey.getPubKey().length + 1] = (byte)0x21;
        System.arraycopy(ecKey1.getPubKey(), 0, buf, 1 + ecKey.getPubKey().length + 1 + 1, ecKey1.getPubKey().length);
        buf[1 + ecKey.getPubKey().length + 1 + 1 + ecKey1.getPubKey().length] = (byte)0xac;

        return new Script(buf);
    }

}
