package cz.cvut.spipes.debug.persistance.dao;

import org.springframework.stereotype.Repository;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.spipes.Vocabulary;
import cz.cvut.spipes.model.PipelineComparison;

@Repository
public class ComparisonResultDao extends AbstractDao<PipelineComparison> {

    protected ComparisonResultDao(EntityManager em) {
        super(em);
    }

    public PipelineComparison findByCompareAndCompareWith(String compare, String compareTo) {
        try {
            return em.createNativeQuery("SELECT ?comparison WHERE {"
                    + "  ?comparison a <" + Vocabulary.s_c_pipeline_comparison + "> ;"
                    + "             <" + Vocabulary.s_p_pipeline + "> <" + compare + "> ;"
                    + "             <" + Vocabulary.s_p_compare_to + "> <" + compareTo + "> ."
                    + "}", PipelineComparison.class).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }

    }

}