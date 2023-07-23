package cz.cvut.spipes.exceptions;

import cz.cvut.spipes.exception.SPipesException;

public class RepositoryAccessException extends SPipesException {

    public RepositoryAccessException(String repositoryName, Throwable cause) {
        super("Cannot connect to repository " + repositoryName + ".", cause);
    }
}