package com.samourai.wallet.util;

import com.samourai.wallet.send.MyTransactionOutPoint;
import org.apache.commons.lang3.tuple.Triple;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;

public class FeeUtil {
  private static final Logger log = LoggerFactory.getLogger(FeeUtil.class);

  private static final int ESTIMATED_INPUT_LEN_P2PKH =
      147; // compressed key (180 uncompressed key)
  private static final int ESTIMATED_INPUT_LEN_P2SH_P2WPKH =
      91; // p2sh, includes segwit discount
  private static final int ESTIMATED_INPUT_LEN_P2WPKH = 68; // bech32, p2wpkh
  private static final int ESTIMATED_INPUT_LEN_P2TR = 58; // bech32m, taproot

  private static final int ESTIMATED_OUTPUT_LEN = 34;
  private static final int ESTIMATED_OUTPUT_P2TR_P2WSH_LEN = 43;
  private static final int ESTIMATED_OPRETURN_LEN = 92; // 80 bytes + overhead

  private static FeeUtil instance = null;

  public static FeeUtil getInstance() {
    if(instance == null) {
      instance = new FeeUtil();
    }
    return instance;
  }

  public int estimatedSizeSegwit  (
      int inputsP2PKH,
      int inputsP2SHP2WPKH,
      int inputsP2WPKH,
      int outputsNonOpReturn,
      int outputsOpReturn) {
    return estimatedSizeSegwit(inputsP2PKH, inputsP2SHP2WPKH, inputsP2WPKH, outputsNonOpReturn, 0, outputsOpReturn);
  }

  public int estimatedSizeSegwit  (
      int inputsP2PKH,
      int inputsP2SHP2WPKH,
      int inputsP2WPKH,
      int outputsNonP2WSH_P2TR,
      int outputsP2WSH_P2TR,
      int outputsOpReturn) {

    int txSize =
            + (outputsP2WSH_P2TR * ESTIMATED_OUTPUT_P2TR_P2WSH_LEN)
            + (outputsNonP2WSH_P2TR * ESTIMATED_OUTPUT_LEN)
            + (outputsOpReturn * ESTIMATED_OPRETURN_LEN)
            + (inputsP2PKH * ESTIMATED_INPUT_LEN_P2PKH)
            + (inputsP2SHP2WPKH * ESTIMATED_INPUT_LEN_P2SH_P2WPKH)
            + (inputsP2WPKH * ESTIMATED_INPUT_LEN_P2WPKH)
            + inputsP2PKH
            + inputsP2SHP2WPKH
            + inputsP2WPKH
            + 8
            + 1
            + 1;
    if (log.isTraceEnabled()) {
      log.trace(
          "tx size estimation: "
              + txSize
              + "b ("
              + inputsP2PKH
              + " insP2PKH, "
              + inputsP2SHP2WPKH
              + " insP2SHP2WPKH, "
              + inputsP2WPKH
              + " insP2WPKH, "
              + outputsNonP2WSH_P2TR
              + " outsNonP2WSH_P2TR, "
              + outputsP2WSH_P2TR
              + " outsP2WSH_P2TR, "
              + outputsOpReturn
              + " outsOpReturn)");
    }
    return txSize;
  }

  public BigInteger estimatedFeeSegwit(Collection<MyTransactionOutPoint> inputs, int outputsNonOpReturn, int outputsOpReturn, BigInteger feePerKb, NetworkParameters params)   {
    Triple<Integer,Integer,Integer> outpointTypes = getOutpointCount(inputs, params);
    return estimatedFeeSegwit(outpointTypes.getLeft(), outpointTypes.getMiddle(), outpointTypes.getRight(), outputsNonOpReturn, outputsOpReturn, feePerKb);
  }

  public BigInteger estimatedFeeSegwit(int inputsP2PKH, int inputsP2SHP2WPKH, int inputsP2WPKH, int outputsNonOpReturn, int outputsOpReturn, BigInteger feePerKb)   {
    int size = estimatedSizeSegwit(inputsP2PKH, inputsP2SHP2WPKH, inputsP2WPKH, outputsNonOpReturn, outputsOpReturn);
    return calculateFee(size, feePerKb);
  }

