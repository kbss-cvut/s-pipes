package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;

public class ModuleIdentity extends AbstractModule {

    private static final String BASE_IRI = "http://onto.fel.cvut.cz/ontologies/lib/module/";
    private static final String MODULE_IRI = BASE_IRI + "identity";

    @Override
    ExecutionContext executeSelf() {
        return executionContext;
    }

    @Override
    public void loadConfiguration() {
    }
}
