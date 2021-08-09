# Depends

Is intended to be a Java based DSL for creating, analysing, and executiong a dependency graph
of configuration files and other dependencies required for local development.

## Example

```java
package com.instana.depends;

import static com.instana.depends.Depends.*;

/**
 * ${JAVA_MAIN_EXEC} apply dev-setup/src/main/resources/tpl dev-setup/src/main/resources/local dev-setup/configs/local
 */
public class App {
    public static void main(String[] args) throws IncompleteGraphException {
        var output = "filler.yaml";

        secrets("secret", "filler.secret");
        props("props", "filler.properties");
        template("filler", "filler-all.yaml.mustache", output, "secret", "props");
        execute(args);
    }
}
```