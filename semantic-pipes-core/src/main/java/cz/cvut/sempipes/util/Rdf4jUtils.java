package cz.cvut.sempipes.util;

import java.io.IOException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rdf4jUtils {

    private static final Logger LOG = LoggerFactory.getLogger(Rdf4jUtils.class);

    public static void createRdf4RepositoryIfNotExist(String rdf4jServerUrl, String repositoryName) {
        Repository repository = null;
        try {
            RepositoryManager repositoryManager = RepositoryProvider.getRepositoryManager(rdf4jServerUrl);
            repository = repositoryManager.getRepository(repositoryName);
            if (repository == null) {
                LOG.info("Creating new repository {} within rdf4j server {} ...",
                    rdf4jServerUrl, repositoryName);
                RepositoryConfig repConfig = new RepositoryConfig(repositoryName);
                SailRepositoryConfig config = new SailRepositoryConfig(new NativeStoreConfig());
                repConfig.setRepositoryImplConfig(config);
                repositoryManager.addRepositoryConfig(repConfig);
                repository = repositoryManager.getRepository(repositoryName);
            }
            repository.initialize();

        } catch (final RepositoryException | RDFParseException | RepositoryConfigException e) {
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
}
