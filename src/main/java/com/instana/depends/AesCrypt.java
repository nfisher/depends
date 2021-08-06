package com.instana.depends;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AesCrypt {
    // TODO: add IV to output bytes.
    private static final byte[] IV = Base64.getDecoder().decode("V6Cu3CPzLzwczb1wA377Pg==");
    private static final byte[] SALT = "She sells, seashells, by the seashore".getBytes(StandardCharsets.UTF_8);

    static byte[] keyExpansion(char[] key) throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(key, SALT, 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(), "AES");
        return secret.getEncoded();
    }

    public static byte[] encrypt(char[] key, byte[] text) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyExpansion(key), "AES"), new IvParameterSpec(IV));
        return cipher.doFinal(text);
    }

    public static byte[] decrypt(char[] key, byte[] enc) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyExpansion(key), "AES"), new IvParameterSpec(IV));
        return cipher.doFinal(enc);
    }
}
