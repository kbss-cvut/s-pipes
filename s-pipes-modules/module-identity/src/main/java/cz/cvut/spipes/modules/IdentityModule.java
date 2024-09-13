package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SPipesModule(label = "identity", comment = "Implements a no-op.")
public class IdentityModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(IdentityModule.class);


    @Override
    ExecutionContext executeSelf() {
        return executionContext;
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.uri + "identity";
    }

    @Override
    public void loadConfiguration() {
    }
}
