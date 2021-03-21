package cz.cvut.spipes.config;

import cz.cvut.spipes.util.CoreConfigProperies;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static String getConfigUrl() {
        return CoreConfigProperies.get("execution.configuration.url", "config.ttl");
    }
}
