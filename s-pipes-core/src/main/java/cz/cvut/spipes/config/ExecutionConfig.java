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
                "execution.validation.checkConstraints",
                "true"));
    }

    public static int getEvidenceNumber() {
        return Integer.parseInt(CoreConfigProperies.get("execution.validation.maxNumberOfConstraintFailureEvidences", "3"));
    }

    public static String getConfigUrl() {
        return CoreConfigProperies.get("execution.configUrl", "config.ttl");
    }

    public static Environment getEnvironment() {
        return  Environment.valueOf(CoreConfigProperies.get(
            "execution.environment",
            Environment.production.toString())
        );
    }

    public static String getDevelopmentServiceUrl() {
        return CoreConfigProperies.get("execution.developmentServiceUrl", "http://localhost:8080/s-pipes/");
    }
}
