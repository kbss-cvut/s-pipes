package cz.cvut.spipes.modules.model;

import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.spipes.constants.CSVW;
import cz.cvut.spipes.constants.KBSS_CSVW;
import cz.cvut.spipes.modules.util.TabularModuleUtils;

import java.net.URI;
import java.net.URISyntaxException;

@OWLClass(iri = CSVW.CellUri)
public class Cell extends AbstractEntity{
    public Cell() {}

    public Cell(String cellUri) {
        try {
            this.setUri(new URI(cellUri));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @OWLAnnotationProperty(iri = CSVW.nameUri)
    private String name;

    @OWLAnnotationProperty(iri = KBSS_CSVW.isPartOfRow)
    private URI row;

    @OWLAnnotationProperty(iri = KBSS_CSVW.isPartOfColumn)
    private URI column;

    @OWLAnnotationProperty(iri = KBSS_CSVW.sameValueAsUri)
    private URI sameValueAsCell;

    private final transient TabularModuleUtils tabularModuleUtils = new TabularModuleUtils();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getSameValueAsCell() {
        return sameValueAsCell;
    }

    public void setSameValueAsCell(URI sameValueAsCell) {
        this.sameValueAsCell = sameValueAsCell;
    }

    public void setRow(URI row) {
        tabularModuleUtils.setVariable(this.row, row, value -> this.row = value, "rowName");
    }

    public URI getRow() {
        return row;
    }

    public void setColumn(URI column) {
        tabularModuleUtils.setVariable(this.column, column, value -> this.column = value, "columnName");
    }

    public URI getColumn() {
        return column;
    }

}
