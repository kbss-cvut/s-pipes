package cz.cvut.spipes.function.time;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddDaysTest {

    @Test
    public void execReturnsTimeFromPast() {

        AddDays addDays = new AddDays();
        Node date = getDateNode("2022-01-01").asNode();
        Node days = NodeValue.makeNodeDecimal("-1").asNode();

        NodeValue returnedDate = addDays.exec(date, days, null);

        NodeValue expectedDate = getDateNode("2021-12-31");
        assertEquals(expectedDate, returnedDate);
    }

    @Test
    public void execReturnsDatatypeOfInputLiteral() {
        Node days = NodeValue.makeNodeDecimal("1").asNode();

        Stream.of(XSDDatatype.XSDdate, XSDDatatype.XSDstring).forEach(
            dt -> {
                Node date = getNode("2021-12-31", dt).asNode();

                AddDays addDays = new AddDays();
                NodeValue returnedDate = addDays.exec(date, days, null);

                NodeValue expectedDate = getNode("2022-01-01", dt);
                assertEquals(expectedDate, returnedDate);
            });
    }


    private NodeValue getDateNode(String date){
        return getNode(date, XSDDatatype.XSDdate);
    }

    private NodeValue getNode(String date, RDFDatatype datatype) {
        return NodeValue.makeNode(
            date,
            null,
            datatype.getURI()
        );
    }
}