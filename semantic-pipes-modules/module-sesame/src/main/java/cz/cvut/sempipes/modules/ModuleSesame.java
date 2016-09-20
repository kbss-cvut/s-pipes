package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.PipelineFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFLanguages;
import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class ModuleSesame extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleSesame.class);

    private static Property getParameter(final String name) {
        return ResourceFactory.createProperty(KBSS_MODULE.deploy.toString() + "/" + name);
    }

    /**
     * URL of the Sesame server
     */
    static final Property P_SESAME_SERVER_URL = getParameter("p-sesame-server-url");
    private String sesameServerURL;

    /**
     * Sesame repository ID
     */
    static final Property P_SESAME_REPOSITORY_NAME = getParameter("p-sesame-repository-name");
    private String sesameRepositoryName;

    /**
     * IRI of the context that should be used for deployment
     */
    static final Property P_SESAME_CONTEXT_IRI = getParameter("p-sesame-context-iri");
    private String sesameContextIRI;

    /**
     * Whether the context should be replaced (true) or just enriched (false).
     */
    static final Property P_IS_REPLACE_CONTEXT_IRI = getParameter("p-is-replace");
    private boolean isReplaceContext;

    @Override
    ExecutionContext executeSelf() {
        // TODO use org.openrdf.repository.manager.RepositoryProvider.getRepository()
        final Repository repository = new HTTPRepository(sesameServerURL, sesameRepositoryName );
        RepositoryConnection connection = null;

        try {
            repository.initialize();
            connection = repository.getConnection();

            final Resource sesameContextIRIResource = (sesameContextIRI == null || sesameContextIRI.isEmpty()) ? null : connection.getValueFactory().createURI(sesameContextIRI);

            connection.begin();
            if (isReplaceContext) {
                connection.clear( sesameContextIRIResource );
            }

            StringWriter w = new StringWriter();
            executionContext.getDefaultModel().write(w, RDFLanguages.NTRIPLES.getName());

            connection.add(new StringReader(w.getBuffer().toString()), "", RDFFormat.N3, sesameContextIRIResource);
            connection.commit();
        } catch (final RepositoryException | RDFParseException | IOException e) {
            LOG.error(e.getMessage(),e);
        } finally {
            try {
                if (connection != null && connection.isOpen()) {
                    connection.close();
                }
            } catch (RepositoryException e) {
                LOG.error(e.getMessage(), e);
            } finally {
                if (repository.isInitialized()) {
                    try {
                        repository.shutDown();
                    } catch (RepositoryException e) {
                        LOG.error("During finally: ", e);
                    }
                }
            }
        }

        return ExecutionContextFactory.createContext(executionContext.getDefaultModel());
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.deploy.getURI();
    }

    @Override
    public void loadConfiguration() {
        sesameServerURL = this.getStringPropertyValue(P_SESAME_SERVER_URL);
        sesameRepositoryName = this.getStringPropertyValue(P_SESAME_REPOSITORY_NAME);
        sesameContextIRI = this.getStringPropertyValue(P_SESAME_CONTEXT_IRI);
        isReplaceContext = this.getPropertyValue(P_IS_REPLACE_CONTEXT_IRI, false);
    }
}
