package com.samourai.wallet.bip47.rpc.obpp05;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import com.samourai.wallet.util.Util;

public class PaymentCode {

    private static final byte MAGIC_VALUE = (byte)0x22;
    private static final byte VERSION_3 = (byte)0x03;

    private static final int BLIND_LEN = 32;
    private static final int CHECKSUM_LEN = 4;
    private static final int PAYLOAD_LEN = 35;
    private static final int PRIVLEY_LEN = 32;
    private static final int PUBKEY_LEN = 33;

    private String strPaymentCode = null;
    private byte[] payload = new byte[PAYLOAD_LEN];
    private String xprv = null;

    private PaymentCode() {
        ;
    }

    public PaymentCode(String xpubstr)  {
        payload = payload(xpubstr);
        System.arraycopy(payload, 0, this.payload, 0, this.payload.length);
        strPaymentCode = serialize(payload);
    }

    public PaymentCode(byte[] payload)  {
        System.arraycopy(payload, 0, this.payload, 0, this.payload.length);
        strPaymentCode = serialize(payload);
    }

    public void setXprv(String xprvstr) {
        byte[] decode = null;
        try {
            decode = Base58.decodeChecked(xprvstr);
            xprv = xprvstr;
        }
        catch (AddressFormatException afe) {
            xprv = null;
        }
    }

    public boolean hasPrivate()  {
        return (xprv == null) ? false : true;
    }

    public String toString()  {
        return strPaymentCode;
    }

    public byte[] getPubkey() throws AddressFormatException  {
        byte[] pubkey = new byte[PUBKEY_LEN];
        byte[] payload = Base58.decode(strPaymentCode);
        System.arraycopy(payload, 2, pubkey, 0, pubkey.length);
        return pubkey;
    }

    public byte[] getChainCode() throws AddressFormatException, NoSuchAlgorithmException  {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(getPubkey());
        byte[] ret = digest.digest(hash);

        return ret;
    }

    public byte[] getPayload() throws AddressFormatException    {
        byte[] pcBytes = Base58.decodeChecked(strPaymentCode);

        byte[] payload = new byte[pcBytes.length - 1];
        System.arraycopy(pcBytes, 1, payload, 0, payload.length);

        return payload;
    }

    public byte[] getNotifPubkey() throws AddressFormatException, NoSuchAlgorithmException  {
        return derivePubkey(0);
    }

    public byte[] derivePubkey(int idx) throws AddressFormatException, NoSuchAlgorithmException  {

        DeterministicKey mKey = null;
        DeterministicKey aKey = null;

        byte[] pubkey = getPubkey();
        byte[] chaincode = getChainCode();

        mKey = HDKeyDerivation.createMasterPubKeyFromBytes(pubkey, chaincode);
        aKey = HDKeyDerivation.deriveChildKey(mKey, new ChildNumber(idx, false));
        ECKey eckey = ECKey.fromPublicOnly(aKey.getPubKey());

        return eckey.getPubKey();
    }

    public byte[] getNotifPrivkey() throws AddressFormatException, NoSuchAlgorithmException  {
        return derivePrivkey(0);
    }

    public byte[] derivePrivkey(int idx) throws AddressFormatException, NoSuchAlgorithmException  {

        if(!hasPrivate())    {
            return null;
        }

        DeterministicKey pKey = null;
        DeterministicKey aKey = null;

        byte[] chaincode = getChainCode();

        byte[] xprivbuf = Base58.decodeChecked(xprv);
        byte[] privkey = new byte[PRIVLEY_LEN];
        System.arraycopy(xprivbuf, xprivbuf.length - 32, privkey, 0, privkey.length);

        pKey = HDKeyDerivation.createMasterPrivKeyFromBytes(privkey, chaincode);
        aKey = HDKeyDerivation.deriveChildKey(pKey, new ChildNumber(idx, false));
        ECKey eckey = ECKey.fromPrivate(aKey.getPrivKeyBytes(), true);

        return eckey.getPrivKeyBytes();
    }

    public byte[] getIdentifierV1() throws AddressFormatException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException   {
        return getIdentifier(new byte[] { (byte)0x01 });
    }

    public byte[] getIdentifierV2() throws AddressFormatException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException   {
        return getIdentifier(new byte[] { (byte)0x02 });
    }

