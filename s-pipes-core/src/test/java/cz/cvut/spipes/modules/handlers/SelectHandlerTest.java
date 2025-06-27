package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import cz.cvut.spipes.spin.model.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SelectHandlerTest {

    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<Select> mockSetter;
    private SelectHandler selectHandler;

    @BeforeEach
    public void setUp() {
        mockResource = ModelFactory.createDefaultModel().createResource();
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        selectHandler = new SelectHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    public void testGetRDFNodeValue() {
        RDFNode mockNode = mock(RDFNode.class);
        Resource mockResource = mock(Resource.class);
        Select mockSelect = mock(Select.class);

        when(mockNode.asResource()).thenReturn(mockResource);
        when(mockResource.as(Select.class)).thenReturn(mockSelect);

        Select result = selectHandler.getRDFNodeValue(mockNode);
        assertEquals(mockSelect, result);
    }

}
