package com.samourai.wallet.ricochet;

import com.samourai.wallet.bipWallet.BipWallet;
import com.samourai.wallet.constants.BIP_WALLET;
import com.samourai.wallet.test.AbstractTest;
import com.samourai.wallet.constants.SamouraiAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class RicochetUtilGenericTest extends AbstractTest {
    private static final String PCODE="PM8TJXp19gCE6hQzqRi719FGJzF6AreRwvoQKLRnQ7dpgaakakFns22jHUqhtPQWmfevPQRCyfFbdDrKvrfw9oZv5PjaCerQMa3BKkPyUf9yN1CDR3w6";
    private RicochetUtilGeneric ricochetUtil;
    private BipWallet bipWalletRicochet;
    private BipWallet bipWalletSpend;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        bipWalletRicochet = walletSupplier.getWallet(BIP_WALLET.RICOCHET_BIP84);
        bipWalletSpend = walletSupplier.getWallet(BIP_WALLET.DEPOSIT_BIP84);

        ricochetUtil = new RicochetUtilGeneric(){
            @Override
            protected int computeSamouraiFeesBip47RandomValue() {
                return 1234; // use static value for reproductible tests
            }
        };
    }

    @Test
    public void ricochet_insufficientBalance() throws Exception {
        utxoProvider.addUtxo(bipWalletSpend, 100000);
        utxoProvider.addUtxo(bipWalletSpend, 5000);

        long spendAmount = 12345;
        String destination = "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg";
        RicochetConfig config = computeConfig(true);
        try {
            Ricochet ricochet = ricochetUtil.ricochet(spendAmount, destination, config); // should throw
            Assertions.assertTrue(false);
        } catch(Exception e) { // ok
            Assertions.assertEquals("Insufficient balance", e.getMessage());
        }
    }

    @Disabled
    @Test
    public void ricochet_bip47() throws Exception {
        utxoProvider.addUtxo(bipWalletSpend, 100000);
        utxoProvider.addUtxo(bipWalletSpend, 5000);
        utxoProvider.addUtxo(bipWalletSpend, 10000);

        long spendAmount = 12345;
        String destination = "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg";
        RicochetConfig config = computeConfig(true);
        Ricochet ricochet = ricochetUtil.ricochet(spendAmount, destination, config);
        String expected = "{\"nTimeLock\":999999,\"spend_account\":0,\"spend_amount\":12345,\"change_amount\":102069,\"samourai_fee\":100000,\"samourai_fee_via_bip47\":true,\"fee_per_hop\":235,\"feeKB\":1000,\"destination\":\"tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg\",\"total_spend\":113871,\"total_miner_fee\":-99414,\"total_vSize\":840,\"total_weight\":3350,\"hops\":[{\"seq\":0,\"spend_amount\":113285,\"fee\":586,\"index\":0,\"destination\":\"tb1q5lc455emwwttdqwf9p32xf8fhgrhvfp5vxvul7\",\"tx\":\"02000000000103e93c5d03e89545aa69c933aa7818f2ef6fe0a2fb3d2c6193085c6306538be50d0300000000fdffffff799901cbde3146618639ddadf2ba79e568e2ed263f9aa046befdef30c681918a0200000000fdffffff3e3f0c4d7c92472be300847fa18669620222af52c416d307c519c2a7c544fabe0100000000fdffffff02b58e010000000000160014f6a884f18f4d7e78a4167c3e56773c3ae58e016485ba010000000000160014a7f15a533b7396b681c92862a324e9ba077624340247304402203a6fbfed1ce6ccf5e334b34b6b8c0fcaf04e014582157c63e374b15aa090ebfa022024fad79afe0eb8002fe684ecb0970f90dbfe93876078f51692a77fa93ebaf05d0121032b6f9c80bcba58d5da83f8270a1a19a8f19109223d9e3df3c298bd6468bba6cc0247304402204b6b9594d4925959477efb40aa0cba87a44d6b2536981d996c041a0ad7ef277c02205bc1371b4fe1f57d1b56a646c14f89b91591a1a27c2eefecc45ca2c2da3e40fa0121020fd264b3db00aa2c43e4688e824f5a6e5247b8474620c3ce53cd20316a457a6402483045022100f837a801704cc6e79151ebb4332aca26a893db30b341fcceb9bb06476f1db22d02204ffb705f47f457fcfc33f33e388e807d7e6770e661076fe20b88a901636ccc0d01210229950f82d3230b06db77c9aec65180e5bbf99fd837fe967dbabe4d9b0422146c3f420f00\",\"hash\":\"6be97f54973de38a422c6b642c642c93dd03a74d0d11af70ec7dde7449f3e0b3\",\"nTimeLock\":999999,\"prev_tx_hash\":null,\"prev_tx_n\":0,\"prev_spend_value\":0,\"script\":null,\"samourai_fee_address\":null,\"samourai_fee_amount\":0},{\"seq\":1,\"spend_amount\":86816,\"fee\":235,\"index\":1,\"destination\":\"tb1q5lc455emwwttdqwf9p32xf8fhgrhvfp5vxvul7\",\"tx\":\"02000000000101b3e0f34974de7dec70af110d4da703dd932c642c646b2c428ae33d97547fe96b0100000000fdffffff022053010000000000160014a7f15a533b7396b681c92862a324e9ba077624347a66000000000000160014bb7f6b09fb91ec76d7b92c5eb730d64da265daec0247304402202cf48b7fb1d6064b3ceaece5d9643afec56b644b3b23f9696ffe6541edf0bd220220284b8ef9d6d5b6514d56b213d7f1d5ea6d705f2cd0149da4439ede2e7cf96a9c012102f4ec32b1971d02bf1a3f55fb2e9004ad69abddf7cf2d7db9f8b75fd8223c79a740420f00\",\"hash\":\"00c42e1ab4ec45fe5a0d12721b16b04dfce95245bfdce7667cb9a14e88371b91\",\"nTimeLock\":1000000,\"prev_tx_hash\":\"6be97f54973de38a422c6b642c642c93dd03a74d0d11af70ec7dde7449f3e0b3\",\"prev_tx_n\":1,\"prev_spend_value\":113285,\"script\":\"0014a7f15a533b7396b681c92862a324e9ba07762434\",\"samourai_fee_address\":\"tb1qhdlkkz0mj8k8d4ae930twvxkfk3xtkhvp4cxsh\",\"samourai_fee_amount\":26234},{\"seq\":2,\"spend_amount\":60347,\"fee\":235,\"index\":2,\"destination\":\"tb1q5lc455emwwttdqwf9p32xf8fhgrhvfp5vxvul7\",\"tx\":\"02000000000101911b37884ea1b97c66e7dcbf4552e9fc4db0161b72120d5afe45ecb41a2ec4000000000000fdffffff02bbeb000000000000160014a7f15a533b7396b681c92862a324e9ba077624347a66000000000000160014ca5042c7e3db2a0f2c6ffac8d0eaeab21b7015c502483045022100d12d24c4c7af5ee1cfebe5ee685cb694964fc3b6021ce77b0e352265c6ca9f3f02203cc247533435b57edfda1648c41933d00410919f731e73d894ee313d9373865a012102f4ec32b1971d02bf1a3f55fb2e9004ad69abddf7cf2d7db9f8b75fd8223c79a741420f00\",\"hash\":\"0b38187304720b144bd4c0f453ebcb8d4bf8a6f453ca84d7232ed5609b0ad923\",\"nTimeLock\":1000001,\"prev_tx_hash\":\"00c42e1ab4ec45fe5a0d12721b16b04dfce95245bfdce7667cb9a14e88371b91\",\"prev_tx_n\":0,\"prev_spend_value\":86816,\"script\":\"0014a7f15a533b7396b681c92862a324e9ba07762434\",\"samourai_fee_address\":\"tb1qefgy93lrmv4q7tr0ltydp6h2kgdhq9w9sj94ay\",\"samourai_fee_amount\":26234},{\"seq\":3,\"spend_amount\":33878,\"fee\":235,\"index\":3,\"destination\":\"tb1q5lc455emwwttdqwf9p32xf8fhgrhvfp5vxvul7\",\"tx\":\"0200000000010123d90a9b60d52e23d784ca53f4a6f84b8dcbeb53f4c0d44b140b72047318380b0000000000fdffffff025684000000000000160014a7f15a533b7396b681c92862a324e9ba077624347a66000000000000160014d812349f1e8d79511634165677c80f42f8cb7d9902483045022100c45f787d014613031437f44fe4491da6b8855bb92d8041bc4b82c4bc3330e89e02207b6fc8ca69f1522bf95ad66c5c47fa7ff30e6f755e399538c03741d0a830c064012102f4ec32b1971d02bf1a3f55fb2e9004ad69abddf7cf2d7db9f8b75fd8223c79a742420f00\",\"hash\":\"412f39f6a73258cef24ea09b4b6da9fe1574420ce5bab97af9b62927f35fe536\",\"nTimeLock\":1000002,\"prev_tx_hash\":\"0b38187304720b144bd4c0f453ebcb8d4bf8a6f453ca84d7232ed5609b0ad923\",\"prev_tx_n\":0,\"prev_spend_value\":60347,\"script\":\"0014a7f15a533b7396b681c92862a324e9ba07762434\",\"samourai_fee_address\":\"tb1qmqfrf8c734u4z935zet80jq0gtuvklvez60yhf\",\"samourai_fee_amount\":26234},{\"seq\":4,\"spend_amount\":12345,\"fee\":235,\"index\":0,\"destination\":\"tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg\",\"tx\":\"0200000000010136e55ff32729b6f97ab9bae50c427415fea96d4b9ba04ef2ce5832a7f6392f410000000000fdffffff023930000000000000160014b137cdc21a149e3310bf8b28c0180e725243ca503253000000000000160014e27d805b035c561c49656624b1d83855f7096ddd02473044022031432650c5b63aef9373bd78760be46a667697fbbaf04740f9ae50cdaca5523a02207c70ba9b451cb7e9ba58e0c9fbc2256b99593117b395df91a39f0dc93cf92145012102f4ec32b1971d02bf1a3f55fb2e9004ad69abddf7cf2d7db9f8b75fd8223c79a743420f00\",\"hash\":\"b2b499f8d9fa2a0e4e51dce31400aa7344e6dfee6e40cbfd8e750cf283b48e92\",\"nTimeLock\":1000003,\"prev_tx_hash\":\"412f39f6a73258cef24ea09b4b6da9fe1574420ce5bab97af9b62927f35fe536\",\"prev_tx_n\":0,\"prev_spend_value\":33878,\"script\":\"0014a7f15a533b7396b681c92862a324e9ba07762434\",\"samourai_fee_address\":\"tb1quf7cqkcrt3tpcjt9vcjtrkpc2hmsjmwauzv0ty\",\"samourai_fee_amount\":21298}]}";
        Assertions.assertEquals(expected, ricochet.toJsonString());
    }

    private RicochetConfig computeConfig(boolean useBip47) {
        int bip47WalletOutgoingIdx = 0;
        return new RicochetConfig(1, useBip47, "tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4", true, true, 999999, utxoProvider, bip47Util, bipWalletRicochet, bipWalletSpend, SamouraiAccount.DEPOSIT, bip47Wallet.getAccount(0), bip47WalletOutgoingIdx);
    }

}
