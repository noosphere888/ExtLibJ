package com.samourai.wallet.bech32;

import com.samourai.wallet.segwit.bech32.*;
import com.samourai.wallet.util.Triple;
import com.samourai.wallet.util.FormatsUtilGeneric;

import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.util.encoders.Hex;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Bech32Test {

  // test vectors, see: https://github.com/sipa/bech32/blob/master/ref/python/tests.py

    private static String[][] VALID_ADDRESS = {
            // example provided in BIP :
            new String[] { "bc1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3qccfmv3", "00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262"},
            // test vectors :
            new String[] { "BC1QW508D6QEJXTDG4Y5R3ZARVARY0C5XW7KV8F3T4", "0014751e76e8199196d454941c45d1b3a323f1433bd6"},
            new String[] { "tb1qrp33g0q5c5txsp9arysrx4k6zdkfs4nce4xj0gdcccefvpysxf3q0sl5k7","00201863143c14c5166804bd19203356da136c985678cd4d27a1b8c6329604903262"},
            new String[] { "bc1pw508d6qejxtdg4y5r3zarvary0c5xw7kw508d6qejxtdg4y5r3zarvary0c5xw7kt5nd6y","5128751e76e8199196d454941c45d1b3a323f1433bd6751e76e8199196d454941c45d1b3a323f1433bd6"},
            new String[] { "BC1SW50QGDZ25J","6002751e"},
            new String[] { "bc1zw508d6qejxtdg4y5r3zarvaryvaxxpcs","5210751e76e8199196d454941c45d1b3a323"},
            new String[] { "tb1qqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesrxh6hy", "0020000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433"},
            new String[] { "tb1pqqqqp399et2xygdj5xreqhjjvcmzhxw4aywxecjdzew6hylgvsesf3hn0c", "5120000000c4a5cad46221b2a187905e5266362b99d5e91c6ce24d165dab93e86433"},
            new String[] { "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqzk5jj0", "512079be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798"},
            // BIP49 test vector :
            new String[] { "tb1q8zt37uunpakpg8vh0tz06jnj0jz5jddn5mlts3", "001438971f73930f6c141d977ac4fd4a727c854935b3"},
            // Sipa testnet P2TR address, bitcoin dev mailing list, Oct 9, 2021
            new String[] { "tb1p84x2ryuyfevgnlpnxt9f39gm7r68gwtvllxqe5w2n5ru00s9aquslzggwq", "51203d4ca193844e5889fc3332ca98951bf0f474396cffcc0cd1ca9d07c7be05e839"},
            // mainnet addresses, unspent before activation
            new String[] { "bc1pw2knldczhudzzydsns4lree0fafdfn4j4nw0e5xx82lhpfvuxmtqmr95g7", "512072ad3fb702bf1a2111b09c2bf1e72f4f52d4ceb2acdcfcd0c63abf70a59c36d6"},
            new String[] { "bc1pv22mcnt30gwvk8g72szz700n4tkkx2qur2adj6pt8hl37hcf9dasd659sg", "51206295bc4d717a1ccb1d1e54042f3df3aaed63281c1abad9682b3dff1f5f092b7b"},
    };

    //
    // P2TR addresses on mainnet before Taproot activation, encoded as bech32 v0 (old checksum) using v1 scriptpubkeys
    //
    private static String[][] INVALID_MAINNET_P2TR = {
            // https://b10c.me/blog/007-spending-p2tr-pre-activation/?tw :
            new String[] { "bc1pqyqszqgpqyqszqgpqyqszqgpqyqszqgpqyqszqgpqyqszqgpqyqs3wf0qm", "51200101010101010101010101010101010101010101010101010101010101010101"},
            new String[] { "bc1pv22mcnt30gwvk8g72szz700n4tkkx2qur2adj6pt8hl37hcf9dascxyf42", "51206295bc4d717a1ccb1d1e54042f3df3aaed63281c1abad9682b3dff1f5f092b7b"},
            // https://lists.linuxfoundation.org/pipermail/bitcoin-dev/2020-October/018254.html
            new String[] { "bc1pmfr3p9j00pfxjh0zmgp99y8zftmd3s5pmedqhyptwy6lm87hf5ss52r5n8", "5120da4710964f7852695de2da025290e24af6d8c281de5a0b902b7135fd9fd74d21"},
            new String[] { "bc1pw2knldczhudzzydsns4lree0fafdfn4j4nw0e5xx82lhpfvuxmtqwl4cdu", "512072ad3fb702bf1a2111b09c2bf1e72f4f52d4ceb2acdcfcd0c63abf70a59c36d6"},

    };

    // test vectors
    private static String[] INVALID_ADDRESS = {
      // Invalid HRP
      "tc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vq5zuyut",
      // Invalid checksum algorithm (bech32 instead of bech32m)
      "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqh2y7hd",
      // Invalid checksum algorithm (bech32 instead of bech32m)
      "tb1z0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqglt7rf",
      // Invalid checksum algorithm (bech32 instead of bech32m)
      "BC1S0XLXVLHEMJA6C4DQV22UAPCTQUPFHLXM9H8Z3K2E72Q4K9HCZ7VQ54WELL",
      // Invalid checksum algorithm (bech32m instead of bech32)
      "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kemeawh",
      // Invalid checksum algorithm (bech32m instead of bech32)
      "tb1q0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vq24jc47",
      // Invalid character in checksum
      "bc1p38j9r5y49hruaue7wxjce0updqjuyyx0kh56v8s25huc6995vvpql3jow4",
      // Invalid witness version
      "BC130XLXVLHEMJA6C4DQV22UAPCTQUPFHLXM9H8Z3K2E72Q4K9HCZ7VQ7ZWS8R",
      // Invalid program length (1 byte)
      "bc1pw5dgrnzv",
      // Invalid program length (41 bytes)
      "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7v8n0nx0muaewav253zgeav",
      // Invalid program length for witness version 0 (per BIP141)
      "BC1QR508D6QEJXTDG4Y5R3ZARVARYV98GJ9P",
      // Mixed case
      "tb1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vq47Zagq",
      // More than 4 padding bits
      "bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7v07qwwzcrf",
      // Non-zero padding in 8-to-5 conversion
      "tb1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vpggkg4j",
      // Empty data section
      "bc1gmk9yu",
    };

    private static String[] VALID_BECH32 = {
        "A12UEL5L",
        "a12uel5l",
        "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1tt5tgs",
        "abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw",
        "11qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqc8247j",
        "split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w",
        "?1ezyfcl",
      };

    private static String[] VALID_BECH32M = {
        "A1LQFN3A",
        "a1lqfn3a",
        "an83characterlonghumanreadablepartthatcontainsthetheexcludedcharactersbioandnumber11sg7hg6",
        "abcdef1l7aum6echk45nj3s0wdvt2fg8x9yrzpqzd3ryx",
        "11llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllludsr8",
        "split1checkupstagehandshakeupstreamerranterredcaperredlc445v",
        "?1v759aa",
        "tb1puu0gl3x9qm0l9xq0ajdepz326n66j702ecw5x7w3lk3knws3pmfq26386t",
      };

    private static String[] INVALID_BECH32 = {
        new String(new char[] { 0x20 }) + "1nwldj5", // HRP character out of range
        new String(new char[] { 0x7f }) + "1axkwrx", // HRP character out of range
        new String(new char[] { 0x80 }) + "1eym55h", // HRP character out of range
        // overall max length exceeded
        "an84characterslonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1569pvx",
        "pzry9x0s0muk",      // No separator character
        "1pzry9x0s0muk",     // Empty HRP
        "x1b4n0q5v",         // Invalid data character
        "li1dgmt3",          // Too short checksum
        "de1lg7wt" + new String(new char[] { 0xff }), // invalid character in checksum
        "A1G7SGD8",          // checksum calculated with uppercase form of HRP
        "10a06t8",           // empty HRP
        "1qzzfhee",          // empty HRP
      };

    private static String[]     INVALID_BECH32M = {
        new String(new char[] { 0x20 }) + "1xj0phk", // HRP character out of range
        new String(new char[] { 0x7f }) + "1g6xzxy", // HRP character out of range
        new String(new char[] { 0x80 }) + "1vctc34", // HRP character out of range
        // overall max length exceeded
        "an84characterslonghumanreadablepartthatcontainsthetheexcludedcharactersbioandnumber11d6pts4",
        "qyrz8wqd2c9m",      // No separator character
        "1qyrz8wqd2c9m",     // Empty HRP
        "y1b0jsk6g",         // Invalid data character
        "lt1igcx5c0",        // Invalid data character
        "in1muywd",          // Too short checksum
        "mm1crxm3i",         // Invalid character in checksum
        "au1s5cgom",         // Invalid character in checksum
        "M1VUXWEZ",          // Checksum calculated with uppercase form of HRP
        "16plkw9",           // Empty HRP
        "1p2gdwpf",          // Empty HRP
        "tb1puu0gl3x9qm0l9xq0ajdepz326n66j702ecw5x7w3lk3knws3pmfq26385t", // bad checksum
      };

      private Triple<String, byte[], Integer> p = null;

      @Test
      public void validBech32() throws Exception {
        for(String s : VALID_BECH32)   {
            p = null;
            p = Bech32.bech32Decode(s);
            assert(p != null && p.getLeft() != null && p.getRight() == Bech32.BECH32);
        }
      }

      @Test
      public void validBech32m() throws Exception {
        for(String s : VALID_BECH32M)   {
            p = null;
            p = Bech32.bech32Decode(s);
            assert(p != null && p.getLeft() != null && p.getRight() == Bech32.BECH32M);
        }
      }

      @Test
      public void invalidBech32() throws Exception {
        for(String s : INVALID_BECH32)   {
            p = null;
            p = Bech32.bech32Decode(s);
            assert(p == null || p.getRight() != Bech32.BECH32);
        }
      }

      @Test
      public void invalidBech32m() throws Exception {
        for(String s : INVALID_BECH32M)   {
            p = null;
            p = Bech32.bech32Decode(s);
            assert(p == null || p.getRight() != Bech32.BECH32M);
        }
      }

      @Test
      public void validAddress() throws Exception {
        for(String[] s : VALID_ADDRESS)   {
          byte witVer;
          String hrp = new String(Bech32.bech32Decode(s[0]).getLeft());

          byte[] witProg;
          Pair<Byte, byte[]> segp = null;
          segp = Bech32Segwit.decode(hrp, s[0]);

          assert(segp != null);
          witVer = segp.getLeft();
          witProg = segp.getRight();
          assert(!(witVer < 0 || witVer > 16));

          byte[] pubkey = Bech32Segwit.getScriptPubkey(witVer, witProg);
          assert(Hex.toHexString(pubkey).equalsIgnoreCase(s[1]));

          String address = Bech32Segwit.encode(hrp, witVer, witProg);
          assert(s[0].equalsIgnoreCase(address));
        }
      }

      @Test
      public void invalidAddress() throws Exception {
        for(String s : INVALID_ADDRESS)   {

            Pair<Byte, byte[]> pair = null;

            try {
              pair = Bech32Segwit.decode("tb", s);
              assert(pair == null);
            }
            catch(Exception e) {
              pair = Bech32Segwit.decode("bc", s);
              assert(pair == null);
            }

        }
      }

      @Test
      public void invalidMainnetP2TR() throws Exception {
        for(String[] s : INVALID_MAINNET_P2TR)   {
          byte witVer;
          String hrp = new String(Bech32.bech32Decode(s[0]).getLeft());

          byte[] witProg;
          Pair<Byte, byte[]> segp = null;
          segp = Bech32Segwit.decode(hrp, s[0]);
          assert(segp == null);
        }
      }

      @Test
      public void encodeBIP49() throws Exception {
        String address = Bech32Segwit.encode("tb", (byte)0x00, Hex.decode("38971f73930f6c141d977ac4fd4a727c854935b3"));

        byte witVer;
        String hrp = new String(Bech32.bech32Decode(address).getLeft());

        byte[] witProg;
        Pair<Byte, byte[]> segp = null;
        segp = Bech32Segwit.decode(hrp, address);
        witVer = segp.getLeft();
        witProg = segp.getRight();

        assert(!(witVer < 0 || witVer > 16));

        byte[] pubkey = Bech32Segwit.getScriptPubkey(witVer, witProg);
        assert(Hex.toHexString(pubkey).equalsIgnoreCase("001438971f73930f6c141d977ac4fd4a727c854935b3"));
      }

      @Test
      public void binance() throws Exception {
        // https://bitcoin.stackexchange.com/questions/111440/is-it-possible-to-convert-a-taproot-address-into-a-native-segwit-address
        String address = Bech32Segwit.encode("bc", (byte)0x01, Hex.decode("4b65fc5025504c267708c52779c5885ae95c94f5f289b32179977af5ada63fec"));
        assert(address.equals("bc1pfdjlc5p92pxzvacgc5nhn3vgtt54e98472ymxgtejaa0ttdx8lkqzn304u"));

        byte witVer;
        String hrp = new String(Bech32.bech32Decode(address).getLeft());

        byte[] witProg;
        Pair<Byte, byte[]> segp = null;
        segp = Bech32Segwit.decode(hrp, address);
        witVer = segp.getLeft();
        witProg = segp.getRight();

        assert(witVer == (byte)0x01);

        byte[] pubkey = Bech32Segwit.getScriptPubkey(witVer, witProg);
        assert(Hex.toHexString(pubkey).equalsIgnoreCase("51204b65fc5025504c267708c52779c5885ae95c94f5f289b32179977af5ada63fec"));

        address = Bech32Segwit.encode(hrp, witVer, witProg);
        assert(address.equals("bc1pfdjlc5p92pxzvacgc5nhn3vgtt54e98472ymxgtejaa0ttdx8lkqzn304u"));
      }

      @Test
      public void invalidAddressEncode() throws Exception {
        String code = null;

        code = Bech32Segwit.encode("BC", (byte)0x00, new byte[20]);
        assert(code == null);
        code = Bech32Segwit.encode("bc", (byte)0x00, new byte[21]);
        assert(code == null);
        code = Bech32Segwit.encode("bc", (byte)0x11, new byte[32]);
        assert(code == null);
        code = Bech32Segwit.encode("bc", (byte)0x01, new byte[1]);
        assert(code == null);
        code = Bech32Segwit.encode("bc", (byte)0x10, new byte[41]);
        assert(code == null);
      }

      @Test
      public void formatUtil() throws Exception {
        for(String[] s : VALID_ADDRESS)   {
          boolean res = FormatsUtilGeneric.getInstance().isValidBech32(s[0]);
          assert(res == true);
        }

        for(String s : INVALID_ADDRESS)   {
          boolean res = FormatsUtilGeneric.getInstance().isValidBech32(s);
          assert(res == false);
        }
      }

}
