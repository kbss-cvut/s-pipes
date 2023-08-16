package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringReader;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class Rdf4jDeployModuleTest {

    @Mock
    RepositoryManager repositoryManager;
    @Mock
    Repository repository;
    @Mock
    RepositoryConnection connection;

    @Test
    void executeSelfWithNoRdf4jContextDeploysToDefaultContext() throws IOException {
        given(repositoryManager.getRepository(any())).willReturn(repository);
        given(repository.getConnection()).willReturn(connection);

        final ExecutionContext inputExecutionContext = ExecutionContextFactory.createEmptyContext();
        final Rdf4jDeployModule moduleRdf4j = new Rdf4jDeployModule();
        moduleRdf4j.setInputContext(inputExecutionContext);
        moduleRdf4j.setRepositoryManager(repositoryManager);

        moduleRdf4j.executeSelf();

        verify(repositoryManager,times(0)).getRepository(anyString());
        verify(connection,times(1)).begin();
        verify(connection,times(1)).commit();
        verify(connection).add(any(StringReader.class),eq(""),eq(RDFFormat.N3),eq(null));
    }

    @Mock
    ValueFactory valueFactory;

    @Test
    void executeSelfWithRdf4jContextDeploysToContext() throws IOException {
        given(repositoryManager.getRepository(any())).willReturn(repository);
        given(repository.getConnection()).willReturn(connection);

        final ExecutionContext inputExecutionContext = ExecutionContextFactory.createEmptyContext();
        final Rdf4jDeployModule moduleRdf4j = new Rdf4jDeployModule();
        moduleRdf4j.setInputContext(inputExecutionContext);
        moduleRdf4j.setRepositoryManager(repositoryManager);
        String rdf4jContext = "http://example.org";
        given(connection.getValueFactory()).willReturn(valueFactory);
        given(connection.getValueFactory().createIRI(rdf4jContext)).willReturn(SimpleValueFactory.getInstance().createIRI(rdf4jContext));
        moduleRdf4j.setRdf4jContextIRI(rdf4jContext);

        moduleRdf4j.executeSelf();

        verify(repositoryManager,times(0)).getRepository(anyString());
        verify(connection,times(1)).begin();
        verify(connection,times(1)).commit();
        verify(connection).add(
                any(StringReader.class),
                eq(""),
                eq(RDFFormat.N3),
                eq(SimpleValueFactory.getInstance().createIRI(rdf4jContext))
        );
    }
}