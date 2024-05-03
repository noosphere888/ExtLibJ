package com.samourai.wallet.segwit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.script.Script;

public class FidelityTimelockAddress extends TimelockAddress {

    private int timelockIndex = 0;

    public FidelityTimelockAddress(byte[] pubkey, NetworkParameters params, int index) throws Exception {
        super(pubkey, params, TYPE_P2WSH);
        this.timelockIndex = index;
        this.timelock = getTimelockAsUnixTime();
    }

    public FidelityTimelockAddress(ECKey ecKey, NetworkParameters params, int index) throws Exception {
        super(ecKey, params);
        this.DEFAULT_TO = TYPE_P2WSH;
        this.timelockIndex = index;
        this.timelock = getTimelockAsUnixTime();
    }

    public Script segwitRedeemScript()    {
        //
        // <timelock> OP_CHECKLOCKTIMEVERIFY OP_DROP <derived_key> OP_CHECKSIG
        //
        byte[] lock = getTimelockAsByteArray();
        byte[] locklen = new byte[1];
        locklen[0] = (byte)lock.length;
        byte[] pubkey = this.ecKey.getPubKey();
        byte[] buf = new byte[1 + lock.length + 2 + 1 + pubkey.length + 1];
        System.arraycopy(locklen, 0, buf, 0, locklen.length);
        System.arraycopy(lock, 0, buf, locklen.length, lock.length);
        buf[locklen.length + lock.length] = (byte)0xb1;
        buf[locklen.length + lock.length + 1] = (byte)0x75;
        buf[locklen.length + lock.length + 2] = (byte)0x21;
        System.arraycopy(pubkey, 0, buf, locklen.length + lock.length + 3, pubkey.length);
        buf[locklen.length + lock.length + 3 + pubkey.length] = (byte)0xac;

        return new Script(buf);
    }

    private long getTimelockAsUnixTime() throws Exception {

        int year = 2020 + (timelockIndex / 12);
        int month = 1 + (timelockIndex % 12);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        String dateString = String.format("%4d", year);
        dateString += "-";
        dateString += String.format("%02d", month);
        dateString += "-";
        dateString += "01 00:00:00 UTC";
        Date date = dateFormat.parse(dateString);

        long unixTime = date.getTime() / 1000L;

        return unixTime;
    }

}
