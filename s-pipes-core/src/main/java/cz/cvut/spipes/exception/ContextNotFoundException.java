package cz.cvut.spipes.exception;

public class ContextNotFoundException  extends SPipesException {

    public ContextNotFoundException(String contextId) {
        super("Context identified by \"" + contextId + "\" was not found.");
    }

}
