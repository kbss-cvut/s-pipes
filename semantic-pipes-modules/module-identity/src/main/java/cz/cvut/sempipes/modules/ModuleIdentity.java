package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.PipelineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleIdentity extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleIdentity.class);

    static {
        LOG.info("Registering {} -> {}", KBSS_MODULE.identity, ModuleIdentity.class);
        PipelineFactory.registerModuleType(KBSS_MODULE.identity, ModuleIdentity.class);
    }

    @Override
    ExecutionContext executeSelf() {
        return executionContext;
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.identity.getURI();
    }

    @Override
    public void loadConfiguration() {
    }
}
