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

    @OWLAnnotationProperty(iri = CSVW.RowUri)
    private String rowName;

    @OWLAnnotationProperty(iri = CSVW.ColumnUri)
    private String columnName;

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

    public void setRowName(String rowName) {
        tabularModuleUtils.setVariable(this.rowName, rowName, value -> this.rowName = value, "rowName");
    }

    public String getRowName() {
        return rowName;
    }

    public void setColumnName(String columnName) {
        tabularModuleUtils.setVariable(this.columnName, columnName, value -> this.columnName = value, "columnName");
    }

    public String getColumnName() {
        return columnName;
    }

}
