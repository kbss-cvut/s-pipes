package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleIdentity extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleIdentity.class);


    @Override
    ExecutionContext executeSelf() {
        return executionContext;
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.getURI()+"identity";
    }

    @Override
    public void loadConfiguration() {
    }
}
