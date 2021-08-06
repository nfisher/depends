package com.instana.depends;

public class Depends {
    private static final DependsGraph depends = new DependsGraph();

    public static void template(String name, String filename, String output, String... dependencies) {
        depends.template(name, filename, output, dependencies);
    }

    public static void props(String name, String filename) {
        depends.props(name, filename);
    }

    public static void secrets(String name, String filename) {
        depends.secrets(name, filename);
    }

    public static void execute(String[] args) throws IncompleteGraphException {
        depends.execute(args);
    }
}
