package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ResourceHandlerTest {

    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<Resource> mockSetter;
    private ResourceHandler resourceHandler;

    @BeforeEach
    public void setUp() {
        mockResource = ModelFactory.createDefaultModel().createResource();
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        resourceHandler = new ResourceHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    public void testGetRDFNodeValue() {
        RDFNode mockNode = mock(RDFNode.class);
        Resource mockResource = ModelFactory.createDefaultModel().createResource("http://example.org/resource");
        when(mockNode.asResource()).thenReturn(mockResource);

        Resource result = resourceHandler.getRDFNodeValue(mockNode);
        assertEquals(mockResource, result);
    }

}
