package cz.cvut.spipes.transform;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class UtilTest {

    @Test
    public void extractModelTest() {
        OntModel m1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        m1.add(m1.createStatement(
                ResourceFactory.createResource("http://example.org/m1"),
                RDF.type,
                OWL.Ontology
        ));
        OntModel m2 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        m2.add(m1.createStatement(
                ResourceFactory.createResource("http://example.org/m2"),
                RDF.type,
                OWL.Ontology
        ));
        Statement s = m2.createStatement(
                ResourceFactory.createResource("http://example.org/s"),
                ResourceFactory.createProperty("http://example.org/p"),
                ResourceFactory.createResource("http://example.org/o")
        );
        m2.add(s);
        m1.addSubModel(m2);
        assertEquals(m2, new TransformerImpl().extractModel(s));
    }

    @Disabled
    @Test
    public void serializeIsIdempotent() {
        Model m = ModelFactory.createDefaultModel().read(getClass().getResource("/construct.ttl").getFile());
        RDFNode o1 = m.listStatements(m.createResource("http://onto.fel.cvut.cz/ontologies/test/example-construct"), m.createProperty("http://topbraid.org/sparqlmotionlib#constructQuery"), (RDFNode) null).next().getObject();
        RDFNode o2 = m.listStatements(m.createResource("http://onto.fel.cvut.cz/ontologies/test/example-construct"), m.createProperty("http://topbraid.org/sparqlmotionlib#constructQuery"), (RDFNode) null).next().getObject();
        assertTrue(o1.isAnon());
        assertTrue(o2.isAnon());
        assertEquals(AnonNodeTransformer.serialize(o1), AnonNodeTransformer.serialize(o1));
        assertEquals(AnonNodeTransformer.serialize(o2), AnonNodeTransformer.serialize(o2));
        assertEquals(AnonNodeTransformer.serialize(o1), AnonNodeTransformer.serialize(o2));
    }
}
