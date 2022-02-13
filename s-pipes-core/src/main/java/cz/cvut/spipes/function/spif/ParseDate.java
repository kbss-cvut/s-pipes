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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

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

    /**
     * @param text The input string.
     * @param pattern The template of the input string.
     * @param patternLanguage The code of the language (e.g. \"de\" for German) to use for parsing. May be <code>null</code>.
     * @param env Environment of the function. May be <code>null</code>.
     * @return NodeValue with parsed date/time/datetime.
     */
    @Override
    public NodeValue exec(Node text, Node pattern, Node patternLanguage, FunctionEnv env) {
        String textValue = getRequiredParameterLiteralValue(text, 1);
        String patternValue = getRequiredParameterLiteralValue(pattern, 2);

        Optional<Node> patternLanguageNode = Optional.ofNullable(patternLanguage);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(patternValue);
        patternLanguageNode.ifPresent(node -> checkLocaleFormat(node, formatter, textValue));

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
            return getTimeNode(localTime.format(DateTimeFormatter.ofPattern("kk:mm:ss")));
        }catch(Exception e){
            throw new ParseException();
        }
    }

    private String getRequiredParameterLiteralValue(Node text, int number) {
        if(text == null){
            throw new IllegalArgumentException(number + ". argument of this function is required but missing.");
        }else if(text.isLiteral()){
            return text.getLiteralValue().toString();
        }else{
            throw new IllegalArgumentException(number + ". argument of this function is not literal.");
        }
    }

    private void checkLocaleFormat(Node patternLanguageNode, DateTimeFormatter formatter, String textValue){
        String patternLanguageValue = patternLanguageNode.getLiteralValue().toString();
        formatter = formatter.withLocale(new Locale(patternLanguageValue));

        LocalDate ld = LocalDate.parse(textValue,formatter);
        LocalDate localeDate = LocalDate.of(ld.getYear(),ld.getMonthValue(),ld.getDayOfMonth());
        DateTimeFormatter localeFormat = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT)
                .withLocale( new Locale(patternLanguageValue));

        String localeString = localeDate.format(localeFormat);

        if(!textValue.contains(localeString)){
            throw new IllegalArgumentException("Pattern does not corresponds to the pattern language.");
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
