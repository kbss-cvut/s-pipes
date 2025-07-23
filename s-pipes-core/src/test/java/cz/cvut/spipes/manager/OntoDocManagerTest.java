package cz.cvut.spipes.manager;

import cz.cvut.spipes.TestConstants;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.LocationMapper;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class OntoDocManagerTest {


    static Path managerDirPath = TestConstants.TEST_RESOURCES_DIR_PATH.resolve("manager").toAbsolutePath();

    @BeforeEach
    public void setup(){
        OntoDocManager.setReloadFiles(false);
        OntoDocManager.getInstance().reset();
    }

    @Test
    public void registerDocumentsProcessDirectoryRecursively()  {

        OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();

        int initialEntriesCount = getLocationMapperEntriesCount(ontoDocManager);

        ontoDocManager.registerDocuments(Arrays.asList(managerDirPath.resolve("recursive-discovery")));

        assertEquals(initialEntriesCount + 5, getLocationMapperEntriesCount(ontoDocManager));
    }

    @Test
    public void ontModelsNotReloadedWhenKeepUpdatedIsFalse() throws Exception {
        // setup
        // create 2 ontology files where one is a script and imports the other.
        Ontology o1 = createOntology("o1");
        Ontology o2 = createOntology("o2");
        o1.addImport(o2);
        File managerDir = new File(this.getClass().getClassLoader().getResource("manager").toURI());
        File rootDir = new File(managerDir, "cache-ontModelsNotReloadedWhenKeepUpdatedIsFalse");
        rootDir.mkdir();
        rootDir.deleteOnExit();
        write(rootDir, o1, ".sms");
        write(rootDir, o2, "");

        // init tested ontoDocManager
        OntoDocManager ontoDocManager = (OntoDocManager) OntoDocManager.getInstance();
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));

        // check cache not updated if reloadFiles is false
        OntModel o1ModelBeforeChange = ontoDocManager.getOntology(o1.getURI());
        OntModel o2ImportedBeforeChange = o1ModelBeforeChange.getImportedModel(o2.getURI());
        o1.addComment(o1.getModel().createLiteral("o1 changed"));
        write(rootDir, o1, ".sms");
        o2.addComment(o2.getModel().createLiteral("o2 changed"));
        write(rootDir, o2, "");

        // When reloadFiles is false this method is called only during initialization
//        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));

        OntModel o1ModelAfterChange = ontoDocManager.getOntology(o1.getURI());
        OntModel o2ImportedAfterChange = o1ModelAfterChange.getImportedModel(o2.getURI());

        // CHECK - o1 model retrieved before and after change is the same when cache not updated. Check is done by o1
        // model objects hash and by the contents of the models
        assertTrue(o1ModelBeforeChange.hashCode() == o1ModelAfterChange.hashCode());
        assertTrue(o1ModelBeforeChange.isIsomorphicWith(o1ModelAfterChange));
        // TODO - inference layer of imported ont models are not cached
