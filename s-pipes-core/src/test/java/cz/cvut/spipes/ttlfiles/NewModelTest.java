package cz.cvut.spipes.ttlfiles;

import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NewModelTest{
    private Model createSimpleModel() {
        Model model = ModelFactory.createDefaultModel();
        Resource subject = ResourceFactory.createResource("http://example.org/resorce/person");
        Property property = ResourceFactory.createProperty("http://example.org/property/hasName");
        Literal literal = ResourceFactory.createPlainLiteral("John");
        Statement statement = ResourceFactory.createStatement(subject,property,literal);
        model.add(statement);
        return model;
    }

    @Disabled
    @Test
    @DisplayName("Create model with 1 simple triple ")
    public void oneTripleTest(){
        Model model = createSimpleModel();
        System.out.println(model.getGraph());
    }

    @Test
    @DisplayName("Create More Complex Triple using namespaces")
    public void createMoreComplexTriple(){

        Model model = ModelFactory.createDefaultModel();
//        Resource john = ResourceFactory.createResource("http://example.com/resource/john"); //Doesnt work "org.apache.jena.rdf.model.HasNoModelException: http://example.com/resource/john"
//        model.createResource(john);
        Resource john = model.createResource("http://example.com/resource/john"); //Works
        john.addProperty(RDF.type, FOAF.Person);
        john.addProperty(FOAF.name,"John Brown");
        model.add(john,FOAF.age,"27");

        model.write(System.out,"JSON-LD");
    }
}
