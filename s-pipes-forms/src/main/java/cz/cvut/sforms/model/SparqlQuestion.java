package cz.cvut.sforms.model;

import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.sforms.Vocabulary;
import java.util.HashSet;
import java.util.Set;

public class SparqlQuestion extends Question {

    @OWLObjectProperty(iri = Vocabulary.s_p_has_answer, fetch = FetchType.EAGER)
    private Set<PrefixDefinition> declaredPrefix = new HashSet<>();

    public Set<PrefixDefinition> getDeclaredPrefix() {
        return declaredPrefix;
    }

    public void setDeclaredPrefix(Set<PrefixDefinition> declaredPrefix) {
        this.declaredPrefix = declaredPrefix;
    }
}
