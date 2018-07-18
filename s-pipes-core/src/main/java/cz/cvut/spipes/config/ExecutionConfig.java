package cz.cvut.spipes.config;

import cz.cvut.spipes.util.CoreConfigProperies;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Created by Miroslav Blasko on 6.3.17.
 */
public class ExecutionConfig {

    public static Path getTempDirectoryPath() {
        return Paths.get(CoreConfigProperies.get("execution.tempDirectoryPath", System.getProperty("java.io.tmpdir")));
    }

    public static boolean isExitOnError() {
        return Boolean.parseBoolean(CoreConfigProperies.get(
                "execution.exitOnError",
                "false"));
    }

    public static boolean isCheckValidationConstrains() {
        return Boolean.parseBoolean(CoreConfigProperies.get(
                "execution.checkValidationConstraints",
                "true"));
    }
}
