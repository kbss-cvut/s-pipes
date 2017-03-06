package cz.cvut.sempipes.exception;

import cz.cvut.sempipes.modules.Module;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Created by Miroslav Blasko on 6.3.17.
 */
public class ValidationConstraintFailed extends RuntimeException {

    public ValidationConstraintFailed(@NonNls String message, @NotNull Module module) {
        super(createModuleInfo(module) + " " +  message);
    }

    private static String createModuleInfo(@NotNull  Module module) {
        return Optional.ofNullable(module.getResource())
                .map(r -> String.format("Execution of module %s failed. ", r.toString()))
                .orElse("Execution of a module with type %s failed." + module.getTypeURI());
    }
}