    public byte[] getIdentifierV3() throws AddressFormatException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException   {
        return getIdentifier(new byte[] { (byte)0x03 });
    }

    public byte[] blind(ECKey eckey, PaymentCode oPcode) throws Exception    {

        SecretPoint secretPoint = new SecretPoint(eckey.getPrivKeyBytes(), oPcode.getNotifPubkey());
        byte[] secretPointX = secretPoint.ECDHSecretAsBytes();
        byte[] blindFactor = Util.getHMAC(secretPointX, eckey.getPubKey());
        byte[] G = new byte[PUBKEY_LEN];
        G[0] = getPayload()[1];
        byte[] bf = new byte[BLIND_LEN];
        byte[] pc = new byte[BLIND_LEN];
        System.arraycopy(blindFactor, 0, bf, 0, bf.length);
        byte[] pl = getPayload();
        System.arraycopy(pl, 2, pc, 0, pc.length);
        System.arraycopy(Util.xor(bf, pc), 0, G, 1, Util.xor(bf, pc).length);

        return G;
    }

    public byte[] unblind(byte[] A, byte[] G) throws AddressFormatException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {

        if(!hasPrivate())    {
            return null;
        }

        SecretPoint secretPoint = new SecretPoint(getNotifPrivkey(), A);
        byte[] secretPointX = secretPoint.ECDHSecretAsBytes();
        byte[] blindFactor = Util.getHMAC(secretPointX, A);
        byte[] bf = new byte[BLIND_LEN];
        System.arraycopy(blindFactor, 0, bf, 0, bf.length);
        byte[] pl = new byte[PAYLOAD_LEN];
        byte[] pc = new byte[BLIND_LEN];
        System.arraycopy(G, 1, pc, 0, pc.length);
        pl[0] = MAGIC_VALUE;
        pl[1] = VERSION_3;
        pl[2] = G[0];
        System.arraycopy(Util.xor(bf, pc), 0, pl, 3, Util.xor(bf, pc).length);

        return pl;
    }

    public static PaymentCode fromV1(String pcodeV1) throws AddressFormatException, BufferUnderflowException    {

      com.samourai.wallet.bip47.rpc.PaymentCode pcV1 = new com.samourai.wallet.bip47.rpc.PaymentCode(pcodeV1);
      if(!pcV1.isValid())    {
        return null;
      }

      byte[] pub = new byte[PUBKEY_LEN];
      byte[] pcodeBytes = Base58.decodeChecked(pcodeV1);
      ByteBuffer bb = ByteBuffer.wrap(pcodeBytes);
      bb.get();
      bb.get();
      bb.get();
      bb.get(pub);

      byte[] payload = new byte[PAYLOAD_LEN];
      payload[0] = MAGIC_VALUE;
      payload[1] = VERSION_3;
      System.arraycopy(pub, 0, payload, 2, PUBKEY_LEN);

      return new PaymentCode(payload);
    }

    private byte[] getIdentifier(byte[] ver) throws AddressFormatException,InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException    {
        byte[] ret = new byte[PUBKEY_LEN];
        ret[0] = (byte)0x02;
        System.arraycopy(Util.getHMAC(getChainCode(), ver), 0, ret, 1, ret.length - 1);
        return ret;
    }

    private byte[] payload(String xpubstr)  {
        byte[] decode = null;
        byte[] payload = new byte[PAYLOAD_LEN];
        try {
            decode = Base58.decodeChecked(xpubstr);
        } catch (AddressFormatException afe) {
            payload = null;
            return null;
        }
        payload[0] = MAGIC_VALUE;
        payload[1] = VERSION_3;
        System.arraycopy(decode, decode.length - 33, payload, 2, PUBKEY_LEN);

        return payload;
    }

    private String serialize(byte[] payload)  {
        byte[] checksum = Arrays.copyOfRange(Sha256Hash.hashTwice(payload), 0, CHECKSUM_LEN);
        byte[] payment_code_checksum = new byte[payload.length + checksum.length];
        System.arraycopy(payload, 0, payment_code_checksum, 0, payload.length);
        System.arraycopy(checksum, 0, payment_code_checksum, payment_code_checksum.length - CHECKSUM_LEN, checksum.length);

        return Base58.encode(payment_code_checksum);
    }

}
