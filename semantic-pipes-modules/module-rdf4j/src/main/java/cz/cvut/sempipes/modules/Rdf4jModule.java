package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
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

public class Rdf4jModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(Rdf4jModule.class);

    private static String TYPE_URI = KBSS_MODULE.getURI()+"deploy";
    private static String PROPERTY_PREFIX_URI = KBSS_MODULE.getURI()+"rdf4j";

    private static Property getParameter(final String name) {
        return ResourceFactory.createProperty(PROPERTY_PREFIX_URI + "/" + name);
    }

    /**
     * URL of the Rdf4j server
     */
    static final Property P_RDF4J_SERVER_URL = getParameter("p-rdf4j-server-url");
    private String rdf4jServerURL;

    /**
     * Rdf4j repository ID
     */
    static final Property P_RDF4J_REPOSITORY_NAME = getParameter("p-rdf4j-repository-name");
    private String rdf4jRepositoryName;

    /**
     * IRI of the context that should be used for deployment
     */
    static final Property P_RDF4J_CONTEXT_IRI = getParameter("p-rdf4j-context-iri");
    private String rdf4jContextIRI;

    /**
     * Whether the context should be replaced (true) or just enriched (false).
     */
    static final Property P_IS_REPLACE_CONTEXT_IRI = getParameter("p-is-replace");
    private boolean isReplaceContext;

    public String getRdf4jServerURL() {
        return rdf4jServerURL;
    }

    public void setRdf4jServerURL(String rdf4jServerURL) {
        this.rdf4jServerURL = rdf4jServerURL;
    }

    public String getRdf4jRepositoryName() {
        return rdf4jRepositoryName;
    }

    public void setRdf4jRepositoryName(String rdf4jRepositoryName) {
        this.rdf4jRepositoryName = rdf4jRepositoryName;
    }

    public String getRdf4jContextIRI() {
        return rdf4jContextIRI;
    }

    public void setRdf4jContextIRI(String rdf4jContextIRI) {
        this.rdf4jContextIRI = rdf4jContextIRI;
    }

    public boolean isReplaceContext() {
        return isReplaceContext;
    }

    public void setReplaceContext(boolean replaceContext) {
        isReplaceContext = replaceContext;
    }

    @Override
    ExecutionContext executeSelf() {
        // TODO use org.openrdf.repository.manager.RepositoryProvider.getRepository()
        final Repository repository = new HTTPRepository(rdf4jServerURL, rdf4jRepositoryName);
        RepositoryConnection connection = null;
        LOG.debug("Deploying data into context {} of rdf4j server repository {}/{}.", rdf4jContextIRI, rdf4jServerURL, rdf4jRepositoryName);

        try {
            repository.initialize();
            connection = repository.getConnection();

            final Resource rdf4jContextIRIResource = (rdf4jContextIRI == null || rdf4jContextIRI.isEmpty()) ? null : connection.getValueFactory().createURI(rdf4jContextIRI);

            connection.begin();
            if (isReplaceContext) {
                connection.clear( rdf4jContextIRIResource );
            }

            StringWriter w = new StringWriter();
            executionContext.getDefaultModel().write(w, RDFLanguages.NTRIPLES.getName());

            connection.add(new StringReader(w.getBuffer().toString()), "", RDFFormat.N3, rdf4jContextIRIResource);
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
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {
        rdf4jServerURL = getEffectiveValue(P_RDF4J_SERVER_URL).asLiteral().getString();
        rdf4jRepositoryName = getEffectiveValue(P_RDF4J_REPOSITORY_NAME).asLiteral().getString();
        rdf4jContextIRI = getEffectiveValue(P_RDF4J_CONTEXT_IRI).asLiteral().getString();
        isReplaceContext = this.getPropertyValue(P_IS_REPLACE_CONTEXT_IRI, false);
    }
}
