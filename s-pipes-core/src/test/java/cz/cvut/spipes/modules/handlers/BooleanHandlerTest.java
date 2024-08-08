package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class BooleanHandlerTest {

    private BooleanHandler booleanHandler;
    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<Boolean> mockSetter;
    private Property mockProperty;
    private Model model;

    @BeforeEach
    public void setUp() {
        mockResource = mock(Resource.class);
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        mockProperty = mock(Property.class);
        model = ModelFactory.createDefaultModel();

        booleanHandler = new BooleanHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    public void testSetValueByPropertyWithBooleanLiteral() {
        RDFNode booleanLiteralNode = model.createLiteral(String.valueOf(true));
        Statement mockStatement = mock(Statement.class);

        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(booleanLiteralNode);

        booleanHandler.setValueByProperty(mockProperty);

        verify(mockSetter).addValue(true);
    }

    @Test
    public void testSetValueByPropertyWithNonLiteralNode() {
        RDFNode nonLiteralNode = model.createResource();
        Statement mockStatement = mock(Statement.class);

        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(nonLiteralNode);

        booleanHandler.setValueByProperty(mockProperty);

        verify(mockSetter, never()).addValue(anyBoolean());
    }

    @Test
    public void testSetValueByPropertyWithNullNode() {
        when(mockResource.getProperty(mockProperty)).thenReturn(null);

        booleanHandler.setValueByProperty(mockProperty);

        verify(mockSetter, never()).addValue(anyBoolean());
    }
}
