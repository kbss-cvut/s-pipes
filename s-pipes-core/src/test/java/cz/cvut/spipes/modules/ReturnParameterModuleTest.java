package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReturnParameterModuleTest {

    @Test
    void executeSelf() {
        ReturnParameterModule module = new ReturnParameterModule();
        module.setInputContext(ExecutionContextFactory.createContext(createSimpleModel()));
        ExecutionContext newContext = module.executeSelf();
        System.out.println(newContext.getDefaultModel().listStatements().toList());
    }

    @Test
    void getTypeURI() {
        ReturnParameterModule module = new ReturnParameterModule();
        module.setInputContext(ExecutionContextFactory.createContext(createSimpleModel()));
        System.out.println(module.getTypeURI());

    }

    public Model createSimpleModel(){

        Model model = ModelFactory.createDefaultModel();
        Resource john = model.createResource("http://example.com/resource/john");
        john.addProperty(RDF.type, FOAF.Person);
        john.addProperty(FOAF.name,"John Brown");
        model.add(john,FOAF.age,"27");
        return model;
    }

}