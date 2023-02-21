package cz.cvut.spipes.debug.persistance.dao;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.spipes.model.Transformation;

@Repository
public class TransformationDao {

    private final EntityManager em;

    @Autowired
    public TransformationDao(EntityManager em) {
        this.em = em;
    }

    public List<Transformation> findAll() {
        return em.createNamedQuery("Transformation.findAll", Transformation.class).getResultList();
    }

    public Transformation findByUri(String uri) {
        Objects.requireNonNull(uri);
        try {
            return em.find(Transformation.class, uri);
        } finally {
            em.close();
        }
    }
}
