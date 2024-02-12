package com.szs.sungsu.util;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class TwoWayEncryptUtil {

    // 키 값은 임의로 할당 하였음
    private final static String KEY = "qwertyuiop123456";
    private final static String ALGORITHM = "AES/CBC/PKCS5Padding";

    public static String encrypt(String input) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, generateKey(), generateIv());
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.encodeBase64String(cipherText);
    }

    public static String decrypt(String cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, generateKey(), generateIv());
        byte[] plainText = cipher.doFinal(Base64.decodeBase64(cipherText));
        return new String(plainText);
    }

    private static SecretKeySpec generateKey() {
        byte[] keyBytes = new byte[16];
        byte[] b = KEY.getBytes(StandardCharsets.UTF_8);
        int len = b.length;
        if (len > keyBytes.length) {
            len = keyBytes.length;
        }
        System.arraycopy(b, 0, keyBytes, 0, len);
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static IvParameterSpec generateIv() {
        String rawIV = KEY.substring(0, 16);
        //byte[] iv = new byte[16];
        //new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(rawIV.getBytes());
    }

}
