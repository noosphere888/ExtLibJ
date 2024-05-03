package com.samourai.wallet.send.spend;

import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipFormat.BipFormat;
import com.samourai.wallet.send.MyTransactionOutPoint;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.send.beans.SpendError;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.beans.SpendTxSimple;
import com.samourai.wallet.send.beans.SpendType;
import com.samourai.wallet.send.exceptions.SpendException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;

public class SpendBuilderTest extends AbstractSpendTest {
    private SpendBuilder spendBuilder;

    private static final long BLOCK_HEIGHT = 12345678;

    public SpendBuilderTest() throws Exception {
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        spendBuilder = new SpendBuilder(utxoProvider);
    }

    private SpendTx spend(String address, long amount, boolean stonewall) throws Exception {
        // spend
        BigInteger feePerKb = BigInteger.valueOf(50);
        BipFormat forcedChangeFormat = null;
        List<MyTransactionOutPoint> preselectedInputs = null;
        return spendBuilder.preview(depositWallet84, depositWallet84, address, amount, stonewall, true, feePerKb, forcedChangeFormat, preselectedInputs, BLOCK_HEIGHT);
    }

    @Test
    public void simpleSpend_noBalance() throws Exception {
        long amount = 10000;

        // no utxo

        // spend
        try {
            spend(ADDRESS_BIP84, amount, true);
            Assertions.assertTrue(false);
        } catch (SpendException e) {
            Assertions.assertEquals(SpendError.INSUFFICIENT_FUNDS, e.getSpendError());
        }
    }

    @Test
    public void simpleSpend_insufficient() throws Exception {
        long amount = 10000;

        // set utxos
        utxoProvider.addUtxo(depositWallet84, 9000);

        // spend
        try {
            spend(ADDRESS_BIP84, amount, true);
            Assertions.assertTrue(false);
        } catch (SpendException e) {
            Assertions.assertEquals(SpendError.INSUFFICIENT_FUNDS, e.getSpendError());
        }
    }

    @Test
    public void simpleSpend_single_bip84() throws Exception {
        long amount = 10000;

        // set utxos
        UTXO utxo1 = utxoProvider.addUtxo(depositWallet84, 10000);
        UTXO utxo2 = utxoProvider.addUtxo(depositWallet84, 20000);

        // should select smallest single utxo
        SpendTx spendTx = spend(ADDRESS_BIP84, amount, true);
        long changeExpected = 9846;

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(ADDRESS_BIP84, amount);
        outputs.put(ADDRESS_CHANGE_84[0], changeExpected);
        verifySpendTx(spendTx, SpendType.SIMPLE, Arrays.asList(utxo2), 154, 154, 0, amount, false, changeExpected, BIP_FORMAT.SEGWIT_NATIVE,
                outputs,
                "a741d9519c16630cf9bea61117d44b192939556edfb9796b8268778d6c22c5ea",
                "010000000001010f934535839dd32f98a041695796f68c43b1e087f3ab68d1380d915ed5dd130d0200000000fdffffff027626000000000000160014f6a884f18f4d7e78a4167c3e56773c3ae58e016410270000000000001600142ecf8c3e5697f0513999ed3aa8ea9cd04d12735502483045022100819354b00d504a5dcff40e355ada09d69c69e1412d0ff0b8df7f1dcbdb837739022048d6b920bb1b94f4b80a6b2592b694e2cb902faf89935c19dbca70b9c413ccdc0121020fd264b3db00aa2c43e4688e824f5a6e5247b8474620c3ce53cd20316a457a6400000000");
    }

    @Test
    public void simpleSpend_single_bip49() throws Exception {
        long amount = 10000;

        // set utxos
        UTXO utxo1 = utxoProvider.addUtxo(depositWallet84, 10000);
        UTXO utxo2 = utxoProvider.addUtxo(depositWallet84, 20000);

        // should select smallest single utxo
        SpendTx spendTx = spend(ADDRESS_BIP49, amount, true);
        long changeExpected = 9846;

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(ADDRESS_BIP49, amount);
        outputs.put(ADDRESS_CHANGE_49[0], changeExpected);
        verifySpendTx(spendTx, SpendType.SIMPLE, Arrays.asList(utxo2), 154, 154, 0, amount, false, changeExpected, BIP_FORMAT.SEGWIT_COMPAT,
                outputs,
                "6f2b26e5429b725f7fdb707bd5f1ed4cc7d6d6f4dc4e8305b0a81048e5b4aed7",
                "010000000001010f934535839dd32f98a041695796f68c43b1e087f3ab68d1380d915ed5dd130d0200000000fdffffff02762600000000000017a9142df6e5e10a713bed1d1753d78285c386dc20f34a87102700000000000017a914336caa13e08b96080a32b5d818d59b4ab3b36742870247304402201733ad64e273b1ac0dbfd610074bb9fe15aba4afb5b36b8ad5696335ec06d87502204f89ae09a63b460bb4ea321def67371cca00f2b91c4508ec402020d11c6101930121020fd264b3db00aa2c43e4688e824f5a6e5247b8474620c3ce53cd20316a457a6400000000");
    }

