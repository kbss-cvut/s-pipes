package cz.cvut.sempipes.config;

import cz.cvut.sempipes.util.CoreConfigProperies;

/**
 * Created by Miroslav Blasko on 6.3.17.
 */
public class ExecutionConfig {

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
