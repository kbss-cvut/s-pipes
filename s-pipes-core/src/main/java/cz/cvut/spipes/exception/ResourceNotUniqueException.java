package cz.cvut.spipes.exception;


import java.util.Set;

/**
 * Exception thrown when a resource found is not unique.
 **/
public class ResourceNotUniqueException extends RuntimeException {

    public ResourceNotUniqueException(String message) {
        super(message);
    }

    public ResourceNotUniqueException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotUniqueException(String resourceId, Set<String> conflictingResources, String contextUri) {
        super("Resource identified by \"" + resourceId + "\" is not unique in context \"" + contextUri + "\". Duplicates of this resource are " + conflictingResources);
    }
}