package cz.cvut.spipes.function.spif;

import cz.cvut.spipes.constants.SPIF;
import cz.cvut.spipes.exception.ParseException;
import cz.cvut.spipes.function.ValueFunction;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.rdf.model.LiteralRequiredException;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Converts a string in a semi-structured format into a xsd:date, xsd:dateTime or xsd:time literal.
 * The input string must be in a given template format, e.g. \"dd.MM.yyyy\" for strings such as 4.2.2022."
 */
public class ParseDate extends FunctionBase implements ValueFunction {

    private static final String TYPE_IRI = SPIF.uri + "parseDate";

    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        if ( args.size() < 2 || args.size() > 3)
            throw new QueryBuildException("Function '" + Lib.className(this) + "' takes two or three arguments");
    }

    @Override
    public final NodeValue exec(List<NodeValue> args) {
        if ( args == null )
            throw new ARQInternalErrorException(Lib.className(this) + ": Null args list");
        if ( args.size() < 2 || args.size() > 3 )
            throw new ExprEvalException(Lib.className(this) + ": Wrong number of arguments: Wanted 2 + 1 optional, got " + args.size());
        NodeValue v1 = args.get(0);
        NodeValue v2 = args.get(1);
        NodeValue v3 = args.size() == 3 ?  args.get(2) : null;
        return exec(v1, v2, v3);
    }

    /**
     * @param text The input string.
     * @param pattern The template of the input string.
     * @param patternLanguage The code of the language (e.g. \"de\" for German) to use for parsing. May be <code>null</code>.
     * @return NodeValue with parsed date/time/datetime.
     */
    public NodeValue exec(NodeValue text, NodeValue pattern, NodeValue patternLanguage) {
        if(text == null || pattern == null){
            return null;
        }
        String textValue = getRequiredParameterLiteralValue(text);
        String patternValue = getRequiredParameterLiteralValue(pattern);

        Optional<NodeValue> patternLanguageNode = Optional.ofNullable(patternLanguage);
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
            return getTimeNode(localTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }catch(Exception e){
            throw new ParseException();
        }
    }

    private String getRequiredParameterLiteralValue(NodeValue textNV) {
        Node text = textNV.asNode();
        if(text.isLiteral()){
            return text.getLiteralValue().toString();
        }else{
            throw new LiteralRequiredException(text);
        }
    }

    private void checkLocaleFormat(NodeValue patternLanguageNode, DateTimeFormatter formatter, String textValue){
        String patternLanguageValue = patternLanguageNode.asNode().getLiteralValue().toString();
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
