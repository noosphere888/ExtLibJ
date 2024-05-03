package com.samourai.wallet.util;

import com.samourai.wallet.test.AbstractTest;
import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.Sha256Hash;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class RandomUtilTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(RandomUtilTest.class);

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        RandomUtil._setTestMode(false);
    }

    @Test
    public void nextBytes() {
        int len = 10;
        byte[] res = RandomUtil.getInstance().nextBytes(len);
        Assertions.assertTrue(res.length == len);
    }

    @Test
    public void nextString() {
        int len = 10;
        String res = RandomUtil.getInstance().nextString(len);
        Assertions.assertTrue(res.length() == len);
    }

    @Test
    public void randomInt() {
        int res = RandomUtil.random(2,10);
        Assertions.assertTrue(res >= 2 && res <= 10);
    }

    @Test
    public void randomLong() {
        long res = RandomUtil.random(2L,10L);
        Assertions.assertTrue(res >= 2 && res <= 10);
    }

    @Test
    public void nextInt() {
        int res = RandomUtil.getInstance().nextInt(10);
        Assertions.assertTrue(res >= 2 && res < 10);
    }

    @Test
    public void nextLong() {
        long res = RandomUtil.getInstance().nextLong();
        Assertions.assertTrue(res >= 0L);
    }

    @Test
    public void nextList() {
        List<Integer> list = Arrays.asList(10,11,12);
        for (int i=0; i<10; i++) {
            int res = RandomUtil.getInstance().next(list);
            Assertions.assertTrue(list.contains(res));
        }
    }

    @Test
    public void nextArray() {
        Integer[] array = new Integer[]{10,11,12};
        for (int i=0; i<10; i++) {
            int res = RandomUtil.getInstance().next(array);
            Assertions.assertTrue(ArrayUtils.contains(array,res));
        }
    }
}