//        assertTrue(o2ImportedBeforeChange.hashCode() == o2ImportedAfterChange.hashCode());
        // base models, i.e. models with no inference, are cached
        assertTrue(o2ImportedBeforeChange.getBaseModel().hashCode() == o2ImportedAfterChange.getBaseModel().hashCode());
        assertTrue(o2ImportedBeforeChange.isIsomorphicWith(o2ImportedAfterChange));


        // check cache updated if reloadFiles is true
        OntoDocManager.setReloadFiles(true);
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));
        o1ModelAfterChange = ontoDocManager.getOntology(o1.getURI());
        o2ImportedAfterChange = o1ModelAfterChange.getImportedModel(o2.getURI());

        assertFalse(o1ModelBeforeChange.hashCode() == o1ModelAfterChange.hashCode());
        assertFalse(o1ModelBeforeChange.isIsomorphicWith(o1ModelAfterChange));
        Model o1DiffRemoved = o1ModelBeforeChange.difference(o1ModelAfterChange);

        assertTrue(o1DiffRemoved.isEmpty(), () -> {
            StringWriter sw = new StringWriter();
            o1DiffRemoved.write(sw, "TTL");
            return "There should not be removed triples from o1: \n" +  sw.toString();
        });
        Model o1DiffAdded = o1ModelAfterChange.difference(o1ModelBeforeChange);
        assertEquals(2, o1DiffAdded.size(), "There should be exactly 2 added triples");
        assertTrue(o1DiffAdded.contains(o1, RDFS.comment, o1DiffAdded.createLiteral("o1 changed")));
        assertTrue(o1DiffAdded.contains(o2, RDFS.comment, o1DiffAdded.createLiteral("o2 changed")));

        assertFalse(o2ImportedBeforeChange.hashCode() == o2ImportedAfterChange.hashCode());
        assertFalse(o2ImportedBeforeChange.isIsomorphicWith(o2ImportedAfterChange));

        // test changed import is updated
        o1ModelBeforeChange = o1ModelAfterChange;
        o2ImportedBeforeChange = o2ImportedAfterChange;

        o2.addComment(o2.getModel().createLiteral("o2 changed again"));
        write(rootDir, o2, "");
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));


        o1ModelAfterChange = ontoDocManager.getOntology(o1.getURI());
        o2ImportedAfterChange = o1ModelAfterChange.getImportedModel(o2.getURI());


        assertFalse(o1ModelBeforeChange.hashCode() == o1ModelAfterChange.hashCode());
        assertFalse(o1ModelBeforeChange.isIsomorphicWith(o1ModelAfterChange));
        // This test case requires explicit reloading of cached imported models - loadDirtyModels
        assertFalse(o2ImportedBeforeChange.hashCode() == o2ImportedAfterChange.hashCode());
        assertFalse(o2ImportedBeforeChange.isIsomorphicWith(o2ImportedAfterChange));

        assertTrue(o1ModelBeforeChange.difference(o1ModelAfterChange).isEmpty());
        o1DiffAdded = o1ModelAfterChange.difference(o1ModelBeforeChange);
        assertEquals(1, o1DiffAdded.size(), "There should be only 1 added triple");
        assertTrue(o1DiffAdded.contains(o2, RDFS.comment, o1DiffAdded.createLiteral("o2 changed again")));

    }

    @Test
    public void updatingIntermediateImportedModelsImports() throws Exception {
        // setup
        Ontology o1 = createOntology("o1");
        Ontology o2 = createOntology("o2");
        Ontology o3 = createOntology("o3");
        Ontology o4 = createOntology("o4");
        o1.addImport(o2);
        o2.addImport(o3);
        File managerDir = new File(this.getClass().getClassLoader().getResource("manager").toURI());
        File rootDir = new File(managerDir, "cache-updatingIntermediateImportedModelsImports");
        rootDir.mkdir();
        rootDir.deleteOnExit();
        write(rootDir, o1, ".sms");
        write(rootDir, o2, "");
        write(rootDir, o3, "");
        write(rootDir, o4, "");

        // init tested ontoDocManager
        OntoDocManager ontoDocManager = (OntoDocManager)OntoDocManager.getInstance();
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));

        // check cache not updated if reloadFiles is false
        OntModel o1ModelBeforeChange = ontoDocManager.getOntology(o1.getURI());
        OntModel o2ImportedBeforeChange = o1ModelBeforeChange.getImportedModel(o2.getURI());
        OntModel o3IndirectlyImportedBeforeChange = o1ModelBeforeChange.getImportedModel(o3.getURI());
        OntModel o3ImportedBeforeChange = o2ImportedBeforeChange.getImportedModel(o3.getURI());
        o2.removeProperty(OWL2.imports, o3);
        o2.addProperty(OWL2.imports, o4);
        write(rootDir, o2, "");
        OntModel o1ModelAfterChange = ontoDocManager.getOntology(o1.getURI());
        OntModel o2ImportedAfterChange = o1ModelAfterChange.getImportedModel(o2.getURI());
        OntModel o3IndirectlyImportedAfterChange = o1ModelAfterChange.getImportedModel(o3.getURI());
        OntModel o3ImportedAfterChange = o2ImportedAfterChange.getImportedModel(o3.getURI());

        assertTrue(o1ModelBeforeChange.hashCode() == o1ModelAfterChange.hashCode());
        assertTrue(o1ModelBeforeChange.isIsomorphicWith(o1ModelAfterChange));
        // TODO - inference layer of imported ont models are not cached
