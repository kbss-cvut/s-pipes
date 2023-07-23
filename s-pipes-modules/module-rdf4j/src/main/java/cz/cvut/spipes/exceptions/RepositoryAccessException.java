package cz.cvut.spipes.exceptions;

public class RepositoryAccessException extends RuntimeException {

    public RepositoryAccessException(String repositoryName, Throwable cause) {
        super("Cannot connect to repository " + repositoryName + ".", cause);
    }
}