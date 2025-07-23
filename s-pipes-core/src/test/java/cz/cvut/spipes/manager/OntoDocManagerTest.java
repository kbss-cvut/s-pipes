package cz.cvut.spipes.manager;

import cz.cvut.spipes.TestConstants;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.util.LocationMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OntoDocManagerTest {


    static Path managerDirPath = TestConstants.TEST_RESOURCES_DIR_PATH.resolve("manager").toAbsolutePath();

    @Test
    public void registerDocumentsProcessDirectoryRecursively()  {

        OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();

        int initialEntriesCount = getLocationMapperEntriesCount(ontoDocManager);

        ontoDocManager.registerDocuments(Arrays.asList(managerDirPath.resolve("recursive-discovery")));

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



    private int getLocationMapperEntriesCount(OntologyDocumentManager ontoDocManager) {
        return getLocationMapperEntriesCount(ontoDocManager.getOntDocumentManager().getFileManager().getLocationMapper());
    }

    private int getLocationMapperEntriesCount(LocationMapper locationMapper) {
        final int[] entriesCount = {0};
        locationMapper.listAltEntries().forEachRemaining(e -> { entriesCount[0]++; } );

        return entriesCount[0];
    }

    @Test
    void getScriptFilesReturnsCorrectFiles(){
        List<String> requiredResources = Stream.of("/pipeline/config.ttl", "/sample/sample.ttl").toList();
        List<String> localPaths = requiredResources.stream().map(r -> r.replaceFirst("/[^/]+/", "")).toList();
        List<File> requiredFiles = requiredResources.stream()
                .map(this::resourcePathAsFile)
                .toList();
        List<String> requiredFilesStr = requiredFiles.stream()
                .map(f -> f.toString().replaceFirst("^([^/\\\\])", "/$1")) // windows prepend forward slash
                .map(s -> s.replaceAll("\\\\","/")) // windows repelace '\'  with '/'. Test works without this.
                .toList();
        List<String> uris = Arrays.asList(
                "http://onto.fel.cvut.cz/ontologies/s-pipes/test/pipeline-config",
                "http://onto.fel.cvut.cz/ontologies/test/sample/config"
        );

        List<File> missingRequiredFiles = requiredFiles.stream()
                .filter(f -> !f.exists())
                .toList();

        assertTrue(
                requiredFiles.size() == requiredFiles.size(),
                () -> "Missing test required resource [%s]".formatted(
                        missingRequiredFiles.stream()
                                .map(f -> "\"%s\"".formatted(f.getAbsolutePath()))
                                .collect(Collectors.joining(", "))
                )
        );

        // set up the location map
        for(int i = 0; i < requiredFiles.size(); i++) {
            OntDocumentManager.getInstance().addAltEntry(uris.get(i), requiredFiles.get(i).toString());
        }

        List<String> scriptPaths = Stream.of("/pipeline", "/sample")
                .map(s -> resourcePathAsFile(s).toString())
                .toList();


//        http://onto.fel.cvut.cz/ontologies/s-pipes/test/pipeline-config, /pipeline/config.ttl
//        http://onto.fel.cvut.cz/ontologies/test/sample/config, /sample/sample.ttl

        test(uris.get(0), scriptPaths, 1); // ontology iri
        test(uris.get(1), scriptPaths, 1); // ontology iri
        test(requiredFilesStr.get(0), scriptPaths, 1); // absolute path
        test(requiredFilesStr.get(1), scriptPaths, 1); // absolute path
        test("file:" + requiredFilesStr.get(0), scriptPaths, 1); // absolute path
        test("file:" + requiredFilesStr.get(1), scriptPaths, 1); // absolute path
        test("file://" + requiredFilesStr.get(0), scriptPaths, 1); // absolute path
        test("file://" + requiredFilesStr.get(1), scriptPaths, 1); // absolute path
        test(localPaths.get(0), scriptPaths, 1); // relative path
        test(localPaths.get(1), scriptPaths, 1); // relative path
        test("./" + localPaths.get(0), scriptPaths, 1); // relative path
        test("./" + localPaths.get(1), scriptPaths, 1); // relative path
        test("../pipeline/" + localPaths.get(0), scriptPaths, 1); // relative path
        test("../sample/" + localPaths.get(1), scriptPaths, 1); // relative path
    }

    protected File resourcePathAsFile(String resourcePath){
        return new File(URI.create(getClass().getResource(resourcePath).toString()));
    }

    protected void test(String uri, List<String> scriptPaths, int expectedSize){
        List<File> ret = OntoDocManager.getScriptFiles(uri, scriptPaths);
        assertEquals(expectedSize, ret.size(),
                "Did not found %d expected number of files for uri <%s>"
                        .formatted(expectedSize, uri)
        );
    }


}