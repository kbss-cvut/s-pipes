package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.*;
import org.junit.Ignore;

public class ModuleSesameTest {

    @Ignore
    @org.junit.Test
    public void testDeployEmpty() throws Exception {
        final ModuleSesame moduleSesame = new ModuleSesame();

        final Model deployModel = ModelFactory.createDefaultModel();
        final Property resource = ResourceFactory.createProperty("http://a");
        deployModel.add(resource, resource, resource);

        final ExecutionContext executionContext = ExecutionContextFactory.createContext(deployModel);

        final Model model = ModelFactory.createDefaultModel();
        final Resource root = model.createResource();
        model.add(root,ModuleSesame.P_IS_REPLACE_CONTEXT_IRI, model.createTypedLiteral(true));
        model.add(root,ModuleSesame.P_SESAME_SERVER_URL, "http://localhost:18080/openrdf-sesame");
        model.add(root,ModuleSesame.P_SESAME_REPOSITORY_NAME, "test-semantic-pipes");
        model.add(root,ModuleSesame.P_SESAME_CONTEXT_IRI, "");

        moduleSesame.setConfigurationResource(root);

        // TODO: currently running server is needed;
        moduleSesame.setExecutionContext(executionContext);
        moduleSesame.execute();
    }
}