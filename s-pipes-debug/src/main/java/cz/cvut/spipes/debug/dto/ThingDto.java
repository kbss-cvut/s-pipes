package cz.cvut.spipes.debug.dto;

import java.util.Set;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jopa.model.annotations.util.NonEntity;
import cz.cvut.spipes.Vocabulary;

@OWLClass(iri = Vocabulary.s_c_Thing)
public class ThingDto{

    @Id(generated = true)
    public String id;

    @Types
    public Set<String> types;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }
}
