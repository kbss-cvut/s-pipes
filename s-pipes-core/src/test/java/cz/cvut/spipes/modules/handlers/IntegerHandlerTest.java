package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class IntegerHandlerTest {

    private IntegerHandler integerHandler;
    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<Integer> mockSetter;

    @BeforeEach
    void setUp() {
        mockResource = mock(Resource.class);
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        integerHandler = new IntegerHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    void getRDFNodeValue() {
        RDFNode rdfNode = ResourceFactory.createTypedLiteral(42);

        Integer result = integerHandler.getRDFNodeValue(rdfNode);

        assertEquals(Integer.valueOf(42), result);
    }
}
