package cz.cvut.spipes.spin.model.impl;

import cz.cvut.spipes.spin.model.Construct;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class ConstructImpl extends QueryImpl implements Construct {
    public ConstructImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
