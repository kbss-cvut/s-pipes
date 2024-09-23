package cz.cvut.spipes.rest.handler;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonLdCoreLiteralDatatypesTest {

    @Test
    public void testConstants() {
        assertEquals("http://www.w3.org/2001/XMLSchema#string", JsonLdCoreLiteralDatatypes.XSD_STRING);
        assertEquals("http://www.w3.org/2001/XMLSchema#integer", JsonLdCoreLiteralDatatypes.XSD_INTEGER);
        assertEquals("http://www.w3.org/2001/XMLSchema#boolean", JsonLdCoreLiteralDatatypes.XSD_BOOLEAN);
        assertEquals("http://www.w3.org/2001/XMLSchema#double", JsonLdCoreLiteralDatatypes.XSD_DOUBLE);
    }

    @Test
    public void testCoreTypesList() {
        List<String> expectedCoreTypes = List.of(
                "http://www.w3.org/2001/XMLSchema#string",
                "http://www.w3.org/2001/XMLSchema#integer",
                "http://www.w3.org/2001/XMLSchema#boolean",
                "http://www.w3.org/2001/XMLSchema#double"
        );
        assertEquals(expectedCoreTypes, JsonLdCoreLiteralDatatypes.CORE_TYPES);
    }

    @Test
    public void testIsCorePrimitiveTypeWithValidTypes() {
        assertTrue(JsonLdCoreLiteralDatatypes.isCorePrimitiveType(JsonLdCoreLiteralDatatypes.XSD_STRING));
        assertTrue(JsonLdCoreLiteralDatatypes.isCorePrimitiveType(JsonLdCoreLiteralDatatypes.XSD_INTEGER));
        assertTrue(JsonLdCoreLiteralDatatypes.isCorePrimitiveType(JsonLdCoreLiteralDatatypes.XSD_BOOLEAN));
        assertTrue(JsonLdCoreLiteralDatatypes.isCorePrimitiveType(JsonLdCoreLiteralDatatypes.XSD_DOUBLE));
    }

    @Test
    public void testIsCorePrimitiveTypeWithInvalidTypes() {
        assertFalse(JsonLdCoreLiteralDatatypes.isCorePrimitiveType("http://www.w3.org/2001/XMLSchema#nonexistent"));
        assertFalse(JsonLdCoreLiteralDatatypes.isCorePrimitiveType("http://www.w3.org/2001/XMLSchema#date"));
        assertFalse(JsonLdCoreLiteralDatatypes.isCorePrimitiveType(""));
        assertFalse(JsonLdCoreLiteralDatatypes.isCorePrimitiveType(null));
    }

    @Test
    public void testIsCorePrimitiveTypeWithEdgeCases() {
        assertFalse(JsonLdCoreLiteralDatatypes.isCorePrimitiveType(""));
        assertFalse(JsonLdCoreLiteralDatatypes.isCorePrimitiveType(null));
    }
}
