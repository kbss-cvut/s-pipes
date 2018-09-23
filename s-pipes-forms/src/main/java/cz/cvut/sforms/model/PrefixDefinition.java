package cz.cvut.sforms.model;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.sforms.Vocabulary;

@OWLClass(iri = Vocabulary.s_c_PrefixDeclaration)
public class PrefixDefinition extends AbstractEntity{

    @OWLDataProperty(iri = Vocabulary.s_p_prefix)
    private String prefix;

    @OWLDataProperty(iri = Vocabulary.s_p_namespace)
    private String namespace;

    public PrefixDefinition() {
    }

    public PrefixDefinition(String prefix, String namespace) {
        this.prefix = prefix;
        this.namespace = namespace;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
