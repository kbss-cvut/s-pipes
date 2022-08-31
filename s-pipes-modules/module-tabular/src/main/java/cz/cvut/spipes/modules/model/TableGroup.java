package cz.cvut.spipes.modules.model;

import cz.cvut.kbss.jopa.model.annotations.CascadeType;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.spipes.constants.CSVW;

@OWLClass(iri = CSVW.tableGroupUri)
public class TableGroup extends AbstractEntity{

    @OWLObjectProperty(iri = CSVW.tableUri, cascade = CascadeType.ALL)
    private Table table;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}
