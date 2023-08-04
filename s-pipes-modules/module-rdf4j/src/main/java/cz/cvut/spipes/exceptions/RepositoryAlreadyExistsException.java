package cz.cvut.spipes.exceptions;

import cz.cvut.spipes.exception.SPipesException;

public class RepositoryAlreadyExistsException extends SPipesException {

    public RepositoryAlreadyExistsException(String repositoryName) {
        super("Repository " + repositoryName + " already exists.");
    }

}