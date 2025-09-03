package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ListHandlerTest {

    @Mock
    private Resource resource;

    @Mock
    private ExecutionContext executionContext;

    @Mock
    private Setter<List<?>> setter;

    @InjectMocks
    private ListHandler listHandler;

    private AutoCloseable mocks;

    static class SampleClass {
        List<String> listField;
    }

    @BeforeEach
    void setUp() {
        // Initializes the mocks and injects them into the @InjectMocks field
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void testGetStringListByProperty_withValidData() throws Exception {
        try (MockedStatic<HandlerRegistry> mockedStatic = mockStatic(HandlerRegistry.class)) {
            // Mock field
            Field field = SampleClass.class.getDeclaredField("listField");
            when(setter.getField()).thenReturn(field);

            Property mockProperty = mock(Property.class);
            Statement mockStatement1 = mock(Statement.class);
            Statement mockStatement2 = mock(Statement.class);
            RDFNode mockNode1 = mock(RDFNode.class);
            RDFNode mockNode2 = mock(RDFNode.class);

            // Mock StmtIterator
            StmtIterator mockIterator = mock(StmtIterator.class);
            when(mockIterator.toList()).thenReturn(List.of(mockStatement1, mockStatement2));

            // Mock the behavior of listProperties to return the mocked StmtIterator
            when(resource.listProperties(mockProperty)).thenReturn(mockIterator);

            // Mock statements' behavior
            when(mockStatement1.getObject()).thenReturn(mockNode1);
            when(mockStatement2.getObject()).thenReturn(mockNode2);

            // Mock handler registry and handler
            HandlerRegistry mockRegistry = mock(HandlerRegistry.class);

            // Use raw type for mocking Handler
            BaseRDFNodeHandler<String> mockHandler = mock(StringHandler.class);


            mockedStatic.when(HandlerRegistry::getInstance).thenReturn(mockRegistry);

            when(mockRegistry.getHandler(eq(String.class), any(Resource.class), any(ExecutionContext.class), any(Setter.class)))
                    .thenReturn(mockHandler);

            // Mocking RDF node value conversion
            when(mockHandler.getRDFNodeValue(mockNode1)).thenReturn("value1");
            when(mockHandler.getRDFNodeValue(mockNode2)).thenReturn("value2");

            // Call the method under test
            List<?> result = listHandler.getRDFNodeListByProperty(mockProperty);

            // Assert the results
            assertEquals(Arrays.asList("value1", "value2"), result);
        }
    }
}
