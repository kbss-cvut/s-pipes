package cz.cvut.spipes.debug.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraint;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.spipes.debug.Vocabulary;

public abstract class ExecutionAbstract extends AbstractDto {

    @OWLObjectProperty(iri = Vocabulary.s_p_has_related_resource)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_p_has_related_resource)
    })
    protected Set<RelatedResource> has_related_resources;

    @OWLDataProperty(iri = Vocabulary.s_p_has_execution_start_date)
    protected Date start_date;

    @OWLDataProperty(iri = Vocabulary.s_p_has_execution_finish_date)
    protected Date finish_date;

    public Set<RelatedResource> getHas_related_resources() {
        return has_related_resources;
    }

    public void setHas_related_resources(Set<RelatedResource> has_related_resources) {
        this.has_related_resources = has_related_resources;
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

    public void addRelated_resource(RelatedResource relatedResource) {
        if(has_related_resources == null){
            has_related_resources = new HashSet<>();
        }
        has_related_resources.add(relatedResource);
    }
}
