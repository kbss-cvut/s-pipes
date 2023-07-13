package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.manager.OntologyDocumentManager;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RetrievePrefixesModuleTest {

    @Mock
    OntologyDocumentManager ontoDocManager;

    private final static String[] ontologyResourcePaths = new String[]{
        "/manager/import-closure/indirect-import.ttl",
        "/manager/import-closure/direct-import.ttl"
    };

    HashMap<String, OntModel> uri2ontModel;

    @BeforeEach
    void setUp() {
        uri2ontModel = new LinkedHashMap<>();
        for (String ontologyPath : ontologyResourcePaths) {
            OntModel model = loadOntModel(ontologyPath);
            String iri = getOntologyIri(model);
            uri2ontModel.put(
                iri,
                model
            );
        }
    }

    @Test
    void testExecuteSelf() {
        given(ontoDocManager.getRegisteredOntologyUris()).willReturn(uri2ontModel.keySet());
        uri2ontModel.forEach((key, value) -> given(ontoDocManager.getOntology(key)).willReturn(value));

        ExecutionContext inputExecutionContext = ExecutionContextFactory.createEmptyContext();

        RetrievePrefixesModule retrievePrefixesModule = new RetrievePrefixesModule();
        retrievePrefixesModule.setOntologyDocumentManager(ontoDocManager);
        retrievePrefixesModule.setInputContext(inputExecutionContext);
        ExecutionContext outputExecutionContext = retrievePrefixesModule.executeSelf();

        outputExecutionContext.getDefaultModel().write(System.out, FileUtils.langTurtle, null);
    }


    private static String getOntologyIri(OntModel model) {
        return model.listResourcesWithProperty(RDF.type, OWL.Ontology).nextResource().toString();
    }

    private static OntModel loadOntModel(String resourcePath) {
        InputStream is = RetrievePrefixesModuleTest.class.getResourceAsStream(resourcePath);

        if (is == null) {
            throw new IllegalArgumentException("Resource " + resourcePath + " not found.");
        }
        return loadOntModel(is);
    }

    private static OntModel loadOntModel(InputStream inputStream) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.setFileManager(FileManager.get());

        ontModel.read(inputStream, null, FileUtils.langTurtle);
        dm.loadImports(ontModel);
        return ontModel;
    }
}