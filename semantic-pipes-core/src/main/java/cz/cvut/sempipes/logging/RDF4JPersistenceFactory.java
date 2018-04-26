package cz.cvut.sempipes.logging;

import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProvider;
import cz.cvut.kbss.ontodriver.config.OntoDriverProperties;
import cz.cvut.kbss.ontodriver.sesame.config.SesameOntoDriverProperties;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class RDF4JPersistenceFactory {

    private RDF4JPersistenceFactory() {
        throw new AssertionError();
    }

    public static @NotNull
    EntityManagerFactory getEntityManagerFactory(String persistenceUnitName, String rdf4jServerUrl, String repositoryName) {
        final Map<String, String> props = new HashMap<>();

        String rdf4jRepositoryUrl = rdf4jServerUrl + "/repositories/" + repositoryName;
        props.putAll(getInitialParams());
        props.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, rdf4jRepositoryUrl );
        return Persistence.createEntityManagerFactory(persistenceUnitName, props);
    }

    private static @NotNull
    Map<String, String> getInitialParams() {
        final Map<String, String> props = new HashMap<>();
        // Here we set up basic storage access properties-driver class, physical location of the storage
        props.put(JOPAPersistenceProperties.DATA_SOURCE_CLASS, "cz.cvut.kbss.ontodriver.sesame.SesameDataSource");
        // View transactional changes during transaction
        props.put(OntoDriverProperties.USE_TRANSACTIONAL_ONTOLOGY, Boolean.TRUE.toString());
        // Don't use Sesame inference
        props.put(SesameOntoDriverProperties.SESAME_USE_INFERENCE, Boolean.FALSE.toString());
        // Don't use cache
        props.put(JOPAPersistenceProperties.CACHE_ENABLED, Boolean.FALSE.toString());
        // Ontology language
        props.put(JOPAPersistenceProperties.LANG, "en");
        // Persistence provider name
        props.put(JOPAPersistenceProperties.JPA_PERSISTENCE_PROVIDER,
            JOPAPersistenceProvider.class.getName());

        props.put(JOPAPersistenceProperties.SCAN_PACKAGE, "cz.cvut.sempipes.model");
        return props;
    }

}
