package cz.cvut.spipes.exception;

/**
 * Runtime exception that should be extended by any specific SPipes exception.
 */
public class SPipesException extends RuntimeException {

    public SPipesException(String message) {
        super(message);
    }

    public SPipesException(String message, Throwable cause) {
        super(message, cause);
    }

    public SPipesException(Throwable cause) {
        super(cause);
    }

    public SPipesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
