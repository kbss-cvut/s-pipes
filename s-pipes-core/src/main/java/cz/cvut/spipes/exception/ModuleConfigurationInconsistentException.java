package cz.cvut.spipes.exception;

/**
 * Indicate that SPipes Module was incorrectly configured.
 */
public class ModuleConfigurationInconsistentException extends SPipesException {

    public ModuleConfigurationInconsistentException(String message) {
        super(message);
    }
}