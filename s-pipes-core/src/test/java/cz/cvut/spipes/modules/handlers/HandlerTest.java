package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HandlerTest {

    private Handler<Object> handler;
    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<Object> mockSetter;
    private Property mockProperty;
    private Model model;

    @BeforeEach
    public void setUp() {
        mockResource = mock(Resource.class);
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        mockProperty = mock(Property.class);
        model = ModelFactory.createDefaultModel();

        handler = new Handler<Object>(mockResource, mockExecutionContext, mockSetter) {
            @Override
            public void setValueByProperty(Property property) {

            }
        };
    }

    @Test
    public void testGetEffectiveValueWhenNotExpression() {
        RDFNode expectedNode = model.createLiteral("value");
        Statement mockStatement = mock(Statement.class);

        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(expectedNode);

        RDFNode result = handler.getEffectiveValue(mockProperty);

        assertEquals(expectedNode, result);
    }
}
