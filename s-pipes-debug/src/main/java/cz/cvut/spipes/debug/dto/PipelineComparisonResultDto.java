package cz.cvut.spipes.debug.dto;

import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraint;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.spipes.Vocabulary;
import cz.cvut.spipes.debug.model.AbstractDto;
import cz.cvut.spipes.debug.model.ModuleExecution;
import cz.cvut.spipes.debug.model.PipelineExecution;

public class PipelineComparisonResultDto extends AbstractDto {

    @OWLObjectProperty(iri = Vocabulary.s_p_pipeline)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_transformation)
    })
    private PipelineExecution pipeline;

    @OWLObjectProperty(iri = Vocabulary.s_p_compare_to)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_transformation)
    })
    private PipelineExecution compare_to;

    @OWLDataProperty(iri = Vocabulary.s_p_are_same)
    private Boolean are_same;

    @OWLObjectProperty(iri = Vocabulary.s_p_difference_found_in)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_transformation)
    })
    private ModuleExecution difference_found_in;

    public PipelineExecution getPipeline() {
        return pipeline;
    }

    public void setPipeline(PipelineExecution pipeline) {
        this.pipeline = pipeline;
    }

    public PipelineExecution getCompare_to() {
        return compare_to;
    }

    public void setCompare_to(PipelineExecution compare_to) {
        this.compare_to = compare_to;
    }

    public Boolean getAre_same() {
        return are_same;
    }

    public void setAre_same(Boolean are_same) {
        this.are_same = are_same;
    }

    public ModuleExecution getDifference_found_in() {
        return difference_found_in;
    }

    public void setDifference_found_in(ModuleExecution difference_found_in) {
        this.difference_found_in = difference_found_in;
    }
}
