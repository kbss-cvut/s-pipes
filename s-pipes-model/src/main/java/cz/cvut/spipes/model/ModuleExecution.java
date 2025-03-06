package cz.cvut.spipes.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraint;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.spipes.Vocabulary;

@OWLClass(iri = Vocabulary.s_c_module_execution)
public class ModuleExecution extends Thing {
    @OWLAnnotationProperty(iri = RDFS.LABEL)
    protected String name;
    @OWLAnnotationProperty(iri = DC.Elements.DESCRIPTION)
    protected String description;
    @Types
    protected Set<String> types;
    @Id(generated = true)
    protected String id;
    @Properties
    protected Map<String, Set<Object>> properties;
    @OWLObjectProperty(iri = Vocabulary.s_p_has_input)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_source_dataset_snapshot, max = 1)
    })
    protected SourceDatasetSnapshot has_input;
    @OWLObjectProperty(iri = Vocabulary.s_p_has_output)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_target_dataset_snapshot, max = 1)
    })
    protected Set<Thing> has_output;

    @OWLObjectProperty(iri = Vocabulary.s_p_executed_in, fetch = FetchType.EAGER)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_pipeline_execution, max = 1)
    })
    protected PipelineExecution executed_in;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_rdf4j_output, fetch = FetchType.EAGER)
    @ParticipationConstraints(
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_target_dataset_snapshot, max = 1))
    protected Thing has_rdf4j_output;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_rdf4j_input, fetch = FetchType.EAGER)
    @ParticipationConstraints(
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_source_dataset_snapshot, max = 1))
    protected Thing has_rdf4j_input;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_next, fetch = FetchType.EAGER)
    @ParticipationConstraints(
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_module_execution))
    protected ModuleExecution has_next;


    @OWLObjectProperty(iri = "http://onto.fel.cvut.cz/ontologies/dataset-descriptor/has-input-binding", fetch = FetchType.EAGER)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_target_dataset_snapshot)
    })
    protected Set<Thing> has_input_binding;

    @OWLDataProperty(iri = Vocabulary.s_p_has_module_execution_start_date, fetch = FetchType.EAGER)
    protected Date start_date;

    @OWLDataProperty(iri = Vocabulary.s_p_has_module_execution_finish_date, fetch = FetchType.EAGER)
    protected Date finish_date;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_module_id, fetch = FetchType.EAGER)
    protected String has_module_id;

    @OWLDataProperty(iri = Vocabulary.s_p_has_output_model_triple_count, fetch = FetchType.EAGER)
    private Long output_triple_count;

    @OWLDataProperty(iri = Vocabulary.s_p_has_input_model_triple_count, fetch = FetchType.EAGER)
    private Long input_triple_count;

    @OWLDataProperty(iri = Vocabulary.s_p_has_duration)
    protected Long duration;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setProperties(Map<String, Set<Object>> properties) {
        this.properties = properties;
    }

    public Map<String, Set<Object>> getProperties() {
        return properties;
    }

    public void setHas_input(SourceDatasetSnapshot has_input) {
        this.has_input = has_input;
    }

    public SourceDatasetSnapshot getHas_input() {
        return has_input;
    }

    public void setHas_output(Set<Thing> has_output) {
        this.has_output = has_output;
    }

    public Set<Thing> getHas_output() {
        return has_output;
    }

    public Date getStart_date() {
        return start_date;
    }

    public void setStart_date(Date start_date) {
        this.start_date = start_date;
    }

    public Date getFinish_date() {
        return finish_date;
    }

    public void setFinish_date(Date finish_date) {
        this.finish_date = finish_date;
    }

    public String getHas_module_id() {
        return has_module_id;
    }

    public void setHas_module_id(String has_module_id) {
        this.has_module_id = has_module_id;
    }

    public ModuleExecution getHas_next() {
        return has_next;
    }

    public void setHas_next(ModuleExecution has_next) {
        this.has_next = has_next;
    }

    public long getOutput_triple_count() {
        return output_triple_count;
    }

    public void setOutput_triple_count(long output_triple_count) {
        this.output_triple_count = output_triple_count;
    }

    public Thing getHas_rdf4j_output() {
        return has_rdf4j_output;
    }


    public void setHas_rdf4j_output(SourceDatasetSnapshot has_rdf4j_output) {
        this.has_rdf4j_output = has_rdf4j_output;
    }

    public Thing getHas_rdf4j_input() {
        return has_rdf4j_input;
    }

    public void setHas_rdf4j_input(SourceDatasetSnapshot has_rdf4j_input) {
        this.has_rdf4j_input = has_rdf4j_input;
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

    public Set<Thing> getHas_input_binding() {
        return has_input_binding;
    }

    public void setHas_input_binding(Set<Thing> has_input_binding) {
        this.has_input_binding = has_input_binding;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public PipelineExecution getExecuted_in() {
        return executed_in;
    }

    public void setExecuted_in(PipelineExecution executed_in) {
        this.executed_in = executed_in;
    }

    public void setHas_rdf4j_output(Thing has_rdf4j_output) {
        this.has_rdf4j_output = has_rdf4j_output;
    }

    public void setHas_rdf4j_input(Thing has_rdf4j_input) {
        this.has_rdf4j_input = has_rdf4j_input;
    }
}
