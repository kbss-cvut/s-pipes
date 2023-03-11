package cz.cvut.spipes.debug.persistance.dao;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.spipes.Vocabulary;
import cz.cvut.spipes.model.Transformation;

@Repository
public class TransformationDao extends AbstractDao {

    protected TransformationDao(EntityManager em) {
        super(em);
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

    public List<String> findModuleOutputContextsForTriple(String executionId, String subjectPattern, String predicatePattern, String objectPattern) {
        try {
            return em.createNativeQuery("SELECT ?context "
                            + "WHERE {"
                            + "  GRAPH ?context {"
                            + "    ?subject ?predicate ?object"
                            + "    FILTER(REGEX(str(?context), \"^" + Vocabulary.s_c_transformation + "/" + executionId + ".*output$\"))"
                            + "    FILTER(REGEX(str(?subject), ?subjectPattern))"
                            + "    FILTER(REGEX(str(?predicate), ?predicatePattern))"
                            + "    FILTER(REGEX(str(?object), ?objectPattern))"
                            + "  }}", String.class)
                    .setParameter("subjectPattern", subjectPattern)
                    .setParameter("predicatePattern", predicatePattern)
                    .setParameter("objectPattern", objectPattern)
                    .getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }
}
