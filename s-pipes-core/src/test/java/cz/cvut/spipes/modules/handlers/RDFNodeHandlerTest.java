package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class RDFNodeHandlerTest {

    private RDFNodeHandler rdfNodeHandler;
    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<RDFNode> mockSetter;
    private Property mockProperty;
    private Model model;

    @BeforeEach
    public void setUp() {
        mockResource = mock(Resource.class);
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        mockProperty = mock(Property.class);
        model = ModelFactory.createDefaultModel();

        rdfNodeHandler = new RDFNodeHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    public void testSetValueByPropertyWithNonNullRDFNode() {
        RDFNode rdfNode = model.createResource("http://example.org/resource");
        Statement mockStatement = mock(Statement.class);

        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(rdfNode);

        rdfNodeHandler.setValueByProperty(mockProperty);

        verify(mockSetter).addValue(rdfNode);
    }

    @Test
    public void testSetValueByPropertyWithNullRDFNode() {
        Statement mockStatement = mock(Statement.class);
        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(null);

        rdfNodeHandler.setValueByProperty(mockProperty);

        verify(mockSetter, never()).addValue(any(RDFNode.class));
    }

    @Test
    public void testSetValueByPropertyWithNoProperty() {
        when(mockResource.getProperty(mockProperty)).thenReturn(null);

        rdfNodeHandler.setValueByProperty(mockProperty);

        verify(mockSetter, never()).addValue(any(RDFNode.class));
    }
}
