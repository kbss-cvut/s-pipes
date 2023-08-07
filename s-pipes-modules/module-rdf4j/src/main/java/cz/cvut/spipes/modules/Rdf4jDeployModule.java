package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.exception.ModuleConfigurationInconsistentException;
import cz.cvut.spipes.util.CoreConfigProperies;
import org.apache.jena.rdf.model.Property;
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Module deploys content of input execution context
 * into default context of repository (if p-rdf4j-context-iri is not specified)
 * or concrete context (if p-rdf4j-context-iri is specified).
 */
public class Rdf4jDeployModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(Rdf4jDeployModule.class);

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

    static final Property P_RDF4J_REPOSITORY_USERNAME = getParameter("p-rdf4j-secured-username-variable");
    private String rdf4jSecuredUsernameVariable;
    private RepositoryManager repositoryManager;
    private Repository repository;

    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    static final Property P_RDF4J_REPOSITORY_PASSWORD = getParameter("p-rdf4j-secured-password-variable");
    private String rdf4jSecuredPasswordVariable;
    /**
     * Whether data should be replaced (true) / appended (false) into the specified context or repository.
     * Default is false.
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
        LOG.debug("Deploying data into {} of rdf4j server repository {}/{}.",
            isRdf4jContextIRIDefined() ? "context " + rdf4jContextIRI : "default context",
            rdf4jServerURL,
            rdf4jRepositoryName);
        RepositoryConnection connection = null;
        try {

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
        rdf4jSecuredUsernameVariable = Optional.ofNullable(
            getEffectiveValue(P_RDF4J_REPOSITORY_USERNAME)).map(n -> n.asLiteral().getString()
        ).orElse(null);
        rdf4jSecuredPasswordVariable = Optional.ofNullable(
            getEffectiveValue(P_RDF4J_REPOSITORY_PASSWORD)).map(n -> n.asLiteral().getString()
        ).orElse(null);
        if (repositoryManager != null && rdf4jServerURL != null) {
            throw new ModuleConfigurationInconsistentException(
                    "Repository manager is already initialized. Trying to override its configuration from RDF.");
        }
        repositoryManager = RepositoryProvider.getRepositoryManager(rdf4jServerURL);
        String username = getConfigurationVariable(rdf4jSecuredUsernameVariable);
        String password = getConfigurationVariable(rdf4jSecuredPasswordVariable);
        if (username != null && password != null) {
            RemoteRepositoryManager remoteRepositoryManager = (RemoteRepositoryManager) repositoryManager;
            remoteRepositoryManager.setUsernameAndPassword(username, password);
        }
        repository = repositoryManager.getRepository(rdf4jRepositoryName);
    }
    private static @Nullable String getConfigurationVariable(String variableName) {
        if (variableName == null) {
            return null;
        }
        return CoreConfigProperies.getConfigurationVariable(variableName);
    }

    private boolean isRdf4jContextIRIDefined() {
        return (rdf4jContextIRI != null) && (!rdf4jContextIRI.isEmpty());
    }
}
