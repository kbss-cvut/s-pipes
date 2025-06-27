package cz.cvut.spipes.spin.util;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;

import java.lang.reflect.Constructor;

public class SPINEnhancedNodeFactory extends Implementation {
    private final Node type;
    private Constructor enhNodeConstructor;

    public SPINEnhancedNodeFactory(Node type, Class implClass) {
        this.type = type;
        try {
            enhNodeConstructor = implClass.getConstructor(Node.class, EnhGraph.class);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public EnhNode wrap(Node node, EnhGraph eg) {
        try {
            return (EnhNode) enhNodeConstructor.newInstance(node, eg);
        }
        catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean canWrap(Node node, EnhGraph eg) {
        return eg.asGraph().contains(node, RDF.type.asNode(), type);
    }
}
