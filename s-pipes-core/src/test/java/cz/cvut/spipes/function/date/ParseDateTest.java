package cz.cvut.spipes.function.date;

import cz.cvut.spipes.exception.ParseException;
import cz.cvut.spipes.function.spif.ParseDate;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import static org.apache.jena.graph.NodeFactory.createLiteral;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParseDateTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void execReturnsDate_ItalianLocale() {
        ParseDate parseDate = new ParseDate();
        Node text = createLiteral("02.11.2021");
        Node pattern = createLiteral("dd.MM.yyyy");
        Node patternLanguage = createLiteral("de");

        NodeValue returnedDate = parseDate.exec(text, pattern, patternLanguage, null);
        NodeValue expectedDate = getDateNode("2021-11-02");
        assertEquals(expectedDate, returnedDate);
    }

    @Test
    public void execReturnsDate_EnglishLocale() {
        ParseDate parseDate = new ParseDate();
        Node text = createLiteral("2010-09-21");
        Node pattern = createLiteral("yyyy-MM-dd");
        Node patternLanguage = createLiteral("de");

        NodeValue returnedDate = parseDate.exec(text, pattern, patternLanguage, null);
        NodeValue expectedDate = getDateNode("2010-09-21");
        assertEquals(expectedDate, returnedDate);
    }

    @Test
    public void execReturnsDate_FrenchLocale() {
        ParseDate parseDate = new ParseDate();
        Node text = createLiteral("19/12/2016");
        Node pattern = createLiteral("dd/MM/yyyy");
        Node patternLanguage = createLiteral("de");

        NodeValue returnedDate = parseDate.exec(text, pattern, patternLanguage, null);
        NodeValue expectedDate = getDateNode("2016-12-19");
        assertEquals(expectedDate, returnedDate);
    }

    @Test
    public void execReturnsTimeWithSeconds() {
        ParseDate parseDate = new ParseDate();
        Node text = createLiteral("09:10:10");
        Node pattern = createLiteral("k:m:s");
        Node patternLanguage = createLiteral("de");
        NodeValue returnedDate = parseDate.exec(text, pattern, patternLanguage, null);

        NodeValue expectedDate = getTimeNode("09:10:10");
        assertEquals(expectedDate, returnedDate);
    }

    @Test
    public void execReturnsTimeWithoutSeconds() {
        ParseDate parseDate = new ParseDate();
        Node text = createLiteral("23:59");
        Node pattern = createLiteral("k:m");
        Node patternLanguage = createLiteral("de");
        NodeValue returnedDate = parseDate.exec(text, pattern, patternLanguage, null);

        NodeValue expectedDate = getTimeNode("23:59");
        assertEquals(expectedDate, returnedDate);
    }

    @Test
    public void execReturnsDateTime() {
        ParseDate parseDate = new ParseDate();
        Node text = createLiteral("2001.07.04 12:08:56");
        Node pattern = createLiteral("yyyy.MM.dd HH:mm:ss");
        Node patternLanguage = createLiteral("en");
        NodeValue returnedDate = parseDate.exec(text, pattern, patternLanguage, null);

        NodeValue expectedDate = getDateTimeNode("2001-07-04T12:08:56");
        assertEquals(expectedDate, returnedDate);
    }

    @Test
    public void execReturnsDateTxime() {
        ParseDate parseDate = new ParseDate();
        Node text = createLiteral("2001.07.04 AD 12:08:56 PDT");
        Node pattern = createLiteral("yyyy.MM.dd G HH:mm:ss z");
        Node patternLanguage = createLiteral("dxxxe");
        NodeValue returnedDate = parseDate.exec(text, pattern, patternLanguage, null);

        NodeValue expectedDate = getDateTimeNode("2001-07-04T12:08:56");
        assertEquals(expectedDate, returnedDate);
    }


    @Test
    public void execThrowsException_badInput() {
        ParseDate parseDate = new ParseDate();
        Node text = createLiteral("2001.07.04 12:08:56exception");
        Node pattern = createLiteral("yyyy.MM.dd HH:mm:ss");
        Node patternLanguage = createLiteral("de");

        assertThrows(ParseException.class, () -> parseDate.exec(text, pattern, patternLanguage, null));
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
}
