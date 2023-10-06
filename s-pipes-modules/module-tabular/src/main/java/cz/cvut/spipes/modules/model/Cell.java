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

    @OWLAnnotationProperty(iri = CSVW.rowUri)
    private String row;

    @OWLAnnotationProperty(iri = CSVW.columnUri)
    private String column;

    @OWLAnnotationProperty(iri = KBSS_CSVW.sameValueAsUri)
    private String sameValueAsCell;

    private final transient TabularModuleUtils tabularModuleUtils = new TabularModuleUtils();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSameValueAsCell() {
        return sameValueAsCell;
    }

    public void setSameValueAsCell(String sameValueAsCell) {
        this.sameValueAsCell = sameValueAsCell;
    }

    public void setRow(String row) {
        tabularModuleUtils.setVariable(this.row, row, value -> this.row = value, "rowName");
    }

    public String getRow() {
        return row;
    }

    public void setColumn(String column) {
        tabularModuleUtils.setVariable(this.column, column, value -> this.column = value, "columnName");
    }

    public String getColumn() {
        return column;
    }

}
