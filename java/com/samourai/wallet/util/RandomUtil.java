package com.samourai.wallet.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    private static final Logger log = LoggerFactory.getLogger(RandomUtil.class);

    private static RandomUtil instance = null;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static boolean testMode = false;

    public static RandomUtil getInstance() {
        if(instance == null) {
            instance = new RandomUtil();
        }
        return instance;
    }

    public static SecureRandom getSecureRandom() {
        return secureRandom;
    }

    //

    public byte[] nextBytes(int length) {
        if (testMode) {
            return new byte[length];
        }
        byte b[] = new byte[length];
        secureRandom.nextBytes(b);
        return b;
    }

    public String nextString(int length) {
        byte[] bytes = nextBytes(length);
        return new String(bytes, Charset.forName("UTF-8"));
    }

    public static int random(int minInclusive, int maxInclusive) {
        if (testMode) {
            return minInclusive;
        }
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    public static long random(long minInclusive, long maxInclusive) {
        if (testMode) {
            return minInclusive;
        }
        return ThreadLocalRandom.current().nextLong(minInclusive, maxInclusive + 1);
    }

    // returns random number between [min, bound-1]
    public int nextInt(int min, int bound) {
        if (testMode) {
            return min;
        }
        return min+getSecureRandom().nextInt(bound-min);
    }

    // returns random number between [0, bound-1]
    public int nextInt(int bound) {
        return nextInt(0, bound);
    }

    public long nextLong() {
        if (testMode) {
            return 0;
        }
        return getSecureRandom().nextLong();
    }

    public <E> E next(Collection<E> collection) {
        return (E)next(collection.toArray());
    }

    public <K, V> Map.Entry<K, V> next(Map<K, V> map) {
        if (map.isEmpty()) {
            log.warn("next(): map is empty");
            return null;
        }
        Object entries[] = map.entrySet().toArray();
        return (Map.Entry<K, V>) next(entries);
    }

    public <T> T next(T[] array) {
        if (array.length == 0) {
            log.warn("next(): array is empty");
            return null;
        }
        int i = nextInt(array.length);
        return array[i];
    }

    public void shuffle(List list) {
        if (testMode) {
            return;
        }
        Collections.shuffle(list);
    }

    public static void _setTestMode(boolean testMode) {
        testMode = testMode;
    }
}
