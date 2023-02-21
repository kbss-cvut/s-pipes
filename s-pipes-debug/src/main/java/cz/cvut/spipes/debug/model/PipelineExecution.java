package cz.cvut.spipes.debug.model;

import java.util.Date;
import java.util.List;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraint;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.spipes.debug.Vocabulary;

@OWLClass(iri = Vocabulary.s_c_pipeline_execution)
public class PipelineExecution extends ExecutionAbstract {

    @OWLDataProperty(iri = Vocabulary.s_p_has_execution_start_date)
    private Date has_pipepline_execution_date;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_module_execution)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_module_execution)
    })
    private List<ModuleExecution> has_module_executions;

    public Date getHas_pipepline_execution_date() {
        return has_pipepline_execution_date;
    }

    public void setHas_pipepline_execution_date(Date has_pipepline_execution_date) {
        this.has_pipepline_execution_date = has_pipepline_execution_date;
    }

    public List<ModuleExecution> getHas_module_executions() {
        return has_module_executions;
    }

    public void setHas_module_executions(List<ModuleExecution> has_module_executions) {
        this.has_module_executions = has_module_executions;
    }

}

