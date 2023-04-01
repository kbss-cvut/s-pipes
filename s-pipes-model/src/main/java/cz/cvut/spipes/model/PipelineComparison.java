package cz.cvut.spipes.model;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraint;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.spipes.Vocabulary;

@OWLClass(iri = Vocabulary.s_c_pipeline_comparison)
public class PipelineComparison extends Thing {

    public PipelineComparison() {
        types.add(Vocabulary.s_c_pipeline_comparison);
    }

    @OWLObjectProperty(iri = Vocabulary.s_p_pipeline)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_transformation)
    })
    private Transformation pipeline;

    @OWLObjectProperty(iri = Vocabulary.s_p_compare_to)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_transformation)
    })
    private Transformation compare_to;

    @OWLDataProperty(iri = Vocabulary.s_p_are_same)
    private Boolean are_same;

    @OWLObjectProperty(iri = Vocabulary.s_p_difference_found_in)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_transformation)
    })
    private Transformation difference_found_in;


    public Transformation getPipeline() {
        return pipeline;
    }

    public void setPipeline(Transformation pipeline) {
        this.pipeline = pipeline;
    }

    public Transformation getCompare_to() {
        return compare_to;
    }

    public void setCompare_to(Transformation compare_to) {
        this.compare_to = compare_to;
    }

    public Boolean getAre_same() {
        return are_same;
    }

    public void setAre_same(Boolean are_same) {
        this.are_same = are_same;
    }

    public Transformation getDifference_found_in() {
        return difference_found_in;
    }

    public void setDifference_found_in(Transformation difference_found_in) {
        this.difference_found_in = difference_found_in;
    }
}
