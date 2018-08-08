package cz.cvut.spipes.environment.generator;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;

public class OntologyGenerator {

    public static Model getSampleModel() {
        Model m = ModelFactory.createDefaultModel();
        m.add(
                ResourceFactory.createResource("http://example.org/mark-twain"),
                ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                ResourceFactory.createResource("http://xmlns.com/foaf/0.1/Person")

        );
        return m;
    }

    public static OntModel getSampleOntModel() {
        OntModel m = ModelFactory.createOntologyModel();
        m.add(
                ResourceFactory.createResource("http://example.org/mark-twain"),
                ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                ResourceFactory.createResource("http://xmlns.com/foaf/0.1/Person")

        );
        return m;
    }

    public static String getSampleOntologyUri() {
        return "http://example.org/ontology";
    }
}
