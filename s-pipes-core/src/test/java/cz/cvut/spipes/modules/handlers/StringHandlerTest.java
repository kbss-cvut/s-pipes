package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class StringHandlerTest {

    private StringHandler stringHandler;
    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<String> mockSetter;
    private Property mockProperty;
    private Model model;

    @BeforeEach
    public void setUp() {
        mockResource = mock(Resource.class);
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        mockProperty = mock(Property.class);
        model = ModelFactory.createDefaultModel();

        stringHandler = new StringHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    public void testSetValueByPropertyWithStringLiteral() {
        RDFNode stringLiteralNode = model.createLiteral("exampleString");
        Statement mockStatement = mock(Statement.class);

        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(stringLiteralNode);

        stringHandler.setValueByProperty(mockProperty);

        verify(mockSetter).addValue("exampleString");
    }

    @Test
    public void testSetValueByPropertyWithResource() {
        RDFNode stringLiteralNode = ResourceFactory.createResource("http://example.com");
        Statement mockStatement = mock(Statement.class);

        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(stringLiteralNode);

        stringHandler.setValueByProperty(mockProperty);

        verify(mockSetter).addValue("http://example.com");
    }

    @Test
    public void testSetValueByPropertyWithNullNode() {
        when(mockResource.getProperty(mockProperty)).thenReturn(null);

        stringHandler.setValueByProperty(mockProperty);

        verify(mockSetter, never()).addValue(anyString());
    }
}
