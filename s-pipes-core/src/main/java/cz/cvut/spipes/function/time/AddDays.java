package cz.cvut.spipes.function.time;

import cz.cvut.spipes.constants.KBSS_TIMEF;
import cz.cvut.spipes.function.ValueFunction;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDBaseStringType;
import org.apache.jena.datatypes.xsd.impl.XSDDateType;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.topbraid.spin.arq.AbstractFunction2;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Extend specified `date` by number of `days`. Return typed literal with same datatype.
 * Currently, supports only xsd:date datatype.
 */
public class AddDays extends AbstractFunction2 implements ValueFunction {


    private static final String TYPE_IRI = KBSS_TIMEF.uri + "add-days";

    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }

    @Override
    protected NodeValue exec(Node date, Node days, FunctionEnv env) {

        Long daysToAdd = getDays(days);
        RDFDatatype datatype = getDatatype(date);

        try {
            if (datatype != null && daysToAdd != null) {
                //TODO quite slow to parse everytime
               String newDate = LocalDate.parse(date.getLiteral().getValue().toString()).plusDays(daysToAdd).toString();
               return NodeValue.makeNode(newDate, datatype);
            }
        } catch (DateTimeParseException e){
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
            .map(n -> getNewDatatype(n.getLiteralDatatype()))
            .orElse(null);
    }

    RDFDatatype getNewDatatype(RDFDatatype datatype){
        if (datatype instanceof XSDDateType) {
            return XSDDatatype.XSDdate;
        }
        if (datatype instanceof XSDBaseStringType) {
            return XSDDatatype.XSDstring;
        }
        return null;
    }
}
