package cz.cvut.spipes.debug.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.spipes.Vocabulary;


@OWLClass(iri = Vocabulary.s_c_related_resource)
public class RelatedResource {

    @Id
    private final String id = null;

    @OWLDataProperty(iri = Vocabulary.s_p_name)
    private String name;

    @OWLDataProperty(iri = Vocabulary.s_p_value)
    private String link;

    @OWLDataProperty(iri = "http://onto.fel.cvut.cz/ontologies/s-pipes/related-resource/params")
    private Map<String, List<String>> paramsOptions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void addParam(String param, List<String> options){
        if(paramsOptions == null){
            paramsOptions = new HashMap<>();
        }
        paramsOptions.put(param, options);
    }

    public Map<String, List<String>> getParamsOptions() {
        return paramsOptions;
    }
}
