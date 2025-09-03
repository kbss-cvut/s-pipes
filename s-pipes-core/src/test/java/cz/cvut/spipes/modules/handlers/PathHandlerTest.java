package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PathHandlerTest {

    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<Path> mockSetter;
    private PathHandler pathHandler;

    @BeforeEach
    public void setUp() {
        mockResource = ModelFactory.createDefaultModel().createResource();
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        pathHandler = new PathHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    public void testGetRDFNodeValue() {
        RDFNode mockNode = mock(RDFNode.class);
        when(mockNode.toString()).thenReturn("/example/path/to/file");

        Path result = pathHandler.getRDFNodeValue(mockNode);
        assertEquals(Paths.get("/example/path/to/file"), result);
    }

}
