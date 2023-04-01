package cz.cvut.spipes.debug.persistance.dao;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.spipes.debug.exception.PersistenceException;

public abstract class AbstractDao<T> {
    protected EntityManager em;

    @Autowired
    protected AbstractDao(EntityManager em) {
        this.em = em;
    }

    public void updateEm(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void persist(T entity) {
        Objects.requireNonNull(entity);
        try {
            em.persist(entity);
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }
}
