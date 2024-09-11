package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class StringHandlerTest {

    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<String> mockSetter;
    private StringHandler stringHandler;

    @BeforeEach
    public void setUp() {
        mockResource = ModelFactory.createDefaultModel().createResource();
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        stringHandler = new StringHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    public void testGetRDFNodeValue() {
        RDFNode mockNode = mock(RDFNode.class);
        String expectedString = "testString";
        when(mockNode.toString()).thenReturn(expectedString);

        String result = stringHandler.getRDFNodeValue(mockNode);
        assertEquals(expectedString, result);
    }

}
