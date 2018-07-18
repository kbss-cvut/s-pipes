package eu.trentorise.opendata.jackan.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import java.util.Set;

@OWLClass(iri = "http://onto.fel.cvut.cz/ontologies/org/ckan/catalog")
public class CkanCatalog {
    @Id
    private String iri;

    @OWLObjectProperty(iri = "http://onto.fel.cvut.cz/ontologies/org/ckan/catalog/has-dataset")
    private Set<CkanDataset> datasets;

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public Set<CkanDataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(Set<CkanDataset> datasets) {
        this.datasets = datasets;
    }
}
