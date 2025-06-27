package cz.cvut.spipes.spin.model.update.impl;

import cz.cvut.spipes.spin.model.update.Load;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class LoadImpl extends UpdateImpl implements Load {
    public LoadImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
