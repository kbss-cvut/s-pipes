package cz.cvut.spipes.modules.exception;

import cz.cvut.spipes.exception.SPipesException;

public class SheetDoesntExistsException extends SPipesException {
    public SheetDoesntExistsException(String message) {
        super(message);
    }
}
