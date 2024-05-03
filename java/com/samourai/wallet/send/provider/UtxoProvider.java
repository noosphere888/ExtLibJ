package com.samourai.wallet.send.provider;

import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.constants.SamouraiAccount;

import java.util.Collection;

public interface UtxoProvider extends UtxoKeyProvider {

    String getNextAddressChange(SamouraiAccount account, BipFormat bipFormat, boolean increment);

    Collection<UTXO> getUtxos(SamouraiAccount account);

    Collection<UTXO> getUtxos(SamouraiAccount account, BipFormat bipFormat);
}
