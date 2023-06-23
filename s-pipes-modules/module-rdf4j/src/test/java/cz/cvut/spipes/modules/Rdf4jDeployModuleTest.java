package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

//import info.aduna.webapp.util.HttpServerUtil;

public class Rdf4jDeployModuleTest {

    @Test
    @Disabled
    public void testDeployEmpty() throws Exception {
        final Rdf4jDeployModule moduleRdf4j = new Rdf4jDeployModule();

        final Model deployModel = ModelFactory.createDefaultModel();
        final Property resource = ResourceFactory.createProperty("http://a");
        deployModel.add(resource, resource, resource);

        final ExecutionContext executionContext = ExecutionContextFactory.createContext(deployModel);

        final Model model = ModelFactory.createDefaultModel();
        final Resource root = model.createResource();
        model.add(root, Rdf4jDeployModule.P_IS_REPLACE_CONTEXT_IRI, model.createTypedLiteral(true));
        model.add(root, Rdf4jDeployModule.P_RDF4J_SERVER_URL, "http://localhost:18080/rdf4j-server");
        model.add(root, Rdf4jDeployModule.P_RDF4J_REPOSITORY_NAME, "test-s-pipes");
        model.add(root, Rdf4jDeployModule.P_RDF4J_CONTEXT_IRI, "");

        moduleRdf4j.setConfigurationResource(root);

        // TODO: currently running server is needed;
        moduleRdf4j.setInputContext(executionContext);
        moduleRdf4j.execute();
    }
}