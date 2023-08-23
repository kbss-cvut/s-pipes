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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Computes difference between two xsd:dateTime values in milliseconds and returns xsd:long datatype
 */
public class Duration extends AbstractFunction2 implements ValueFunction {

    private static final String TYPE_IRI = KBSS_TIMEF.getURI() + "add-days";

    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }

    @Override
    protected NodeValue exec(Node t2, Node t1, FunctionEnv functionEnv) {
        Calendar start = parseNodeToCalendar(t1);
        Calendar end = parseNodeToCalendar(t2);

        long duration = end.getTimeInMillis()-start.getTimeInMillis();
        Node node = NodeFactory.createLiteralByValue(duration, XSDDatatype.XSDlong);
        return NodeValue.makeNode(node);
    }

    private Calendar parseNodeToCalendar(Node x){
        return DatatypeConverter.parseDateTime(extractDateTimePart(x.getLiteral().toString()));
    }

    private static String extractDateTimePart(String rdfLiteral) {
        String dateTimePattern = "^(.*?)(?:\\^\\^.*|$)";
        Pattern pattern = Pattern.compile(dateTimePattern);
        Matcher matcher = pattern.matcher(rdfLiteral);

        if (matcher.find())return matcher.group(1);
        return null;
    }

}
