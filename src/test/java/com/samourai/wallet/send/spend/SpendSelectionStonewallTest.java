package com.samourai.wallet.send.spend;

import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.constants.SamouraiAccount;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

public class SpendSelectionStonewallTest extends AbstractSpendTest {

    public SpendSelectionStonewallTest() throws Exception {
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    private Pair<List<UTXO>, List<UTXO>> stonewallInputs(BipFormat changeFormat, long amount, SamouraiAccount account) throws Exception {
        BigInteger feePerKb = BigInteger.valueOf(50);
        List<Collection<UTXO>> utxoSets = StonewallUtil.getInstance().utxoSets(utxoProvider, changeFormat, account);
        return StonewallUtil.getInstance().stonewallInputs(utxoSets, changeFormat, amount, params, feePerKb);
    }

    @Test
    public void stonewallInputs_noBalance() throws Exception {
        long amount = 100000;
        SamouraiAccount account = SamouraiAccount.DEPOSIT;
        BipFormat changeFormat = BIP_FORMAT.SEGWIT_NATIVE;

        // no utxo

        // test
        Pair<List<UTXO>, List<UTXO>> inputs = stonewallInputs(changeFormat, amount, account);
        Assertions.assertNull(inputs); // stonewall not possible
    }

    @Test
    public void stonewallInputs_insufficient() throws Exception {
        long amount = 100000;
        SamouraiAccount account = SamouraiAccount.DEPOSIT;
        BipFormat changeFormat = BIP_FORMAT.SEGWIT_NATIVE;

        // set utxos
        utxoProvider.addUtxo(depositWallet84, 9000);

        // test
        Pair<List<UTXO>, List<UTXO>> inputs = stonewallInputs(changeFormat, amount, account);
        Assertions.assertNull(inputs); // stonewall not possible
    }

    @Test
    public void stonewallInputs_insufficient_exact() throws Exception {
        long amount = 100000;
        SamouraiAccount account = SamouraiAccount.DEPOSIT;
        BipFormat changeFormat = BIP_FORMAT.SEGWIT_NATIVE;

        // set utxos
        utxoProvider.addUtxo(depositWallet84, amount);

        // test
        Pair<List<UTXO>, List<UTXO>> inputs = stonewallInputs(changeFormat, amount, account);
        Assertions.assertNull(inputs); // stonewall not possible
    }

    @Test
    public void stonewallInputs_exact() throws Exception {
        long amount = 100000;
        SamouraiAccount account = SamouraiAccount.DEPOSIT;
        BipFormat changeFormat = BIP_FORMAT.SEGWIT_NATIVE;

        // set utxos
        utxoProvider.addUtxo(depositWallet84, amount);

        // test
        Pair<List<UTXO>, List<UTXO>> inputs = stonewallInputs(changeFormat, amount, account);
        Assertions.assertNull(inputs); // stonewall not possible
    }

    @Test
    public void stonewallInputs_1_utxo() throws Exception {
        long amount = 1000000;
        SamouraiAccount account = SamouraiAccount.DEPOSIT;
        BipFormat changeFormat = BIP_FORMAT.SEGWIT_NATIVE;

        // set utxos
        utxoProvider.addUtxo(depositWallet84, 3333333);

        // test
        Pair<List<UTXO>, List<UTXO>> inputs = stonewallInputs(changeFormat, amount, account);
        Assertions.assertEquals(1, inputs.getLeft().size());
        Assertions.assertNull(inputs.getRight());
    }

    @Test
    public void stonewallInputs_2_utxos() throws Exception {
        long amount = 1000000;
        SamouraiAccount account = SamouraiAccount.DEPOSIT;
        BipFormat changeFormat = BIP_FORMAT.SEGWIT_NATIVE;

        // set utxos
        utxoProvider.addUtxo(depositWallet84, 3333333);
        utxoProvider.addUtxo(depositWallet44, 4444444);

        // test
        Pair<List<UTXO>, List<UTXO>> inputs = stonewallInputs(changeFormat, amount, account);
        Assertions.assertEquals(1, inputs.getLeft().size());
        Assertions.assertEquals(1, inputs.getRight().size());
    }
}
