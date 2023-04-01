package cz.cvut.spipes.debug.persistance.dao;

import org.springframework.stereotype.Repository;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.spipes.model.PipelineComparison;

@Repository
public class ComparisonResultDao extends AbstractDao<PipelineComparison> {

    protected ComparisonResultDao(EntityManager em) {
        super(em);
    }

    public PipelineComparison findByCompareAndCompareWith(String compare, String compareTo) {
        try {
            return em.createNativeQuery("SELECT ?comparison WHERE {"
                    + "  ?comparison a <http://onto.fel.cvut.cz/ontologies/dataset-descriptor/pipeline-comparison> ;"
                    + "             <http://onto.fel.cvut.cz/ontologies/dataset-descriptor/compare> <" + compare + "> ;"
                    + "             <http://onto.fel.cvut.cz/ontologies/dataset-descriptor/compare-to> <" + compareTo + "> ."
                    + "}", PipelineComparison.class).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }

    }

}