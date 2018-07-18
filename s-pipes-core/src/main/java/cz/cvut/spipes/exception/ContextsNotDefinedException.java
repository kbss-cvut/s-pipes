package cz.cvut.spipes.exception;

/**
 * Exception thrown when there is no context defined to search a resource.
 *
 * Created by Miroslav Blasko on 21.7.16.
 */

/**
 *
 */
public class ContextsNotDefinedException extends RuntimeException {

    public ContextsNotDefinedException(String message, Throwable cause) {
        super(message, cause);
    }
}