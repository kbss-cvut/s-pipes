package cz.cvut.spipes.debug.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.spipes.debug.config.PersistenceConfig;
import cz.cvut.spipes.debug.persistance.PersistenceFactory;
import cz.cvut.spipes.debug.persistance.RDf4jPersistenceProvider;
import cz.cvut.spipes.debug.persistance.dao.AbstractDao;

@Service
public class ConfigurationService {

    private final PersistenceFactory persistenceFactory;

    private final RDf4jPersistenceProvider rDf4jPersistenceProvider;

    private final List<AbstractDao> allDaos;

    @Autowired
    public ConfigurationService(
            PersistenceFactory persistenceFactory,
            RDf4jPersistenceProvider rDf4jPersistenceProvider,
            List<AbstractDao> allDaos,
            PersistenceConfig persistenceConfig) {
        this.persistenceFactory = persistenceFactory;
        this.rDf4jPersistenceProvider = rDf4jPersistenceProvider;
        this.allDaos = allDaos;
    }


    public void changeRepository(String repositoryName) {
        persistenceFactory.reloadEmf(repositoryName);
        rDf4jPersistenceProvider.changeRepository(repositoryName);
        EntityManager entityManager = persistenceFactory.getEmf().createEntityManager();
        allDaos.forEach(dao -> dao.updateEm(entityManager));
    }
}
