package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;

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
