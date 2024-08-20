package cz.cvut.spipes.modules.handlers;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.topbraid.spin.util.SPINExpressions;

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
        handler = Mockito.spy(new BaseRDFNodeHandler<String>(mockResource, mockExecutionContext, mockSetter) {
            @Override
            String getRDFNodeValue(RDFNode node) throws Exception {
                return node.asLiteral().getString();
            }
        });
    }

    @Test
    void testGetEffectiveValueWithString() {
        try(MockedStatic<SPINExpressions> mockedStatic = mockStatic(SPINExpressions.class)){
            var model = ModelFactory.createDefaultModel();

            RDFNode mockNode = model.createLiteral("test");
            Statement mockStatement = mock(Statement.class);
            when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
            when(mockStatement.getObject()).thenReturn(mockNode);
            mockedStatic.when(() -> SPINExpressions.isExpression(mockNode)).thenReturn(false);

            RDFNode result =handler.getEffectiveValue(mockProperty);
            assertEquals(mockNode, result);
        }
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
