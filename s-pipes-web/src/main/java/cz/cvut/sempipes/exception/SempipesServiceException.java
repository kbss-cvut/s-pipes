package cz.cvut.sempipes.exception;

public class SempipesServiceException extends RuntimeException {
    public SempipesServiceException(String message) {
        super(message);
    }
    public SempipesServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
