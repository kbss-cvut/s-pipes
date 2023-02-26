package cz.cvut.spipes.debug.model;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraint;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.spipes.debug.Vocabulary;

@OWLClass(iri = Vocabulary.s_c_module_execution)
public class ModuleExecution extends ExecutionAbstract {
    @OWLDataProperty(iri = Vocabulary.s_p_has_execution_time)
    protected Long execution_time_ms;

    @OWLDataProperty(iri = Vocabulary.s_p_has_module_id)
    protected String has_module_id;

    @OWLObjectProperty(iri = Vocabulary.s_p_executed_in)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_pipeline_execution, max = 1)
    })
    private String executed_in;

    public Long getExecution_time_ms() {
        return execution_time_ms;
    }

    public void setExecution_time_ms(Long execution_time_ms) {
        this.execution_time_ms = execution_time_ms;
    }

    public String getHas_module_id() {
        return has_module_id;
    }

    public void setHas_module_id(String has_module_id) {
        this.has_module_id = has_module_id;
    }

    public String getExecuted_in() {
        return executed_in;
    }

    public void setExecuted_in(String executed_in) {
        this.executed_in = executed_in;
    }
}
