package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class Rdf4jUpdateModuleTest {

    @Test
    @Disabled
    void executeSelf() {
        Rdf4jUpdateModule module = new Rdf4jUpdateModule();
        ExecutionContext newContext = module.executeSelf();
//        System.out.println(newContext.getDefaultModel().listStatements().toList());
    }

    @Test
    @Disabled
    public void testUpdateEmpty() {
        final Rdf4jUpdateModule moduleRdf4j = new Rdf4jUpdateModule();

        final Model updateModel = ModelFactory.createDefaultModel();
        final Property resource = ResourceFactory.createProperty("http://a");
        updateModel.add(resource, resource, resource);

        final ExecutionContext executionContext = ExecutionContextFactory.createContext(updateModel);

        final Model model = ModelFactory.createDefaultModel();
        final Resource root = model.createResource();
        model.add(root, Rdf4jDeployModule.P_RDF4J_SERVER_URL, "http://localhost:8080/rdf4j-server/");
        model.add(root, Rdf4jDeployModule.P_RDF4J_REPOSITORY_NAME, "1");

        moduleRdf4j.setConfigurationResource(root);

        // TODO: currently running server is needed;
        moduleRdf4j.setInputContext(executionContext);
        moduleRdf4j.execute();
    }
}