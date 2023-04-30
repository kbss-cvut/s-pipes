package cz.cvut.spipes.debug.dto;

import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraint;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.spipes.Vocabulary;

public class PipelineComparisonResultDto extends ThingDto {

    @OWLObjectProperty(iri = Vocabulary.s_p_pipeline)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_pipeline_execution)
    })
    private PipelineExecutionDto pipeline;

    @OWLObjectProperty(iri = Vocabulary.s_p_compare_to)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_pipeline_execution)
    })
    private PipelineExecutionDto compare_to;

    @OWLDataProperty(iri = Vocabulary.s_p_are_same)
    private Boolean are_same;

    @OWLObjectProperty(iri = Vocabulary.s_p_difference_found_in)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_module_execution)
    })
    private ModuleExecutionDto difference_found_in;

    public PipelineExecutionDto getPipeline() {
        return pipeline;
    }

    public void setPipeline(PipelineExecutionDto pipeline) {
        this.pipeline = pipeline;
    }

    public PipelineExecutionDto getCompare_to() {
        return compare_to;
    }

    public void setCompare_to(PipelineExecutionDto compare_to) {
        this.compare_to = compare_to;
    }

    public Boolean getAre_same() {
        return are_same;
    }

    public void setAre_same(Boolean are_same) {
        this.are_same = are_same;
    }

    public ModuleExecutionDto getDifference_found_in() {
        return difference_found_in;
    }

    public void setDifference_found_in(ModuleExecutionDto difference_found_in) {
        this.difference_found_in = difference_found_in;
    }
}
