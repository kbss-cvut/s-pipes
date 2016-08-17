package cz.cvut.sempipes.manager;

import cz.cvut.sempipes.TestConstants;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.apache.jena.util.LocationMapper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

/**
 * Created by Miroslav Blasko on 22.7.16.
 */
public class OntoDocManagerTest {


    static Path managerDirPath = TestConstants.TEST_RESOURCES_DIR_PATH.resolve("manager").toAbsolutePath();


    @Ignore
    @Test
    public void registerDocumentsDirectoryRecursive()  {

        OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();

        LocationMapper lm = ontoDocManager.getOntDocumentManager().getFileManager().getLocationMapper();


        assertEquals(0, getLocationMapperEntriesCount(lm));

        ontoDocManager.registerDocuments(managerDirPath.resolve("recursive-discovery"));

        assertEquals(4, getLocationMapperEntriesCount(lm));
    }

    @Test
    public void registerDocumentsForAllSupportedFormats()  {

        OntologyDocumentManager ontoDocManager = null;

        List<String> supportedFileExtensions = OntoDocManager.getInstance().getSupportedFileExtensions();

        for (String ext : supportedFileExtensions) {

            ontoDocManager = new OntoDocManager();

            assertEquals(0, getLocationMapperEntriesCount(ontoDocManager));

            ontoDocManager.registerDocuments(managerDirPath.resolve("supported-formats").resolve("loading-test." + ext));
            OntModel model = ontoDocManager.getOntDocumentManager().getOntology(
                  "http://onto.fel.cvut.cz/ontologies/test/loading-test",
                  OntModelSpec.OWL_MEM);

            assertEquals(1, getLocationMapperEntriesCount(ontoDocManager));
            assertEquals(1, model.listClasses().toList().size());

        }
    }

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

    private int getLocationMapperEntriesCount(OntologyDocumentManager ontoDocManager) {
        return getLocationMapperEntriesCount(ontoDocManager.getOntDocumentManager().getFileManager().getLocationMapper());
    }

    private int getLocationMapperEntriesCount(LocationMapper locationMapper) {
        final int[] entriesCount = {0};
        locationMapper.listAltEntries().forEachRemaining(e -> { entriesCount[0]++; } );

        return entriesCount[0];
    }



}