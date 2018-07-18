package cz.cvut.spipes.modules.eccairs;

import cz.cvut.kbss.eccairs.cfg.ConfigurationJopa;
import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.ontodriver.sesame.config.SesameOntoDriverProperties;
import org.eclipse.rdf4j.repository.Repository;

import java.util.Map;

/**
 * Created by Miroslav Blasko on 29.11.16.
 */
public class JopaPersistenceUtils {

    public static EntityManagerFactory createEntityManagerFactoryWithMemoryStore(){
        Map<String,String> persistenceProperties = ConfigurationJopa.getDefaultPersistenceParams();
        persistenceProperties.put(SesameOntoDriverProperties.SESAME_USE_VOLATILE_STORAGE, "true");
        persistenceProperties.put(JOPAPersistenceProperties.ONTOLOGY_URI_KEY, "http://test");
        persistenceProperties.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, "local://test");// jopa uses the URI scheme to choose between local and remote repo, file and (http, https and ftp)resp.

        return Persistence.createEntityManagerFactory("testPersistenceUnit", persistenceProperties);
    }


    public static Repository getRepository(EntityManager entityManager) {
        try {
            return entityManager.unwrap(Repository.class);
        } finally {
            entityManager.close();
        }
    }
}
