package cz.cvut.sempipes.config;

import cz.cvut.sempipes.util.CoreConfigProperies;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Miroslav Blasko on 17.1.17.
 */
public class ContextLoaderConfig {

    public static List<Path> getScriptPaths() {
        return Arrays
                .stream(CoreConfigProperies.get("contexts.scriptPaths").split(";"))
                .map(path -> Paths.get(path))
                .collect(Collectors.toList());
    }
}
