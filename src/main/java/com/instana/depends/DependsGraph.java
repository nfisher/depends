package com.instana.depends;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Consumer;

public class DependsGraph {
    final DirectedAcyclicGraph<String, DefaultEdge> dag = new DirectedAcyclicGraph<>(DefaultEdge.class);
    final Map<String, Dependency> entries = new HashMap<>();

    public void template(String name, String filename, String output, String ...dependencies) {
        Template template = new Template(name, filename, output);
        put(template, dependencies);
    }

    public void props(String name, String filename) {
        PropertyKeyValues properties = new PropertyKeyValues(name, filename);
        put(properties);
    }

    public void secrets(String name, String filename) {
        SecretKeyValues secrets = new SecretKeyValues(name, filename);
        put(secrets);
    }

    public DirectedAcyclicGraph<String, DefaultEdge> analyse() throws IncompleteGraphException {
        Map<String, Set<String>> missing = new HashMap<>();
        for (String entry : entries.keySet()) {
            Set<String> descendants = dag.getAncestors(entry);
            Set<String> missingValues = new HashSet<>();
            for (String descendant : descendants) {
                if (!entries.containsKey(descendant)) {
                    missingValues.add(descendant);
                }
            }
            if (!missingValues.isEmpty()) {
                missing.put(entry, missingValues);
            }
        }

        if (!missing.isEmpty()) {
            throw new IncompleteGraphException(missing);
        }

        return dag;
    }

    public void put(Dependency target, String ...dependencies) {
        dag.addVertex(target.getName());
        for (String dependency : dependencies) {
            dag.addVertex(dependency);
            dag.addEdge(dependency, target.getName());
        }
        entries.put(target.getName(), target);
    }

    public Set<Dependency> dependencies(Dependency target) {
        Set<String> names = dag.getAncestors(target.getName());
        Set<Dependency> deps = new HashSet<>();
        for (String n : names) {
            deps.add(entries.get(n));
        }
        return deps;
    }

    public static DependsGraph nowThatDepends() {
        return new DependsGraph();
    }

    public void apply(Consumer<Dependency> c) {
        TopologicalOrderIterator<String, DefaultEdge> it = new TopologicalOrderIterator<>(dag);
        while (it.hasNext()) {
            String name = it.next();
            Dependency dep = entries.get(name);
            c.accept(dep);
        }
    }

    public boolean execute(String[] args) {
        // https://argparse4j.github.io/examples.html
        try {
            if (args.length < 4) {
                System.err.println("Too few arguments provided:\napp [cmd] [tpl] [in] [out]");
                return false;
            }
            final String cmd = args[0];
            final String tpl = args[1];
            final String in = args[2];
            final String out = args[3];
            final String secret = (args.length > 4) ? args[4] : "";

            System.out.println("Working: " + Paths.get(".").toAbsolutePath());
            System.out.println("Templates: " + tpl);
            System.out.println("Inputs: " + in);
            System.out.println("Outputs: " + out);

            execute(cmd, tpl, in, out, secret);
        } catch(IncompleteGraphException ige) {
            System.err.println("missing relationship for "+ ige.getMissing());
            ige.printStackTrace();
            return false;
        } catch(RuntimeException rex) {
            System.err.println("runtime error "+ rex.getMessage());
            rex.printStackTrace();
            return false;
        }

        return true;
    }

    public void execute(String cmd, String tpl, String in, String out, String secret) throws IncompleteGraphException {
        analyse();

        switch (cmd) {
            case "apply":
                apply(tpl, in, out, secret);
                return;

            default:
                throw new RuntimeException("Unknown command: " + cmd);
        }
    }

    void apply(String tpl, String in, String out, String secret) {
        List<Exception> exceptionsList = new ArrayList<>();
        apply((Dependency dep) -> {
            try {
                MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
                // load content
                if (dep instanceof Readable) {
                    String base = (dep instanceof Template) ? tpl : in;
                    Path p = Paths.get(base, ((Readable) dep).getInputFilename());
                    byte[] contents = Files.readAllBytes(p);
                    byte[] digest = shaDigest.digest(contents);
                    dep.setContent(contents);
                    dep.setDigest(digest);
                }

                if (!secret.isEmpty() && dep instanceof Encryptable) {
                    ((Encryptable) dep).decrypt(secret);
                }

                // write content
                if (dep instanceof Writeable) {
                    Set<Dependency> dependencies = dependencies(dep);
                    Writeable writeable = (Writeable) dep;
                    Map<String, Object> merged = new HashMap<>();
                    for (Dependency d : dependencies) {
                        if (d instanceof Valueable) {
                            merged.putAll(((Valueable)d).getValues());
                        }
                    }
                    File f = new File(out, writeable.getOutputFilename());
                    f.getParentFile().mkdirs();
                    Writer w = new FileWriter(f);
                    writeable.render(w, merged);
                    w.close();
                }
            } catch (IOException | GeneralSecurityException e) {
                exceptionsList.add(e);
            }
        });

        if (!exceptionsList.isEmpty()) {
            throw new RuntimeException("exceptions received during apply") {
              final List<Exception> exceptions = exceptionsList;
              @Override
              public String getMessage() {
                  StringBuffer sb = new StringBuffer();
                  for (Exception ex : exceptions) {
                      sb.append(ex.getMessage());
                      sb.append(": ");
                      sb.append("\n");
                      ex.printStackTrace();
                  }
                  return sb.toString();
              }
            };
        }
    }
}
