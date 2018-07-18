package cz.cvut.sempipes.config;

import cz.cvut.sempipes.util.CoreConfigProperies;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Miroslav Blasko on 14.1.17.
 */
public class AuditConfig {

    public static Path getResourcesPath() {
        return Paths.get(CoreConfigProperies.get("audit.resourcesPath"));
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(CoreConfigProperies.get("audit.enable"));
    }
}
