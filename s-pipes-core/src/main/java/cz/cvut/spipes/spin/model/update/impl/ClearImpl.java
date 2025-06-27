package cz.cvut.spipes.spin.model.update.impl;

import cz.cvut.spipes.spin.model.update.Clear;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class ClearImpl extends UpdateImpl implements Clear {
    public ClearImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
