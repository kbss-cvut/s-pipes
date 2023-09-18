package cz.cvut.spipes.modules.exception;

import cz.cvut.spipes.exception.SPipesException;

public class ValueNotFoundException extends SPipesException {
    public ValueNotFoundException(String message) {
        super(message);
    }
}