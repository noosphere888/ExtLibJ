package com.samourai.wallet.crypto;

import com.samourai.wallet.util.CharSequenceX;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AESUtilTest {

    private String expected = "all all all all all all all all all all all all";
    private String password = "secret";

    @Test
    public void encryptDecrypt() throws Exception {
        doEncryptDecrypt(this.expected, this.password);
    }

    private void doEncryptDecrypt(String cleartext, String password) throws Exception {
        // valid for password
        CharSequenceX passwordx = new CharSequenceX(password);
        String encrypted = AESUtil.encrypt(cleartext, passwordx);
        String decrypted = AESUtil.decrypt(encrypted, passwordx);

        Assertions.assertEquals(cleartext, decrypted);
        Assertions.assertNotEquals(cleartext, encrypted);

        // invalid for wrong password
        String encryptedWrong = AESUtil.encrypt(cleartext, passwordx);
        try {
            String decryptedWrong = AESUtil.decrypt(encryptedWrong, new CharSequenceX("wrong"));
            Assertions.assertNotEquals(cleartext, decryptedWrong);
        } catch(Exception e) {
            // ok
        }
    }

    @Test
    public void decrypt() throws Exception {
        String encrypted = "t6MNj4oCb9T54lKWNAF274Hg72E0q0uJooUwKjzGD+ysWsFv8Ib47ubdnjStkeJ/G9UltiERHAm1tKRtHbaJiA==";
        doDecrypt(encrypted, this.password, this.expected);

        encrypted = "DLXJBb7/Kbn6LrUESX/wASjbM3Xh7+ENDe/GaxcI6j/3Rid5qJwYSwo0MKGJR5eUD4BAkw9Y4nhMDPe6wNldIQ==:";
        doDecrypt(encrypted, this.password, this.expected);
    }

    private void doDecrypt(String encrypted, String password, String expected) throws Exception {
        CharSequenceX passwordx = new CharSequenceX(password);
        String decrypted = AESUtil.decrypt(encrypted, passwordx);
        Assertions.assertEquals(expected, decrypted);
    }



    @Test
    public void encryptDecryptSHA256() throws Exception {
        doEncryptDecryptSHA256(this.expected, this.password);
    }


    private void doEncryptDecryptSHA256(String cleartext, String password) throws Exception {
        // valid for password
        CharSequenceX passwordx = new CharSequenceX(password);
        String encrypted = AESUtil.encryptSHA256(cleartext, passwordx);
        String decrypted = AESUtil.decryptSHA256(encrypted, passwordx);

        Assertions.assertEquals(cleartext, decrypted);
        Assertions.assertNotEquals(cleartext, encrypted);

        // invalid for wrong password
        String encryptedWrong = AESUtil.encryptSHA256(cleartext, passwordx);
        try {
            String decryptedWrong = AESUtil.decryptSHA256(encryptedWrong, new CharSequenceX("wrong"));
            Assertions.assertNotEquals(cleartext, decryptedWrong);
        } catch (Exception e) {
            // ok
        }
    }

    @Test
    public void encryptException() throws Exception {
        // throw Exception for null password
        Assertions.assertThrows(DecryptionException.class,
            () -> {
                AESUtil.encrypt(this.expected, null);
            });
    }
}
