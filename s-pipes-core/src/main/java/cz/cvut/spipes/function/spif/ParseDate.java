package cz.cvut.spipes.function.spif;

import cz.cvut.spipes.constants.SPIF;
import cz.cvut.spipes.exception.ParseException;
import cz.cvut.spipes.function.ValueFunction;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.topbraid.spin.arq.AbstractFunction3;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Converts a string in a semi-structured format into a xsd:date, xsd:dateTime or xsd:time literal.
 * The input string must be in a given template format, e.g. \"dd.MM.yyyy\" for strings such as 4.2.2022."
 */
public class ParseDate extends AbstractFunction3 implements ValueFunction {

    private static final String TYPE_IRI = SPIF.getURI() + "parseDate";

    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }

    @Override
    public NodeValue exec(Node text, Node pattern, Node patternLanguage, FunctionEnv env) {

        String textValue = text.getLiteralValue().toString();
        String patternValue = pattern.getLiteralValue().toString();
        String patternLanguageValue = patternLanguage.getLiteralValue().toString();
        Locale locale = new Locale(patternLanguageValue);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(patternValue).withLocale(locale);

        try{
            LocalDateTime localDateTime = LocalDateTime.parse(textValue,formatter);
            return getDateTimeNode(String.valueOf(localDateTime));
        }catch(Exception ignored){}

        try{
            LocalDate localDate = LocalDate.parse(textValue,formatter);
            return getDateNode(String.valueOf(localDate));
        }catch(Exception ignored){}

        try{
            LocalTime localTime = LocalTime.parse(textValue,formatter);
            return getTimeNode(String.valueOf(localTime));
        }catch(Exception e){
            throw new ParseException();
        }
    }

    private NodeValue getDateNode(String date){
        return getNode(date, XSDDatatype.XSDdate);
    }
    private NodeValue getTimeNode(String date){
        return getNode(date, XSDDatatype.XSDtime);
    }
    private NodeValue getDateTimeNode(String date){return getNode(date, XSDDatatype.XSDdateTime);}

    private NodeValue getNode(String date,XSDDatatype type) {
        return NodeValue.makeNode(
                date,
                null,
                ((RDFDatatype) type).getURI()
        );
    }
}
