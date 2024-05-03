package com.samourai.wallet.util;

import com.samourai.wallet.test.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class UtilTest extends AbstractTest {

    @Test
    public void sha256Hex() throws Exception{
        Assertions.assertEquals("2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae", Util.sha256Hex("foo"));
    }

    @Test
    public void sha512Hex() throws Exception {
        Assertions.assertEquals("f7fbba6e0636f890e56fbbf3283e524c6fa3204ae298382d624741d0dc6638326e282c41be5e4254d8820772c5518a2c5a8c0c7f7eda19594a7eb539453e1ed7", Util.sha512Hex("foo"));
    }

    @Test
    public void formatDuration() {
        Assertions.assertEquals("1d 3h 46m 40s", Util.formatDuration(100000000));
        Assertions.assertEquals("2h 46m 40s", Util.formatDuration(10000000));
        Assertions.assertEquals("16m 40s", Util.formatDuration(1000000));
        Assertions.assertEquals("10s", Util.formatDuration(10000));
    }
}
