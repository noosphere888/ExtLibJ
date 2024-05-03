package com.samourai.wallet.cahoots;

import com.samourai.wallet.cahoots.stonewallx2.Stonewallx2Context;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.beans.SpendType;
import com.samourai.wallet.constants.SamouraiAccountIndex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class Stonewallx2ServiceTest extends AbstractCahootsTest {
    private static final Logger log = LoggerFactory.getLogger(Stonewallx2ServiceTest.class);

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void STONEWALLx2_BIP84() throws Exception {
        int account = 0;

        // setup wallets
        UTXO utxoSender1 = utxoProviderSender.addUtxo(account, "senderTx1", 1, 10000, "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg");
        utxoProviderCounterparty.addUtxo(account, "counterpartyTx1", 1, 10000, "tb1qh287jqsh6mkpqmd8euumyfam00fkr78qhrdnde");

        // setup Cahoots
        long spendAmount = 5000;
        String address = ADDRESS_BIP84;
        Stonewallx2Context cahootsContextSender = Stonewallx2Context.newInitiator(cahootsWalletSender, account, FEE_PER_B, spendAmount, address, null);
        Stonewallx2Context cahootsContextCp = Stonewallx2Context.newCounterparty(cahootsWalletCounterparty, account);

        Cahoots cahoots = doCahoots(stonewallx2Service, cahootsContextSender, cahootsContextCp, null);

        // verify TX
        String txid = "0910de5bf08354a2dcccc2509fedfa82ec97541568620768e8644d95fa81f293";
        String raw = "02000000000102d54f4c6e366d8fc11b8630d4dd1536765ec8022bd3ab8a62fefc2ee96b9ccf140100000000fdffffffad05bb9c893f5cb9762ea57729efaf4a4b8eb1e377533fddc49d15d01fb307940100000000fdffffff04fa1200000000000016001440852bf6ea044204b826a182d1b75528364fd0bdfa120000000000001600144e4fed51986dbaf322d2b36e690b8638fa0f020488130000000000001600142ecf8c3e5697f0513999ed3aa8ea9cd04d1273558813000000000000160014657b6afdeef6809fdabce7face295632fbd94feb0247304402207952c54ff51ba91a725b11240e450ecf34342de4758cd70081fdde55fcd144c202201cc76a1bb4994aa9c22309e0a5c5ab6837e635cc6205a6597101e723aa62272e012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f2024730440220065df3ac0a96df95c5533f4886b391da0046cc13f863399d9eda320ac134ea9802204e0a6d79500192088edf8e84f64aa09bb7f0bf118c567dba19f84f7cae0e893e012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f2d2040000";

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(address, spendAmount);
        outputs.put(COUNTERPARTY_CHANGE_84[0], spendAmount); // counterparty mix
        outputs.put(COUNTERPARTY_CHANGE_84[1], 4858L);
        outputs.put(SENDER_CHANGE_84[0], 4858L);
        verifyTx(cahoots.getTransaction(), txid, raw, outputs);
        pushTx.assertTx(txid, raw);

        // verify SpendTx
        SpendTx spendTx = cahoots.getSpendTx(cahootsContextSender, utxoProviderSender);
        verifySpendTx(spendTx, SpendType.CAHOOTS_STONEWALL2X, Arrays.asList(utxoSender1), 284, 142, 0, spendAmount, false, 4858);
    }

    @Test
    public void STONEWALLx2_P2TR() throws Exception {
        int account = 0;

        // setup wallets
        UTXO utxoSender1 = utxoProviderSender.addUtxo(account, "senderTx1", 1, 10000, "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg");
        utxoProviderCounterparty.addUtxo(account, "counterpartyTx1", 1, 10000, "tb1qh287jqsh6mkpqmd8euumyfam00fkr78qhrdnde");

        // setup Cahoots
        long spendAmount = 5000;
        String address = ADDRESS_P2TR;
        Stonewallx2Context cahootsContextSender = Stonewallx2Context.newInitiator(cahootsWalletSender, account, FEE_PER_B, spendAmount, address, null);
        Stonewallx2Context cahootsContextCp = Stonewallx2Context.newCounterparty(cahootsWalletCounterparty, account);

        Cahoots cahoots = doCahoots(stonewallx2Service, cahootsContextSender, cahootsContextCp, null);

        // verify TX
        String txid = "72dbbe4d806023322047fdb2aeed512366c79ccae1bd843bc8202d95edf52068";
        String raw = "02000000000102d54f4c6e366d8fc11b8630d4dd1536765ec8022bd3ab8a62fefc2ee96b9ccf140100000000fdffffffad05bb9c893f5cb9762ea57729efaf4a4b8eb1e377533fddc49d15d01fb307940100000000fdffffff04f51200000000000016001440852bf6ea044204b826a182d1b75528364fd0bdf5120000000000001600144e4fed51986dbaf322d2b36e690b8638fa0f02048813000000000000160014657b6afdeef6809fdabce7face295632fbd94feb8813000000000000225120000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e864330247304402200d7efe565a82787c5519301e85d58cc22a0aa7057ebcb9ae1822adce4dfc98a2022078a02bcfa60ebb3c87dc7b7238d5e795e9d857893f0a723e3d050c454240b568012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f202483045022100aece9329c01df348a6f06de622f49885ca911db62c68cdce86655879ad2c0be902206a91d074b77c9fd7dd6761435ecaa954d90649fedb939e4539fec09a0d8eb219012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f2d2040000";

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(address, spendAmount);
        outputs.put(COUNTERPARTY_CHANGE_84[0], spendAmount); // counterparty mix
        outputs.put(COUNTERPARTY_CHANGE_84[1], 4853L);
        outputs.put(SENDER_CHANGE_84[0], 4853L);
        verifyTx(cahoots.getTransaction(), txid, raw, outputs);
        pushTx.assertTx(txid, raw);

        // verify SpendTx
        SpendTx spendTx = cahoots.getSpendTx(cahootsContextSender, utxoProviderSender);
        verifySpendTx(spendTx, SpendType.CAHOOTS_STONEWALL2X, Arrays.asList(utxoSender1), 294, 147, 0, spendAmount, false, 4853);
    }

    @Test
    public void STONEWALLx2_BIP84_POSTMIX() throws Exception {
        int account = SamouraiAccountIndex.POSTMIX;

        // setup wallets
        UTXO utxoSender1 = utxoProviderSender.addUtxo(account, "senderTx1", 1, 10000, "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg");
        utxoProviderCounterparty.addUtxo(account, "counterpartyTx1", 1, 10000, "tb1qh287jqsh6mkpqmd8euumyfam00fkr78qhrdnde");

        // setup Cahoots
        long spendAmount = 5000;
        String address = ADDRESS_BIP84;
        Stonewallx2Context cahootsContextSender = Stonewallx2Context.newInitiator(cahootsWalletSender, account, FEE_PER_B, spendAmount, address, null);
        Stonewallx2Context cahootsContextCp = Stonewallx2Context.newCounterparty(cahootsWalletCounterparty, account);

        Cahoots cahoots = doCahoots(stonewallx2Service, cahootsContextSender, cahootsContextCp, null);

        // verify TX
        String txid = "2dd6bc01934c10b76d4656b76d7c882c4f1b2c6732fa53ec945ccbeeb468cf02";
        String raw = "02000000000102d54f4c6e366d8fc11b8630d4dd1536765ec8022bd3ab8a62fefc2ee96b9ccf140100000000fdffffffad05bb9c893f5cb9762ea57729efaf4a4b8eb1e377533fddc49d15d01fb307940100000000fdffffff04fa120000000000001600146a4f3d067dad1077d7414a442ddc5554f0ddc2d5fa12000000000000160014c92e53e44ae5d9d392aecf1ce7a980b073b01cd688130000000000001600142ecf8c3e5697f0513999ed3aa8ea9cd04d12735588130000000000001600148f7446b625c8f1365c42f5e0b17b33f1a2b27ae402483045022100eddec5148ccb76cdabf42e7a64533662dbfe7a80127017664005cd061a0c0b740220712edf14c46fff7389d56bdef65bcfbef5a5359d94107eda8e7531bf654d082e012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f202483045022100d05637212a27a8614a0f5966c3906bb7031ce57f7df655d427e2aeca726d6cf402202b6821cd72a7b24628f4e57b56111581921966cd6d02e792eb84b89b56fa378f012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f2d2040000";

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(address, spendAmount);
        outputs.put(COUNTERPARTY_CHANGE_POSTMIX_84[0], spendAmount); // counterparty mix
        outputs.put(COUNTERPARTY_CHANGE_POSTMIX_84[1], 4858L);
        outputs.put(SENDER_CHANGE_POSTMIX_84[0], 4858L);
        verifyTx(cahoots.getTransaction(), txid, raw, outputs);
        pushTx.assertTx(txid, raw);

        // verify SpendTx
        SpendTx spendTx = cahoots.getSpendTx(cahootsContextSender, utxoProviderSender);
        verifySpendTx(spendTx, SpendType.CAHOOTS_STONEWALL2X, Arrays.asList(utxoSender1), 284, 142, 0, spendAmount, false, 4858);
    }

    @Test
    public void STONEWALLx2_BIP44() throws Exception {
        int account = 0;

        // setup wallets
        UTXO utxoSender1 = utxoProviderSender.addUtxo(account, "senderTx1", 1, 10000, "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg");
        utxoProviderCounterparty.addUtxo(account, "counterpartyTx1", 1, 10000, "tb1qh287jqsh6mkpqmd8euumyfam00fkr78qhrdnde");

        // setup Cahoots
        long spendAmount = 5000;
        String address = ADDRESS_BIP44;
        Stonewallx2Context cahootsContextSender = Stonewallx2Context.newInitiator(cahootsWalletSender, account, FEE_PER_B, spendAmount, address, null);
        Stonewallx2Context cahootsContextCp = Stonewallx2Context.newCounterparty(cahootsWalletCounterparty, account);

        Cahoots cahoots = doCahoots(stonewallx2Service, cahootsContextSender, cahootsContextCp, null);

        // verify TX
        String txid = "142cceb968e279c8d2e9ddda31c61b7d30baac488d06aa1312275607b38cadac";
        String raw = "02000000000102d54f4c6e366d8fc11b8630d4dd1536765ec8022bd3ab8a62fefc2ee96b9ccf140100000000fdffffffad05bb9c893f5cb9762ea57729efaf4a4b8eb1e377533fddc49d15d01fb307940100000000fdffffff04fa120000000000001600144e4fed51986dbaf322d2b36e690b8638fa0f0204fa12000000000000160014657b6afdeef6809fdabce7face295632fbd94feb88130000000000001976a9145f6f1ffd91751f52b20e0c0dad81daa6211d607688ac88130000000000001976a9149bcdad097fa4695a3bdab991e3212da04002ce4088ac0247304402200baba29b3b4e75c69e144ed7fae6702ea5e32f05e1a3a5ebcef29ef430abf33d0220784d8a0cdaefaf475895e160bafaecb07a668391f71ab6b87fb30792ae313bd0012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f2024730440220488134d3971f1c09e9a5809cc59b99da6ddf57a7a14dde4a74be12d8a656aeac02207e3ad89a11fc67a8fbb1d1d88c6baceb72e250892022da55a60462b9f85fea9c012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f2d2040000";

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(address, spendAmount);
        outputs.put(COUNTERPARTY_CHANGE_44[0], spendAmount); // counterparty mix
        outputs.put(COUNTERPARTY_CHANGE_84[0], 4858L);
        outputs.put(SENDER_CHANGE_84[0], 4858L);
        verifyTx(cahoots.getTransaction(), txid, raw, outputs);
        pushTx.assertTx(txid, raw);

        // verify SpendTx
        SpendTx spendTx = cahoots.getSpendTx(cahootsContextSender, utxoProviderSender);
        verifySpendTx(spendTx, SpendType.CAHOOTS_STONEWALL2X, Arrays.asList(utxoSender1), 284, 142, 0, spendAmount, false, 4858);
    }

    @Test
    public void STONEWALLx2_BIP44_POSTMIX() throws Exception {
        int account = SamouraiAccountIndex.POSTMIX;

        // setup wallets
        UTXO utxoSender1 = utxoProviderSender.addUtxo(account, "senderTx1", 1, 10000, "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg");
        utxoProviderCounterparty.addUtxo(account, "counterpartyTx1", 1, 10000, "tb1qh287jqsh6mkpqmd8euumyfam00fkr78qhrdnde");

        // setup Cahoots
        long spendAmount = 5000;
        String address = ADDRESS_BIP44;
        Stonewallx2Context cahootsContextSender = Stonewallx2Context.newInitiator(cahootsWalletSender, account, FEE_PER_B, spendAmount, address, null);
        Stonewallx2Context cahootsContextCp = Stonewallx2Context.newCounterparty(cahootsWalletCounterparty, account);

        Cahoots cahoots = doCahoots(stonewallx2Service, cahootsContextSender, cahootsContextCp, null);

        // verify TX
        String txid = "def48514c5ece8798c57baf9affb592e161f5610edec9ac550f56ba1fb7b4672";
        String raw = "02000000000102d54f4c6e366d8fc11b8630d4dd1536765ec8022bd3ab8a62fefc2ee96b9ccf140100000000fdffffffad05bb9c893f5cb9762ea57729efaf4a4b8eb1e377533fddc49d15d01fb307940100000000fdffffff04fa120000000000001600146a4f3d067dad1077d7414a442ddc5554f0ddc2d5fa12000000000000160014c92e53e44ae5d9d392aecf1ce7a980b073b01cd688130000000000001976a9148f7446b625c8f1365c42f5e0b17b33f1a2b27ae488ac88130000000000001976a9149bcdad097fa4695a3bdab991e3212da04002ce4088ac02483045022100e1baa7a6ce9d549ea491e66c83d49be686461d730d4dadbd1d80f4754b79eef602205235ce0e49128cf6d3c464bf588a5267d7aae198d77de1944679742a278d0a56012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f2024730440220207f1aa64ccfbc38ef8593ce1e6f4807d9422598998679a3bc10965e83ebc01f02204cb6f78c7d5b853a2f61de2d03e2ac57602c73260b034703905ace35f78fae5b012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f2d2040000";

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(address, spendAmount);
        outputs.put(COUNTERPARTY_CHANGE_POSTMIX_44[0], spendAmount); // counterparty mix
        outputs.put(COUNTERPARTY_CHANGE_POSTMIX_84[1], 4858L);
        outputs.put(SENDER_CHANGE_POSTMIX_84[0], 4858L);
        verifyTx(cahoots.getTransaction(), txid, raw, outputs);
        pushTx.assertTx(txid, raw);

        // verify SpendTx
        SpendTx spendTx = cahoots.getSpendTx(cahootsContextSender, utxoProviderSender);
        verifySpendTx(spendTx, SpendType.CAHOOTS_STONEWALL2X, Arrays.asList(utxoSender1), 284, 142, 0, spendAmount, false, 4858);
    }

    @Test
    public void STONEWALLx2_BIP49() throws Exception {
        int account = 0;

        // setup wallets
        UTXO utxoSender1 = utxoProviderSender.addUtxo(account, "senderTx1", 1, 10000, "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg");
        utxoProviderCounterparty.addUtxo(account, "counterpartyTx1", 1, 10000, "tb1qh287jqsh6mkpqmd8euumyfam00fkr78qhrdnde");

        // setup Cahoots
        long spendAmount = 5000;
        String address = ADDRESS_BIP49;
        Stonewallx2Context cahootsContextSender = Stonewallx2Context.newInitiator(cahootsWalletSender, account, FEE_PER_B, spendAmount, address, null);
        Stonewallx2Context cahootsContextCp = Stonewallx2Context.newCounterparty(cahootsWalletCounterparty, account);

        Cahoots cahoots = doCahoots(stonewallx2Service, cahootsContextSender, cahootsContextCp, null);

        // verify TX
        String txid = "373310bcd31b36607e6c8c4d155e1c59696473ac59d88154e282b2b34c536698";
        String raw = "02000000000102d54f4c6e366d8fc11b8630d4dd1536765ec8022bd3ab8a62fefc2ee96b9ccf140100000000fdffffffad05bb9c893f5cb9762ea57729efaf4a4b8eb1e377533fddc49d15d01fb307940100000000fdffffff04fa120000000000001600144e4fed51986dbaf322d2b36e690b8638fa0f0204fa12000000000000160014657b6afdeef6809fdabce7face295632fbd94feb881300000000000017a914336caa13e08b96080a32b5d818d59b4ab3b3674287881300000000000017a9149f0432151dfca9ed873a3dab5106f470181be85c8702483045022100bf83c1b7c86b19b419d11fb787a20990b8e477319b94263136195e61715fcb5e02204dd31b81d213fa8407fbdca4b183982d5ec85bfdca6ba0db6c60b3fe212ee8ea012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f202483045022100b21b4d0ba699a549b62fce7bd3036a9a2ac0a183d600006ea9fdca96cd4dee3502203a18ef9439ef24a4e16fcc53ba11d478afa2e93d70a174bdf0f7b4e068e0bca0012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f2d2040000";

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(address, spendAmount);
        outputs.put(COUNTERPARTY_CHANGE_49[0], spendAmount); // counterparty mix
        outputs.put(COUNTERPARTY_CHANGE_84[0], 4858L);
        outputs.put(SENDER_CHANGE_84[0], 4858L);
        verifyTx(cahoots.getTransaction(), txid, raw, outputs);
        pushTx.assertTx(txid, raw);

        // verify SpendTx
        SpendTx spendTx = cahoots.getSpendTx(cahootsContextSender, utxoProviderSender);
        verifySpendTx(spendTx, SpendType.CAHOOTS_STONEWALL2X, Arrays.asList(utxoSender1), 284, 142, 0, spendAmount, false, 4858);
    }

    @Test
    public void STONEWALLx2_BIP84_paynym() throws Exception {
        int account = 0;
        String paynymDestination = "TESTPAYNYM";

        // setup wallets
        UTXO utxoSender1 = utxoProviderSender.addUtxo(account, "senderTx1", 1, 10000, "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg");
        utxoProviderCounterparty.addUtxo(account, "counterpartyTx1", 1, 10000, "tb1qh287jqsh6mkpqmd8euumyfam00fkr78qhrdnde");

        // setup Cahoots
        long spendAmount = 5000;
        String address = ADDRESS_BIP84;
        Stonewallx2Context cahootsContextSender = Stonewallx2Context.newInitiator(cahootsWalletSender, account, FEE_PER_B, spendAmount, address, paynymDestination);
        Stonewallx2Context cahootsContextCp = Stonewallx2Context.newCounterparty(cahootsWalletCounterparty, account);

        Cahoots cahoots = doCahoots(stonewallx2Service, cahootsContextSender, cahootsContextCp, null);

        // verify TX
        String txid = "0910de5bf08354a2dcccc2509fedfa82ec97541568620768e8644d95fa81f293";
        String raw = "02000000000102d54f4c6e366d8fc11b8630d4dd1536765ec8022bd3ab8a62fefc2ee96b9ccf140100000000fdffffffad05bb9c893f5cb9762ea57729efaf4a4b8eb1e377533fddc49d15d01fb307940100000000fdffffff04fa1200000000000016001440852bf6ea044204b826a182d1b75528364fd0bdfa120000000000001600144e4fed51986dbaf322d2b36e690b8638fa0f020488130000000000001600142ecf8c3e5697f0513999ed3aa8ea9cd04d1273558813000000000000160014657b6afdeef6809fdabce7face295632fbd94feb0247304402207952c54ff51ba91a725b11240e450ecf34342de4758cd70081fdde55fcd144c202201cc76a1bb4994aa9c22309e0a5c5ab6837e635cc6205a6597101e723aa62272e012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f2024730440220065df3ac0a96df95c5533f4886b391da0046cc13f863399d9eda320ac134ea9802204e0a6d79500192088edf8e84f64aa09bb7f0bf118c567dba19f84f7cae0e893e012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f2d2040000";

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(address, spendAmount);
        outputs.put(COUNTERPARTY_CHANGE_84[0], spendAmount); // counterparty mix
        outputs.put(COUNTERPARTY_CHANGE_84[1], 4858L);
        outputs.put(SENDER_CHANGE_84[0], 4858L);
        verifyTx(cahoots.getTransaction(), txid, raw, outputs);
        pushTx.assertTx(txid, raw);

        // verify paynym
        Assertions.assertEquals(paynymDestination, cahoots.getPaynymDestination());

        // verify SpendTx
        SpendTx spendTx = cahoots.getSpendTx(cahootsContextSender, utxoProviderSender);
        verifySpendTx(spendTx, SpendType.CAHOOTS_STONEWALL2X, Arrays.asList(utxoSender1), 284, 142, 0, spendAmount, false, 4858);
    }

    @Test
    public void invalidStonewallExcetion() throws Exception {
        String address = "tb1q9m8cc0jkjlc9zwvea5a2365u6px3yu646vgez4";

        // throw Exception for 0 spend amount
        Assertions.assertThrows(Exception.class,
                () -> {
                    Stonewallx2Context cahootsContextSender = Stonewallx2Context.newInitiator(cahootsWalletSender, 0, FEE_PER_B, 0, address, null);
                    stonewallx2Service.startInitiator(cahootsContextSender);
                });

        // throw Exception for blank address
        Assertions.assertThrows(Exception.class,
                () -> {
                    Stonewallx2Context cahootsContextSender = Stonewallx2Context.newInitiator(cahootsWalletSender, 0, FEE_PER_B, 0, "", null);
                    stonewallx2Service.startInitiator(cahootsContextSender);
                });
    }
}
