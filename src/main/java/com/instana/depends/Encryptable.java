package com.instana.depends;

import java.security.GeneralSecurityException;

public interface Encryptable {
    void decrypt(String key) throws GeneralSecurityException;
}
