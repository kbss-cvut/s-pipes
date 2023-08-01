package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

//import info.aduna.webapp.util.HttpServerUtil;

@ExtendWith(MockitoExtension.class)
public class Rdf4jDeployModuleTest {

    @Mock
    RepositoryManager repositoryManager;
    @Mock
    Repository repository;
    @Mock
    RepositoryConnection connection;

    @Test
    void executeSelfDeployEmpty() {
        given(repository.getConnection()).willReturn(connection);

        final ExecutionContext inputExecutionContext = ExecutionContextFactory.createEmptyContext();
        final Rdf4jDeployModule moduleRdf4j = new Rdf4jDeployModule();
        moduleRdf4j.setInputContext(inputExecutionContext);
        moduleRdf4j.setRdf4jServerURL("http://localhost:18080/rdf4j-server");
        moduleRdf4j.setRdf4jRepositoryName("test-s-pipes");
        moduleRdf4j.setRdf4jContextIRI("");
        moduleRdf4j.setConnection(connection);
        moduleRdf4j.setRepository(repository);
        moduleRdf4j.setRepositoryManager(repositoryManager);


        moduleRdf4j.executeSelf();
    }

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