    @Test
    public void simpleSpend_2utxos() throws Exception {
        long amount = 40000;

        // set utxos
        UTXO utxo1 = utxoProvider.addUtxo(depositWallet84, 10000);
        UTXO utxo2 = utxoProvider.addUtxo(depositWallet84, 20000);
        UTXO utxo3 = utxoProvider.addUtxo(depositWallet84, 30000);
        UTXO utxo4 = utxoProvider.addUtxo(depositWallet84, 500);

        // should select largest Utxos
        SpendTx spendTx = spend(ADDRESS_BIP84, amount, true);
        long changeExpected = 9774;

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(ADDRESS_BIP84, amount);
        outputs.put(ADDRESS_CHANGE_84[0], changeExpected);
        verifySpendTx(spendTx, SpendType.SIMPLE, Arrays.asList(utxo3, utxo2), 226, 226, 0, amount, false, changeExpected, BIP_FORMAT.SEGWIT_NATIVE,
                outputs,
                "869c346d8f2440b87c98e7f42d92ff3113bec1f3c940f9d5b3c343eab5ece6fa",
                "010000000001020f934535839dd32f98a041695796f68c43b1e087f3ab68d1380d915ed5dd130d0200000000fdffffff4a2c8e9e650ec859ca8ca1f63da4791284c8e09c8eed9d0e641962c8fe38f4e60300000000fdffffff022e26000000000000160014f6a884f18f4d7e78a4167c3e56773c3ae58e0164409c0000000000001600142ecf8c3e5697f0513999ed3aa8ea9cd04d12735502473044022015607215be89704738e8d1afbb5b9b26fa98773010cac1c7ec69163a4953824302200d9fbac976f2f5a0edd6accedc54bd119b51805d5a7ed3a1788d89ca4f29a0e50121020fd264b3db00aa2c43e4688e824f5a6e5247b8474620c3ce53cd20316a457a64024730440220794d87a3780cb773f3bd74307d550bfa4a4ede8b1a8eb67765c4f1a552fd2587022003b9ddfd25edc034e07331e775b4c07fcb2ced7f64a09e31009cc2bdaf04146e0121032b6f9c80bcba58d5da83f8270a1a19a8f19109223d9e3df3c298bd6468bba6cc00000000");
    }

    @Test
    public void simpleSpend_3utxos() throws Exception {
        long amount = 50000;

        // set utxos
        UTXO utxo1 = utxoProvider.addUtxo(depositWallet84, 10000);
        UTXO utxo2 = utxoProvider.addUtxo(depositWallet84, 20000);
        UTXO utxo3 = utxoProvider.addUtxo(depositWallet84, 30000);
        UTXO utxo4 = utxoProvider.addUtxo(depositWallet84, 500);

        // should select largest Utxos
        SpendTx spendTx = spend(ADDRESS_BIP84, amount, true);
        long changeExpected = 9701;

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(ADDRESS_BIP84, amount);
        outputs.put(ADDRESS_CHANGE_84[0], changeExpected);
        verifySpendTx(spendTx, SpendType.SIMPLE, Arrays.asList(utxo3, utxo2, utxo1), 299, 299, 0, amount, false, changeExpected, BIP_FORMAT.SEGWIT_NATIVE,
                outputs,
                "9672ecb40977b9d4d817570d352b9f55a96457f977a4421bdcb2455bd3f69d2b",
                "010000000001030f934535839dd32f98a041695796f68c43b1e087f3ab68d1380d915ed5dd130d0200000000fdffffffcbaf7f91af27d64b157eeeaee086a25a0057bff334e491f1b9e9a66a4c7b002b0100000000fdffffff4a2c8e9e650ec859ca8ca1f63da4791284c8e09c8eed9d0e641962c8fe38f4e60300000000fdffffff02e525000000000000160014f6a884f18f4d7e78a4167c3e56773c3ae58e016450c30000000000001600142ecf8c3e5697f0513999ed3aa8ea9cd04d12735502483045022100d8b55ec97b7f0f1126e55533009044c9876a4e83dab87ff0a6fa2f8f843597440220061cc2f0dfac5c584e51e65ae9e8e69338ba5d64dcf9adae378a617aa20dce480121020fd264b3db00aa2c43e4688e824f5a6e5247b8474620c3ce53cd20316a457a640247304402205a6c897cce081f012e4a870928b88a9fd491ccd6667573def4efb551a16f44a1022052bb550cef0b13e8c29646aae4ed8aad7d047781bb22677f38eadab82c5a7ddc01210229950f82d3230b06db77c9aec65180e5bbf99fd837fe967dbabe4d9b0422146c024730440220769393e2d5bd123075e4515702d7d2b78a93884202bc6e1f81d170392a75afab022007bb3e4fe048ab66224c29f6f72889867c99a097820cea6f21292cad760704bd0121032b6f9c80bcba58d5da83f8270a1a19a8f19109223d9e3df3c298bd6468bba6cc00000000");
    }

