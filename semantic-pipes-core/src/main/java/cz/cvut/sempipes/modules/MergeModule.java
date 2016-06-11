package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;

/**
 * Created by Miroslav Blasko on 28.5.16.
 */
public class MergeModule extends AbstractModule  {

    @Override
    public ExecutionContext executeSelf() {
        return ExecutionContextFactory.createContext(executionContext.getDefaultModel());
    }

    @Override
    public void loadConfiguration() {
    }
}
