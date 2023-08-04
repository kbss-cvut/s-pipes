package cz.cvut.spipes.exception;

/**
 * Exception thrown when there is no context defined to search a resource.
 **/
public class ContextsNotDefinedException extends SPipesException {

    public ContextsNotDefinedException(String message, Throwable cause) {
        super(message, cause);
    }
}