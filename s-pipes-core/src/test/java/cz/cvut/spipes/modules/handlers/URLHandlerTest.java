package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class URLHandlerTest {
    private URLHandler urlHandler;
    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<URL> mockSetter;

    @BeforeEach
    void setUp() {
        mockResource = Mockito.mock(Resource.class);
        mockExecutionContext = Mockito.mock(ExecutionContext.class);
        mockSetter = Mockito.mock(Setter.class);
        urlHandler = new URLHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    void getRDFNodeValueWhenValidURL() throws MalformedURLException {

        RDFNode node = ResourceFactory.createPlainLiteral("http://example.com");

        URL result = urlHandler.getRDFNodeValue(node);

        assertEquals(new URL("http://example.com"), result);
    }

    @Test
    void getRDFNodeValueWhenInvalidURLThrowsRuntimeError() {

        RDFNode node = ResourceFactory.createPlainLiteral("invalid-url");

        assertThrows(IllegalArgumentException.class, () -> urlHandler.getRDFNodeValue(node));
    }

    @Test
    void getRDFNodeValueWhenNullNodeThrowsNullPointerException() {

        RDFNode node = null;

        assertThrows(NullPointerException.class, () -> urlHandler.getRDFNodeValue(node));
    }
}
