package cz.cvut.spipes.spin.model.impl;

import cz.cvut.spipes.spin.model.Ask;
import cz.cvut.spipes.spin.util.SPINEnhancedNodeFactory;
import cz.cvut.spipes.spin.vocabulary.SP;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;

public class AskImpl extends QueryImpl implements Ask {

    public AskImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}