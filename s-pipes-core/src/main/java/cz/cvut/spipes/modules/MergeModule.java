package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;

public class MergeModule extends AbstractModule  {

    @Override
    public ExecutionContext executeSelf() {
        return ExecutionContextFactory.createContext(executionContext.getDefaultModel());
    }

    @Override
    public String getTypeURI() {
        return SML.Merge.getURI();
    }

    @Override
    public void loadConfiguration() {
    }
}
