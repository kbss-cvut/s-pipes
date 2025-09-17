package cz.cvut.spipes.spin.model.impl;

import cz.cvut.spipes.spin.model.Ask;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class AskImpl extends QueryImpl implements Ask {

    public AskImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}