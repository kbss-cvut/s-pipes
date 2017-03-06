package cz.cvut.sempipes.config;

import cz.cvut.sempipes.util.CoreConfigProperies;

/**
 * Created by Miroslav Blasko on 6.3.17.
 */
public class DebugConfig {

    public static boolean isExitOnError() {
        return Boolean.parseBoolean(CoreConfigProperies.get("debug.exitOnError"));
    }
}
