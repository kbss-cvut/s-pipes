package cz.cvut.spipes.rest.handler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class XsdCoreDataTypesTest {

    @Test
    public void testConstants() {
        assertEquals("http://www.w3.org/2001/XMLSchema#string", XsdCoreDataTypes.XSD_STRING);
        assertEquals("http://www.w3.org/2001/XMLSchema#integer", XsdCoreDataTypes.XSD_INTEGER);
        assertEquals("http://www.w3.org/2001/XMLSchema#boolean", XsdCoreDataTypes.XSD_BOOLEAN);
        assertEquals("http://www.w3.org/2001/XMLSchema#decimal", XsdCoreDataTypes.XSD_DECIMAL);
        assertEquals("http://www.w3.org/2001/XMLSchema#double", XsdCoreDataTypes.XSD_DOUBLE);
    }

    @Test
    public void testCoreTypesList() {
        List<String> expectedCoreTypes = List.of(
                "http://www.w3.org/2001/XMLSchema#string",
                "http://www.w3.org/2001/XMLSchema#integer",
                "http://www.w3.org/2001/XMLSchema#boolean",
                "http://www.w3.org/2001/XMLSchema#decimal",
                "http://www.w3.org/2001/XMLSchema#double"
        );
        assertEquals(expectedCoreTypes, XsdCoreDataTypes.CORE_TYPES);
    }

    @Test
    public void testIsCorePrimitiveTypeWithValidTypes() {
        assertTrue(XsdCoreDataTypes.isCorePrimitiveType(XsdCoreDataTypes.XSD_STRING));
        assertTrue(XsdCoreDataTypes.isCorePrimitiveType(XsdCoreDataTypes.XSD_INTEGER));
        assertTrue(XsdCoreDataTypes.isCorePrimitiveType(XsdCoreDataTypes.XSD_BOOLEAN));
        assertTrue(XsdCoreDataTypes.isCorePrimitiveType(XsdCoreDataTypes.XSD_DECIMAL));
        assertTrue(XsdCoreDataTypes.isCorePrimitiveType(XsdCoreDataTypes.XSD_DOUBLE));
    }

    @Test
    public void testIsCorePrimitiveTypeWithInvalidTypes() {
        assertFalse(XsdCoreDataTypes.isCorePrimitiveType("http://www.w3.org/2001/XMLSchema#nonexistent"));
        assertFalse(XsdCoreDataTypes.isCorePrimitiveType("http://www.w3.org/2001/XMLSchema#date"));
        assertFalse(XsdCoreDataTypes.isCorePrimitiveType(""));
        assertFalse(XsdCoreDataTypes.isCorePrimitiveType(null));
    }

    @Test
    public void testIsCorePrimitiveTypeWithEdgeCases() {
        assertFalse(XsdCoreDataTypes.isCorePrimitiveType(""));
        assertFalse(XsdCoreDataTypes.isCorePrimitiveType(null));
    }
}
