package cz.cvut.spipes.spin.model.impl;

import cz.cvut.spipes.spin.model.Describe;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class DescribeImpl extends QueryImpl implements Describe {
    public DescribeImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
