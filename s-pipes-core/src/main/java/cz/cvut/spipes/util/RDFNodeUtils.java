package cz.cvut.spipes.util;

import org.apache.jena.rdf.model.RDFNode;

public class RDFNodeUtils {

    public static String toString(RDFNode node) {
        if (node.isLiteral()) {
            return "\"" + node.asLiteral().getLexicalForm() + "\"";
        } else if (node.isURIResource() || node.isResource()) {
            return "<" + node.asResource().getURI() + ">";
        } else {
            return node.toString();
        }
    }
}
