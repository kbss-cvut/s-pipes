package cz.cvut.spipes.config;

import cz.cvut.spipes.util.CoreConfigProperies;

public class CompatibilityConfig {

    public static boolean isLoadSparqlMotionFiles() {
        return Boolean.parseBoolean(CoreConfigProperies.get("compatibility.loadSparqlMotionFiles", "false"));
    }
}
