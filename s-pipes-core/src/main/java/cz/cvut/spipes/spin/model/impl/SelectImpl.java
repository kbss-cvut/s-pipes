package cz.cvut.spipes.spin.model.impl;

import cz.cvut.spipes.spin.model.Select;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class SelectImpl extends QueryImpl implements Select {
    public SelectImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}

