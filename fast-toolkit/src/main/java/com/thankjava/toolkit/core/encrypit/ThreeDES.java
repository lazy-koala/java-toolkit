package com.thankjava.toolkit.core.encrypit;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 可逆加解密3DS
 * <p>Function: ThreeDES</p>
 * <p>Description: </p>
 *
 * @author acexy@thankjava.com
 * @version 1.0
 * @date 2015年12月30日 上午11:08:20
 */
public final class ThreeDES {


    private String priKey;

    /**
     * 32位字符对称加密密钥
     *
     * @param priKey
     */
    public ThreeDES(String priKey) {
        if (priKey == null || priKey.length() != 32) {
            throw new IllegalArgumentException("priKey can not be null and length will be 32");
        }
        this.priKey = priKey;
    }

    public ThreeDES() {
        this.priKey = "00000000000000000000000000000000";
    }

    private static final String[] BYTE_HEX_TABLE = {
            "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B",
            "0C", "0D", "0E", "0F", "10", "11", "12", "13", "14", "15", "16", "17",
            "18", "19", "1A", "1B", "1C", "1D", "1E", "1F", "20", "21", "22", "23",
            "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
            "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B",
            "3C", "3D", "3E", "3F", "40", "41", "42", "43", "44", "45", "46", "47",
            "48", "49", "4A", "4B", "4C", "4D", "4E", "4F", "50", "51", "52", "53",
            "54", "55", "56", "57", "58", "59", "5A", "5B", "5C", "5D", "5E", "5F",
            "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6A", "6B",
            "6C", "6D", "6E", "6F", "70", "71", "72", "73", "74", "75", "76", "77",
            "78", "79", "7A", "7B", "7C", "7D", "7E", "7F", "80", "81", "82", "83",
            "84", "85", "86", "87", "88", "89", "8A", "8B", "8C", "8D", "8E", "8F",
            "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9A", "9B",
            "9C", "9D", "9E", "9F", "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7",
            "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF", "B0", "B1", "B2", "B3",
            "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
            "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB",
            "CC", "CD", "CE", "CF", "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7",
            "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF", "E0", "E1", "E2", "E3",
            "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF",
            "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "FA", "FB",
            "FC", "FD", "FE", "FF"
    };


    private final static Map<byte[], Cipher> ENCRYPT = new HashMap<>();
    private final static Map<byte[], Cipher> DECRYPT = new HashMap<>();

    private Cipher getCipher() {
        try {
            return Cipher.getInstance("DESEDE/ECB/NOPADDING");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 进件加密
     *
     * @param pubKey
     * @param content
     * @return
     */
    public String encrypt(String pubKey, String content) {
        pubKey += "0000000000000000";
        pubKey = pubKey.substring(0, 16);
        String tempKey = diverse(priKey, pubKey);

        StringBuilder stringBuilder = new StringBuilder(hex(content.getBytes()));
        stringBuilder.append("80");
        while (stringBuilder.length() % 16 != 0) {
            stringBuilder.append("0");
        }
        return encryptStr(tempKey, stringBuilder.toString());
    }

    /**
     * 进行解密
     *
     * @param pubKey
     * @param content
     * @return
     */
    public String decrypt(String pubKey, String content) {
        pubKey += "0000000000000000";
        pubKey = pubKey.substring(0, 16);
        String tempKey = diverse(priKey, pubKey);
        try {
            String result = deEncryptStr(tempKey, content);
            if (result.length() % 16 == 0) {
                return new String(unhexba(result.substring(0, result.lastIndexOf("80"))));
            } else {
                return null;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private String diverse(String dkey, String random) {
        byte[] rand = unhexba(random);
        byte[] key = unhexba(dkey);
        byte[] tleft = do3des(rand, key);
        byte[] tright = do3des(not(rand), key);
        byte[] tkey = bajoin(tleft, tright);
        return hex(tkey);
    }


    private String hex(byte[] dst) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dst.length; i++) {
            sb.append(BYTE_HEX_TABLE[dst[i] & 0xff]);
        }
        return sb.toString();
    }

    private byte h2b(byte c) {
        if (c <= '9' && c >= '0') {
            return (byte) (c - '0');
        } else if (c >= 'a' && c <= 'z') {
            return (byte) (c - 'a' + 10);
        } else {
            return (byte) (c - 'A' + 10);
        }
    }

    private byte[] unhexba(String str) {
        byte[] origBe = str.getBytes();
        byte[] newBe = new byte[str.length() / 2];

        for (int i = 0; i < str.length(); i += 2) {
            newBe[i / 2] = (byte) (h2b(origBe[i]) * 0x10 + h2b(origBe[i + 1]));
        }
        return newBe;
    }

    private byte[] bajoin(byte[] left, byte[] right) {
        return unhexba(hex(left) + hex(right));
    }

    private static byte[] not(byte[] be) {
        byte[] x = new byte[be.length];
        for (int i = 0; i < be.length; i++) {
            x[i] = (byte) ~be[i];
        }
        return x;
    }

    private byte[] do3des(byte[] data, byte[] key) {
        String str = hex(key);
        String b = str + str.substring(0, 16);
        byte[] key1 = unhexba(b);
        try {
            Cipher cipher = ENCRYPT.get(key1);
            if (cipher == null) {
                cipher = getCipher();
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key1, "DESEDE"));
                ENCRYPT.put(key1, cipher);
            }
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] do3desun(byte[] data, byte[] key) {
        String a = hex(key);
        String b = a + a.substring(0, 16);
        byte[] key1 = unhexba(b);
        try {
            Cipher cipher = DECRYPT.get(key1);
            if (cipher == null) {
                cipher = getCipher();
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key1, "DESEDE"));
                DECRYPT.put(key1, cipher);
            }
            return cipher.doFinal(data);
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    private String encryptStr(String key, String data) {
        return hex(Objects.requireNonNull(do3des(unhexba(data), unhexba(key))));
    }

    private String deEncryptStr(String key, String data) {
        return hex(Objects.requireNonNull(do3desun(unhexba(data), unhexba(key))));
    }
}
