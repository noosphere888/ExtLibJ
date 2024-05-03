package com.samourai.wallet;

import org.bitcoinj.core.Coin;

import java.math.BigInteger;

public class SamouraiWalletConst {
    public static final BigInteger bDust = BigInteger.valueOf(Coin.parseCoin("0.00000546").longValue());    // https://github.com/bitcoin/bitcoin/pull/2760

    // hard limit for acceptable fees 0.005
    public static final long MAX_ACCEPTABLE_FEES = 500000;

    public static final BigInteger RBF_SEQUENCE_VAL_WITH_NLOCKTIME = BigInteger.valueOf(0xffffffffL - 1L);
    public static final BigInteger RBF_SEQUENCE_VAL = BigInteger.valueOf(0xffffffffL - 2L);
    public static final BigInteger NLOCKTIME_SEQUENCE_VAL = BigInteger.valueOf(0xffffffffL - 3L);

    public static final String samouraiDonationPCode = "PM8TJVzLGqWR3dtxZYaTWn3xJUop3QP3itR4eYzX7XvV5uAfctEEuHhKNo3zCcqfAbneMhyfKkCthGv5werVbwLruhZyYNTxqbCrZkNNd2pPJA2e2iAh";
    public static final  String SAAS_PCODE_MAINNET = "PM8TJUYeU1rF5zcVkNsiN6LEzikQXH4NTtgzL7bCbnznNtef5mWVi9i3LetDByv9HHMTq5czDppAP4gyimC7LyNLiC1hfAyQCHvDBP3EzPU5sx3yyxRY";
    public static final  String SAAS_PCODE_TESTNET = "PM8TJXBr2UNrPuhTFrmiCrww74GCFm1WbTqpxEXACpfzAsKqM3xvgZPG2PhDGycW2Ud9RiCzVHb3NprRvGffpYbi9bw6sYjU5nZJm94syV1J67V9fRND";

    public static String getSaasPcode(boolean testnet) {
        return testnet ? SAAS_PCODE_TESTNET : SAAS_PCODE_MAINNET;
    }
}
