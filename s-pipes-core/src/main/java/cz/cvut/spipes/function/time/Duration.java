package cz.cvut.spipes.function.time;

import cz.cvut.spipes.constants.KBSS_TIMEF;
import cz.cvut.spipes.function.ValueFunction;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.topbraid.spin.arq.AbstractFunction2;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;

/**
 * Computes the time difference between two xsd:dateTime values in milliseconds and returns the result as xsd:long.
 * Returns a negative value if the first parameter represents a later time.
 */
public class Duration extends AbstractFunction2 implements ValueFunction {

    private static final String TYPE_IRI = KBSS_TIMEF.getURI() + "add-days";

    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }

    @Override
    protected NodeValue exec(Node startDateTime, Node endDateTime, FunctionEnv functionEnv) {
        Calendar start = parseNodeToCalendar(startDateTime);
        Calendar end = parseNodeToCalendar(endDateTime);

        long duration = end.getTimeInMillis()-start.getTimeInMillis();
        Node node = NodeFactory.createLiteralByValue(duration, XSDDatatype.XSDlong);
        return NodeValue.makeNode(node);
    }

    private Calendar parseNodeToCalendar(Node dateTime){
        return DatatypeConverter.parseDateTime(dateTime.getLiteral().getLexicalForm());
    }
}
