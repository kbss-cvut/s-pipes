package cz.cvut.spipes.function.date;

import cz.cvut.spipes.exception.ParseException;
import cz.cvut.spipes.function.spif.ParseDate;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.LiteralRequiredException;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class ParseDateTest {

    @Test
    public void execReturnsDate_ItalianLocale() {
        ParseDate parseDate = new ParseDate();

        NodeValue text = createLiteral("02/11/21");
        NodeValue pattern = createLiteral("dd/MM/yy");
        NodeValue patternLanguage = createLiteral("it");

        NodeValue returnedDate = parseDate.exec(text, pattern, patternLanguage);
        NodeValue expectedDate = getDateNode("2021-11-02");
        assertEquals(expectedDate, returnedDate);
    }

    @Test
    public void execReturnsDate_EnglishLocale() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("9/21/10");
        NodeValue pattern = createLiteral("M/dd/yy");
        NodeValue patternLanguage = createLiteral("en");

        NodeValue returnedDate = parseDate.exec(text, pattern, patternLanguage);
        NodeValue expectedDate = getDateNode("2010-09-21");
        assertEquals(expectedDate, returnedDate);
    }

    @Test
    public void execReturnsDate_FrenchLocale() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("19/12/2016");
        NodeValue pattern = createLiteral("dd/MM/yyyy");
        NodeValue patternLanguage = createLiteral("fr");

        NodeValue returnedDate = parseDate.exec(text, pattern, patternLanguage);
        NodeValue expectedDate = getDateNode("2016-12-19");
        assertEquals(expectedDate, returnedDate);
    }

    @Test
    public void execReturnsTime_WithSeconds() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("09:10:10");
        NodeValue pattern = createLiteral("HH:m:s");
        NodeValue returnedTime = parseDate.exec(text, pattern, null);

        NodeValue expectedTime = getTimeNode("09:10:10");
        assertEquals(expectedTime, returnedTime);
    }

    @Test
    public void execReturnsTime_WithoutSeconds() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("23:59");
        NodeValue pattern = createLiteral("HH:m");

        NodeValue returnedTime = parseDate.exec(text, pattern, null);
        NodeValue expectedTime = getTimeNode("23:59:00");

        assertEquals(expectedTime, returnedTime);
    }

    @Test
    public void execReturnsTime_OnlyHours() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("15");
        NodeValue pattern = createLiteral("HH");

        NodeValue returnedTime = parseDate.exec(text, pattern, null);
        NodeValue expectedTime = getTimeNode("15:00:00");

        assertEquals(expectedTime, returnedTime);
    }


    @Test
    public void execReturnsDateTime_FrenchLocale() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("19/12/2016 12:08:56");
        NodeValue pattern = createLiteral("dd/MM/yyyy HH:mm:ss");
        NodeValue patternLanguage = createLiteral("fr");

        NodeValue returnedDateTime = parseDate.exec(text, pattern, patternLanguage);
        NodeValue expectedDateTime = getDateTimeNode("2016-12-19T12:08:56");

        assertEquals(expectedDateTime, returnedDateTime);
    }

    @Test
    public void execReturnsDateTime_complexPattern() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("2001.07.04 at 12:08:56 PDT");
        NodeValue pattern = createLiteral("yyyy.MM.dd 'at' HH:mm:ss z");

        NodeValue returnedDateTime = parseDate.exec(text, pattern, null);
        NodeValue expectedDateTime = getDateTimeNode("2001-07-04T12:08:56");

        assertEquals(expectedDateTime, returnedDateTime);
    }

    @Test
    public void execReturnsDateTime_nullPatternLanguage() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("2022.01.01 23:59:59");
        NodeValue pattern = createLiteral("yyyy.MM.dd HH:mm:ss");

        NodeValue returnedDateTime = parseDate.exec(text, pattern, null);
        NodeValue expectedDateTime = getDateTimeNode("2022-01-01T23:59:59");

        assertEquals(expectedDateTime, returnedDateTime);
    }

    @Test
    public void execReturnsTime_afterMidnight() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("00:35");
        NodeValue pattern = createLiteral("HH:mm");

        NodeValue returnedDateTime = parseDate.exec(text, pattern, null);
        NodeValue expectedDateTime = getTimeNode("00:35:00");

        assertEquals(expectedDateTime, returnedDateTime);
    }


    @Test
    public void execThrowsException_badInput() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("Lorem Ipsum");
        NodeValue pattern = createLiteral("yyyy.MM.dd");

        assertThrows(ParseException.class, () -> parseDate.exec(text, pattern, null));
    }

    @Test
    public void execThrowsException_nullInputText() {
        ParseDate parseDate = new ParseDate();
        NodeValue pattern = createLiteral("yyyy.MM.dd");
        NodeValue result = parseDate.exec(null, pattern, null);

        assertNull(result);
    }

    @Test
    public void execThrowsException_nullPattern() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("21/10/2013");

        NodeValue result = parseDate.exec(text,null, null);
        assertNull(result);
    }

    @Test
    public void execThrowsException_badPatternLanguage() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("19/12/2016");
        NodeValue pattern = createLiteral("dd/MM/yyyy");
        NodeValue patternLanguage = createLiteral("en");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parseDate.exec(text, pattern, patternLanguage)
        );

        String expectedMessage = "Pattern does not corresponds to the pattern language.";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void execReturnsDate_uriNode() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createLiteral("19/12/2016");
        NodeValue pattern = createURI("htttp://example.org/person");

        assertThrows(LiteralRequiredException.class,
                () -> parseDate.exec(text, pattern, null)
        );
    }

    @Test
    public void execReturnsDate_blankNode() {
        ParseDate parseDate = new ParseDate();
        NodeValue text = createBlankNode("blank node");
        NodeValue pattern = createLiteral("dd/MM/yy");

        assertThrows(
                LiteralRequiredException.class,
                () -> parseDate.exec(text, pattern, null)
        );
    }


    private NodeValue getDateNode(String date){
        return getNode(date, XSDDatatype.XSDdate);
    }
    private NodeValue getTimeNode(String time){
        return getNode(time, XSDDatatype.XSDtime);
    }
    private NodeValue getDateTimeNode(String dateTime){
        return getNode(dateTime, XSDDatatype.XSDdateTime);
    }

    private NodeValue getNode(String text, XSDDatatype type) {
        return NodeValue.makeNode(
                text,
                null,
                ((RDFDatatype) type).getURI()
        );
    }

    private NodeValue createURI(String uri){
        return NodeValue.makeNode(NodeFactory.createURI(uri));
    }

    private NodeValue createBlankNode(String id){
        return NodeValue.makeNode(NodeFactory.createBlankNode(id));
    }

    private NodeValue createLiteral(String text){
        return NodeValue.makeNode(text, null);
    }
}
