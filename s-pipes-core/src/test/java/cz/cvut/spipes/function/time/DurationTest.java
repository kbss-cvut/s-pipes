package cz.cvut.spipes.function.time;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DurationTest {

    @Test
    public void execSimple() {

        Duration duration = new Duration();
        Node t1 = getDateTimeNode("2023-08-21T13:00:00.100").asNode();
        Node t2 = getDateTimeNode("2023-08-21T13:00:00.250").asNode();

        NodeValue durationInMS = duration.exec(t2,t1,null);
        long expected = 150;
        assertEquals(expected,durationInMS.getInteger().intValue());
    }

    @Test
    public void execT2IsEarlierThenT1() {

        Duration duration = new Duration();
        Node t1 = getDateTimeNode("2023-08-21T13:20:00").asNode();
        Node t2 = getDateTimeNode("2023-08-21T13:00:00").asNode();

        NodeValue durationInMS = duration.exec(t2,t1,null);
        long expected = -1200000;
        assertEquals(expected,durationInMS.getInteger().intValue());
    }

    @Test
    public void execTimeZoneFormat() {

        Duration duration = new Duration();
        Node t1 = getDateTimeNode("2023-08-21T15:30:00.100-05:00").asNode();
        Node t2 = getDateTimeNode("2023-08-21T14:30:05.600-06:00").asNode();

        NodeValue durationInMS = duration.exec(t2,t1,null);
        long expected = 5500;
        assertEquals(expected,durationInMS.getInteger().intValue());
    }

    private NodeValue getDateTimeNode(String dateTime){
        return getNode(dateTime, XSDDatatype.XSDdateTime);
    }

    private NodeValue getNode(String time, RDFDatatype datatype) {
        return NodeValue.makeNode(
                time,
                null,
                datatype.getURI()
        );
    }
}