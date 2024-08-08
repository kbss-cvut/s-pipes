package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;

public class PathHandlerTest {

    private PathHandler pathHandler;
    private Resource mockResource;
    private ExecutionContext mockExecutionContext;
    private Setter<Path> mockSetter;
    private Property mockProperty;
    private Model model;

    @BeforeEach
    public void setUp() {
        mockResource = mock(Resource.class);
        mockExecutionContext = mock(ExecutionContext.class);
        mockSetter = mock(Setter.class);
        mockProperty = mock(Property.class);
        model = ModelFactory.createDefaultModel();

        pathHandler = new PathHandler(mockResource, mockExecutionContext, mockSetter);
    }

    @Test
    public void testSetValueByPropertyWithValidPath() {
        RDFNode pathNode = model.createLiteral("/some/path/to/file");
        Statement mockStatement = mock(Statement.class);

        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(pathNode);

        pathHandler.setValueByProperty(mockProperty);

        verify(mockSetter).addValue(Paths.get("/some/path/to/file"));
    }


    @Test
    public void testSetValueByPropertyWithNullNode() {
        Statement mockStatement = mock(Statement.class);

        when(mockResource.getProperty(mockProperty)).thenReturn(mockStatement);
        when(mockStatement.getObject()).thenReturn(null);

        pathHandler.setValueByProperty(mockProperty);

        verify(mockSetter, never()).addValue(any(Path.class));
    }
}