    @Test
    public void simpleSpend_entireBalance() throws Exception {
        long amount = 60500;

        // set utxos
        UTXO utxo1 = utxoProvider.addUtxo(depositWallet84, 10000);
        UTXO utxo2 = utxoProvider.addUtxo(depositWallet84, 20000);
        UTXO utxo3 = utxoProvider.addUtxo(depositWallet84, 30000);
        UTXO utxo4 = utxoProvider.addUtxo(depositWallet84, 500);

        // spend entire balance
        SpendTx spendTx = spend(ADDRESS_BIP84, amount, true);

        // fees deduced from amount, no change
        long changeExpected = 0;
        long amountExpected = 60164;
        boolean entireBalanceExpected = true;
        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(ADDRESS_BIP84, amountExpected);
        verifySpendTx(spendTx, SpendType.SIMPLE, Arrays.asList(utxo1, utxo2, utxo3, utxo4), 336, 336, 0, amountExpected, entireBalanceExpected, changeExpected, BIP_FORMAT.SEGWIT_NATIVE,
                outputs,
                "e56bf90a0c548f0bb24a8b299f81c59b61b382b3c501da4e60e25e70222b6913",
                "010000000001040f934535839dd32f98a041695796f68c43b1e087f3ab68d1380d915ed5dd130d0200000000fdffffffcbaf7f91af27d64b157eeeaee086a25a0057bff334e491f1b9e9a66a4c7b002b0100000000fdffffffb2b62d26c5d91df9ac9b3d26a86762966379df322f645b1a2cbe9f95655622380400000000fdffffff4a2c8e9e650ec859ca8ca1f63da4791284c8e09c8eed9d0e641962c8fe38f4e60300000000fdffffff0104eb0000000000001600142ecf8c3e5697f0513999ed3aa8ea9cd04d12735502483045022100a014b9888f497c3323e623a469a02cfde272b53098bb3b9fe83f3d93dd07d4a702201836b681c2a896df3465ea58cc54b4f59f34b7412f918ac7075e1112d99dc8e70121020fd264b3db00aa2c43e4688e824f5a6e5247b8474620c3ce53cd20316a457a6402483045022100bd8872d55af8aa295431e0ce794bc001200dd6eeefb0726e5464e38e79a0015d02202624d5faa94ed3a2315a5778956106e2fae5f8a4e09dfe23843a4354e03190ee01210229950f82d3230b06db77c9aec65180e5bbf99fd837fe967dbabe4d9b0422146c02483045022100cc024301a0fbc82f881b8088459a94b06369e4885a85a234f3dbffec90e13f7c0220482016b36c61d19a66dfda8fbf029c203c21cecaedd80a6ab9a1777992b33005012102fbb6632bb7263a07d4f3e43ea6e92922b86d813785068145c49de168ed21dd8602473044022005d3ff676facc7208ce5a7870eb22ced41fad58a75e944f6065c5308bfb3adda022043db427f1da0db055063fda9032b83b22ef1be68e7d079196284b13a7077bac70121032b6f9c80bcba58d5da83f8270a1a19a8f19109223d9e3df3c298bd6468bba6cc00000000");
    }

