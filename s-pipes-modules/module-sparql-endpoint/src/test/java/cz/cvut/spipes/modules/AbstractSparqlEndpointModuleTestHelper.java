package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.PipelineFactory;
import org.apache.jena.ontology.OntModel;

/**
 * This is helper class to write tests that load configuration of modules from ttl file
 * that is organized in directory `test/resources/module/${moduleName}/`.
 *
 * See more information in {@link AbstractModuleTestHelper}.
 */
public abstract class AbstractSparqlEndpointModuleTestHelper extends AbstractModuleTestHelper {

    @Override
    Module getSingleModule(OntModel configModel) {
        return PipelineFactory.loadPipelines(configModel).get(0);
    }

    public Module getConfigRootModule() {
        return (Module) super.getConfigRootModule();
    }
    public Module getRootModule(String fileName) {
        return (Module) super.getRootModule(fileName);
    }

}
