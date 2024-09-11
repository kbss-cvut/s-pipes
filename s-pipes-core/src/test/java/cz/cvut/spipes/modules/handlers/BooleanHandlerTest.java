package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.exception.ScriptRuntimeErrorException;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

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
    void testGetRDFNodeValueTrue() {
        RDFNode rdfNode = ResourceFactory.createTypedLiteral(true);
        Boolean result = booleanHandler.getRDFNodeValue(rdfNode);
        assertTrue(result);
    }

    @Test
    void testGetRDFNodeValueFalse() {
        RDFNode rdfNode = ResourceFactory.createTypedLiteral(false);
        Boolean result = booleanHandler.getRDFNodeValue(rdfNode);
        assertFalse(result);
    }

}
