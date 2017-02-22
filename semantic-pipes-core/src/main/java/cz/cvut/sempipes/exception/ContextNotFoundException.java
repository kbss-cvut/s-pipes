package cz.cvut.sempipes.exception;

import java.util.Set;

/**
 * Created by Miroslav Blasko on 21.2.17.
 */
public class ContextNotFoundException  extends RuntimeException {

    public ContextNotFoundException(String contextId) {
        super("Context identified by \"" + contextId + "\" was not found.");
    }

}
