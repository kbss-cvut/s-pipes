package cz.cvut.spipes.function.time;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DurationTest {

    @Test
    public void execSimple() {

        Duration duration = new Duration();
        NodeValue startDateTime = getDateTimeNode("2023-08-21T13:00:00.100");
        NodeValue endDateTime = getDateTimeNode("2023-08-21T13:00:00.250");

        NodeValue durationInMS = duration.exec(startDateTime, endDateTime);
        long expected = 150;
        assertEquals(expected,durationInMS.getInteger().intValue());
    }

    @Test
    public void execStartDateTimeIsLaterThenEndDateTime() {

        Duration duration = new Duration();
        NodeValue startDateTime = getDateTimeNode("2023-08-21T13:20:00");
        NodeValue endDateTime = getDateTimeNode("2023-08-21T13:00:00");

        NodeValue durationInMS = duration.exec(startDateTime, endDateTime);
        long expected = -1200000;
        assertEquals(expected,durationInMS.getInteger().intValue());
    }

    @Test
    public void execTimeZoneFormat() {

        Duration duration = new Duration();
        NodeValue startDateTime = getDateTimeNode("2023-08-21T15:30:00.100-05:00");
        NodeValue endDateTime = getDateTimeNode("2023-08-21T14:30:05.600-06:00");

        NodeValue durationInMS = duration.exec(startDateTime, endDateTime);
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