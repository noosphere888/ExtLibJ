package com.samourai.wallet.bipFormat;

import com.samourai.wallet.segwit.bech32.Bech32Segwit;
import com.samourai.wallet.segwit.bech32.Bech32UtilGeneric;
import com.samourai.wallet.util.FormatsUtilGeneric;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class BipFormatSupplierImpl implements BipFormatSupplier {
    private static final Logger log = LoggerFactory.getLogger(BipFormatSupplierImpl.class);
    private static final Bech32UtilGeneric bech32Util = Bech32UtilGeneric.getInstance();

    private Map<String,BipFormat> bipFormats = new LinkedHashMap<>();

    public BipFormatSupplierImpl() {
        register(BIP_FORMAT.LEGACY);
        register(BIP_FORMAT.SEGWIT_COMPAT);
        register(BIP_FORMAT.SEGWIT_NATIVE);
        register(BIP_FORMAT.TAPROOT);
    }

    public void register(BipFormat bipFormat) {
        bipFormats.put(bipFormat.getId(), bipFormat);
    }

    @Override
    public BipFormat findByAddress(String address, NetworkParameters params) {
        try {
            Pair<Byte, byte[]> bech32Segwit = Bech32Segwit.decode(params instanceof TestNet3Params ? "tb" : "bc", address);
            int witnessVer = bech32Segwit.getLeft().intValue();
            if(witnessVer == 0) {
                // P2WPKH, P2WSH
                return BIP_FORMAT.SEGWIT_NATIVE;
            } else if(witnessVer == 1) {
                // P2TR
                return BIP_FORMAT.TAPROOT;
            }
        } catch(NullPointerException ignored) {
        }

        try {
            if (Address.fromBase58(params, address).isP2SHAddress()) {
                // P2SH
                return BIP_FORMAT.SEGWIT_COMPAT;
            } else {
                // P2PKH
                return BIP_FORMAT.LEGACY;
            }
        } catch (Exception e) {
            log.error("Unknown address type: "+(address!=null?address:"null"));
            return null;
        }
    }

    @Override
    public String getToAddress(TransactionOutput output) throws Exception {
        return getToAddress(output.getScriptBytes(), output.getParams());
    }

    @Override
    public String getToAddress(byte[] scriptBytes, NetworkParameters params) throws Exception {
        String script = Hex.toHexString(scriptBytes);
        if(bech32Util.isBech32Script(script))    {
            // bech32
            return bech32Util.getAddressFromScript(script, params);
        } else {
            // P2PKH or P2SH
            return new Script(scriptBytes).getToAddress(params).toString();
        }
    }

    @Override
    public TransactionOutput getTransactionOutput(String address, long amount, NetworkParameters params) throws Exception {
        if(!FormatsUtilGeneric.getInstance().isValidBitcoinAddress(address, params) && FormatsUtilGeneric.getInstance().isValidBIP47OpReturn(address)) {
            // BIP47
            Script toOutputScript = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN).data(Hex.decode(address)).build();
            return new TransactionOutput(params, null, Coin.valueOf(0L), toOutputScript.getProgram());
        }
        else {
            if (FormatsUtilGeneric.getInstance().isValidBech32(address)) {
                // bech32
                return Bech32UtilGeneric.getInstance().getTransactionOutput(address, amount, params);
            } else {
                Script outputScript = ScriptBuilder.createOutputScript(org.bitcoinj.core.Address.fromBase58(params, address));
                return new TransactionOutput(params, null, Coin.valueOf(amount), outputScript.getProgram());
            }
        }
    }

    @Override
    public BipFormat findById(String bipFormatId) {
        return bipFormats.get(bipFormatId);
    }

    @Override
    public Collection<BipFormat> getList() {
        return bipFormats.values();
    }
}
