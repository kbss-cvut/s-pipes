package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class URLHandlerTest {

    private URLHandler urlHandler;
    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<URL> mockSetter;
    private Property mockProperty;
    private Model model;

    @BeforeEach
    public void setUp() {
        mockResource = mock(Resource.class);
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        mockProperty = mock(Property.class);
        model = ModelFactory.createDefaultModel();

        urlHandler = new URLHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    public void testSetValueByPropertyWithValidURL() throws MalformedURLException {
        RDFNode urlNode = model.createLiteral("http://example.com");
        Statement mockStatement = mock(Statement.class);

        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(urlNode);

        urlHandler.setValueByProperty(mockProperty);
        verify(mockSetter).addValue(new URL("http://example.com"));
    }

    @Test
    public void testSetValueByPropertyWithResource() {
        RDFNode urlNode = model.createResource("http://example.com");
        Statement mockStatement = mock(Statement.class);

        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(urlNode);

        try {
            urlHandler.setValueByProperty(mockProperty);
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof MalformedURLException);
        }
    }

    @Test
    public void testSetValueByPropertyWithNullNode() {
        Statement mockStatement = mock(Statement.class);
        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(null);

        urlHandler.setValueByProperty(mockProperty);

        verify(mockSetter, never()).addValue(any(URL.class));
    }
}
