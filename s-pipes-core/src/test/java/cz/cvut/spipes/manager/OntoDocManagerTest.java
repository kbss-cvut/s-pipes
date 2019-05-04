package cz.cvut.spipes.manager;

import cz.cvut.spipes.test.TestConstants;
import java.nio.file.Path;
import java.util.List;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.util.LocationMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class OntoDocManagerTest {


    static Path managerDirPath = TestConstants.TEST_RESOURCES_DIR_PATH.resolve("manager").toAbsolutePath();


    @Disabled
    @Test
    public void registerDocumentsDirectoryRecursive()  {

        OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();

        LocationMapper lm = ontoDocManager.getOntDocumentManager().getFileManager().getLocationMapper();


        assertEquals(0, getLocationMapperEntriesCount(lm));

        ontoDocManager.registerDocuments(managerDirPath.resolve("recursive-discovery"));

        assertEquals(4, getLocationMapperEntriesCount(lm));
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



    private int getLocationMapperEntriesCount(OntologyDocumentManager ontoDocManager) {
        return getLocationMapperEntriesCount(ontoDocManager.getOntDocumentManager().getFileManager().getLocationMapper());
    }

    private int getLocationMapperEntriesCount(LocationMapper locationMapper) {
        final int[] entriesCount = {0};
        locationMapper.listAltEntries().forEachRemaining(e -> { entriesCount[0]++; } );

        return entriesCount[0];
    }



}