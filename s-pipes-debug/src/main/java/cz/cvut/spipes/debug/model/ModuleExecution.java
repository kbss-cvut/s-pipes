package cz.cvut.spipes.debug.model;

import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraint;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.spipes.Vocabulary;
import cz.cvut.spipes.model.Thing;

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

    @OWLDataProperty(iri = Vocabulary.s_p_has_next)
    private String has_next;

    @OWLDataProperty(iri = Vocabulary.s_p_has_output_model_triple_count)
    private Long output_triple_count;

    @OWLDataProperty(iri = Vocabulary.s_p_has_input_model_triple_count)
    private Long input_triple_count;

    @OWLDataProperty(iri = "http://onto.fel.cvut.cz/ontologies/dataset-descriptor/has-input-binding", fetch = FetchType.EAGER)
    private Thing has_input_binding;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_rdf4j_output, fetch = FetchType.EAGER)
    @ParticipationConstraints(
            @ParticipationConstraint(owlObjectIRI = cz.cvut.spipes.Vocabulary.s_c_target_dataset_snapshot, max = 1))
    protected Thing has_rdf4j_output;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_rdf4j_input, fetch = FetchType.EAGER)
    @ParticipationConstraints(
            @ParticipationConstraint(owlObjectIRI = cz.cvut.spipes.Vocabulary.s_c_source_dataset_snapshot, max = 1))
    protected Thing has_rdf4j_input;

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

    public Long getOutput_triple_count() {
        return output_triple_count;
    }

    public void setOutput_triple_count(Long output_triple_count) {
        this.output_triple_count = output_triple_count;
    }

    public Long getInput_triple_count() {
        return input_triple_count;
    }

    public void setInput_triple_count(Long input_triple_count) {
        this.input_triple_count = input_triple_count;
    }

    public Thing getHas_rdf4j_output() {
        return has_rdf4j_output;
    }

    public void setHas_rdf4j_output(Thing has_rdf4j_output) {
        this.has_rdf4j_output = has_rdf4j_output;
    }

    public Thing getHas_rdf4j_input() {
        return has_rdf4j_input;
    }

    public void setHas_rdf4j_input(Thing has_rdf4j_input) {
        this.has_rdf4j_input = has_rdf4j_input;
    }

    public Thing getHas_input_binding() {
        return has_input_binding;
    }

    public void setHas_input_binding(Thing has_input_binding) {
        this.has_input_binding = has_input_binding;
    }
}
