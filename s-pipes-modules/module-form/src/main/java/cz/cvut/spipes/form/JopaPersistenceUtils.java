package cz.cvut.spipes.form;

import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProvider;
import cz.cvut.kbss.jopa.model.PersistenceProperties;
import cz.cvut.kbss.ontodriver.jena.config.JenaOntoDriverProperties;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.query.Dataset;

public class JopaPersistenceUtils {

    public static EntityManagerFactory createEntityManagerFactoryWithMemoryStore(){

        Map<String,String> persistenceProperties = new HashMap<>();
        persistenceProperties.put(JOPAPersistenceProperties.ONTOLOGY_URI_KEY, "http://temporary");
        persistenceProperties.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, "local://temporary");
        persistenceProperties.put(JOPAPersistenceProperties.DATA_SOURCE_CLASS, "cz.cvut.kbss.ontodriver.jena.JenaDataSource");
        persistenceProperties.put(JOPAPersistenceProperties.LANG, "en");
        persistenceProperties.put(JOPAPersistenceProperties.SCAN_PACKAGE, "cz.cvut.sforms.model");
        persistenceProperties.put(PersistenceProperties.JPA_PERSISTENCE_PROVIDER, JOPAPersistenceProvider.class.getName());
        persistenceProperties.put(JenaOntoDriverProperties.IN_MEMORY, "true");

        return Persistence.createEntityManagerFactory("testPersistenceUnit", persistenceProperties);
    }


    public static Dataset getDataset(EntityManager entityManager) {
        try {
            return entityManager.unwrap(Dataset.class);
        } finally {
            entityManager.close();
        }
    }
}