    @Test
    public void stonewall_bip84() throws Exception {
        long amount = 40000;

        // set utxos
        UTXO utxo1 = utxoProvider.addUtxo(depositWallet84, 10000);
        UTXO utxo2 = utxoProvider.addUtxo(depositWallet84, 10000);
        UTXO utxo3 = utxoProvider.addUtxo(depositWallet84, 20000);
        UTXO utxo4 = utxoProvider.addUtxo(depositWallet84, 20000);
        UTXO utxo5 = utxoProvider.addUtxo(depositWallet84, 30000);
        UTXO utxo6 = utxoProvider.addUtxo(depositWallet84, 30000);
        UTXO utxo7 = utxoProvider.addUtxo(depositWallet84, 40000);
        UTXO utxo8 = utxoProvider.addUtxo(depositWallet84, 40000);

        // should select STONEWALL
        SpendTx spendTx = spend(ADDRESS_BIP84, amount, true);

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(ADDRESS_BIP84, amount);
        outputs.put(ADDRESS_CHANGE_84[0], 9742L);
        outputs.put(ADDRESS_CHANGE_84[1], amount);
        outputs.put(ADDRESS_CHANGE_84[2], 19742L);
        verifySpendTx(spendTx, SpendType.STONEWALL, Arrays.asList(utxo3, utxo4, utxo2, utxo6, utxo5), 516, 516, 0, amount, false, 69484, BIP_FORMAT.SEGWIT_NATIVE,
                outputs,
                "2cc43c44fe3b63fffc66c584b8afef12fd610617f581426ac66b5fa18470b017",
                "020000000001050f934535839dd32f98a041695796f68c43b1e087f3ab68d1380d915ed5dd130d0200000000fdffffffb2b62d26c5d91df9ac9b3d26a86762966379df322f645b1a2cbe9f95655622380400000000fdffffff710eee3a480151e5b479bd0d1952dca0397c0c82e1da65d906606c5dd7779e5a0600000000fdffffff9bfd729fa0f2fce42b6a4346f7218eeaa624ce346e4cc8c1b2dd8a318a06686a0500000000fdffffff4a2c8e9e650ec859ca8ca1f63da4791284c8e09c8eed9d0e641962c8fe38f4e60300000000fdffffff040e26000000000000160014f6a884f18f4d7e78a4167c3e56773c3ae58e01641e4d0000000000001600144910e17f5ca698222657369753a164262605087a409c0000000000001600141bd05eb7c9cb516fddd8187cecb2e0cb4e21ac87409c0000000000001600142ecf8c3e5697f0513999ed3aa8ea9cd04d12735502483045022100ef2f3ec5fa3f3f0d8c994e0857765643d90e2c9762dc8b7d7b45c8a0ea6209600220737d1df6e16a3236d7e0dad765b24f1d0670b2447f9fa20372a1b864a11e9c4e0121020fd264b3db00aa2c43e4688e824f5a6e5247b8474620c3ce53cd20316a457a64024730440220042189efd3205f01f237d4ae302b5917db189b362ec8fd6a598d4ae55b33e37e02201326424310845ebfe43879313a6070eff70cbc87b766a5c243cb2b2cf5f73b2a012102fbb6632bb7263a07d4f3e43ea6e92922b86d813785068145c49de168ed21dd8602483045022100fbe19675b8ce7bd11780f3a49bb61792eb882f18aa261bb5754268701cda60f80220230891004982f3193533feacbb96f3f1664abd1fe5cdc92c964824d5b62c64ae012102b2a7e1a1911083a9a8750921de866e4f14afcbe0d3d9d934fecb5b6a5c0ce3a40247304402201b9bad005f5e2037925f93e79bb85e627084fb393367aea8a1b11f4850a3e152022064d647131bc7be70de9c63052c0fb4868e79ffe307e16ebb52013308e07f6d52012102bd1d0567294f540aff84cbbe0d999b81f58a2357939ca734576de20ec3730ec302483045022100f52f81d06d152fb5f6cf6a9ab647a0852a395302965108347b871c4f6b4465fe02203fb679a3cefec7524dede301983090045573d78b070f724b250fb42dde098d310121032b6f9c80bcba58d5da83f8270a1a19a8f19109223d9e3df3c298bd6468bba6cc4e61bc00");
    }

