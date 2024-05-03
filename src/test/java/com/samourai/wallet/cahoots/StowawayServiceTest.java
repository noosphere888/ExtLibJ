package com.samourai.wallet.cahoots;

import com.samourai.wallet.cahoots.stowaway.StowawayContext;
import com.samourai.wallet.send.UTXO;
import com.samourai.wallet.send.beans.SpendTx;
import com.samourai.wallet.send.beans.SpendType;
import com.samourai.wallet.constants.SamouraiAccountIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class StowawayServiceTest extends AbstractCahootsTest {
    private static final Logger log = LoggerFactory.getLogger(StowawayServiceTest.class);


    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void Stowaway() throws Exception {
        int account = 0;

        // setup wallets
        UTXO utxoSender1 = utxoProviderSender.addUtxo(account, "senderTx1", 1, 10000, "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg");
        utxoProviderCounterparty.addUtxo(account, "counterpartyTx1", 1, 10000, "tb1qh287jqsh6mkpqmd8euumyfam00fkr78qhrdnde");

        // setup Cahoots
        long spendAmount = 5000;
        StowawayContext cahootsContextSender = StowawayContext.newInitiator(cahootsWalletSender, account, FEE_PER_B, spendAmount);
        StowawayContext cahootsContextCp = StowawayContext.newCounterparty(cahootsWalletCounterparty, account);

        Cahoots cahoots = doCahoots(stowawayService, cahootsContextSender, cahootsContextCp, null);

        // verify TX
        String txid = "5ee43707672e0fdf27c87e398c6f547f33dadef43fa510c2b7e22ab5fc271b85";
        String raw = "02000000000102d54f4c6e366d8fc11b8630d4dd1536765ec8022bd3ab8a62fefc2ee96b9ccf140100000000fdffffffad05bb9c893f5cb9762ea57729efaf4a4b8eb1e377533fddc49d15d01fb307940100000000fdffffff02b0120000000000001600144e4fed51986dbaf322d2b36e690b8638fa0f0204983a00000000000016001428a90fa3f4f285fc689f389115326dbf96917d6202483045022100e1507e5b457b5052488d41c1685bab3f2eb5ef51fc515168d3bbb14c30adf9cf02203c463ceb0394d68a3e4b037fcff91eec0e04e9b7357eb3479d07fa3a6e93ed7e012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f20247304402203ab7cba03ee40b4f4fceb02c5d2735be560dab25eb264e6850b307be3350383d022046a4a23ed073d13a7792a4dbfa22a46ad123b7cabb31fa8e9aa0cdcf1318f6e1012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f200000000";

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(COUNTERPARTY_RECEIVE_84[0], 15000L);
        outputs.put(SENDER_CHANGE_84[0], 4784L);
        verifyTx(cahoots.getTransaction(), txid, raw, outputs);
        pushTx.assertTx(txid, raw);

        // verify SpendTx
        SpendTx spendTx = cahoots.getSpendTx(cahootsContextSender, utxoProviderSender);
        verifySpendTx(spendTx, SpendType.CAHOOTS_STOWAWAY, Arrays.asList(utxoSender1), 216, 216, 0, spendAmount, false, 4784L);
    }

    @Test
    public void Stowaway_POSTMIX() throws Exception {
        int account = SamouraiAccountIndex.POSTMIX;

        // setup wallets
        UTXO utxoSender1 = utxoProviderSender.addUtxo(account, "senderTx1", 1, 10000, "tb1qkymumss6zj0rxy9l3v5vqxqwwffy8jjsyhrkrg");
        utxoProviderCounterparty.addUtxo(account, "counterpartyTx1", 1, 10000, "tb1qh287jqsh6mkpqmd8euumyfam00fkr78qhrdnde");

        // setup Cahoots
        long spendAmount = 5000;
        StowawayContext cahootsContextSender = StowawayContext.newInitiator(cahootsWalletSender, account, FEE_PER_B, spendAmount);
        StowawayContext cahootsContextCp = StowawayContext.newCounterparty(cahootsWalletCounterparty, account);

        Cahoots cahoots = doCahoots(stowawayService, cahootsContextSender, cahootsContextCp, null);

        // verify TX
        String txid = "2cd56ca8314c049270cf389a8aefcef0ffd741baf00e94b9b50d509135c6c2d1";
        String raw = "02000000000102d54f4c6e366d8fc11b8630d4dd1536765ec8022bd3ab8a62fefc2ee96b9ccf140100000000fdffffffad05bb9c893f5cb9762ea57729efaf4a4b8eb1e377533fddc49d15d01fb307940100000000fdffffff02b012000000000000160014c92e53e44ae5d9d392aecf1ce7a980b073b01cd6983a00000000000016001428a90fa3f4f285fc689f389115326dbf96917d6202483045022100c611140c3a74977157725a4da7580d19476eb35e5cf21f6950fb7c6cd0ed8f95022045b3319b51214c431fc76d3dd74f3908fba98b63ef6377ec0b187c3cb38a8ae9012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f202483045022100a7d438bdea83d240151d2ec2df818786bc7f94411e5f43f8eec78b984939de51022024635264353161c41523cef01230a499aaac50993285625f95aa7528cfc4d117012102e37648435c60dcd181b3d41d50857ba5b5abebe279429aa76558f6653f1658f200000000";

        Map<String,Long> outputs = new LinkedHashMap<>();
        outputs.put(COUNTERPARTY_RECEIVE_84[0], 15000L);
        outputs.put(SENDER_CHANGE_POSTMIX_84[0], 4784L);
        verifyTx(cahoots.getTransaction(), txid, raw, outputs);
        pushTx.assertTx(txid, raw);

        // verify SpendTx
        SpendTx spendTx = cahoots.getSpendTx(cahootsContextSender, utxoProviderSender);
        verifySpendTx(spendTx, SpendType.CAHOOTS_STOWAWAY, Arrays.asList(utxoSender1), 216, 216, 0, spendAmount, false, 4784L);
    }
}
