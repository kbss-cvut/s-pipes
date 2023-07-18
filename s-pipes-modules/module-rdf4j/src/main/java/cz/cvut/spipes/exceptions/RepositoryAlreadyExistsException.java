package cz.cvut.spipes.exceptions;

public class RepositoryAlreadyExistsException extends RuntimeException {

    public RepositoryAlreadyExistsException(String repositoryName) {
        super("Repository " + repositoryName + " already exists.");
    }

}