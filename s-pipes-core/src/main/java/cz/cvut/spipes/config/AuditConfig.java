package cz.cvut.spipes.config;

import cz.cvut.spipes.util.CoreConfigProperties;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AuditConfig {

    public static Path getResourcesPath() {
        return Paths.get(CoreConfigProperties.get("audit.resourcesPath"));
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(CoreConfigProperties.get("audit.enable"));
    }
}
