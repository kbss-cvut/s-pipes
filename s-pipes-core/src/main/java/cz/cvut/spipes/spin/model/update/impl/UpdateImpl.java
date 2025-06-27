package cz.cvut.spipes.spin.model.update.impl;

import cz.cvut.spipes.spin.model.impl.SPINResourceImpl;
import cz.cvut.spipes.spin.model.update.Update;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class UpdateImpl extends SPINResourceImpl implements Update {
    public UpdateImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
