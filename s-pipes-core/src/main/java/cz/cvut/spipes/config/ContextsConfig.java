package cz.cvut.spipes.config;

import cz.cvut.spipes.util.CoreConfigProperties;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ContextsConfig {


    public static List<Path> getScriptPaths() {
        return Arrays
                .stream(CoreConfigProperties.get("contexts.scriptPaths").split(";"))
                .map(Paths::get)
                .collect(Collectors.toList());
    }
}
