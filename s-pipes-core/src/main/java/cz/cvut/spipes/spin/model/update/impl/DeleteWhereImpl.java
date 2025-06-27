package cz.cvut.spipes.spin.model.update.impl;

import cz.cvut.spipes.spin.model.update.DeleteWhere;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class DeleteWhereImpl extends UpdateImpl implements DeleteWhere {
    public DeleteWhereImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
