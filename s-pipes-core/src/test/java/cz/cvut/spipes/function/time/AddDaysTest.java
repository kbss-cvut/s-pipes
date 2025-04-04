package cz.cvut.spipes.function.time;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddDaysTest {

    @Test
    public void execReturnsTimeFromPast() {

        AddDays addDays = new AddDays();
        NodeValue date = getDateNode("2022-01-01");
        NodeValue days = NodeValue.makeNodeDecimal("-1");

        NodeValue returnedDate = addDays.exec(date, days);

        NodeValue expectedDate = getDateNode("2021-12-31");
        assertEquals(expectedDate, returnedDate);
    }

    @Test
    public void execReturnsDatatypeOfInputLiteral() {
        NodeValue days = NodeValue.makeNodeDecimal("1");

        Stream.of(XSDDatatype.XSDdate, XSDDatatype.XSDstring).forEach(
            dt -> {
                NodeValue date = getNode("2021-12-31", dt);

                AddDays addDays = new AddDays();
                NodeValue returnedDate = addDays.exec(date, days);

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