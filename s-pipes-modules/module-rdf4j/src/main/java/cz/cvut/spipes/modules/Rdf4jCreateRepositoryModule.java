package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.exceptions.RepositoryAlreadyExistsException;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Rdf4jCreateRepositoryModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(Rdf4jUpdateModule.class.getName());
    private static final String TYPE_URI = KBSS_MODULE.getURI() + "rdf4j-create-repository";
    private static final String PROPERTY_PREFIX_URI = KBSS_MODULE.getURI() + "rdf4j";

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
     * Don't try to create new repository if it already exists
     */
    static final Property P_RDF4J_IGNORE_IF_EXISTS = getParameter("p-rdf4j-ignore-if-exists");
    private boolean rdf4jIgnoreIfExists;

    private RepositoryManager repositoryManager;

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

    public boolean isRdf4jIgnoreIfExists() {
        return rdf4jIgnoreIfExists;
    }

    public void setRdf4jIgnoreIfExists(boolean rdf4jIgnoreIfExists) {
        this.rdf4jIgnoreIfExists = rdf4jIgnoreIfExists;
    }

    private static Property getParameter(final String name) {
        return ResourceFactory.createProperty(PROPERTY_PREFIX_URI + "/" + name);
    }

    void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @Override
    ExecutionContext executeSelf() {
        NativeStoreConfig nativeStoreConfig = new NativeStoreConfig();
        SailRepositoryConfig sailRepositoryConfig = new SailRepositoryConfig(nativeStoreConfig);

        repositoryManager.init();
        LOG.info("Server url:{}, Repsitory name:{}, Ignore if repository exist:{}.",
                rdf4jServerURL,
                rdf4jRepositoryName,
                rdf4jIgnoreIfExists);

        if((!rdf4jIgnoreIfExists) && repositoryManager.hasRepositoryConfig(rdf4jRepositoryName)){

            LOG.info("Repository \"{}\" already exists",
                    rdf4jRepositoryName);
            throw new RepositoryAlreadyExistsException(rdf4jRepositoryName);
        }

        RepositoryConfig repositoryConfig = new RepositoryConfig(rdf4jRepositoryName,sailRepositoryConfig);
        repositoryManager.addRepositoryConfig(repositoryConfig);
        repositoryManager.getRepository(rdf4jRepositoryName).init();

        return executionContext;
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {
        rdf4jServerURL = getEffectiveValue(P_RDF4J_SERVER_URL).asLiteral().getString();
        rdf4jRepositoryName = getEffectiveValue(P_RDF4J_REPOSITORY_NAME).asLiteral().getString();
        rdf4jIgnoreIfExists = (Objects.equals(getEffectiveValue(P_RDF4J_IGNORE_IF_EXISTS).asLiteral().getString(), "true"));
        repositoryManager = new RemoteRepositoryManager(rdf4jServerURL);
    }
}
