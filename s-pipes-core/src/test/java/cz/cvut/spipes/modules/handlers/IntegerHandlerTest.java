package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class IntegerHandlerTest {

    private IntegerHandler integerHandler;
    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<Integer> mockSetter;
    private Property mockProperty;
    private Model model;

    @BeforeEach
    public void setUp() {
        mockResource = mock(Resource.class);
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        mockProperty = mock(Property.class);
        model = ModelFactory.createDefaultModel();

        integerHandler = new IntegerHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    public void testSetValueByPropertyWithIntegerLiteral() {
        RDFNode integerLiteralNode = model.createLiteral(String.valueOf(42));
        Statement mockStatement = mock(Statement.class);

        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(integerLiteralNode);

        integerHandler.setValueByProperty(mockProperty);

        verify(mockSetter).addValue(42);
    }

    @Test
    public void testSetValueByPropertyWithNonLiteralNode() {
        RDFNode nonLiteralNode = model.createResource();
        Statement mockStatement = mock(Statement.class);

        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(nonLiteralNode);

        integerHandler.setValueByProperty(mockProperty);

        verify(mockSetter, never()).addValue(anyInt());
    }

    @Test
    public void testSetValueByPropertyWithNullNode() {

        when(mockResource.getProperty(mockProperty)).thenReturn(null);

        integerHandler.setValueByProperty(mockProperty);

        verify(mockSetter, never()).addValue(anyInt());
    }
}
