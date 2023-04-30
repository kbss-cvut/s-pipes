package cz.cvut.spipes.debug.dto;

import java.util.Set;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraint;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.spipes.Vocabulary;
import cz.cvut.spipes.model.SourceDatasetSnapshot;
import cz.cvut.spipes.model.Thing;

@OWLClass(iri = Vocabulary.s_c_module_execution)
public class ModuleExecutionDto extends ExecutionThing {

    @OWLDataProperty(iri = Vocabulary.s_p_has_duration)
    protected Long duration;

    @OWLDataProperty(iri = Vocabulary.s_p_has_module_id)
    protected String has_module_id;

    @OWLObjectProperty(iri = Vocabulary.s_p_executed_in)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_pipeline_execution, max = 1)
    })
    private ThingDto executed_in;

    @OWLDataProperty(iri = Vocabulary.s_p_has_next)
    private String has_next;

    @OWLDataProperty(iri = Vocabulary.s_p_has_output_model_triple_count)
    private Long output_triple_count;

    @OWLDataProperty(iri = Vocabulary.s_p_has_input_model_triple_count)
    private Long input_triple_count;

    @OWLObjectProperty(iri = "http://onto.fel.cvut.cz/ontologies/dataset-descriptor/has-input-binding")
    @ParticipationConstraints(
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_Thing))
    private Set<ThingDto> has_input_binding;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_rdf4j_output)
    @ParticipationConstraints(
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_target_dataset_snapshot, max = 1))
    protected ThingDto has_rdf4j_output;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_rdf4j_input)
    @ParticipationConstraints(
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_source_dataset_snapshot, max = 1))
    protected ThingDto has_rdf4j_input;

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getHas_module_id() {
        return has_module_id;
    }

    public void setHas_module_id(String has_module_id) {
        this.has_module_id = has_module_id;
    }

    public ThingDto getExecuted_in() {
        return executed_in;
    }

    public void setExecuted_in(ThingDto executed_in) {
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

    public ThingDto getHas_rdf4j_output() {
        return has_rdf4j_output;
    }

    public void setHas_rdf4j_output(ThingDto has_rdf4j_output) {
        this.has_rdf4j_output = has_rdf4j_output;
    }

    public ThingDto getHas_rdf4j_input() {
        return has_rdf4j_input;
    }

    public void setHas_rdf4j_input(ThingDto has_rdf4j_input) {
        this.has_rdf4j_input = has_rdf4j_input;
    }

    public Set<ThingDto> getHas_input_binding() {
        return has_input_binding;
    }

    public void setHas_input_binding(Set<ThingDto> has_input_binding) {
        this.has_input_binding = has_input_binding;
    }
}
