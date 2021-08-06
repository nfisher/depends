package com.instana.depends;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

public class Template extends Dependency implements Readable, Writeable {
    private final String inputFilename;
    private final String outputFilename;
    private Mustache tpl;

    public Template(String name, String inputFilename, String outputFilename) {
        super(name);
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;
    }

    @Override
    public void setContent(byte[] content) {
        MustacheFactory mf = new DefaultMustacheFactory();
        Reader r = new InputStreamReader(new ByteArrayInputStream(content));
        tpl = mf.compile(r, inputFilename);
    }

    @Override
    public String getInputFilename() {
        return inputFilename;
    }

    public void render(Writer w, Map<String, Object> values) {
        tpl.execute(w, values);
    }

    @Override
    public String getOutputFilename() {
        return outputFilename;
    }
}
