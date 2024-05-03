package com.samourai.wallet.bip47.rpc.obpp05;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;

import org.bouncycastle.util.encoders.Hex;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class SecretPoint {

    private PrivateKey privKey = null;
    private PublicKey pubKey = null;

    private KeyFactory kf = null;

    private static final ECParameterSpec params = ECNamedCurveTable.getParameterSpec("secp256k1");

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private SecretPoint()    { ; }

    public SecretPoint(byte[] dataPrv, byte[] dataPub) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        kf = KeyFactory.getInstance("ECDH", "BC");
        privKey = loadPrivateKey(dataPrv);
        pubKey = loadPublicKey(dataPub);
    }

    public PrivateKey getPrivKey() {
        return privKey;
    }

    public void setPrivKey(PrivateKey privKey) {
        this.privKey = privKey;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public void setPubKey(PublicKey pubKey) {
        this.pubKey = pubKey;
    }

    public byte[] ECDHSecretAsBytes() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException  {
        return ECDHSecret().getEncoded();
    }

    public boolean isShared(SecretPoint secret) throws Exception {
        return equals(secret);
    }

    private SecretKey ECDHSecret() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException  {

        KeyAgreement ka = KeyAgreement.getInstance("ECDH", "BC");
        ka.init(privKey);
        ka.doPhase(pubKey, true);
        SecretKey secret = ka.generateSecret("AES");

        return secret;
    }

    private boolean equals(SecretPoint secret) throws Exception {
        return Hex.toHexString(this.ECDHSecretAsBytes()).equals(Hex.toHexString(secret.ECDHSecretAsBytes()));
    }

    private PublicKey loadPublicKey(byte[] data) throws InvalidKeySpecException {
        ECPublicKeySpec pubKey = new ECPublicKeySpec(params.getCurve().decodePoint(data), params);
        return kf.generatePublic(pubKey);
    }

    private PrivateKey loadPrivateKey(byte[] data) throws InvalidKeySpecException {
        ECPrivateKeySpec prvkey = new ECPrivateKeySpec(new BigInteger(1, data), params);
        return kf.generatePrivate(prvkey);
    }

}
