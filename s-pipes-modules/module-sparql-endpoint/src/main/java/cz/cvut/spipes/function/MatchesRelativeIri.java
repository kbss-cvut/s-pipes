package cz.cvut.spipes.function;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.vocabulary.XSD;
import org.topbraid.spin.arq.AbstractFunction2;

public class MatchesRelativeIri extends AbstractFunction2 implements ValueFunction {


    private static final String TYPE_IRI = "http://onto.fel.cvut.cz/ontologies/lib/function/general/matches-relative-iri";


    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }

    @Override
    protected NodeValue exec(Node object, Node relativeIRI, FunctionEnv env) {

        if (hasSecondArgument(relativeIRI)) {
            if (! object.toString().equals(relativeIRI.getLiteral().toString())) {
                return  NodeValue.makeNodeBoolean(false);
            }
        }

        if (! object.isURI()) {
            return NodeValue.makeNodeBoolean(false);
        }

        if (object.toString().contains("://")) {
            return NodeValue.makeNodeBoolean(false);
        }

        return NodeValue.makeNodeBoolean(true);

    }

    private boolean hasSecondArgument(Node relativeIRI) {
        if (relativeIRI != null) {
            if ((! relativeIRI.isLiteral()) && (relativeIRI.getLiteralDatatypeURI().equals(XSD.xstring.toString()))) {
                throw new IllegalStateException("Argument '" + relativeIRI + "' is not of type xsd:string.");
            }
            return true;
        }
        return false;
    }
}
