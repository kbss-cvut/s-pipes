package cz.cvut.spipes.modules.exception;

import cz.cvut.spipes.exception.SPipesException;

public class NoMatchException extends SPipesException {
    public NoMatchException(String message) {
        super(message);
    }
}
