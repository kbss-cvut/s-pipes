package cz.cvut.spipes.modules.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.spipes.constants.CSVW;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;


@OWLClass(iri = CSVW.TableUri)
public class Table extends AbstractEntity{

    @OWLObjectProperty(iri = CSVW.tableSchemaUri, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private TableSchema tableSchema;

    @OWLAnnotationProperty(iri = CSVW.URL)
    private URI url;

    @OWLObjectProperty(iri = CSVW.rowUri, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Row> rows = new HashSet<>();

    @OWLAnnotationProperty(iri = "http://www.w3.org/2000/01/rdf-schema#label")
    private String label;

    public TableSchema getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(TableSchema tableSchema) {
        this.tableSchema = tableSchema;
    }

    public Set<Row> getRows() {
        return rows;
    }

    public void setRows(Set<Row> rows) {
        this.rows = rows;
    }

    public URI getUrl() {
        return url;
    }

    public void setUrl(String url) {
        setUrl(URI.create(url));
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
