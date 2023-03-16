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

    @OWLDataProperty(iri = "http://onto.fel.cvut.cz/ontologies/dataset-descriptor/has-next")
    private String has_next;

    @OWLDataProperty(iri = "http://onto.fel.cvut.cz/ontologies/s-pipes/has-output-model-triple-count")
    private long output_triple_count;

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

    public String getHas_next() {
        return has_next;
    }

    public void setHas_next(String has_next) {
        this.has_next = has_next;
    }

    public long getOutput_triple_count() {
        return output_triple_count;
    }

    public void setOutput_triple_count(long output_triple_count) {
        this.output_triple_count = output_triple_count;
    }
}
