package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.http.HTTPUpdate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Rdf4jUpdateModuleTest {

    @Mock
    Repository updateRepository;
    @Mock
    RepositoryConnection updateConnection;
    @Mock
    Update prepareUpdate;

    private static String query = "DELETE {\n" +
        "\t<http://example.org/people/john>  <http://example.org/people/age> ?oldAge .\n" +
        "}\n" +
        "INSERT {\n" +
        "\t<http://example.org/people/john>  <http://example.org/people/age> ?newAge .\n" +
        "} WHERE {\n" +
        "   OPTIONAL {\n" +
        "      <http://example.org/people/john>  <http://example.org/people/age> ?oldAge .\n" +
        "   }\n" +
        "   BIND(COALESCE(?oldAge+1, 1) as ?newAge)\n" +
        "}";

    @Test
    void executeSelfExecutesAllUpdateQueries() {

        final ExecutionContext inputExecutionContext = ExecutionContextFactory.createEmptyContext();
        Model configModel = ModelFactory.createDefaultModel();
        final Rdf4jUpdateModule moduleRdf4j = new Rdf4jUpdateModule();
        final Resource root = configModel.createResource();
        given(updateRepository.getConnection()).willReturn(updateConnection);
        given(updateConnection.size()).willReturn(1L);
        given(updateConnection.prepareUpdate(any(QueryLanguage.class), anyString())).willReturn(prepareUpdate);
        moduleRdf4j.setUpdateRepository(updateRepository);

        configModel.add(root, Rdf4jUpdateModule.P_RDF4J_SERVER_URL, "http://localhost:8080/rdf4j-server/");
        configModel.add(root, Rdf4jUpdateModule.P_RDF4J_REPOSITORY_NAME, "new-test");
        configModel.add(root, SML.updateQuery, Rdf4jUpdateModule.createUpdateQueryResource(configModel, query));
        configModel.add(root, KBSS_MODULE.has_max_iteration_count, "5");
        configModel.write(System.out, FileUtils.langTurtle, null);
        moduleRdf4j.setConfigurationResource(root);
        moduleRdf4j.setInputContext(inputExecutionContext);
        moduleRdf4j.execute();

        verify(updateConnection,times(5)).prepareUpdate(any(QueryLanguage.class), anyString());
    }
}