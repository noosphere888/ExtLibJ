package com.samourai.wallet.taproot;

import com.samourai.wallet.bip340.BIP340Util;
import com.samourai.wallet.segwit.P2TRAddress;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TaprootRedeemScriptTests  {
    private static final String PRIVATE_KEY = "0cbb6e8bc0271939a1ea977ef52b6a3d9bb7e918d4bfb97c7bd2951052898c08";
    private static final NetworkParameters PARAMS = MainNetParams.get();
    @Test
    public void redeemScriptTests() throws Exception {
        ECKey key = ECKey.fromPrivate(Hex.decode(PRIVATE_KEY));
        Assertions.assertEquals("15NxdNkHC11jL2tDDLbCe9a1fzVhwW5QFJ", key.toAddress(PARAMS).toBase58());
        P2TRAddress p2trAddress = new P2TRAddress(key, PARAMS);
        Assertions.assertEquals("bc1qxqr6cm2xnmxv09pmmvtxvrg0xhxg7kjg5sv4r6", p2trAddress.getBech32AsString());
        Script taprootRedeemScript0 = p2trAddress.segwitRedeemScript();
        // If our current key was a Taproot address or a tweaked key, this would be our redeem script:
        Assertions.assertEquals("5120a7a1e130c202bec7e87b5b35b7b85a9e2cf274b71f133ab76b8a4a9a5f8a8072", Hex.toHexString(taprootRedeemScript0.getProgram()));

        ECKey tweakedPrivKey = BIP340Util.getTweakedPrivKey(key, null);
        P2TRAddress taprootAddress = new P2TRAddress(tweakedPrivKey, null);
        Script taprootRedeemScript1 = taprootAddress.segwitRedeemScript();
        // The original key could be tweaked or not, the outside world wouldn't know. Only we do, so we tweak it above, and here's the new redeem script:
        Assertions.assertEquals("5120b6aef8b3d0e502ac28a46daa0eaf28cb98d9b1736c52e57223ef3acec0be79be", Hex.toHexString(taprootRedeemScript1.getProgram()));
    }
}
