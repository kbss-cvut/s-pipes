package cz.cvut.spipes.spin.util;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

public class SPINEnhancedNodeFactory extends Implementation {
    private final Node type;
    private Constructor<? extends EnhNode> enhNodeConstructor;

    private static final Logger log = LoggerFactory.getLogger(SPINEnhancedNodeFactory.class);

    public SPINEnhancedNodeFactory(Node type, Class<? extends EnhNode> implClass) {
        this.type = type;
        try {
            enhNodeConstructor = implClass.getConstructor(Node.class, EnhGraph.class);
        }
        catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    @Override
    public EnhNode wrap(Node node, EnhGraph eg) {
        try {
            return enhNodeConstructor.newInstance(node, eg);
        }
        catch (Throwable t) {
            log.error(t.getMessage(), t);
            return null;
        }
    }

    @Override
    public boolean canWrap(Node node, EnhGraph eg) {
        return eg.asGraph().contains(node, RDF.type.asNode(), type);
    }
}
