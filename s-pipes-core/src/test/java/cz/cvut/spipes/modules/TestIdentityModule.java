package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;

public class TestIdentityModule extends AnnotatedAbstractModule {

    @Override ExecutionContext executeSelf() {
        return executionContext;
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.getURI() + "test-identity";
    }

    @Override
    public void loadConfiguration() {
    }
}
