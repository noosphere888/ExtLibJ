package com.samourai.wallet.payload;

import com.samourai.wallet.test.AbstractTest;
import com.samourai.wallet.util.JSONUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupEncryptedTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(BackupEncryptedTest.class);

    @Test
    public void encryptBackupPayload() throws Exception {
        String PAYLOAD = "{\"wallet\":{\"whirlpool_account\":[{\"changeIdx\":0,\"receiveIdx\":0,\"zpub\":\"zpub6rKtc12xtiYXcPMowvBhoNV5XqXDdLAbBPWbTbPGTzzS1ehz94P2KFCKoyeHyUTMFhD5Bj4XevZwrRvBK39hG67SH4x3i5eauzZeQV1EwBw\",\"id\":2147483645},{\"changeIdx\":0,\"receiveIdx\":0,\"zpub\":\"zpub6rKtc12xtiYXei7YcAfs88sEB5A4adcAR4gVNfqHMiWdd2hXJSpnVmy65rPjZJRn5bxAkmpSfRzw8Kp4buKygSQAkhmH2uRGrvDsuFLAyiM\",\"id\":2147483646},{\"changeIdx\":0,\"receiveIdx\":0,\"zpub\":\"zpub6rKtc12xtiYXa9zjfimmeXHGeXektVaQDttRk5R3gzZwfeNaKTj3rCmnsMrJYsakr9rJ8fF2p6jbU7V3zxApyP6gc72Dh6ZLK9DjYfR3wsy\",\"id\":2147483644}],\"bip84_accounts\":[{\"changeIdx\":0,\"receiveIdx\":0,\"zpub\":\"zpub6rKtc12pZ41ZY66Bajg7dPE8G6Gov5qSADMZc5uznJjLwQBX3zpLXPvqMjoTSnh3U4217VnxG7X2jNYJCS4T5QDsYWrLN9yA1eNw1ofrTsz\",\"id\":0}],\"seed\":\"e598320ab2d5c42f94b108ec0dece582\",\"fingerprint\":\"77275290\",\"payment_code_feature\":\"PM8TJbvQrAdpXquDjkh7MQ1ZbtTTXsMVri9ydDbHPWNwNFvuLJanbsqugjLU2CGG78ceHJgui3C94TH2xVBcm9itNvoFSFHV5jkF9z61UfsC5nDFt8o4\",\"bip49_accounts\":[{\"changeIdx\":0,\"receiveIdx\":0,\"id\":0,\"ypub\":\"ypub6WvxpidnzPZMy1S6udXxo6WT8dcd7MrQokJKeqE2HLQrbmVGfPzNoyyvtyJJFvxjxLtbFPQfVk8r5m872evxReLMJckqEg8SZum96pzzq7U\"}],\"passphrase\":\"test\",\"accounts\":[{\"changeIdx\":0,\"receiveIdx\":0,\"id\":0,\"xpub\":\"xpub6C2STbGiq9qvjhgfTyXhvHK2HQg4HFgqWcPBVJM82stnyfcLfLTvRKc5M5GCn7t1a5LgsCvvB9bUhQPoLZYFQjN4Wu8yV6y3ZbHtF3KyRXe\"}],\"testnet\":false,\"payment_code\":\"PM8TJbvQrAdpXquDjkh7MQ1ZbtTTXsMVri9ydDbHPWNwNFvuLJanbsqugjLU2CGG78ceHJgui3C94TH2xVBcm9itNvoFSFHV5jkF9z61UfsC5n59n2Xj\"},\"meta\":{\"device_product\":\"Jelly2_EEA\",\"rbf_opt_in\":false,\"blocked_utxos\":{\"blocked\":[],\"blockedPostMix\":[],\"notDustedPostMix\":[],\"notDusted\":[],\"blockedBadBank\":[]},\"xpubpostxreg\":false,\"pin2\":\"\",\"haptic_pin\":true,\"remote\":false,\"xpubricochetlock\":false,\"xpubpostlock\":false,\"pin\":\"00000\",\"xpubprereg\":false,\"use_like_typed_change\":true,\"use_trusted\":false,\"xpubreg84\":true,\"xpubreg44\":true,\"xpubpostreg\":false,\"ricochet_staggered_delivery\":false,\"spend_type\":0,\"xpubreg49\":true,\"use_segwit\":true,\"tx0_display\":[],\"prev_balance\":0,\"device_model\":\"Jelly2\",\"xpubricochetreg\":false,\"paynym_featured_v1\":false,\"rbfs\":[],\"scramble_pin\":true,\"utxo_notes\":[],\"xpublock49\":false,\"device_manufacturer\":\"Unihertz\",\"trusted_no\":\"\",\"user_offline\":false,\"xpublock44\":false,\"xpublock84\":false,\"use_ricochet\":false,\"cahoots\":[],\"tor\":{\"active\":false},\"ricochet\":{\"index\":0,\"staggered\":[],\"xpub\":\"zpub6rKtc12xtiYXhEMfFySB7D762WjSyHZSXnXuz1Q6cordkRu5uabzSpuA5EaB2b8Q7UmvgFCMnCFSBDcjoZ6GfZFodmLdM75MK1dbE1LCeem\",\"queue\":[]},\"paynym_claimed\":false,\"bip47\":{\"pcodes\":[],\"incoming_notif_hashes\":[]},\"xpubbadbanklock\":false,\"auto_backup\":true,\"check_sim\":false,\"batch_send\":[],\"strict_outputs\":true,\"whirlpool\":{},\"sent_tos_from_bip47\":[],\"broadcast_tx\":true,\"paynym_refused\":true,\"localIndexes\":{\"local49idx\":0,\"local84idx\":0,\"local44idx\":0},\"sent_tos\":[],\"utxo_scores\":[],\"version_name\":\"0.99.97a\",\"utxo_tags\":[],\"android_release\":\"10\",\"xpubprelock\":false,\"is_sat\":false}}";
        String password = "test";
        String encrypted = BackupEncrypted.encryptPayload(PAYLOAD, password);
        String decrypted = BackupEncrypted.decryptPayload(encrypted, password, BackupEncrypted.VERSION_CURRENT);

        Assertions.assertEquals(PAYLOAD, decrypted);
    }

    @Test
    public void encrypt_decrypt() throws Exception {
        String BACKUP_FILE = "{\"version\":2,\"payload\":\"U2FsdGVkX1+vzNxPLqaOpV8Dv7OPnorGrFtAFcqJ+DLQofA2E5IwRU9ZaGw9TExs\\ndHI9PENEOhYq3YKWNH+Sly5ObFSNo8goCHmzpSjYP7za2psfhCI11+1brl4iDqAI\\nKjTSLqwZVSbDcKFAh5lOiSViUwnI+6Q\\/sUrRXYektaFq086Oj0hpr46yUoSy8PsI\\n38bdO0fkjhn5Pa+rE3INldporrdI4vXOJgmUyUJrh0faJNWWAn6AkQdHL0kEX\\/du\\nLat7YI\\/vzKWhlwx8z1i+DwelSyqQq7AyYuJfAmWmyKMyc6G0tNHczASXgzBk3isD\\nubAYguMR8fqBYsqGeWH+6ffRFiCrdCmos\\/uGyKN7KrgwyGGGXY2Bpp8AaEOnwSIY\\na0Ka+GicjPrkONGeHngDawgghLDb5pH9o\\/J052JmbDcHpt\\/Q+3PfzKnVOPTBZdT7\\nBj1jZGLs8fCDXKH+0f3NM3GWZbKZRA\\/fLi91U+pSopXkrcH7CAO2HiXzM1Q5rZFs\\nfgZBZ5GlzTy3ngSI3DHOwxanPeMTdyKFVRfTMlf0bVCC\\/glWXvoJG6emB71WTXh7\\nkTcEhb\\/+GT6YnL0Jq\\/LNjXh7\\/jhZsokYoA7lrVt6VaklA0BmAutRi5a67Ntcp2w6\\nGntx+uiaL5y9lyaTIiwjDLgaaK3Sp7gNYH78UyhvqxdDcSxTS2q4dxNfWjBXaV65\\n2zWtQuVDGL9CHFDO6b9jDvD5RpvPeHa9oQ1xc9csu0h2ozUT95plyR1nnSF5qH+U\\njQJMRe65K4\\/QtW2heBLcLHxy\\/s01unVEb29Jeo\\/Wjl8vbTIcxilALl2ulXwwZtAR\\nPLrFp3fObQ8NGo\\/\\/iuf0LcqxxfuKAr8oOZX4X6zfAivRrcwhLuY6WSng4+QBytOl\\nWtkajwf8cMsRND3xvX9KT6Q2kgAHIQZB8zowVJapHG56IOZHMw7xcLzmJdcGR8Vc\\nIksGkDaBKG7QYW3dUr\\/2DuMHMpsVugtttNweQyqULuviKYAjOtvQMP2mthzQ7eJ1\\nzizvEaHB+UaYIB2KTm9hy7kX9I+c6UmB+8ZxvKncaO3YzEvDqLq8TNqOpv1pK1BM\\nokXVY5TZwYD97D8HZiBdeVxV28T3tWIUfN+EeIwC095F0ith6Hn35nAgUftbE196\\nREpR3FInIMQzaAbXFsiXUpmWFmRU1EmnJGOQ9DxHxCai38dr1RZxii3fifS7TWs8\\nadbB7Gi9Bc3r79yNI2pxyUqj5HjSb0Vk7GOJH2VToivrjM+YSpdyYDnFZw\\/9Bzkj\\nbpLhHR8Y0ZSS9OQijQDuD4Veave+igdx6sf\\/BvdHkf3U0gaSCOCynhS9uLopz5wA\\nhylPGkvJRvsNa3+1OQQVLw9kFCjjh+2+sVeKOBOWNJ1ANKCgQ1AZtfb3aVEQSYAs\\nBAX+q1vI5O\\/rJ6zaWACJbUuwsk3HCg03y83Fv8bXTFM+4GW5XVBB7adpYUWFBERd\\nMBEPFrXwERUsQ\\/51eRr6csOwE9FfB9J+oJ4XyicmN5+KxiVO66CpgnqeNBP4asc1\\nOHdDTrKDHkzutAeMNBGlqmpCLdgtU37eh2NuKfFOleq9zgLPcoAlveD\\/x5uOm\\/5U\\nrfiXSlazSgz7kTVzvUZZQ\\/isrqVar2NN2RDiB+JO3xPeOMSzaKim+FlyUaUWuA2U\\nSxWYRtXQ4xmFsNOF1A3i8MlMI7apG96sLuFtbMe\\/eBxZk1ykW3svJnCjf8rIXpT5\\noUy\\/MnTxRApQBTeYnKTgxNu74Cqw\\/+kRwcStWlNtZwRgK1J8MXVQB+hkj6qzFY+7\\nqO53yLErZN0IzKxEvPSauA5oMkxsIB7aPbyoxzcOQERDMZ35ekwl\\/vx5fk3csk1k\\nxRavH26hz3iFiabKwH1OV+UyeT2ImdJMM9MSSkG8H5qzBxwTE9qUMblrnXphbIt1\\nW2gtwm1e\\/\\/gWKRqzQmSnjDPLaMWwN0G9KOzRTtmccC6pZHp0f2e2XJOJrNO9yCsw\\nK6SvV0JhMjDpnVyx500TtNUkaUiljNU2+Hfo8kOQeDZwnXDhymQQFKSpl5NONzZy\\nE0XT9Y4V3gWxArT4FnSEWC+\\/FBn6XJBimIc6q9\\/9exyMV2ENQ+8PUR+ouSZUfRNI\\njV08mBpSVovTFnR7UD1+bC3lAiLFXIEaxZIDgg6CpAmnDJkZQmTOKk9d1iNi89zN\\n1s5dM5LpahKF6cpmClUc9sx00rTUetG5083o8JWFn75EMdfmu6yIU\\/3Ijduu2Jne\\nXsw18KXv\\/YmPjiZ8gdDwOK21ypvr2zSeFAxOGGcJrFxEmRalWRz2xV3J6MJ5izLL\\nKihKbjJFD7BmX6RLyM9jviRY9CU5\\/uNck3tX80z1tMQ\\/MnYL3q7WzGjpOQBWujzb\\noUZW8AhGjWp4h547FND2jMJX5NZlpGs9cVgKzQBybnd0O2OJOJveZ6et9oi8niNe\\ncJT2V9cj7b9B2Z2fIbMaLILnBynnsJaYbCXO2IIV7CniQ6qyktX6Vb5Pgmc1MN1Z\\nKIp11aeXU87CG\\/I6y0FDVUKvfSxOUsMmW4Uo7RRx+xD5BuZiJBhEqpXIhiGu9+Dx\\nWc0EQgoA6jzMWIPSDZLbHoO6zP2x9LOfUXMaEQgp21bIXi0qYFjtSUsmjXKkFvZS\\nPQuYxrA61FSnSgjUZ591hk67yNAk\\/uzb3e0HFLAQhpev1tWJZiNUMMKOesWshHas\\nrxAYvtp3wNQPsZwAcWQM2K72EFtYLfOqpTLhkhkuhzEG5Oh5vGG\\/Jn+Y4r3oc7YT\\n9Xpt\\/Q9YOFXYZC0B82WJi56NznjjSxwqgbiB1SQw\\/+c87p\\/Qgv7ah+pz2J+jsUZ3\\nL7QlgPdofbQZk3ZXplHcSkAFdgwS1UWhiYOAAqvHIFUWeuOSg37cP4XLxSShC254\\nTRk7Mw\\/JNuSCgZ6aZryni48oGfbCbohPvr28dmG6f1xTbIgGV+MfHiUKLjg\\/eGn+\\nIgOGxIyylftAj2vagcNleXJ9JevdRofMF4dYCPTdnqZCTtGSO0dN1PVf5E6kBXZ6\\noeIxgVQfM8qeaPpXbut6g7edFP7kJtIEIBxtz5HQrsov4Qpzl3hCH1J5\\/WxJYQZw\\npfFv3BCF2PtTyeFhhPXM3jouAh46mHC6grku6dQ26HKgGn0EYhMNEkqQQ1vncIZD\\nP7GhkH\\/hHXB6VltiJVXpFjtxQMx\\/RJKWUqD6WMFoZ6pfe\\/cwjkSxTx5VZ4Jqfz\\/b\\nDbsUtzld65rMA1u8Is2w8Z30A93aYFbToD8CYeCkcQ\\/TIADdfw3BPpnwXi6we34C\\nJcC65brjf6k4vOVFtfLyVr3GHsjq\\/1HpMtEXRiaCNgKpaF59HwI2Z9bR\\/O6bic8C\\neKS2e6h70N0puGJvRdhb1feiWeT33oMZrcUfTpJ4s91L15zHtUd+vsv8500qSeyJ\\nJaJdJFDnRrY7+OhfkIC6GRpnot85Usi\\/CDznGEM21fklx36tQsElv9qeBbQ9MGGZ\\nNRZqjQDsmbq7AYnXrjHnR6L5eyAiRGYGKZnd+a2247QJ\\/t+SCpqQrh+eoDo6tthP\\n8LTIi+srBNjF+swcg3q75CDyN8\\/Ni1TWVlbdUbMfgRgdKzWEwatEYFbjW\\/U0CH2+\\nSnnkUQmz8ibshNra4RUZSjBeWzWPBwQBAFwCtGZRltG4MiWQnFekWHehZ9tpcj1s\\nHjCpr1yaY5PZxubQMlzUlQT60GLtMUOv7fE\\/cmDC9O7MWoFlBI2HkFhpTzEnEust\\nSg+L\\/91gL5RqKx2UwV5gNB1nBlDg4X++XSvblD64+Y3Jh4oEAFaIfzfR1xXgqb7B\\nC+VceZNmY6WH0L3apMC1t4L1ekPMnDUhzq8I0ir6npmwojtIc3+7bPgbzSevqQzs\\nXKIQp58vCMkL+kEuu\\/bvFT1fq+Do8OzHgzUbucs7WrZ1I8f+Oy+04IkA5Fi5iCqL\\n\",\"external\":false}";
        String password = "test";
        BackupPayload backupPayload = payloadUtil.readBackup(BACKUP_FILE, password);
        String PAYLOAD = "{\"wallet\":{\"whirlpool_account\":[{\"changeIdx\":0,\"receiveIdx\":0,\"zpub\":\"zpub6rKtc12xtiYXcPMowvBhoNV5XqXDdLAbBPWbTbPGTzzS1ehz94P2KFCKoyeHyUTMFhD5Bj4XevZwrRvBK39hG67SH4x3i5eauzZeQV1EwBw\",\"id\":2147483645},{\"changeIdx\":0,\"receiveIdx\":0,\"zpub\":\"zpub6rKtc12xtiYXei7YcAfs88sEB5A4adcAR4gVNfqHMiWdd2hXJSpnVmy65rPjZJRn5bxAkmpSfRzw8Kp4buKygSQAkhmH2uRGrvDsuFLAyiM\",\"id\":2147483646},{\"changeIdx\":0,\"receiveIdx\":0,\"zpub\":\"zpub6rKtc12xtiYXa9zjfimmeXHGeXektVaQDttRk5R3gzZwfeNaKTj3rCmnsMrJYsakr9rJ8fF2p6jbU7V3zxApyP6gc72Dh6ZLK9DjYfR3wsy\",\"id\":2147483644}],\"bip84_accounts\":[{\"changeIdx\":0,\"receiveIdx\":0,\"zpub\":\"zpub6rKtc12pZ41ZY66Bajg7dPE8G6Gov5qSADMZc5uznJjLwQBX3zpLXPvqMjoTSnh3U4217VnxG7X2jNYJCS4T5QDsYWrLN9yA1eNw1ofrTsz\",\"id\":0}],\"seed\":\"e598320ab2d5c42f94b108ec0dece582\",\"fingerprint\":\"77275290\",\"payment_code_feature\":\"PM8TJbvQrAdpXquDjkh7MQ1ZbtTTXsMVri9ydDbHPWNwNFvuLJanbsqugjLU2CGG78ceHJgui3C94TH2xVBcm9itNvoFSFHV5jkF9z61UfsC5nDFt8o4\",\"bip49_accounts\":[{\"changeIdx\":0,\"receiveIdx\":0,\"id\":0,\"ypub\":\"ypub6WvxpidnzPZMy1S6udXxo6WT8dcd7MrQokJKeqE2HLQrbmVGfPzNoyyvtyJJFvxjxLtbFPQfVk8r5m872evxReLMJckqEg8SZum96pzzq7U\"}],\"passphrase\":\"test\",\"accounts\":[{\"changeIdx\":0,\"receiveIdx\":0,\"id\":0,\"xpub\":\"xpub6C2STbGiq9qvjhgfTyXhvHK2HQg4HFgqWcPBVJM82stnyfcLfLTvRKc5M5GCn7t1a5LgsCvvB9bUhQPoLZYFQjN4Wu8yV6y3ZbHtF3KyRXe\"}],\"testnet\":false,\"payment_code\":\"PM8TJbvQrAdpXquDjkh7MQ1ZbtTTXsMVri9ydDbHPWNwNFvuLJanbsqugjLU2CGG78ceHJgui3C94TH2xVBcm9itNvoFSFHV5jkF9z61UfsC5n59n2Xj\"},\"meta\":{\"device_product\":\"Jelly2_EEA\",\"rbf_opt_in\":false,\"blocked_utxos\":{\"blocked\":[],\"blockedPostMix\":[],\"notDustedPostMix\":[],\"notDusted\":[],\"blockedBadBank\":[]},\"xpubpostxreg\":false,\"pin2\":\"\",\"haptic_pin\":true,\"remote\":false,\"xpubricochetlock\":false,\"xpubpostlock\":false,\"pin\":\"00000\",\"xpubprereg\":false,\"use_like_typed_change\":true,\"use_trusted\":false,\"xpubreg84\":true,\"xpubreg44\":true,\"xpubpostreg\":false,\"ricochet_staggered_delivery\":false,\"spend_type\":0,\"xpubreg49\":true,\"use_segwit\":true,\"tx0_display\":[],\"prev_balance\":0,\"device_model\":\"Jelly2\",\"xpubricochetreg\":false,\"paynym_featured_v1\":false,\"rbfs\":[],\"scramble_pin\":true,\"utxo_notes\":[],\"xpublock49\":false,\"device_manufacturer\":\"Unihertz\",\"trusted_no\":\"\",\"user_offline\":false,\"xpublock44\":false,\"xpublock84\":false,\"use_ricochet\":false,\"cahoots\":[],\"tor\":{\"active\":false},\"ricochet\":{\"index\":0,\"staggered\":[],\"xpub\":\"zpub6rKtc12xtiYXhEMfFySB7D762WjSyHZSXnXuz1Q6cordkRu5uabzSpuA5EaB2b8Q7UmvgFCMnCFSBDcjoZ6GfZFodmLdM75MK1dbE1LCeem\",\"queue\":[]},\"paynym_claimed\":false,\"bip47\":{\"pcodes\":[],\"incoming_notif_hashes\":[]},\"xpubbadbanklock\":false,\"auto_backup\":true,\"check_sim\":false,\"batch_send\":[],\"strict_outputs\":true,\"whirlpool\":{},\"sent_tos_from_bip47\":[],\"broadcast_tx\":true,\"paynym_refused\":true,\"localIndexes\":{\"local49idx\":0,\"local84idx\":0,\"local44idx\":0},\"sent_tos\":[],\"utxo_scores\":[],\"version_name\":\"0.99.97a\",\"utxo_tags\":[],\"android_release\":\"10\",\"xpubprelock\":false,\"is_sat\":false}}";
        Assertions.assertEquals(PAYLOAD, backupPayload.toJson().toString());

        // encrypt
        BackupEncrypted backupEncrypted = BackupEncrypted.encrypt(backupPayload, password);
        Assertions.assertEquals(2, backupEncrypted.getVersion());
        Assertions.assertEquals(false, backupEncrypted.isExternal());
        Assertions.assertNotEquals(PAYLOAD, backupEncrypted.getPayload());

        // decrypt
        BackupPayload bp = backupEncrypted.decrypt(password);
        Assertions.assertEquals(PAYLOAD, bp.toJson().toString());
    }

    @Test
    public void decryptPayloadException() throws Exception {
        String password = "test";
        // throw Exception for empty Backup File
        Assertions.assertThrows(Exception.class,
                () -> {
                    BackupPayload backupPayload = payloadUtil.readBackup("", password);
                });
    }


    @Test
    public void noPayloadValidateException() throws Exception {
        String BACKUP_FILE_NO_PAYLOAD = "{\"version\":2,\"external\":false}";
        BackupEncrypted backupPayload = JSONUtils.getInstance().getObjectMapper().readValue(BACKUP_FILE_NO_PAYLOAD, BackupEncrypted.class);

        // throw Exception for no payload
        Assertions.assertThrows(Exception.class,
            () -> {
                backupPayload.validate();
            });

    }
}
