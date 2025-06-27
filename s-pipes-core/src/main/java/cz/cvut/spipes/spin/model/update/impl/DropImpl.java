package cz.cvut.spipes.spin.model.update.impl;

import cz.cvut.spipes.spin.model.update.Drop;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class DropImpl extends UpdateImpl implements Drop {
    public DropImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
