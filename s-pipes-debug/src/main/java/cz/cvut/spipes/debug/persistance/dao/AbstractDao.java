package cz.cvut.spipes.debug.persistance.dao;

import org.springframework.beans.factory.annotation.Autowired;

import cz.cvut.kbss.jopa.model.EntityManager;

public abstract class AbstractDao {
    protected EntityManager em;

    @Autowired
    protected AbstractDao(EntityManager em) {
        this.em = em;
    }

    public void updateEm(EntityManager em){
        this.em = em;
    }
}
