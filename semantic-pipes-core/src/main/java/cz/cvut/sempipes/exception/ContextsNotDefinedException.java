package cz.cvut.sempipes.exception;

/**
 * Exception thrown when there is no context defined to search a resource.
 *
 * Created by Miroslav Blasko on 21.7.16.
 */

import java.util.Set;

/**
 *
 */
public class ContextsNotDefinedException extends RuntimeException {

    public ContextsNotDefinedException(String message, Throwable cause) {
        super(message, cause);
    }
}