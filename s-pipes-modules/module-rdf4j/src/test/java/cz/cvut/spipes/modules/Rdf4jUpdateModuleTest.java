package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

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

    private static final String query = "DELETE {\n" +
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
        given(updateRepository.getConnection()).willReturn(updateConnection);
        given(updateConnection.size()).willReturn(1L);
        given(updateConnection.prepareUpdate(any(QueryLanguage.class), anyString())).willReturn(prepareUpdate);

        final ExecutionContext inputExecutionContext = ExecutionContextFactory.createEmptyContext();
        final Rdf4jUpdateModule moduleRdf4j = new Rdf4jUpdateModule();
        moduleRdf4j.setInputContext(inputExecutionContext);
        moduleRdf4j.setUpdateRepository(updateRepository);
        moduleRdf4j.setUpdateQueries(Collections.singletonList(query));
        moduleRdf4j.setIterationCount(5);

        moduleRdf4j.executeSelf();

        verify(updateConnection,times(5)).prepareUpdate(any(QueryLanguage.class), anyString());
    }
}