package cz.cvut.spipes.spin.model.update.impl;

import cz.cvut.spipes.spin.model.update.Insert;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

@Deprecated
public class InsertImpl extends UpdateImpl implements Insert {

    public InsertImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
