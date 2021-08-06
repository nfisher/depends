package com.instana.depends;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertyKeyValues extends Dependency implements Readable, Valueable {
    private final String inputFilename;

    private final Map<String, Object> m = new HashMap<>();

    public PropertyKeyValues(String name, String inputFilename) {
        super(name);
        this.inputFilename = inputFilename;
    }

    @Override
    public String getInputFilename() {
        return inputFilename;
    }

    @Override
    public void setContent(byte[] content) {
        Reader r = new InputStreamReader(new ByteArrayInputStream(content));
        try {
            final Properties properties = new Properties();
            properties.load(r);
            Set<Map.Entry<Object, Object>> set = properties.entrySet();
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