    @Test
    public void stonewall_bip49() throws Exception {
        long amount = 40000;

        // set utxos
        UTXO utxo1 = utxoProvider.addUtxo(depositWallet84, 10000);
        UTXO utxo2 = utxoProvider.addUtxo(depositWallet84, 10000);
        UTXO utxo3 = utxoProvider.addUtxo(depositWallet84, 20000);
        UTXO utxo4 = utxoProvider.addUtxo(depositWallet84, 20000);
        UTXO utxo5 = utxoProvider.addUtxo(depositWallet84, 30000);
        UTXO utxo6 = utxoProvider.addUtxo(depositWallet84, 30000);
        UTXO utxo7 = utxoProvider.addUtxo(depositWallet84, 40000);
        UTXO utxo8 = utxoProvider.addUtxo(depositWallet84, 40000);

        // should select STONEWALL
        SpendTx spendTx = spend(ADDRESS_BIP49, amount, true);

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(ADDRESS_BIP49, amount);
        outputs.put(ADDRESS_CHANGE_49[0], amount);
        outputs.put(ADDRESS_CHANGE_84[0], 9742L);
        outputs.put(ADDRESS_CHANGE_84[1], 19742L);
        verifySpendTx(spendTx, SpendType.STONEWALL, Arrays.asList(utxo3, utxo4, utxo2, utxo6, utxo5), 516, 516, 0, amount, false, 69484, BIP_FORMAT.SEGWIT_COMPAT,
                outputs,
                "2ca71a7bce12ef95a33bacac12c91b3e2e2ed8ae03a95ac51fe59faea5eed039",
                "020000000001050f934535839dd32f98a041695796f68c43b1e087f3ab68d1380d915ed5dd130d0200000000fdffffffb2b62d26c5d91df9ac9b3d26a86762966379df322f645b1a2cbe9f95655622380400000000fdffffff710eee3a480151e5b479bd0d1952dca0397c0c82e1da65d906606c5dd7779e5a0600000000fdffffff9bfd729fa0f2fce42b6a4346f7218eeaa624ce346e4cc8c1b2dd8a318a06686a0500000000fdffffff4a2c8e9e650ec859ca8ca1f63da4791284c8e09c8eed9d0e641962c8fe38f4e60300000000fdffffff040e26000000000000160014f6a884f18f4d7e78a4167c3e56773c3ae58e01641e4d0000000000001600141bd05eb7c9cb516fddd8187cecb2e0cb4e21ac87409c00000000000017a9142df6e5e10a713bed1d1753d78285c386dc20f34a87409c00000000000017a914336caa13e08b96080a32b5d818d59b4ab3b36742870247304402200731d584da0be21ee6725c3b0c596379b5e5d0a5da115af5dddfd001e2ca6b9b022019cb2e3ff74052619716720f3673345323a7efd31af84d5b387c218f717174b20121020fd264b3db00aa2c43e4688e824f5a6e5247b8474620c3ce53cd20316a457a6402483045022100f2ef298a57c5bf9d0182db024f2a4ea3b4d021c4f52c692664d3a42086d700a102204938651c435661e592dd940dde4904fc381eee167c384f9b2da8574e56c70a6d012102fbb6632bb7263a07d4f3e43ea6e92922b86d813785068145c49de168ed21dd860247304402200bac3025391534227a0368dee85964689741c9c5a387bde22718943d3c49afe102204a8477d7f3d7aa08457a5de5a7f086666f9382b1a2958f7d972e42c5e2466eca012102b2a7e1a1911083a9a8750921de866e4f14afcbe0d3d9d934fecb5b6a5c0ce3a402473044022050d65b659c4a2c6a7e9ad5d7642779157d6be527017a5a14dccbb7c126f5711602202f8c9d068e21f3ef9a1a83397ec7b538eab961c1c9812304bbeca0fffd099eaf012102bd1d0567294f540aff84cbbe0d999b81f58a2357939ca734576de20ec3730ec302483045022100d0af4a2648d9e9a948469cd18cb29e71fbed4074403eeaaf38b7d34f167a4ef202206296789c2890e58457e0697be01d796d581530853052baccf4f3543994386b430121032b6f9c80bcba58d5da83f8270a1a19a8f19109223d9e3df3c298bd6468bba6cc4e61bc00");
    }

