package cz.cvut.spipes.config;

import cz.cvut.spipes.util.CoreConfigProperties;

public class CompatibilityConfig {

    public static boolean isLoadSparqlMotionFiles() {
        return Boolean.parseBoolean(CoreConfigProperties.get("compatibility.loadSparqlMotionFiles", "false"));
    }
}
