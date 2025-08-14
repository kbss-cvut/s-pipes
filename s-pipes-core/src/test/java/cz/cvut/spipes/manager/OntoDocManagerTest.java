package cz.cvut.spipes.manager;

import cz.cvut.spipes.TestConstants;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.util.LocationMapper;
import org.apache.jena.util.ResourceUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class OntoDocManagerTest {

    static Path managerDirPath = TestConstants.TEST_RESOURCES_DIR_PATH.resolve("manager").toAbsolutePath();

    @Test
    public void registerDocumentsProcessDirectoryRecursively()  {

        OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();

        int initialEntriesCount = getLocationMapperEntriesCount(ontoDocManager);

        ontoDocManager.registerDocuments(managerDirPath.resolve("recursive-discovery"));

        assertEquals(initialEntriesCount + 5, getLocationMapperEntriesCount(ontoDocManager));
    }

    @Disabled //TODO does not work in jenkins if project dir contains " "
    @Test
    public void registerDocumentsForAllSupportedFormats()  {

        OntologyDocumentManager ontoDocManager = null;

        List<String> supportedFileExtensions = OntoDocManager.getInstance().getSupportedFileExtensions();

        for (String ext : supportedFileExtensions) {

            ontoDocManager = OntoDocManager.getInstance();

            assertEquals(0, getLocationMapperEntriesCount(ontoDocManager));

            ontoDocManager.registerDocuments(managerDirPath.resolve("supported-formats").resolve("loading-test." + ext));
            OntModel model = ontoDocManager.getOntDocumentManager().getOntology(
                  "http://onto.fel.cvut.cz/ontologies/test/loading-test",
                  OntModelSpec.OWL_MEM);

            assertEquals(1, getLocationMapperEntriesCount(ontoDocManager));
            assertEquals(1, model.listClasses().toList().size());

        }
    }

    @Disabled //TODO does not work in jenkins if project dir contains " "
    @Test
    public void registerDocumentsToLoadImportClosure() {

        OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();

        ontoDocManager.registerDocuments(managerDirPath.resolve("import-closure"));

        OntModel model = ontoDocManager.getOntDocumentManager().getOntology("http://onto.fel.cvut.cz/ontologies/test/loading-test", OntModelSpec.OWL_MEM);

        model.loadImports();

        List<String> loadedClassNames = model.listClasses().mapWith(c -> c.asResource().getLocalName()).toList();

        assertTrue(loadedClassNames.contains("LoadedTestClass"));
        assertTrue(loadedClassNames.contains("DirectImportTestClass"));
        assertTrue(loadedClassNames.contains("IndirectImportTestClass"));
        assertEquals(3, loadedClassNames.size());
    }

    @Disabled
    @Test //TODO move to a "jena experiment project"
    public void registerDocumentsToLoadImportClosure2() {

        OntDocumentManager docManager = OntDocumentManager.getInstance();


        String dirPath = "file:///home/blcha/projects/kbss/git/s-pipes/s-pipes-core/src/test/resources/manager/import-closure";

        docManager.addAltEntry("http://onto.fel.cvut.cz/ontologies/test/direct-import-test", dirPath + "/" + "direct-import.ttl");
        docManager.addAltEntry("http://onto.fel.cvut.cz/ontologies/test/indirect-import-test", dirPath + "/" + "indirect-import.ttl");
        docManager.addAltEntry("http://onto.fel.cvut.cz/ontologies/test/loading-test", dirPath + "/" + "loading-test.ttl");

        OntModel model = docManager.getOntology("http://onto.fel.cvut.cz/ontologies/test/loading-test", OntModelSpec.OWL_MEM);

        model.loadImports();

        List<String> loadedClassNames = model.listClasses().mapWith(c -> c.asResource().getLocalName()).toList();

        assertTrue(loadedClassNames.contains("LoadedTestClass"));
        assertTrue(loadedClassNames.contains("DirectImportTestClass"));
        assertTrue(loadedClassNames.contains("IndirectImportTestClass"));
        assertEquals(3, loadedClassNames.size());
    }

    @Test //TODO move to a "jena experiment project"
    public void jenaOntDocumentManagerLoadsDocumentChanges() throws IOException {

        OntDocumentManager docManager = OntDocumentManager.getInstance();
        String testOntologyPrefix = "http://onto.fel.cvut.cz/ontologies/test/";

        Path sourceDirPath = managerDirPath.resolve("file-update");
        List<String> ontologyNames = Arrays.asList(
            "directly-imported",
            "indirectly-imported",
            "loading-test"
        );

        String loadingTestOntologyUri = testOntologyPrefix + "loading-test-ontology";
        String indirectlyImportedOntologyUri = testOntologyPrefix + "indirectly-imported-ontology";

        ontologyNames.forEach( on -> {
            docManager.addAltEntry(
                testOntologyPrefix + on + "-ontology",
                String.valueOf(sourceDirPath.resolve(on + ".ttl").toUri().toURL())
            );
        });

        OntModel model = docManager.getOntology(loadingTestOntologyUri, OntModelSpec.OWL_MEM);

        assertTrue(containClassWithLocalName(model, "directly-imported-class"));
        assertTrue(containClassWithLocalName(model,"indirectly-imported-class"));
        assertTrue(containClassWithLocalName(model,"loading-test-class"));

        assertEquals(3, getOntologyClassesLocalNames(model).size());

        OntModel indirectlyImportedModel = docManager.getOntology(indirectlyImportedOntologyUri, OntModelSpec.OWL_MEM);

        OntClass resource = indirectlyImportedModel
            .listClasses()
            .filterKeep(r -> r.isResource() && r.asResource().getLocalName().equals("indirectly-imported-class"))
            .next();
        ResourceUtils.renameResource(resource, resource.getNameSpace() + "new-indirectly-imported-class");

        // check locally modified model
        assertFalse(containClassWithLocalName(indirectlyImportedModel,"indirectly-imported-class"));
        assertTrue(containClassWithLocalName(indirectlyImportedModel,"new-indirectly-imported-class"));
        assertEquals(1, getOntologyClassesLocalNames(indirectlyImportedModel).size());

        // update the model globally
        docManager.getFileManager().addCacheModel(indirectlyImportedOntologyUri, indirectlyImportedModel);

        // check new model is updated
        assertTrue(containClassWithLocalName(model, "directly-imported-class"));
        assertTrue(containClassWithLocalName(model,"new-indirectly-imported-class"));
        assertTrue(containClassWithLocalName(model,"loading-test-class"));
        assertEquals(3, getOntologyClassesLocalNames(model).size());
    }

    private List<String> getOntologyClassesLocalNames(OntModel model) {
        return model.listClasses().mapWith(c -> c.asResource().getLocalName()).toList();
    }

    private boolean containClassWithLocalName(OntModel model, String localName) {
        return getOntologyClassesLocalNames(model).contains(localName);
    }



    private int getLocationMapperEntriesCount(OntologyDocumentManager ontoDocManager) {
        return getLocationMapperEntriesCount(ontoDocManager.getOntDocumentManager().getFileManager().getLocationMapper());
    }

    private int getLocationMapperEntriesCount(LocationMapper locationMapper) {
        final int[] entriesCount = {0};
        locationMapper.listAltEntries().forEachRemaining(e -> { entriesCount[0]++; } );

        return entriesCount[0];
    }



}