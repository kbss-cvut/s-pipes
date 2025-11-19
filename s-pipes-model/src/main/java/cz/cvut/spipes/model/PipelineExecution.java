package cz.cvut.spipes.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.spipes.Vocabulary;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@OWLClass(iri = Vocabulary.s_c_pipeline_execution)
public class PipelineExecution extends Thing {
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

    @OWLObjectProperty(iri = Vocabulary.s_p_has_rdf4j_output, fetch = FetchType.EAGER)
    @ParticipationConstraints(
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_target_dataset_snapshot, max = 1))
    protected Thing has_rdf4j_output;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_rdf4j_input, fetch = FetchType.EAGER)
    @ParticipationConstraints(
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_source_dataset_snapshot, max = 1))
    protected Thing has_rdf4j_input;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_part, fetch = FetchType.EAGER)
    @ParticipationConstraints(
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_module_execution))
    protected Set<ModuleExecution> has_part = new HashSet<>();


    @OWLObjectProperty(iri = "http://onto.fel.cvut.cz/ontologies/dataset-descriptor/has-input-binding", fetch = FetchType.EAGER)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_target_dataset_snapshot)
    })
    protected Set<Thing> has_input_binding;

    @OWLDataProperty(iri = Vocabulary.s_p_has_module_execution_start_date, fetch = FetchType.EAGER)
    protected Date start_date;

    @OWLDataProperty(iri = Vocabulary.s_p_has_module_execution_finish_date, fetch = FetchType.EAGER)
    protected Date finish_date;

    @OWLDataProperty(iri = Vocabulary.s_p_has_pipeline_execution_start_date, fetch = FetchType.EAGER)
    protected Date has_pipepline_execution_start_date;

    @OWLDataProperty(iri = Vocabulary.s_p_has_pipeline_execution_finish_date, fetch = FetchType.EAGER)
    protected Date has_pipeline_execution_finish_date;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_script)
    protected URI has_script;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_function)
    protected URI has_function;

    @OWLDataProperty(iri = Vocabulary.s_p_has_script_path)
    protected String has_script_path;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_module_id, fetch = FetchType.EAGER)
    protected String has_module_id;

    @OWLDataProperty(iri = Vocabulary.s_p_has_output_model_triple_count, fetch = FetchType.EAGER)
    private Long output_triple_count;

    @OWLDataProperty(iri = Vocabulary.s_p_has_input_model_triple_count, fetch = FetchType.EAGER)
    private Long input_triple_count;

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


    public Date getHas_pipepline_execution_start_date() {
        return has_pipepline_execution_start_date;
    }

    public void setHas_pipepline_execution_start_date(Date has_pipepline_execution_start_date) {
        this.has_pipepline_execution_start_date = has_pipepline_execution_start_date;
    }

    public Date getHas_pipeline_execution_finish_date() {
        return has_pipeline_execution_finish_date;
    }

    public void setHas_pipeline_execution_finish_date(Date has_pipeline_execution_finish_date) {
        this.has_pipeline_execution_finish_date = has_pipeline_execution_finish_date;
    }

    public URI getHas_script() {
        return has_script;
    }

    public void setHas_script(URI has_script) {
        this.has_script = has_script;
    }

    public URI getHas_function() {
        return has_function;
    }

    public void setHas_function(URI has_function) {
        this.has_function = has_function;
    }

    public String getHas_script_path() {
        return has_script_path;
    }

    public void setHas_script_path(String has_script_path) {
        this.has_script_path = has_script_path;
    }

    public String getHas_module_id() {
        return has_module_id;
    }

    public void setHas_module_id(String has_module_id) {
        this.has_module_id = has_module_id;
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


    public void setHas_rdf4j_output(TargetDatasetSnapshot has_rdf4j_output) {
        this.has_rdf4j_output = has_rdf4j_output;
    }

    public Thing getHas_rdf4j_input() {
        return has_rdf4j_input;
    }

    public void setHas_rdf4j_input(SourceDatasetSnapshot has_rdf4j_input) {
        this.has_rdf4j_input = has_rdf4j_input;
    }

    public Set<ModuleExecution> getHas_part() {
        return has_part;
    }

    public void setHas_part(Set<ModuleExecution> has_part) {
        this.has_part = has_part;
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
}
