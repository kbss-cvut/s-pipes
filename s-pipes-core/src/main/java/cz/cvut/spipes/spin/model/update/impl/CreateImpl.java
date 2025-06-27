package cz.cvut.spipes.spin.model.update.impl;

import cz.cvut.spipes.spin.model.update.Create;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class CreateImpl extends UpdateImpl implements Create {
    public CreateImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