  public BigInteger estimatedFeeSegwit(int inputsP2PKH, int inputsP2SHP2WPKH, int inputsP2WPKH, int outputsNonP2WSH_P2TR, int outputsP2WSH_P2TR, int outputsOpReturn, BigInteger feePerKb)   {
      int size = estimatedSizeSegwit(inputsP2PKH, inputsP2SHP2WPKH, inputsP2WPKH, outputsNonP2WSH_P2TR, outputsP2WSH_P2TR, outputsOpReturn);
      return calculateFee(size, feePerKb);
  }

  public long estimatedFeeSegwit(
      int inputsP2PKH,
      int inputsP2SHP2WPKH,
      int inputsP2WPKH,
      int outputsNonOpReturn,
      int outputsOpReturn,
      long feePerB) {
    return estimatedFeeSegwit(inputsP2PKH, inputsP2SHP2WPKH, inputsP2WPKH, outputsNonOpReturn, 0, outputsOpReturn, feePerB);
  }

  public long estimatedFeeSegwit(
      int inputsP2PKH,
      int inputsP2SHP2WPKH,
      int inputsP2WPKH,
      int outputsNonP2WSH_P2TR,
      int outputsP2WSH_P2TR,
      int outputsOpReturn,
      long feePerB) {
    int size =
        estimatedSizeSegwit(
            inputsP2PKH, inputsP2SHP2WPKH, inputsP2WPKH, outputsNonP2WSH_P2TR, outputsP2WSH_P2TR, outputsOpReturn);
    long minerFee = calculateFee(size, feePerB);
    if (log.isTraceEnabled()) {
      log.trace("minerFee = " + minerFee + " (size=" + size + "b, feePerB=" + feePerB + "s/b)");
    }
    return minerFee;
  }

  public long calculateFee(int txSize, long feePerB) {
    long fee = txSize * feePerB;
    if (Math.ceil(fee) < txSize) {
      long adjustedFee = txSize + (txSize / 20);
      if (log.isTraceEnabled()) {
        log.trace("adjustedFee: " + adjustedFee + " (fee=" + fee + ", txSize=" + txSize + ")");
      }
      return adjustedFee;
    } else {
      return fee;
    }
  }

  public BigInteger calculateFee(int txSize, BigInteger feePerKb)   {
    long feePerB = toFeePerB(feePerKb);
    long fee = calculateFee(txSize, feePerB);
    return BigInteger.valueOf(fee);
  }

  public long toFeePerB(BigInteger feePerKb) {
    long feePerB = Math.round(feePerKb.doubleValue() / 1000.0);
    return feePerB;
  }

  public BigInteger toFeePerKB(long feePerB) {
    return BigInteger.valueOf(feePerB * 1000L);
  }

  public Triple<Integer,Integer,Integer> getOutpointCount(Collection<MyTransactionOutPoint> outpoints, NetworkParameters params) {

    int p2wpkh_p2tr = 0;
    int p2sh_p2wpkh = 0;
    int p2pkh = 0;

    for(MyTransactionOutPoint out : outpoints)   {
      if(FormatsUtilGeneric.getInstance().isValidBech32(out.getAddress()))    {
        p2wpkh_p2tr++;
      }
      else if(Address.fromBase58(params, out.getAddress()).isP2SHAddress())    {
        p2sh_p2wpkh++;
      }
      else   {
        p2pkh++;
      }
    }

    return Triple.of(p2pkh, p2sh_p2wpkh, p2wpkh_p2tr);
  }

  public int getOutpointCountP2TR(Collection<MyTransactionOutPoint> outpoints) {

    int count = 0;

    for(MyTransactionOutPoint out : outpoints)   {
      if(FormatsUtilGeneric.getInstance().isValidP2TR(out.getAddress()))    {
        count++;
      }
    }

    return count;
  }

  public int getOutpointCountP2WSH(Collection<MyTransactionOutPoint> outpoints) {

    int count = 0;

    for(MyTransactionOutPoint out : outpoints)   {
      if(FormatsUtilGeneric.getInstance().isValidP2WSH(out.getAddress()))    {
        count++;
      }
    }

    return count;
  }

  public int getOutpointCountP2TR_P2WSH(Collection<MyTransactionOutPoint> outpoints) {

    return getOutpointCountP2TR(outpoints) + getOutpointCountP2WSH(outpoints);

  }

}
