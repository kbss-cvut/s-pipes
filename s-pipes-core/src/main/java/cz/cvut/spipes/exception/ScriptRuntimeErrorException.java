package cz.cvut.spipes.exception;

/**
 * Indicate that SPipes engine encountered error during execution of a script.
 */
public class ScriptRuntimeErrorException extends SPipesException {

    public ScriptRuntimeErrorException(String message, Exception exception) {
        super(message, exception);
    }
}