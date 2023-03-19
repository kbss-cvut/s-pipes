package cz.cvut.spipes.debug.persistance.dao;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
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

    public Boolean askContainOutput(String context, String graphPattern) {
        try {
            return (Boolean) em.createNativeQuery("ASK {"
                    + "  GRAPH <" + context + "> {"
                    + graphPattern +
                    "  }}").getSingleResult();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public Boolean askContainInputAndNotContainOutput(String inputContext,String outputContext, String graphPattern) {
        try {
           return (Boolean) em.createNativeQuery(String.format( "ASK {"
                    + "  GRAPH <%s> {"
                    + "    FILTER NOT EXISTS {%s}"
                    + "  }"
                    + "  GRAPH <%s> {%s}"
                    + "}",inputContext, graphPattern, outputContext, graphPattern)).getSingleResult();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
