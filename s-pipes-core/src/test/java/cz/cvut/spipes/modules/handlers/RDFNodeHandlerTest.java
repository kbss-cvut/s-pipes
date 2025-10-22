package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RDFNodeHandlerTest {

    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<RDFNode> mockSetter;
    private RDFNodeHandler rdfNodeHandler;

    @BeforeEach
    public void setUp() {
        mockResource = mock(Resource.class);
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        rdfNodeHandler = new RDFNodeHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    public void getRDFNodeValue() {
        RDFNode mockNode = mock(RDFNode.class);
        RDFNode result = rdfNodeHandler.getRDFNodeValue(mockNode);
        assertEquals(mockNode, result);
    }
}
