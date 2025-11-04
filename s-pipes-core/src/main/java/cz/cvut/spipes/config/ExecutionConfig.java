package cz.cvut.spipes.config;

import cz.cvut.spipes.util.CoreConfigProperties;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExecutionConfig {

    public static Path getTempDirectoryPath() {
        return Paths.get(CoreConfigProperties.get("execution.tempDirectoryPath", System.getProperty("java.io.tmpdir")));
    }

    public static boolean isExitOnError() {
        return Boolean.parseBoolean(CoreConfigProperties.get(
                "execution.exitOnError",
                "false"));
    }

    public static boolean isCheckValidationConstrains() {
        return Boolean.parseBoolean(CoreConfigProperties.get(
                "execution.validation.checkConstraints",
                "true"));
    }

    public static int getEvidenceNumber() {
        return Integer.parseInt(CoreConfigProperties.get("execution.validation.maxNumberOfConstraintFailureEvidences", "3"));
    }

    public static String getConfigUrl() {
        return CoreConfigProperties.get("execution.configUrl", "config.ttl");
    }

    public static Environment getEnvironment() {
        return  Environment.valueOf(CoreConfigProperties.get(
            "execution.environment",
            Environment.production.toString())
        );
    }

    public static String getDevelopmentServiceUrl() {
        return CoreConfigProperties.get("execution.developmentServiceUrl", "http://localhost:8080/s-pipes/");
    }

    public static boolean isInDevelopmentPrettyPrintScripts() {
        return Boolean.parseBoolean(CoreConfigProperties.get(
            "execution.development.prettyPrintScripts",
            "false"));
    }
}
