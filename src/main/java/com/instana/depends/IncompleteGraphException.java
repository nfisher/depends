package com.instana.depends;

import java.util.Map;
import java.util.Set;

public class IncompleteGraphException extends Exception {
    private final Map<String, Set<String>> missing;

    public IncompleteGraphException(Map<String, Set<String>> missing) {
        this.missing = missing;
    }

    public Map<String, Set<String>> getMissing() {
        return missing;
    }
}
