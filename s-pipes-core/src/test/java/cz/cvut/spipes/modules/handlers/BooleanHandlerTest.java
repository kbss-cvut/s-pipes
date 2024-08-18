package cz.cvut.spipes.modules.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BooleanHandlerTest {

    private BooleanHandler booleanHandler;
    private Resource resource;
    private ExecutionContext executionContext;
    private Setter<Boolean> setter;
    private RDFNode rdfNode;

    @BeforeEach
    public void setUp() {
        resource = mock(Resource.class);
        executionContext = mock(ExecutionContext.class);
        setter = mock(Setter.class);
        booleanHandler = new BooleanHandler(resource, executionContext, setter);
    }

    @Test
    public void testGetJavaNativeValueTrue() {
        Literal literal = mock(Literal.class);
        when(literal.getBoolean()).thenReturn(true);
        rdfNode = mock(RDFNode.class);
        when(rdfNode.asLiteral()).thenReturn(literal);

        Boolean result = booleanHandler.getJavaNativeValue(rdfNode);
        assertTrue(result);
    }

    @Test
    public void testGetJavaNativeValueFalse() {
        Literal literal = mock(Literal.class);
        when(literal.getBoolean()).thenReturn(false);
        rdfNode = mock(RDFNode.class);
        when(rdfNode.asLiteral()).thenReturn(literal);

        Boolean result = booleanHandler.getJavaNativeValue(rdfNode);
        assertFalse(result);
    }

    @Test
    public void testGetJavaNativeValueInvalidType() {
        Literal literal = mock(Literal.class);
        when(literal.getBoolean()).thenThrow(new ClassCastException("Not a boolean"));
        rdfNode = mock(RDFNode.class);
        when(rdfNode.asLiteral()).thenReturn(literal);

        assertThrows(ClassCastException.class, () -> booleanHandler.getJavaNativeValue(rdfNode));
    }

    @Test
    public void testSetValueByPropertyNullNode() {
        Property property = mock(Property.class);
        when(booleanHandler.getEffectiveValue(property)).thenReturn(null);

        booleanHandler.setValueByProperty(property);

        verify(setter, never()).addValue(anyBoolean());
    }

}
