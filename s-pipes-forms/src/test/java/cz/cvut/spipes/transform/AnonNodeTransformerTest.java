package cz.cvut.spipes.transform;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class AnonNodeTransformerTest {

    private static final String EX = "http://example.org/";
    private static final Property HAS_QUERY =  getExProperty("has-query");

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