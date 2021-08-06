package com.instana.depends;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.junit.Test;

import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class AnalyseTest {

    @Test(expected = IncompleteGraphException.class)
    public void should_throw_exception_when_template_dependency_missing() throws IncompleteGraphException {
        DependsGraph depends = DependsGraph.nowThatDepends();
        depends.template("template", "template.yaml.mustache", "config.yaml", "props");
        depends.analyse();
    }

    @Test
    public void should_succeed_when_template_has_no_dependencies() throws IncompleteGraphException {
        DependsGraph depends = DependsGraph.nowThatDepends();
        depends.template("template", "template.yaml.mustache", "config.yaml");

        DirectedAcyclicGraph<String, DefaultEdge> dag = depends.analyse();
        List<String> li = toOrderedList(dag);
        assertThat(li, contains("template"));
    }

    @Test
    public void should_succeed_when_template_has_dependency_on_prop() throws IncompleteGraphException {
        DependsGraph depends = DependsGraph.nowThatDepends();
        depends.props("prop", "values.properties");
        depends.template("template", "template.yaml.mustache", "config.yaml", "prop");

        DirectedAcyclicGraph<String, DefaultEdge> dag = depends.analyse();
        List<String> li = toOrderedList(dag);

        assertThat(li, contains("prop", "template"));
    }

    @Test
    public void dependencies_should_be_ordered_correctly() throws IncompleteGraphException {
        DependsGraph depends = DependsGraph.nowThatDepends();
        depends.secrets("secret", "secret.properties");
        depends.props("prop", "values.properties");
        depends.template("template", "template.yaml.mustache", "config.yaml", "prop");

        DirectedAcyclicGraph<String, DefaultEdge> dag = depends.analyse();
        List<String> li = toOrderedList(dag);

        assertThat(li, contains("secret", "prop", "template"));
    }

    List<String> toOrderedList(DirectedAcyclicGraph<String, DefaultEdge> dag) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new TopologicalOrderIterator<>(dag), 0), false).collect(Collectors.toList());
    }
}
