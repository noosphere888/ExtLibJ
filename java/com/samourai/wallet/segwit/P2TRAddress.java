package com.samourai.wallet.segwit;

import com.samourai.wallet.bip340.Point;
import com.samourai.wallet.segwit.bech32.Bech32Segwit;
import com.samourai.wallet.util.Util;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;

public class P2TRAddress extends SegwitAddress {

    private boolean tweak = true;

    public P2TRAddress(byte[] pubkey, NetworkParameters params) throws Exception {
        super(pubkey, params, TYPE_P2TR);
    }

    public P2TRAddress(ECKey ecKey, NetworkParameters params) throws Exception {
        super(ecKey, params);
        this.DEFAULT_TO = TYPE_P2TR;
    }

    public P2TRAddress(byte[] pubkey, NetworkParameters params, boolean tweak) throws Exception {
        super(pubkey, params, TYPE_P2TR);
        this.tweak = tweak;
    }

    public P2TRAddress(ECKey ecKey, NetworkParameters params, boolean tweak) throws Exception {
        super(ecKey, params);
        this.DEFAULT_TO = TYPE_P2TR;
        this.tweak = tweak;
    }

    public String getP2TRAddressAsString()    {

        String address = null;
        Point oPoint = null;
        if(tweak) {
            oPoint = getTweakedPubKeyFromPoint();
        } else {
            oPoint = getInternalPubKey();
        }

        try {
          if (Point.isSecp256k1(oPoint.toBytes())) {
              address = Bech32Segwit.encode(params instanceof TestNet3Params ? "tb" : "bc", (byte) 0x01, BigIntegers.asUnsignedByteArray(oPoint.getX()));
          } else {
              return null;
          }
        }
        catch(Exception e) {
            ;
        }

        return address;
    }

    public String getDefaultToAddressAsString()  {

      return getP2TRAddressAsString();

    }

    public Script segwitOutputScript() throws java.security.NoSuchAlgorithmException  {

        byte[] hash = Util.sha256(this.segwitRedeemScript().getProgram());
        byte[] buf = new byte[2 + hash.length];
        buf[0] = (byte)0x51;
        buf[1] = (byte)0x20;
        System.arraycopy(hash, 0, buf, 2, hash.length);

        return new Script(buf);
    }

    public Script segwitRedeemScript()    {

      //
      // The P2TR redeemScript is always 34 bytes. It starts with a OP_1, followed by a canonical push of the tweaked pub key (i.e. 0x0120{32-byte tweaked key})
      //
      Point internalPubKeyPoint = getInternalPubKey();

      if(internalPubKeyPoint == null) return null;

      byte[] tweakedPubKey = internalPubKeyPoint.toBytes();
      byte[] buf = new byte[2 + tweakedPubKey.length];
      buf[0] = (byte)0x51;  // OP_1
      buf[1] = (byte)0x20;  // push 32 bytes
      System.arraycopy(tweakedPubKey, 0, buf, 2, tweakedPubKey.length); // tweaked key

      return new Script(buf);
    }

    public Point getInternalPubKey() {
        ECPoint ecPoint = ecKey.getPubKeyPoint();
        Point point = new Point(ecPoint.getAffineXCoord().toBigInteger(), ecPoint.getAffineYCoord().toBigInteger());
        Point iPoint = Point.liftX(point.getX().toByteArray());
        return iPoint;
    }

    public Point getTweakedPubKeyFromPoint() {
        Point iPoint = getInternalPubKey();
        Point oPoint = null;
        try {
            byte[] taggedHash = Point.taggedHash("TapTweak", BigIntegers.asUnsignedByteArray(iPoint.getX()));
            oPoint = Point.add(Point.getG().mul(new BigInteger(1, taggedHash)), iPoint);
        }
        catch (Exception e) {
            return null;
        }

        return oPoint;
    }

    /**
     * @param originalPrivKey The original private key.
     * @param hash            For more complex Taproot functionality you would commit to a scripthash tree. For single-sig wallets this will almost always be null.
     * @return Returns a private key in bytes.
     * @throws IOException
     */
    public ECKey getTweakedPrivKey(byte[] hash) throws IOException {
        BigInteger privKey0 = ecKey.getPrivKey();
        Point privPoint = Point.mul(Point.getG(), ecKey.getPrivKey());
        BigInteger privKey;
        if (privPoint.hasEvenY()) {
            privKey = privKey0;
        } else {
            privKey = Point.getn().subtract(privKey0);
        }
        ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(32);
        byte[] tag = Sha256Hash.hash("TapTweak".getBytes());
        bos.write(tag);
        bos.write(tag);
        bos.write(privPoint.toBytes());
        if (hash != null) {
            bos.write(hash);
        }
        byte[] tweak = Sha256Hash.hash(bos.toByteArray());
        ECKey tweakKey = ECKey.fromPrivate(tweak);
        return ECKey.fromPrivate((privKey.add(tweakKey.getPrivKey())).mod(Point.getn()));
    }

}
