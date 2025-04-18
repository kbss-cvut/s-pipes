package cz.cvut.spipes.config;

import cz.cvut.spipes.util.CoreConfigProperies;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ContextsConfig {


    public static List<Path> getScriptPaths() {
        return Arrays
                .stream(CoreConfigProperies.get("contexts.scriptPaths").split(";"))
                .map(path -> Paths.get(path))
                .collect(Collectors.toList());
    }
}
