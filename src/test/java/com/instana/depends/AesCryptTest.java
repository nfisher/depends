package com.instana.depends;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import static com.instana.depends.AesCrypt.decrypt;
import static com.instana.depends.AesCrypt.encrypt;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AesCryptTest {

    @Test
    public void should_encrypt_and_decrypt_correctly() throws GeneralSecurityException {
        char[] secret = "secret".toCharArray();
        byte[] dec = decrypt(secret, encrypt(secret, "hello world".getBytes(StandardCharsets.UTF_8)));
        assertThat(new String(dec), is("hello world"));
    }

    @Test
    public void should_encrypt() throws GeneralSecurityException {
        String s = Base64.getEncoder().encodeToString(encrypt("secret123".toCharArray(), "secret=hello".getBytes(StandardCharsets.UTF_8)));
        assertThat(s, is("rwSn9Y8AtnvjEicQuZeAnQ=="));
    }
}