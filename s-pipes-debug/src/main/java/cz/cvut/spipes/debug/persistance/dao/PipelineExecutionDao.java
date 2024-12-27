package cz.cvut.spipes.debug.persistance.dao;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.spipes.Vocabulary;
import cz.cvut.spipes.model.PipelineExecution;

@Repository
public class PipelineExecutionDao extends AbstractDao<PipelineExecution> {

    public List<PipelineExecution> findAll() {
        return em.createNativeQuery("SELECT ?x WHERE { ?x a <" + Vocabulary.s_c_pipeline_execution + "> .}", PipelineExecution.class).getResultList();
    }

    public PipelineExecution findById(String id) {
        return findByUri(Vocabulary.s_c_pipeline_execution + "/" + id);
    }

    public PipelineExecution findByUri(String uri) {
        Objects.requireNonNull(uri);
        try {
            return em.find(PipelineExecution.class, uri);
        } finally {
            em.close();
        }
    }

    protected PipelineExecutionDao(EntityManager em) {
        super(em);
    }
}



