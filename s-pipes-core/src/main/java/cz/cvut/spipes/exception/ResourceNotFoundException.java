package cz.cvut.spipes.exception;

import java.util.Set;

/**
 * Exception thrown when a resource is not found.
 */
public class ResourceNotFoundException extends SPipesException {

    public ResourceNotFoundException(String entityId, Set<String> contextUris) {
        super("Resource identified by \"" + entityId + "\" was not found in contexts " + contextUris + ".");
    }

    public ResourceNotFoundException(String entityId, String contextUri) {
        super("Resource identified by \"" + entityId + "\" was not found in context \"" + contextUri + "\".");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }


}