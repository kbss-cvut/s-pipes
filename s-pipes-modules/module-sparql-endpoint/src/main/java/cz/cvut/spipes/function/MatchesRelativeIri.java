package cz.cvut.spipes.function;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.apache.jena.vocabulary.XSD;

public class MatchesRelativeIri extends FunctionBase2 implements ValueFunction {


    private static final String TYPE_IRI = "http://onto.fel.cvut.cz/ontologies/lib/function/general/matches-relative-iri";


    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }

    @Override
    public NodeValue exec(NodeValue object, NodeValue relativeIRI) {

        if (hasSecondArgument(relativeIRI)) {
            if (! object.toString().equals(relativeIRI.asNode().getLiteral().toString())) {
                return  NodeValue.makeNodeBoolean(false);
            }
        }

        if (! object.isIRI()) {
            return NodeValue.makeNodeBoolean(false);
        }

        if (object.toString().contains("://")) {
            return NodeValue.makeNodeBoolean(false);
        }

        return NodeValue.makeNodeBoolean(true);

    }

    private boolean hasSecondArgument(NodeValue relativeIRI) {
        if (relativeIRI != null) {
            if ((! relativeIRI.isLiteral()) && (relativeIRI.asNode().getLiteralDatatypeURI().equals(XSD.xstring.toString()))) {
                throw new IllegalStateException("Argument '" + relativeIRI + "' is not of type xsd:string.");
            }
            return true;
        }
        return false;
    }
}
