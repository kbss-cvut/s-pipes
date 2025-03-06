package cz.cvut.spipes.function.time;

import cz.cvut.spipes.constants.KBSS_TIMEF;
import cz.cvut.spipes.function.ValueFunction;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDBaseStringType;
import org.apache.jena.datatypes.xsd.impl.XSDDateType;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Extend specified `date` by number of `days`. Return typed literal with same datatype.
 * Currently, supports only xsd:date datatype.
 */
public class AddDays extends FunctionBase2 implements ValueFunction {


    private static final String TYPE_IRI = KBSS_TIMEF.uri + "add-days";

    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }


    @Override
    public NodeValue exec(NodeValue date, NodeValue days) {
        Long daysToAdd = getDays(days);
        RDFDatatype datatype = getDatatype(date);

        try {
            if (datatype != null && daysToAdd != null) {
                //TODO quite slow to parse everytime
                String newDate = LocalDate.parse(date.asNode().getLiteral().getValue().toString()).plusDays(daysToAdd).toString();
                return NodeValue.makeNode(newDate, datatype);
            }
        } catch (DateTimeParseException e){
        }

        return null;
    }

    private Long getDays(NodeValue days) {
        return Optional.of(days)
            .filter(NodeValue::isLiteral)
            .filter(n -> n.asNode().getLiteralValue() instanceof Integer)
            .map(n -> ((Integer) n.asNode().getLiteralValue()).longValue())
            .orElse(null);
    }

    RDFDatatype getDatatype(NodeValue date) {

        return Optional.of(date)
            .filter(NodeValue::isLiteral)
            .map(n -> getNewDatatype(n.getNode().getLiteralDatatype()))
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
