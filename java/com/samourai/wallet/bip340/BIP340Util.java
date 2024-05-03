package com.samourai.wallet.bip340;

import com.samourai.wallet.segwit.bech32.Bech32Segwit;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcoinj.params.TestNet3Params;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class BIP340Util {
    protected static final Logger log = LoggerFactory.getLogger(BIP340Util.class);

    public static Point getInternalPubkey(ECKey eckey) {
        ECPoint ecPoint = eckey.getPubKeyPoint();
        Point point = new Point(ecPoint.getAffineXCoord().toBigInteger(), ecPoint.getAffineYCoord().toBigInteger());
        Point iPoint = Point.liftX(point.getX().toByteArray());
        return iPoint;
    }

    public static String getP2TRAddress(NetworkParameters params, Point opoint) {
        if (Point.isSecp256k1(opoint.toBytes())) {
            String address = Bech32Segwit.encode(params instanceof TestNet3Params ? "tb" : "bc", (byte) 0x01, BigIntegers.asUnsignedByteArray(opoint.getX()));
            return address;
        } else {
            return null;
        }
    }

    public static String getP2TRAddress(NetworkParameters params, ECKey eckey, boolean tweak) {
        try {
            Point iPoint = getInternalPubkey(eckey); // may throw IllegalStateException: point not in normal form
            Point oPoint = null;
            if (tweak) {
                oPoint = getTweakedPubKeyFromPoint(iPoint);
            } else {
                oPoint = iPoint;
            }
            return getP2TRAddress(params, oPoint);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    /**
     * @param originalPrivKey The original private key.
     * @param hash            For more complex Taproot functionality you would commit to a scripthash tree. For single-sig wallets this will almost always be null.
     * @return Returns a private key in bytes.
     * @throws IOException
     */
    public static ECKey getTweakedPrivKey(ECKey originalPrivKey, byte[] hash) throws IOException {
        BigInteger privKey0 = originalPrivKey.getPrivKey();
        Point privPoint = Point.mul(Point.getG(), originalPrivKey.getPrivKey());
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

    public static Point getTweakedPubKeyFromPoint(Point ipoint) {
        Point oPoint = null;
        try {
            byte[] taggedHash = Point.taggedHash("TapTweak", BigIntegers.asUnsignedByteArray(ipoint.getX()));
            oPoint = Point.add(Point.getG().mul(new BigInteger(1, taggedHash)), ipoint);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }

        return oPoint;
    }
}
