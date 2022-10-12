package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFLanguages;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

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
        RepositoryConnection connection = null;
        Repository repository = null;
        LOG.debug("Deploying data into {} of rdf4j server repository {}/{}.",
            isRdf4jContextIRIDefined() ? "context " + rdf4jContextIRI : "default context",
            rdf4jServerURL,
            rdf4jRepositoryName);

        VariablesBinding variablesBinding = getExecutionContext().getVariablesBinding();

        String username = Optional
                .ofNullable(variablesBinding.getNode("p-username"))
                .map(RDFNode::toString)
                .orElse(null);

        String password = Optional
                .ofNullable(variablesBinding.getNode("p-password"))
                .map(RDFNode::toString)
                .orElse(null);

        try {
            RepositoryManager repositoryManager = RepositoryProvider.getRepositoryManager(rdf4jServerURL);

            if (username != null && password != null) {
                RemoteRepositoryManager remoteRepositoryManager = (RemoteRepositoryManager) repositoryManager;
                remoteRepositoryManager.setUsernameAndPassword(username, password);
            }

            repository = repositoryManager.getRepository(rdf4jRepositoryName);
            if (repository == null) {
                LOG.info("Creating new repository {} within rdf4j server {} ...",
                    rdf4jServerURL, rdf4jRepositoryName);
                RepositoryConfig repConfig = new RepositoryConfig(rdf4jRepositoryName);
                SailRepositoryConfig config = new SailRepositoryConfig(new NativeStoreConfig());
                repConfig.setRepositoryImplConfig(config);
                repositoryManager.addRepositoryConfig(repConfig);
                repository = repositoryManager.getRepository(rdf4jRepositoryName);
            }
            repository.initialize();
            connection = repository.getConnection();

            final Resource rdf4jContextIRIResource =
                isRdf4jContextIRIDefined() ? connection.getValueFactory().createIRI(rdf4jContextIRI) : null;

            connection.begin();
            if (isReplaceContext) {
                connection.clear( rdf4jContextIRIResource );
            }

            StringWriter w = new StringWriter();
            executionContext.getDefaultModel().write(w, RDFLanguages.NTRIPLES.getName());

            connection.add(new StringReader(w.getBuffer().toString()), "", RDFFormat.N3, rdf4jContextIRIResource);
            connection.commit();
        } catch (final RepositoryException | RDFParseException | RepositoryConfigException | IOException e) {
            LOG.error(e.getMessage(),e);
        } finally {
            try {
                if (connection != null && connection.isOpen()) {
                    connection.close();
                }
            } catch (RepositoryException e) {
                LOG.error(e.getMessage(), e);
            } finally {
                if ((repository != null) && (repository.isInitialized())) {
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
        if (this.getPropertyValue(P_RDF4J_CONTEXT_IRI) != null) {
            rdf4jContextIRI = getEffectiveValue(P_RDF4J_CONTEXT_IRI).asLiteral().getString();
        }
        isReplaceContext = this.getPropertyValue(P_IS_REPLACE_CONTEXT_IRI, false);
    }

    private boolean isRdf4jContextIRIDefined() {
        return (rdf4jContextIRI != null) && (!rdf4jContextIRI.isEmpty());
    }
}
