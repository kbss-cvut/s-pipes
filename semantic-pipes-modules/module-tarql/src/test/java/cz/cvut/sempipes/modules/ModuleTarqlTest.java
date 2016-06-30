package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.*;
import org.junit.Ignore;

public class ModuleTarqlTest {

    @Ignore
    @org.junit.Test
    public void testDeployEmpty() throws Exception {
        final ModuleTarql module = new ModuleTarql();

        final Model deployModel = ModelFactory.createDefaultModel();
        final Property resource = ResourceFactory.createProperty("http://a");
        deployModel.add(resource, resource, resource);

        final ExecutionContext executionContext = ExecutionContextFactory.createContext(deployModel);

        final Model model = ModelFactory.createDefaultModel();
        final Resource root = model.createResource();
        model.add(root, ModuleTarql.P_IS_REPLACE_CONTEXT_IRI, model.createTypedLiteral(true));
        model.add(root, ModuleTarql.P_SESAME_SERVER_URL, "http://localhost:18080/openrdf-sesame");
        model.add(root, ModuleTarql.P_SESAME_REPOSITORY_NAME, "test-semantic-pipes");
        model.add(root, ModuleTarql.P_SESAME_CONTEXT_IRI, "");

//        moduleSesame.setResource(root);
//        moduleSesame.loadConfiguration(null);

        // TODO: currently running server is needed;
        //module.execute(executionContext);
    }
}