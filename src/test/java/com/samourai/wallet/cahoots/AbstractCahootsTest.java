package com.samourai.wallet.cahoots;

import com.samourai.wallet.cahoots.manual.ManualCahootsMessage;
import com.samourai.wallet.cahoots.manual.ManualCahootsService;
import com.samourai.wallet.bipFormat.BIP_FORMAT;
import com.samourai.wallet.bipWallet.WalletSupplier;
import com.samourai.wallet.bipWallet.WalletSupplierImpl;
import com.samourai.wallet.cahoots.multi.MultiCahootsService;
import com.samourai.wallet.cahoots.stonewallx2.Stonewallx2Service;
import com.samourai.wallet.cahoots.stowaway.StowawayService;
import com.samourai.wallet.client.indexHandler.MemoryIndexHandlerSupplier;
import com.samourai.wallet.constants.BIP_WALLETS;
import com.samourai.wallet.constants.BIP_WALLET;
import com.samourai.wallet.hd.Chain;
import com.samourai.wallet.hd.HD_Wallet;
import com.samourai.wallet.send.provider.MockUtxoProvider;
import com.samourai.wallet.test.AbstractTest;
import com.samourai.wallet.util.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCahootsTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(AbstractCahootsTest.class);

    private static final String SEED_WORDS = "all all all all all all all all all all all all";
    private static final String SEED_PASSPHRASE_INITIATOR = "initiator";
    private static final String SEED_PASSPHRASE_COUNTERPARTY = "counterparty";
    protected static final int FEE_PER_B = 1;

    protected CahootsWallet cahootsWalletSender;
    protected CahootsWallet cahootsWalletCounterparty;

    protected MockUtxoProvider utxoProviderSender;
    protected MockUtxoProvider utxoProviderCounterparty;

    protected static String[] SENDER_RECEIVE_84;
    protected static String[] COUNTERPARTY_RECEIVE_84;
    protected static String[] COUNTERPARTY_RECEIVE_44;
    protected static String[] COUNTERPARTY_RECEIVE_49;
    protected static String[] COUNTERPARTY_RECEIVE_POSTMIX_84;
    protected static String[] SENDER_CHANGE_84;
    protected static String[] SENDER_CHANGE_POSTMIX_84;
    protected static String[] COUNTERPARTY_CHANGE_44;
    protected static String[] COUNTERPARTY_CHANGE_49;
    protected static String[] COUNTERPARTY_CHANGE_84;
    protected static String[] COUNTERPARTY_CHANGE_POSTMIX_44;
    protected static String[] COUNTERPARTY_CHANGE_POSTMIX_84;

    protected Stonewallx2Service stonewallx2Service = new Stonewallx2Service(bipFormatSupplier, params);
    protected StowawayService stowawayService = new StowawayService(bipFormatSupplier, params);
    protected MultiCahootsService multiCahootsService = new MultiCahootsService(bipFormatSupplier, params, stonewallx2Service, stowawayService);
    protected ManualCahootsService manualCahootsService = new ManualCahootsService(stowawayService, stonewallx2Service, multiCahootsService);

    public void setUp() throws Exception {
        super.setUp();

        final HD_Wallet bip84WalletSender = TestUtil.computeBip84wallet(SEED_WORDS, SEED_PASSPHRASE_INITIATOR);
        WalletSupplier walletSupplierSender = new WalletSupplierImpl(bipFormatSupplier, new MemoryIndexHandlerSupplier(), bip84WalletSender, BIP_WALLETS.WHIRLPOOL);
        utxoProviderSender = new MockUtxoProvider(params, walletSupplierSender);
        cahootsWalletSender = new CahootsWalletImpl(mockChainSupplier, walletSupplierSender, utxoProviderSender.getCahootsUtxoProvider());

        final HD_Wallet bip84WalletCounterparty = TestUtil.computeBip84wallet(SEED_WORDS, SEED_PASSPHRASE_COUNTERPARTY);
        WalletSupplier walletSupplierCounterparty = new WalletSupplierImpl(bipFormatSupplier, new MemoryIndexHandlerSupplier(), bip84WalletCounterparty, BIP_WALLETS.WHIRLPOOL);
        utxoProviderCounterparty = new MockUtxoProvider(params, walletSupplierCounterparty);
        cahootsWalletCounterparty = new CahootsWalletImpl(mockChainSupplier, walletSupplierCounterparty, utxoProviderCounterparty.getCahootsUtxoProvider());

        SENDER_RECEIVE_84 = new String[4];
        for (int i = 0; i < 4; i++) {
            SENDER_RECEIVE_84[i] = walletSupplierSender.getWallet(BIP_WALLET.DEPOSIT_BIP84).getAddressAt(Chain.RECEIVE.getIndex(), i).getAddressString();
        }

        COUNTERPARTY_RECEIVE_84 = new String[4];
        for (int i = 0; i < 4; i++) {
            COUNTERPARTY_RECEIVE_84[i] = walletSupplierCounterparty.getWallet(BIP_WALLET.DEPOSIT_BIP84).getAddressAt(Chain.RECEIVE.getIndex(), i).getAddressString();
        }

        COUNTERPARTY_RECEIVE_44 = new String[4];
        for (int i = 0; i < 4; i++) {
            COUNTERPARTY_RECEIVE_44[i] = walletSupplierCounterparty.getWallet(BIP_WALLET.DEPOSIT_BIP44).getAddressAt(Chain.RECEIVE.getIndex(), i).getAddressString();
        }

        COUNTERPARTY_RECEIVE_49 = new String[4];
        for (int i = 0; i < 4; i++) {
            COUNTERPARTY_RECEIVE_49[i] = walletSupplierCounterparty.getWallet(BIP_WALLET.DEPOSIT_BIP49).getAddressAt(Chain.RECEIVE.getIndex(), i).getAddressString();
        }

        COUNTERPARTY_RECEIVE_POSTMIX_84 = new String[4];
        for (int i = 0; i < 4; i++) {
            COUNTERPARTY_RECEIVE_POSTMIX_84[i] = BIP_FORMAT.SEGWIT_NATIVE.getAddressString(walletSupplierCounterparty.getWallet(BIP_WALLET.POSTMIX_BIP84).getAddressAt(Chain.RECEIVE.getIndex(), i).getHdAddress());
        }

        SENDER_CHANGE_84 = new String[4];
        for (int i = 0; i < 4; i++) {
            SENDER_CHANGE_84[i] = walletSupplierSender.getWallet(BIP_WALLET.DEPOSIT_BIP84).getAddressAt(Chain.CHANGE.getIndex(), i).getAddressString();
        }

        SENDER_CHANGE_POSTMIX_84 = new String[4];
        for (int i = 0; i < 4; i++) {
            SENDER_CHANGE_POSTMIX_84[i] = BIP_FORMAT.SEGWIT_NATIVE.getAddressString(walletSupplierSender.getWallet(BIP_WALLET.POSTMIX_BIP84).getAddressAt(Chain.CHANGE.getIndex(), i).getHdAddress());
        }

        COUNTERPARTY_CHANGE_44 = new String[4];
        for (int i = 0; i < 4; i++) {
            COUNTERPARTY_CHANGE_44[i] = walletSupplierCounterparty.getWallet(BIP_WALLET.DEPOSIT_BIP44).getAddressAt(Chain.CHANGE.getIndex(), i).getAddressString();
        }

        COUNTERPARTY_CHANGE_49 = new String[4];
        for (int i = 0; i < 4; i++) {
            COUNTERPARTY_CHANGE_49[i] = walletSupplierCounterparty.getWallet(BIP_WALLET.DEPOSIT_BIP49).getAddressAt(Chain.CHANGE.getIndex(), i).getAddressString();
        }

        COUNTERPARTY_CHANGE_84 = new String[4];
        for (int i = 0; i < 4; i++) {
            COUNTERPARTY_CHANGE_84[i] = walletSupplierCounterparty.getWallet(BIP_WALLET.DEPOSIT_BIP84).getAddressAt(Chain.CHANGE.getIndex(), i).getAddressString();
        }

        COUNTERPARTY_CHANGE_POSTMIX_84 = new String[4];
        for (int i = 0; i < 4; i++) {
            COUNTERPARTY_CHANGE_POSTMIX_84[i] = BIP_FORMAT.SEGWIT_NATIVE.getAddressString(walletSupplierCounterparty.getWallet(BIP_WALLET.POSTMIX_BIP84).getAddressAt(Chain.CHANGE.getIndex(), i).getHdAddress());
        }

        COUNTERPARTY_CHANGE_POSTMIX_44 = new String[4];
        for (int i = 0; i < 4; i++) {
            COUNTERPARTY_CHANGE_POSTMIX_44[i] = BIP_FORMAT.LEGACY.getAddressString(walletSupplierCounterparty.getWallet(BIP_WALLET.POSTMIX_BIP84).getAddressAt(Chain.CHANGE.getIndex(), i).getHdAddress());
        }
    }

    protected Cahoots cleanPayload(String payloadStr) throws Exception {
        Cahoots copy = Cahoots.parse(payloadStr);
        CahootsTestUtil.cleanPayload(copy);
        return copy;
    }

    protected void verify(String expectedPayload, String payloadStr) throws Exception {
        payloadStr = cleanPayload(payloadStr).toJSONString();
        Assertions.assertEquals(expectedPayload, payloadStr);
    }

    protected void verify(String expectedPayload, ManualCahootsMessage cahootsMessage, boolean lastStep, CahootsType type, CahootsTypeUser typeUser) throws Exception {
        verify(expectedPayload, cahootsMessage.getCahoots().toJSONString());
        Assertions.assertEquals(lastStep, cahootsMessage.isDone());
        Assertions.assertEquals(type, cahootsMessage.getType());
        Assertions.assertEquals(typeUser, cahootsMessage.getTypeUser());
    }

    protected Cahoots doCahoots(AbstractCahootsService cahootsService, CahootsContext cahootsContextSender, CahootsContext cahootsContextCp, String[] EXPECTED_PAYLOADS) throws Exception {
        int nbSteps = EXPECTED_PAYLOADS != null ? EXPECTED_PAYLOADS.length : ManualCahootsMessage.getNbSteps(cahootsContextSender.getCahootsType());

        // sender => _0
        String lastPayload = cahootsService.startInitiator(cahootsContextSender).toJSONString();
        if (log.isDebugEnabled()) {
            log.debug("#0 SENDER => "+lastPayload);
        }
        if (EXPECTED_PAYLOADS != null) {
            verify(EXPECTED_PAYLOADS[0], lastPayload);
        }

        // counterparty => _1
        lastPayload = cahootsService.startCollaborator(cahootsContextCp, Cahoots.parse(lastPayload)).toJSONString();
        if (log.isDebugEnabled()) {
            log.debug("#1 COUNTERPARTY => "+lastPayload);
        }
        if (EXPECTED_PAYLOADS != null) {
            verify(EXPECTED_PAYLOADS[1], lastPayload);
        }

        for (int i=2; i<nbSteps; i++) {
            if (i%2 == 0) {
                // sender
                lastPayload = cahootsService.reply(cahootsContextSender, Cahoots.parse(lastPayload)).toJSONString();
                if (log.isDebugEnabled()) {
                    log.debug("#"+i+" SENDER => "+lastPayload);
                }
            } else {
                // counterparty
                lastPayload = cahootsService.reply(cahootsContextCp, Cahoots.parse(lastPayload)).toJSONString();
                if (log.isDebugEnabled()) {
                    log.debug("#"+i+" COUNTERPARTY => "+lastPayload);
                }
            }
            if (EXPECTED_PAYLOADS != null) {
                verify(EXPECTED_PAYLOADS[i], lastPayload);
            }
        }
        Cahoots cahoots = Cahoots.parse(lastPayload);
        cahoots.pushTx(pushTx);
        return cahoots;
    }
}
