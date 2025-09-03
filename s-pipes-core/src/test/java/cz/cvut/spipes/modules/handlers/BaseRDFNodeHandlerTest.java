package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.VariablesBinding;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseRDFNodeHandlerTest {

    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<String> mockSetter;
    private BaseRDFNodeHandler<String> handler;
    private Property mockProperty;

    @BeforeEach
    void setUp() {
        mockResource = mock(Resource.class);
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        mockProperty = mock(Property.class);
        handler = new BaseRDFNodeHandler<>(mockResource, mockExecutionContext, mockSetter) {
            @Override
            String getRDFNodeValue(RDFNode node) {
                return node.asLiteral().getString();
            }
        };
    }

    @Test
    void testGetEffectiveValueWithString() {
            var model = ModelFactory.createDefaultModel();

            RDFNode mockNode = model.createLiteral("test");
            Statement mockStatement = mock(Statement.class);
            when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
            when(mockExecutionContext.getVariablesBinding()).thenReturn(new VariablesBinding());
            when(mockStatement.getObject()).thenReturn(mockNode);

            RDFNode result = handler.getEffectiveValue(mockProperty);
            assertEquals(mockNode, result);
    }

    @Test
    void testHasParameterValueAssignment_Assigned() {
        Statement mockStatement = mock(Statement.class);
        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);

        boolean result = handler.hasParameterValueAssignment(mockProperty);

        assertTrue(result);
    }

    @Test
    void testHasParameterValueAssignment_NotAssigned() {
        when(mockResource.getProperty(mockProperty)).thenReturn(null);

        boolean result = handler.hasParameterValueAssignment(mockProperty);

        assertFalse(result);
    }
}
