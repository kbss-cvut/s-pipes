package cz.cvut.sforms.model;

import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.sforms.Vocabulary;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@OWLClass(iri = Vocabulary.s_c_answer)
public class Answer extends AbstractEntity {

    @OWLDataProperty(iri = Vocabulary.s_p_has_data_value)
    private String textValue;

    @OWLAnnotationProperty(iri = Vocabulary.s_p_has_object_value)
    private URI codeValue;

    @OWLAnnotationProperty(iri = Vocabulary.s_p_has_answer_origin)
    private URI origin;

    @OWLDataProperty(iri = Vocabulary.s_p_has_answer_hash)
    private String hash;

    @Types
    private Set<String> types = new HashSet<>();

    public Answer() {
    }

    public Answer(Answer other) {
        this.textValue = other.textValue;
        this.codeValue = other.codeValue;
        this.origin = other.origin;
        if (other.types != null) {
            this.types.addAll(other.types);
        }
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public URI getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(URI codeValue) {
        this.codeValue = codeValue;
    }

    public URI getOrigin() {
        return origin;
    }

    public void setOrigin(URI origin) {
        this.origin = origin;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        String out = "Answer{";
        if (textValue != null && codeValue != null) {
            out += "value=" + textValue + ", code=" + codeValue;
        } else if (textValue == null) {
            out += "code=" + codeValue;
        } else {
            out += "text=" + textValue;
        }
        out += '}';
        return out;
    }
}
