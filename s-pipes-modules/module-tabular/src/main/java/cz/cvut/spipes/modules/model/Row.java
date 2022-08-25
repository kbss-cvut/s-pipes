package cz.cvut.spipes.modules.model;

import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.spipes.constants.CSVW;

@OWLClass(iri = CSVW.RowUri)
public class Row extends AbstractEntity {

    @OWLAnnotationProperty(iri = CSVW.URL)
    private String url;

    @OWLDataProperty(iri = CSVW.rowNumUri)
    private Integer rownum;

    @OWLAnnotationProperty(iri = CSVW.describesUri)
    private String describes;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getRownum() {
        return rownum;
    }

    public void setRownum(Integer rownum) {
        this.rownum = rownum;
    }

    public String getDescribes() {
        return describes;
    }

    public void setDescribes(String describes) {
        this.describes = describes;
    }
}
