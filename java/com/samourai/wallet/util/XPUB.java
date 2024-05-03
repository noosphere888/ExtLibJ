package com.samourai.wallet.util;

import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.hd.Purpose;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class XPUB {

    private static final int CHAIN_LEN = 32;
    private static final int PUBKEY_LEN = 33;
    private static final int VERSION_LEN = 4;
    private static final int FINGERPRINT_LEN = 4;
    private static final int CHILD_LEN = 4;
    private static final int DEPTH_LEN = 1;

    public static final int MAGIC_XPUB = 0x0488B21E;
    public static final int MAGIC_TPUB = 0x043587CF;
    public static final int MAGIC_YPUB = 0x049D7CB2;
    public static final int MAGIC_UPUB = 0x044A5262;
    public static final int MAGIC_ZPUB = 0x04B24746;
    public static final int MAGIC_VPUB = 0x045F1CF6;
    private static final long HARDENED = 2147483648L;

    private String strXPUB = null;

    private byte[] chain = null;
    private byte[] pub = null;
    private byte depth = 0x00;
    private int version = -1;
    private int fingerprint = -1;
    private int child = -1;

    private XPUB()  { ; }

    public XPUB(String xpub)   {

        strXPUB = xpub;
        chain = new byte[CHAIN_LEN];
        pub = new byte[PUBKEY_LEN];

    }

    public void decode() throws AddressFormatException {

        byte[] xpubBytes = Base58.decodeChecked(strXPUB);

        ByteBuffer bb = ByteBuffer.wrap(xpubBytes);
        version = bb.getInt();
        if(version != MAGIC_XPUB && version != MAGIC_TPUB && version != MAGIC_YPUB && version != MAGIC_UPUB && version != MAGIC_ZPUB && version != MAGIC_VPUB)   {
            throw new AddressFormatException("invalid xpub version");
        }

        // depth:
        depth = bb.get();
        // parent fingerprint:
        fingerprint = bb.getInt();
        // child no.
        child = bb.getInt();
        // chain
        bb.get(chain);
        //
        bb.get(pub);

    }

    public byte[] getPubkey() {
        return pub;
    }

    public byte[] getChain() {
        return chain;
    }

    public byte getDepth() {
        return depth;
    }

    public int getVersion() {
        return version;
    }

    public int getPurpose() {
        switch(version) {
            case MAGIC_XPUB: case MAGIC_TPUB:
                return Purpose.PURPOSE_44;
            case MAGIC_YPUB: case MAGIC_UPUB:
                return Purpose.PURPOSE_49;
            case MAGIC_ZPUB: case MAGIC_VPUB:
                return Purpose.PURPOSE_84;
            default:
                throw new RuntimeException("Unknown purpose for version: "+version);
        }
    }

    public boolean isTestnet() {
        return version == MAGIC_TPUB || version == MAGIC_UPUB || version == MAGIC_VPUB;
    }

    public int getFingerprint() {
        return fingerprint;
    }

    public int getChild() {
        return child;
    }

    public int getAccount() {
        return (int)(child + HARDENED);
    }

    public String getPathAddress(int chainIndex, int addressIndex) {
        NetworkParameters params = FormatsUtilGeneric.getInstance().getNetworkParams(isTestnet());
        int coinType = FormatsUtilGeneric.getInstance().getCoinType(params);
        return HD_Address.getPathAddress(getPurpose(), coinType, getAccount(), chainIndex, addressIndex);
    }

    public static String makeXPUB(byte[] version, byte[] depth, byte[] fingerprint, byte[] child, byte[] chain, byte[] pubkey) {

        if(version.length != VERSION_LEN) {
            return null;
        }
        if(depth.length != DEPTH_LEN) {
            return null;
        }
        if(fingerprint.length != FINGERPRINT_LEN) {
            return null;
        }
        if(child.length != CHILD_LEN) {
            return null;
        }
        if(chain.length != CHAIN_LEN) {
            return null;
        }
        if(pubkey.length != PUBKEY_LEN) {
            return null;
        }

        byte[] buf = new byte[78];

        System.arraycopy(version, 0, buf, 0, version.length);
        System.arraycopy(depth, 0, buf, version.length, depth.length);
        System.arraycopy(fingerprint, 0, buf, version.length + depth.length, fingerprint.length);
        System.arraycopy(child, 0, buf, version.length + depth.length + fingerprint.length, child.length);
        System.arraycopy(chain, 0, buf, version.length + depth.length + fingerprint.length + child.length, chain.length);
        System.arraycopy(pubkey, 0, buf, version.length + depth.length + fingerprint.length + child.length + chain.length, pubkey.length);

        byte[] xpub = new byte[78 + 4];
        byte[] checksum = Arrays.copyOfRange(Sha256Hash.hashTwice(buf), 0, 4);
        System.arraycopy(buf, 0, xpub, 0, buf.length);
        System.arraycopy(checksum, 0, xpub, 78, checksum.length);

        String ret = Base58.encode(xpub);

        return ret;
    }

    public static String makeXPUB(int version, byte depth, int fingerprint, int child, byte[] chain, byte[] pubkey) {

        return makeXPUB(ByteBuffer.allocate(VERSION_LEN).putInt(version).array(), new byte[] { depth }, ByteBuffer.allocate(FINGERPRINT_LEN).putInt(fingerprint).array(), ByteBuffer.allocate(CHILD_LEN).putInt(child).array(), chain,  pubkey);

    }

    public static String makeXPUB(int version, byte depth, int fingerprint, int child, String chain, String pubkey) {

        return makeXPUB(ByteBuffer.allocate(VERSION_LEN).putInt(version).array(), new byte[] { depth }, ByteBuffer.allocate(FINGERPRINT_LEN).putInt(fingerprint).array(), ByteBuffer.allocate(CHILD_LEN).putInt(child).array(), Util.hexToBytes(chain),  Util.hexToBytes(pubkey));

    }

}
