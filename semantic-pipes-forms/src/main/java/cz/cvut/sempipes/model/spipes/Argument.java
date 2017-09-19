package cz.cvut.sempipes.model.spipes;

import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.sempipes.Vocabulary;
import cz.cvut.sempipes.model.AbstractEntity;
import java.net.URI;

public class Argument extends AbstractEntity {



    Boolean optional;

//    @OWLObjectProperty(iri = Vocabulary.n)
    private URI predicate;

    @OWLDataProperty(iri = Vocabulary.s_p_comment)
    private String comment;

    public Boolean getOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public URI getPredicate() {
        return predicate;
    }

    public void setPredicate(URI predicate) {
        this.predicate = predicate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