    @Test
    public void stonewall_bip44() throws Exception {
        long amount = 40000;

        // set utxos
        UTXO utxo1 = utxoProvider.addUtxo(depositWallet84, 10000);
        UTXO utxo2 = utxoProvider.addUtxo(depositWallet84, 10000);
        UTXO utxo3 = utxoProvider.addUtxo(depositWallet84, 20000);
        UTXO utxo4 = utxoProvider.addUtxo(depositWallet84, 20000);
        UTXO utxo5 = utxoProvider.addUtxo(depositWallet84, 30000);
        UTXO utxo6 = utxoProvider.addUtxo(depositWallet84, 30000);
        UTXO utxo7 = utxoProvider.addUtxo(depositWallet84, 40000);
        UTXO utxo8 = utxoProvider.addUtxo(depositWallet84, 40000);

        // should select STONEWALL
        SpendTx spendTx = spend(ADDRESS_BIP44, amount, true);

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(ADDRESS_BIP44, amount);
        outputs.put(ADDRESS_CHANGE_44[0], amount);
        outputs.put(ADDRESS_CHANGE_84[0], 9742L);
        outputs.put(ADDRESS_CHANGE_84[1], 19742L);
        verifySpendTx(spendTx, SpendType.STONEWALL, Arrays.asList(utxo8, utxo5, utxo3, utxo4, utxo2), 516, 516, 0, amount, false, 69484, BIP_FORMAT.LEGACY,
                outputs,
                "b4d7b0d0acf19ef8ab416ffd2971e68dc4fc0c020c16afd0fcc092415fdf8a70",
                "020000000001050f934535839dd32f98a041695796f68c43b1e087f3ab68d1380d915ed5dd130d0200000000fdffffffb2b62d26c5d91df9ac9b3d26a86762966379df322f645b1a2cbe9f95655622380400000000fdffffff710eee3a480151e5b479bd0d1952dca0397c0c82e1da65d906606c5dd7779e5a0600000000fdffffff9bfd729fa0f2fce42b6a4346f7218eeaa624ce346e4cc8c1b2dd8a318a06686a0500000000fdffffff4a2c8e9e650ec859ca8ca1f63da4791284c8e09c8eed9d0e641962c8fe38f4e60300000000fdffffff040e26000000000000160014f6a884f18f4d7e78a4167c3e56773c3ae58e01641e4d0000000000001600141bd05eb7c9cb516fddd8187cecb2e0cb4e21ac87409c0000000000001976a91479f7962428741fa25e70036f6719e2c11efa75d388ac409c0000000000001976a9149bcdad097fa4695a3bdab991e3212da04002ce4088ac0247304402207e74dae7e551c0e5856330102d845d6815506095ebaf0929aeec5f0267f44619022048d25962d3472ad2c6b1707b878380d52c0860331f004a952ce031ea787b44670121020fd264b3db00aa2c43e4688e824f5a6e5247b8474620c3ce53cd20316a457a64024730440220693cecc9bfb4f481146c4ee841a5d89f9f6f66e214c10bb68206c0dca5cb5fa002201bc5a8d18fd57cc2e1ac7154994cd8e48f923c2c034d32c7abf2f23044dd92e4012102fbb6632bb7263a07d4f3e43ea6e92922b86d813785068145c49de168ed21dd8602483045022100f87aa0fa176c96f0cdd1b7b2ec5523fcc1a5cf5ac638eb43f0e545c3f33534ed02200407d2a286276b7153d62cf7093aca840b82b02c567ace541af23fba60474bcd012102b2a7e1a1911083a9a8750921de866e4f14afcbe0d3d9d934fecb5b6a5c0ce3a4024730440220341ae36598507050b49672fc268e40dd83ce11d96c4779959d62cdc808b930c80220724345603ed0f7a7e052b8b11b7a67c0f44e3dfa38c0d58460a4618b2557a6d5012102bd1d0567294f540aff84cbbe0d999b81f58a2357939ca734576de20ec3730ec302483045022100c9a28cceedb69831155cce52992c3e29730c2f20612568b793dc102b2d6146f902204e96ca166620107ce817d9037a8f1a8b59e8d672c6fe0628129265ddfc9dea510121032b6f9c80bcba58d5da83f8270a1a19a8f19109223d9e3df3c298bd6468bba6cc4e61bc00");
    }

    private void verifySpendTx(SpendTx spendTx, SpendType spendType, Collection<UTXO> utxos, long minerFeeTotal, long minerFeePaid, long samouraiFee, long amount, boolean entireBalanceExpected, long change, BipFormat changeFormat, Map<String,Long> outputsExpected, String txid, String raw) throws Exception {
        verifySpendTx(spendTx, spendType, utxos, minerFeeTotal, minerFeePaid, samouraiFee, amount, entireBalanceExpected, change);

        // consistency check
        Assertions.assertEquals(UTXO.sumValue(utxos), amount + samouraiFee + minerFeePaid + change);

        // verify tx
        verifyTx(((SpendTxSimple)spendTx).getTx(), txid, raw, outputsExpected);
    }
}
