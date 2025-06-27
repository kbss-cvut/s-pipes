package cz.cvut.spipes.spin.model.update.impl;

import cz.cvut.spipes.spin.model.update.Modify;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class ModifyImpl extends UpdateImpl implements Modify {
    public ModifyImpl(Node n, EnhGraph m) {
        super(n, m);
    }
}
