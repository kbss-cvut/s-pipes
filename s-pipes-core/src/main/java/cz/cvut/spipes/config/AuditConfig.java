package cz.cvut.spipes.config;

import cz.cvut.spipes.util.CoreConfigProperies;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AuditConfig {

    public static Path getResourcesPath() {
        return Paths.get(CoreConfigProperies.get("audit.resourcesPath"));
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(CoreConfigProperies.get("audit.enable"));
    }
}
