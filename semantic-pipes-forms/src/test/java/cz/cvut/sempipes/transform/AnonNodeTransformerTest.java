package cz.cvut.sempipes.transform;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.topbraid.spin.system.SPINModuleRegistry;

public class AnonNodeTransformerTest {

    private static final String EX = "http://example.org/";
    private static final Property HAS_QUERY =  getExProperty("has-query");

    @Test
    public void serializeForAskWithoutTextReturnsTextRepresentation() {
        RDFNode node = getQueryNode("ask-constraint");
        String nodeStr = AnonNodeTransformer.serialize(node);

        assertTrue(nodeStr.contains("?descriptorType"));
        assertFalse(nodeStr.contains("spinrdf"));
    }

    @Test
    public void serializeForConstructWithTextRepresentationReturnsItsText() {
        RDFNode node = getQueryNode("construct-with-comments");
        String nodeStr = AnonNodeTransformer.serialize(node);

        assertTrue(nodeStr.contains("comment1"));
        assertTrue(nodeStr.contains("comment2"));
    }

    private RDFNode getQueryNode(String name) {
        return loadQueriesModel().getResource(EX + name).getProperty(HAS_QUERY).getObject();
    }

    private Model loadQueriesModel() {
        return ModelFactory.createDefaultModel().read(AnonNodeTransformerTest.class.getResource("/spin-queries.ttl").toString(), "", FileUtils.langTurtle);
    }

    private static Property getExProperty(final String localName) {
        return ResourceFactory.createProperty(EX + localName);
    }
}