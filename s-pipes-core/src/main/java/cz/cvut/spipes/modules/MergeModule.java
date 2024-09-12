package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;

public class MergeModule extends AnnotatedAbstractModule  {

    @Override
    public ExecutionContext executeSelf() {
        return ExecutionContextFactory.createContext(executionContext.getDefaultModel());
    }

    @Override
    public String getTypeURI() {
        return SML.Merge;
    }

}
