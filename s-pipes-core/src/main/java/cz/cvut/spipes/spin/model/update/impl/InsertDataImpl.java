package cz.cvut.spipes.spin.model.update.impl;

import cz.cvut.spipes.spin.model.update.InsertData;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class InsertDataImpl extends UpdateImpl implements InsertData {
    public InsertDataImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
