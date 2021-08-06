package com.instana.depends;

import java.io.Writer;
import java.util.Map;

public interface Writeable {
    String getOutputFilename();
    void render(Writer w, Map<String, Object> values);
}
