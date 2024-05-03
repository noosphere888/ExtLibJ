package com.samourai.wallet.cahoots;

import com.samourai.wallet.cahoots.multi.MultiCahoots;

public class CahootsTestUtil {
    public static void cleanPayload(Cahoots copy) {
        if (copy instanceof Cahoots2x) {
            Cahoots2x cahoots2x = (Cahoots2x)copy;
            // static values for test
            cahoots2x.strID = "testID";
            cahoots2x.ts = 123456;
            cahoots2x.psbt = null;
        }
        if (copy instanceof MultiCahoots) {
            MultiCahoots multiCahoots = (MultiCahoots)copy;
            cleanPayload(multiCahoots.getStonewallx2());
            cleanPayload(multiCahoots.getStowaway());
        }
    }
}
