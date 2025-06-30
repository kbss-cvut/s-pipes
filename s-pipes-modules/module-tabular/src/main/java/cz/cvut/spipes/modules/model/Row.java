package cz.cvut.spipes.modules.model;

import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.spipes.constants.CSVW;

import java.net.URI;

@OWLClass(iri = CSVW.RowUri)
public class Row extends AbstractEntity {

    @OWLAnnotationProperty(iri = CSVW.URL)
    private URI url;

    @OWLDataProperty(iri = CSVW.rowNumUri)
    private Integer rownum;

    @OWLAnnotationProperty(iri = CSVW.describesUri)
    private URI describes;

    public URI getUrl() {
        return url;
    }

    public void setUrl(String url) {
        setUrl(URI.create(url));
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public Integer getRownum() {
        return rownum;
    }

    public void setRownum(Integer rownum) {
        this.rownum = rownum;
    }

    public URI getDescribes() {
        return describes;
    }

    public void setDescribes(URI describes) {
        this.describes = describes;
    }
}
