package com.instana.depends;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class SecretKeyValues extends Dependency implements Encryptable, Readable, Valueable {
    private final String inputFilename;
    private final Map<String, Object> m = new HashMap<>();

    public SecretKeyValues(String name, String inputFilename) {
        super(name);
        this.inputFilename = inputFilename;
    }

    @Override
    public String getInputFilename() {
        return inputFilename;
    }

    @Override
    public void decrypt(String key) throws GeneralSecurityException {
        try {
            var raw = Base64.getDecoder().decode(getContent());
            var bytes = AesCrypt.decrypt(key.toCharArray(), raw);
            var p = new Properties();
            p.load(new InputStreamReader(new ByteArrayInputStream(bytes)));
            var set = p.entrySet();
            for (Map.Entry<Object, Object> e : set) {
                if (e.getKey() instanceof String) {
                    m.put((String)e.getKey(), e.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Object> getValues() {
        return m;
    }
}
