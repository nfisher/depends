package com.instana.depends;

import static com.instana.depends.Depends.execute;
import static com.instana.depends.Depends.template;

public class App {
    public static void main(String[] args) throws IncompleteGraphException {
        template("filler", "filler.yaml.mustache", "filler.yaml");
        execute(args);
    }
}
