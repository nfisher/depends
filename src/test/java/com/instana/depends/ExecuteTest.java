package com.instana.depends;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;

import static com.instana.depends.Depends.*;
import static com.instana.depends.DependsGraph.nowThatDepends;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.io.FileMatchers.aFileWithSize;
import static org.hamcrest.io.FileMatchers.anExistingFile;

public class ExecuteTest {

    static final String TEMPLATE_BASE = "src/test/resources";
    static final String INPUT_BASE = "src/test/resources/env";
    static final String OUTPUT_BASE = "target/test-output/execute";

    @BeforeClass
    public static void setupClass() throws IOException {
        try {
            Files.walk(Path.of(OUTPUT_BASE))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (NoSuchFileException e) { /* ignore no such file exceptions */ }
    }

    @Test
    public void should_write_template_with_secret_and_props() throws IncompleteGraphException {
        var output = "filler-all.yaml";

        secrets("secret", "filler.secret");
        props("props", "filler.properties");
        template("filler", "filler-all.yaml.mustache", output, "secret", "props");
        execute(new String[]{"apply", TEMPLATE_BASE, INPUT_BASE, OUTPUT_BASE, "secret123"});

        assertThat(output + " file should be created", outFile(output), anExistingFile());
        assertThat(output + " file should have size", outFile(output), aFileWithSize(61));
    }

    @Test
    public void should_write_template_without_inputs() throws IncompleteGraphException {
        var output = "filler-no-inputs.yaml";
        var depends = nowThatDepends();
        depends.template("filler", "filler.yaml.mustache", output);
        depends.execute("apply", TEMPLATE_BASE, INPUT_BASE, OUTPUT_BASE, "");

        assertThat(output + " file should be created", outFile(output), anExistingFile());
        assertThat(output + " file should have size", outFile(output), aFileWithSize(43));
    }

    @Test
    public void should_write_template_with_prop_input() throws IncompleteGraphException {
        var output = "filler-prop-input.yaml";
        var depends = nowThatDepends();
        depends.props("props", "filler.properties");
        depends.template("filler", "filler-prop.yaml.mustache", output, "props");
        depends.execute("apply", TEMPLATE_BASE, INPUT_BASE, OUTPUT_BASE, "");

        assertThat(output + " file should be created", outFile(output), anExistingFile());
        assertThat(output + " file should have size", outFile(output), aFileWithSize(43));
    }

    @Test(expected = RuntimeException.class)
    public void should_fail_when_prop_is_missing() throws IncompleteGraphException {
        var output = "filler-prop-input.yaml";
        var depends = nowThatDepends();
        depends.template("filler", "filler-prop.yaml.mustache", output);
        depends.execute("apply", TEMPLATE_BASE, INPUT_BASE, OUTPUT_BASE, "");
    }

    @Test
    public void should_write_template_with_secret_input() {
        var output = "filler-secret-input.yaml";
        var depends = nowThatDepends();
        depends.secrets("secret", "filler.secret");
        depends.template("filler", "filler-secret.yaml.mustache", output, "secret");
        depends.execute(new String[]{"apply", TEMPLATE_BASE, INPUT_BASE, OUTPUT_BASE, "secret123"});

        assertThat(output + " file should be created", outFile(output), anExistingFile());
        assertThat(output + " file should have size", outFile(output), aFileWithSize(61));
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_runtime_exception_when_template_not_found() throws IncompleteGraphException {
        var output = "filler-prop-input.yaml";
        var depends = nowThatDepends();
        depends.props("props", "non-existent.properties");
        depends.template("filler", "filler-non-existent.yaml.mustache", output, "props");
        depends.execute("apply", TEMPLATE_BASE, INPUT_BASE, OUTPUT_BASE, "");
    }

    @Test
    public void should_fail_if_too_few_arguments_are_provided() {
        var depends = nowThatDepends();
        assertThat(depends.execute(new String[]{"apply", "boop"}), is(false));
    }

    File outFile(String filename) {
        return new File(OUTPUT_BASE, filename);
    }
}
