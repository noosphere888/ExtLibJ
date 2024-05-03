package com.samourai.wallet.util;

import com.samourai.wallet.test.AbstractTest;
import org.bitcoinj.core.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FeeUtilTest extends AbstractTest {
  private static final FeeUtil feeUtil = FeeUtil.getInstance();

  @BeforeEach
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void estimatedSizeSegwit_cahoots() throws Exception {
    // ideally it should return 406, see 0edc06f566a615bf2f8cd9034744c41bcd282f702a9dbce17b21f06b2e75ad3f
    estimatedSizeSegwit(0, 0, 4, 4, 0, 0);
  }

  @Test
  public void estimatedSizeSegwitWhirlpoolTx0() throws Exception {
    estimatedSizeSegwit(0, 0, 1, 3, 0, 1);
  }

  @Test
  public void estimatedSizeSegwit_inputP2PKH() throws Exception {
    estimatedSizeSegwit(1, 0, 0, 1, 0, 0);
    estimatedSizeSegwit(2, 0, 0, 1, 0, 0);
    estimatedSizeSegwit(3, 0, 0, 1, 0, 0);
    estimatedSizeSegwit(4, 0, 0, 1, 0, 0);
  }

  @Test
  public void estimatedSizeSegwit_inputP2SHP2WPKH() throws Exception {
    estimatedSizeSegwit(0, 1, 0, 1, 0, 0);
    estimatedSizeSegwit(0, 2, 0, 1, 0, 0);
    estimatedSizeSegwit(0, 3, 0, 1, 0, 0);
    estimatedSizeSegwit(0, 4, 0, 1, 0, 0);
  }

  @Test
  public void estimatedSizeSegwit_inputP2WPKH() throws Exception {
    estimatedSizeSegwit(0, 0, 1, 1, 0, 0);
    estimatedSizeSegwit(0, 0, 2, 1, 0, 0);
    estimatedSizeSegwit(0, 0, 3, 1, 0, 0);
    estimatedSizeSegwit(0, 0, 4, 1, 0, 0);
  }

  @Test
  public void estimatedSizeSegwit_outputNonP2TR() throws Exception {
    estimatedSizeSegwit(0, 0, 1, 1, 0, 0);
    estimatedSizeSegwit(0, 0, 1, 2, 0, 0);
    estimatedSizeSegwit(0, 0, 1, 3, 0, 0);
    estimatedSizeSegwit(0, 0, 1, 4, 0, 0);
  }

  @Test
  public void estimatedSizeSegwit_outputOpReturn() throws Exception {
    estimatedSizeSegwit(0, 0, 1, 0, 0, 1);
    estimatedSizeSegwit(0, 0, 1, 0, 0, 2);
    estimatedSizeSegwit(0, 0, 1, 0, 0, 3);
    estimatedSizeSegwit(0, 0, 1, 0, 0, 4);
  }

  @Test
  public void estimatedSizeSegwit_outputP2TR() throws Exception {
    estimatedSizeSegwit(0, 0, 1, 0, 1, 0);
    estimatedSizeSegwit(0, 0, 1, 0, 2, 0);
    estimatedSizeSegwit(0, 0, 1, 0, 3, 0);
    estimatedSizeSegwit(0, 0, 1, 0, 4, 0);
  }

  @Test
  public void estimatedSizeSegwit() throws Exception {
    estimatedSizeSegwit(0, 0, 1, 1, 0, 0);
    estimatedSizeSegwit(0, 0, 1, 1, 0, 0);
    estimatedSizeSegwit(1, 0, 0, 1, 0, 0);
    estimatedSizeSegwit(1, 0, 0, 1, 0, 1);

    estimatedSizeSegwit(0, 0, 3, 3, 0, 0);
    estimatedSizeSegwit(0, 0, 4, 4, 0, 0);
    estimatedSizeSegwit(0, 0, 5, 5, 0, 0);
  }

  @Test
  public void estimatedSizeSegwit_taproot() throws Exception {
    estimatedSizeSegwit(0, 0, 1, 1, 0, 0);
    estimatedSizeSegwit(0, 0, 1, 1, 1, 0);

    estimatedSizeSegwit(1, 0, 0, 1, 0, 0);
    estimatedSizeSegwit(1, 0, 0, 1, 1, 0);

    estimatedSizeSegwit(1, 0, 0, 1, 0, 1);
    estimatedSizeSegwit(1, 0, 0, 1, 1, 1);

    estimatedSizeSegwit(0, 0, 3, 3, 0, 0);
    estimatedSizeSegwit(0, 0, 3, 3, 1,0);

    estimatedSizeSegwit(0, 0, 4, 4, 0, 0);
    estimatedSizeSegwit(0, 0, 4, 4, 1, 0);

    estimatedSizeSegwit(0, 0, 5, 5, 0, 0);
    estimatedSizeSegwit(0, 0, 5, 5, 1, 0);
  }

  @Test
  public void estimatedFeeSegwit_nonP2TR() throws Exception {
    Assertions.assertEquals(118, feeUtil.estimatedFeeSegwit(0, 0, 1, 1, 0, 0));
    Assertions.assertEquals(113, feeUtil.estimatedFeeSegwit(0, 0, 1, 1, 0, 1));
    Assertions.assertEquals(1130, feeUtil.estimatedFeeSegwit(0, 0, 1, 1, 0, 10));
    Assertions.assertEquals(11300, feeUtil.estimatedFeeSegwit(0, 0, 1, 1, 0, 100));

    Assertions.assertEquals(1920, feeUtil.estimatedFeeSegwit(1, 0, 0, 1, 0, 10));
    Assertions.assertEquals(2840, feeUtil.estimatedFeeSegwit(1, 0, 0, 1, 1, 10));
    Assertions.assertEquals(3190, feeUtil.estimatedFeeSegwit(0, 0, 3, 3, 0, 10));
  }

  @Test
  public void estimatedFeeSegwit_taproot() throws Exception {
    Assertions.assertEquals(118, feeUtil.estimatedFeeSegwit(0, 0, 1, 1, 0, 0, 0));
    Assertions.assertEquals(128, feeUtil.estimatedFeeSegwit(0, 0, 1, 0, 1, 0, 0));
    Assertions.assertEquals(208, feeUtil.estimatedFeeSegwit(0, 0, 1, 1, 2, 0, 0));

    Assertions.assertEquals(113, feeUtil.estimatedFeeSegwit(0, 0, 1, 1, 0, 0, 1));
    Assertions.assertEquals(122, feeUtil.estimatedFeeSegwit(0, 0, 1, 0, 1, 0, 1));
    Assertions.assertEquals(199, feeUtil.estimatedFeeSegwit(0, 0, 1, 1, 2, 0, 1));

    Assertions.assertEquals(1130, feeUtil.estimatedFeeSegwit(0, 0, 1, 1, 0, 0, 10));
    Assertions.assertEquals(1220, feeUtil.estimatedFeeSegwit(0, 0, 1, 0, 1, 0, 10));
    Assertions.assertEquals(1990, feeUtil.estimatedFeeSegwit(0, 0, 1, 1, 2, 0, 10));

    Assertions.assertEquals(11300, feeUtil.estimatedFeeSegwit(0, 0, 1, 1, 0, 0, 100));
    Assertions.assertEquals(12200, feeUtil.estimatedFeeSegwit(0, 0, 1, 0, 1, 0, 100));
    Assertions.assertEquals(19900, feeUtil.estimatedFeeSegwit(0, 0, 1, 1, 2, 0, 100));

    Assertions.assertEquals(1920, feeUtil.estimatedFeeSegwit(1, 0, 0, 1, 0, 0, 10));
    Assertions.assertEquals(2010, feeUtil.estimatedFeeSegwit(1, 0, 0, 0, 1, 0, 10));
    Assertions.assertEquals(2780, feeUtil.estimatedFeeSegwit(1, 0, 0, 1, 2, 0, 10));

    Assertions.assertEquals(2840, feeUtil.estimatedFeeSegwit(1, 0, 0, 1, 0, 1, 10));
    Assertions.assertEquals(2930, feeUtil.estimatedFeeSegwit(1, 0, 0, 0, 1, 1, 10));
    Assertions.assertEquals(3700, feeUtil.estimatedFeeSegwit(1, 0, 0, 1, 2, 1, 10));
  }

  private void estimatedSizeSegwit(int inputsP2PKH,
                                   int inputsP2SHP2WPKH,
                                   int inputsP2WPKH,
                                   int outputsNonP2WSH_P2TR,
                                   int outputsP2WSH_P2TR,
                                   int outputsOpReturn) throws Exception {
    // estimate
    long estimatedSize = feeUtil.estimatedSizeSegwit(inputsP2PKH, inputsP2SHP2WPKH, inputsP2WPKH, outputsNonP2WSH_P2TR, outputsP2WSH_P2TR, outputsOpReturn);

    // check against real tx
    Transaction tx = cryptoTestUtil.generateTx(inputsP2PKH, inputsP2SHP2WPKH, inputsP2WPKH, outputsNonP2WSH_P2TR, outputsP2WSH_P2TR, outputsOpReturn);
    long realSize = tx.getVirtualTransactionSize();
    long diff = estimatedSize-realSize;
    log.info("estimatedSizeSegwit: inputs="+inputsP2PKH+"/"+inputsP2SHP2WPKH+"/"+inputsP2WPKH+", outputs="+outputsNonP2WSH_P2TR+"/"+outputsP2WSH_P2TR+"/"+outputsOpReturn+", estimatedSize="+estimatedSize+", realSize="+realSize+" => diff="+diff);

    // must not underestimate realSize
    Assertions.assertTrue(estimatedSize >= realSize, "estimatedSize < realSize");
    Assertions.assertTrue(diff<21, "estimation is too far from realSize");
  }
}
