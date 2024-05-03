package com.samourai.wallet.bip47.rpc;

import com.samourai.wallet.bip47.BIP47UtilGeneric;
import com.samourai.wallet.crypto.CryptoUtil;
import com.samourai.wallet.crypto.impl.ECDHKeySet;
import com.samourai.wallet.hd.HD_Address;
import com.samourai.wallet.util.MessageSignUtilGeneric;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bip47PartnerImpl implements Bip47Partner {
  private static final Logger log = LoggerFactory.getLogger(Bip47PartnerImpl.class);
  private static final MessageSignUtilGeneric messageSignUtil = MessageSignUtilGeneric.getInstance();

  private BIP47Account bip47Account;
  private PaymentCode paymentCodePartner;
  private boolean initiator; // true if my role is initiator, false if partner is initiator

  private CryptoUtil cryptoUtil;
  private BIP47UtilGeneric bip47Util;

  private HD_Address notificationAddressMine;
  private HD_Address notificationAddressPartner;
  private ECDHKeySet sharedSecret;
  private String sharedAddressBech32;

  public Bip47PartnerImpl(
          BIP47Account bip47Account,
      PaymentCode paymentCodePartner,
      boolean initiator,
      CryptoUtil cryptoUtil,
      BIP47UtilGeneric bip47Util) throws Exception {
    this.bip47Account = bip47Account;
    this.paymentCodePartner = paymentCodePartner;
    this.initiator = initiator;

    this.cryptoUtil = cryptoUtil;
    this.bip47Util = bip47Util;

    // precompute these values and throw on invalid paymentcode
    NetworkParameters params = bip47Account.getParams();
    this.notificationAddressMine = bip47Account.getNotificationAddress();
    this.notificationAddressPartner = paymentCodePartner.notificationAddress(params);
    this.sharedSecret = cryptoUtil.getSharedSecret(notificationAddressMine.getECKey(), notificationAddressPartner.getECKey());
    PaymentAddress sharedPaymentAddress = bip47Util.getPaymentAddress(paymentCodePartner, 0, notificationAddressMine.getECKey(), params);
    this.sharedAddressBech32 = sharedPaymentAddress.getSegwitAddress(initiator).getBech32AsString();
  }

  @Override
  public Bip47Partner createNewIdentity(BIP47Account bip47AccountNewIdentity) throws Exception {
    return new Bip47PartnerImpl(bip47AccountNewIdentity, paymentCodePartner, initiator, cryptoUtil, bip47Util);
  }

  @Override
  public String sign(String message) {
    ECKey notificationAddressKey = notificationAddressMine.getECKey();
    return messageSignUtil.signMessage(notificationAddressKey, message);
  }

  @Override
  public boolean verifySignature(String message, String signature) {
    NetworkParameters params = bip47Account.getParams();
    String signingAddress = notificationAddressPartner.getAddressString();
    return messageSignUtil.verifySignedMessage(signingAddress, message, signature, params);
  }

  @Override
  public String decrypt(byte[] encrypted) throws Exception {
    return cryptoUtil.decryptString(encrypted, sharedSecret);
  }

  @Override
  public byte[] encrypt(String payload) throws Exception {
    return cryptoUtil.encrypt(payload.getBytes(), sharedSecret);
  }

  @Override
  public BIP47Account getBip47Account() {
    return bip47Account;
  }

  @Override
  public PaymentCode getPaymentCodePartner() {
    return paymentCodePartner;
  }

  @Override
  public String getSharedAddressBech32() {
    return sharedAddressBech32;
  }

  protected ECDHKeySet getSharedSecret() {
    return sharedSecret;
  }
}
