package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BooleanHandlerTest {

    private BooleanHandler booleanHandler;
    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<Boolean> mockSetter;

    @BeforeEach
    void setUp() {
        mockResource = mock(Resource.class);
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        booleanHandler = new BooleanHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    void getRDFNodeValueWhenTrue() {
        RDFNode rdfNode = ResourceFactory.createTypedLiteral(true);
        Boolean result = booleanHandler.getRDFNodeValue(rdfNode);
        assertTrue(result);
    }

    @Test
    void getRDFNodeValueWhenFalse() {
        RDFNode rdfNode = ResourceFactory.createTypedLiteral(false);
        Boolean result = booleanHandler.getRDFNodeValue(rdfNode);
        assertFalse(result);
    }

}