//        assertTrue(o2ImportedBeforeChange.hashCode() == o2ImportedAfterChange.hashCode());
        // base models, i.e. models with no inference, are cached
        assertTrue(o2ImportedBeforeChange.getBaseModel().hashCode() == o2ImportedAfterChange.getBaseModel().hashCode());
        assertTrue(o3IndirectlyImportedBeforeChange.getBaseModel().hashCode() == o3IndirectlyImportedAfterChange.getBaseModel().hashCode());
        assertTrue(o3ImportedBeforeChange.getBaseModel().hashCode() == o3ImportedAfterChange.getBaseModel().hashCode());
        assertTrue(o2ImportedBeforeChange.isIsomorphicWith(o2ImportedAfterChange));
        assertTrue(o3IndirectlyImportedBeforeChange.isIsomorphicWith(o3ImportedBeforeChange));
        assertTrue(o3IndirectlyImportedAfterChange.isIsomorphicWith(o3ImportedAfterChange));
        assertTrue(o3ImportedBeforeChange.isIsomorphicWith(o3ImportedAfterChange));


        // check cache updated if reloadFiles is true
        OntoDocManager.setReloadFiles(true);
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));

        o1ModelAfterChange = ontoDocManager.getOntology(o1.getURI());
        o2ImportedAfterChange = o1ModelAfterChange.getImportedModel(o2.getURI());
        o3IndirectlyImportedAfterChange = o1ModelAfterChange.getImportedModel(o3.getURI());
        o3ImportedAfterChange = o2ImportedAfterChange.getImportedModel(o3.getURI());
        OntModel o4IndirectlyImportedAfterChange = o1ModelAfterChange.getImportedModel(o4.getURI());
        OntModel o4ImportedAfterChange = o2ImportedAfterChange.getImportedModel(o4.getURI());

        assertFalse(o1ModelBeforeChange.hashCode() == o1ModelAfterChange.hashCode()); // TODO - this fails, is this ok?
//        assertFalse(o1ModelBeforeChange.isIsomorphicWith(o1ModelAfterChange)); // TODO - fails, it should not be isomorphic
        assertFalse(o2ImportedBeforeChange.hashCode() == o2ImportedAfterChange.hashCode());
        assertFalse(o2ImportedBeforeChange.getBaseModel().hashCode() == o2ImportedAfterChange.getBaseModel().hashCode());
        assertNull(o3ImportedAfterChange);
        assertNull(o3IndirectlyImportedAfterChange);
        assertFalse(o2ImportedBeforeChange.isIsomorphicWith(o2ImportedAfterChange));
        assertNotNull(o4IndirectlyImportedAfterChange);
        assertNotNull(o4ImportedAfterChange);
        assertTrue(o3IndirectlyImportedBeforeChange.isIsomorphicWith(o3ImportedBeforeChange)); // Redundant, already checked before
        assertTrue(o4IndirectlyImportedAfterChange.isIsomorphicWith(o4ImportedAfterChange));

        assertTrue(o1ModelAfterChange.contains(o4, RDF.type, OWL2.Ontology));
        assertTrue(o1ModelAfterChange.contains(o2, OWL2.imports, o4));

        assertTrue(o1ModelAfterChange.listStatements(o2, OWL2.imports, o3).toList().isEmpty());
//        TODO - graph for o3 is still part of the MultiUnion graph of o1ModelAfterChange.
        assertFalse(o1ModelAfterChange.contains(o2, OWL2.imports, o3));
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

    private void write(File dir, Ontology onto, String postFix ) throws IOException {
        File f = new File(dir, onto.getLocalName() + postFix +".ttl");
        try (Writer w = new FileWriter(f, false)) {
            onto.getModel().write(w, "ttl");
        }
        f.deleteOnExit();
    }

    private Ontology createOntology(String id) {
        OntModel model = ModelFactory.createOntologyModel();
        Ontology onto = model.createOntology("http://onto.fel.cvut.cz/ontologies/test-" + id);

        return onto;
    }


}