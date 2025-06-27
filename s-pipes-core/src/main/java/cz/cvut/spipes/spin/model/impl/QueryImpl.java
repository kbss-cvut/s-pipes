package cz.cvut.spipes.spin.model.impl;

import cz.cvut.spipes.spin.model.SolutionModifierQuery;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class QueryImpl extends SPINResourceImpl implements SolutionModifierQuery {
    public QueryImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
