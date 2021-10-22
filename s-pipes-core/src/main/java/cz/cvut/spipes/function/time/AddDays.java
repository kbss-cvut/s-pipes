package cz.cvut.spipes.function.time;

import cz.cvut.spipes.function.ValueFunction;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDDateType;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.topbraid.spin.arq.AbstractFunction2;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Extend specified `date` by number of `days`. Return typed literal with same datatype.
 * Currently, supports only xsd:date datatype.
 */
public class AddDays extends AbstractFunction2 implements ValueFunction {


    private static final String TYPE_IRI = "http://onto.fel.cvut.cz/ontologies/lib/function/time/add-days";

    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }

    @Override
    protected NodeValue exec(Node date, Node days, FunctionEnv env) {

        Long daysToAdd = getDays(days);

        if (getDatatype(date).equals(XSDDatatype.XSDdate) && daysToAdd != null) {

            String newDate = LocalDate.parse(date.getLiteral().getValue().toString()).plusDays(daysToAdd).toString();

            return NodeValue.makeNode(newDate, XSDDatatype.XSDdate);
        }

        return null;
    }

    private Long getDays(Node days) {
        return Optional.of(days)
            .filter(Node::isLiteral)
            .filter(n -> n.getLiteralValue() instanceof Integer)
            .map(n -> ((Integer) n.getLiteralValue()).longValue())
            .orElse(null);
    }

    RDFDatatype getDatatype(Node date) {

        return Optional.of(date)
            .filter(Node::isLiteral)
            .filter(n -> n.getLiteralDatatype() instanceof XSDDateType)
            .map(Node::getLiteralDatatype)
            .orElse(null);
    }
}
