package cz.cvut.spipes.modules.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class ListHandlerTest {

    private ListHandler listHandler;
    private Resource resource;
    private ExecutionContext executionContext;
    private ListSetter setter;

    @BeforeEach
    public void setUp() {
        resource = mock(Resource.class);
        executionContext = mock(ExecutionContext.class);
        setter = mock(ListSetter.class);
        listHandler = new ListHandler(resource, executionContext, setter);
    }

    @Test
    public void testGetRDFNodeListByProperty() {
        Property property = mock(Property.class);
        Statement statement1 = mock(Statement.class);
        Statement statement2 = mock(Statement.class);
        RDFNode node1 = mock(RDFNode.class);
        RDFNode node2 = mock(RDFNode.class);
        Resource resource1 = mock(Resource.class);
        Resource resource2 = mock(Resource.class);

        when(statement1.getObject()).thenReturn(node1);
        when(statement2.getObject()).thenReturn(node2);
        when(node1.asResource()).thenReturn(resource1);
        when(node2.asResource()).thenReturn(resource2);

        StmtIterator stmtIterator = mock(StmtIterator.class);
        when(stmtIterator.toList()).thenReturn(Arrays.asList(statement1, statement2));
        when(resource.listProperties(property)).thenReturn(stmtIterator);

        List<RDFNode> result = listHandler.getRDFNodeListByProperty(property);
        List<RDFNode> expectedList = Arrays.asList(resource1, resource2);

        assertEquals(expectedList, result);
    }

    @Test
    public void testSetValueByProperty() {
        Property property = mock(Property.class);
        RDFNode node1 = mock(RDFNode.class);
        RDFNode node2 = mock(RDFNode.class);
        Resource resource1 = mock(Resource.class);
        Resource resource2 = mock(Resource.class);
        Statement statement1 = mock(Statement.class);
        Statement statement2 = mock(Statement.class);

        when(statement1.getObject()).thenReturn(node1);
        when(statement2.getObject()).thenReturn(node2);
        when(node1.asResource()).thenReturn(resource1);
        when(node2.asResource()).thenReturn(resource2);

        StmtIterator stmtIterator = mock(StmtIterator.class);
        when(stmtIterator.toList()).thenReturn(Arrays.asList(statement1, statement2));
        when(resource.listProperties(property)).thenReturn(stmtIterator);

        listHandler.setValueByProperty(property);

        List<RDFNode> expectedList = Arrays.asList(resource1, resource2);
        verify(setter).addValue(expectedList);
    }

    @Test
    public void testGetRDFNodeListByPropertyEmpty() {
        Property property = mock(Property.class);
        StmtIterator stmtIterator = mock(StmtIterator.class);
        when(stmtIterator.toList()).thenReturn(Arrays.asList());
        when(resource.listProperties(property)).thenReturn(stmtIterator);

        List<RDFNode> result = listHandler.getRDFNodeListByProperty(property);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetRDFNodeListByPropertyNoStatements() {
        Property property = mock(Property.class);
        when(resource.listProperties(property)).thenReturn(mock(StmtIterator.class));

        List<RDFNode> result = listHandler.getRDFNodeListByProperty(property);
        assertTrue(result.isEmpty());
    }
}
