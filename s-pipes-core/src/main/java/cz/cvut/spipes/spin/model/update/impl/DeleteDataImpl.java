package cz.cvut.spipes.spin.model.update.impl;

import cz.cvut.spipes.spin.model.update.DeleteData;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class DeleteDataImpl extends UpdateImpl implements DeleteData {
    public DeleteDataImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
