package cz.cvut.spipes.spin.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDFS;

public class SPINResourceImpl extends ResourceImpl {

    public SPINResourceImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public String getComment(){
        return getString(RDFS.comment);
    }

    public String getString(Property predicate){
        Statement s = getProperty(predicate);
        if(s != null && s.getObject().isLiteral()) {
            return s.getString();
        }
        else {
            return null;
        }
    }
}

