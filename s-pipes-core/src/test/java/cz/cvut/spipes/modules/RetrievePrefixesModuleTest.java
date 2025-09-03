package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.manager.OntologyDocumentManager;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

import static cz.cvut.spipes.test.JenaTestUtils.assertIsomorphic;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class RetrievePrefixesModuleTest {

    @Mock
    OntologyDocumentManager ontoDocManager;

    private static final Logger LOG = LoggerFactory.getLogger(RetrievePrefixesModuleTest.class);

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
    void executeSelfReturnPrefixes() throws URISyntaxException {
        given(ontoDocManager.getRegisteredOntologyUris()).willReturn(uri2ontModel.keySet());
        uri2ontModel.forEach((key, value) -> doReturn(value).when(ontoDocManager).getOntology(key));

        ExecutionContext inputExecutionContext = ExecutionContextFactory.createEmptyContext();

        RetrievePrefixesModule retrievePrefixesModule = new RetrievePrefixesModule();
        retrievePrefixesModule.setOntologyDocumentManager(ontoDocManager);
        retrievePrefixesModule.setInputContext(inputExecutionContext);
        ExecutionContext outputExecutionContext = retrievePrefixesModule.executeSelf();

        Model actualModel = outputExecutionContext.getDefaultModel();

        Model expectedModel = ModelFactory.createDefaultModel()
                .read(getFilePath("module/retrieve-prefixes/expected-output.ttl").toString());

        assertIsomorphic(actualModel, expectedModel);
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
        dm.setFileManager(FileManager.getInternal());

        ontModel.read(inputStream, null, FileUtils.langTurtle);
        dm.loadImports(ontModel);
        return ontModel;
    }


    public Path getFilePath(String fileName) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(getClass().getResource("/" + fileName)).toURI());
    }
}