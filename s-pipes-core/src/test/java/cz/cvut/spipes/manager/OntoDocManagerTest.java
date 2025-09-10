package cz.cvut.spipes.manager;

import cz.cvut.spipes.TestConstants;
import cz.cvut.spipes.util.SPipesUtil;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.FileUtils;
import org.apache.jena.util.LocationMapper;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class OntoDocManagerTest {


    static Path managerDirPath = TestConstants.TEST_RESOURCES_DIR_PATH.resolve("manager").toAbsolutePath();
    protected static File managerDir;

    @BeforeAll
    public static void init() throws URISyntaxException {
        managerDir = new File(OntoDocManagerTest.class.getClassLoader().getResource("manager").toURI());
    }

    @BeforeEach
    public void setup(){
        OntoDocManager.setReloadFiles(false);
        OntoDocManager.getInstance().reset();
        SPipesUtil.resetFunctions();
    }

    @Test
    public void registerDocumentsProcessDirectoryRecursively()  {

        OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();

        int initialEntriesCount = getLocationMapperEntriesCount(ontoDocManager);

        ontoDocManager.registerDocuments(Arrays.asList(managerDirPath.resolve("recursive-discovery")));

        assertEquals(initialEntriesCount + 5, getLocationMapperEntriesCount(ontoDocManager));
    }


    /**
     * This test verifies ontology cache in 3 scenarios calling OntoDocManager.registerDocuments:
     * <ol>
     * <li> Cached ontology files are NOT UPDATED when changed and OntoDocManager.reloadFiles = FALSE - changed script and its import
     * <li> Cached ontology files are UPDATED when changed and OntoDocManager.reloadFiles = TRUE
     * <ol>
     * <li> Verify that if an ontology file and its import are changed, cached models of the ontology and its import
     * are properly updated.
     * <li> Verify that if an imported ontology file is changed, its cached model is properly updated.
     * </ol>
     * </ol>
     * @throws Exception
     */
    @Test
    public void registerDocumentsWithReloadFilesFalseOntologyCacheIsUnchanged_WithReloadFilesTrueOntologyCacheIsCorrectlyUpdated() throws Exception {
        // setup - create 2 ontology files where one is a script and imports the other.
        Workspace ws = build(2, "ontModelsNotReloadedWhenKeepUpdatedIsFalse", onts -> {
            onts.get(0).addImport(onts.get(1));
        });

        Ontology o1 = ws.getOnts().get(0);
        Ontology o2 = ws.getOnts().get(1);
        File rootDir = ws.getRootDir();

        // call tested method
        OntoDocManager ontoDocManager = (OntoDocManager) OntoDocManager.getInstance();
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));

        OntModel o1ModelBeforeChange = ontoDocManager.getOntology(o1.getURI());
        OntModel o2ImportedBeforeChange = o1ModelBeforeChange.getImportedModel(o2.getURI());

        // change files, for scenarios 1 and 2.1
        ws.addUpdate(o1, triple(o1, RDFS.comment, "o1 changed"));
        ws.addUpdate(o2, triple(o2, RDFS.comment, "o2 changed"));


        // When reloadFiles is false this method is called only during initialization
//        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));

        OntModel o1ModelAfterChange = ontoDocManager.getOntology(o1.getURI());
        OntModel o2ImportedAfterChange = o1ModelAfterChange.getImportedModel(o2.getURI());

        // VERIFY scenario 1 - o1 model retrieved before and after change is the same when cache not updated.
        assertNotChanged(o1ModelBeforeChange, o1ModelAfterChange, "o1");
        // TODO - implement feature. Inference layer of imported ont models is not cached.
        assertNotChangedByBase(o2ImportedBeforeChange, o2ImportedAfterChange, "o2");


        // VERIFY scenario 2.1 - cached models o1 and its import o2 are properly updated when o1 and o2 files are
        // changed and reloadFiles is true

        // call tested method
        OntoDocManager.setReloadFiles(true);
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));

        o1ModelAfterChange = ontoDocManager.getOntology(o1.getURI());
        o2ImportedAfterChange = o1ModelAfterChange.getImportedModel(o2.getURI());

        assertChanged(o1ModelBeforeChange, o1ModelAfterChange, "o1");

        assertExtended(o1ModelBeforeChange, o1ModelAfterChange, "o1",
                triple(o1, RDFS.comment, o1ModelAfterChange.createLiteral("o1 changed")),
                triple(o2, RDFS.comment, o1ModelAfterChange.createLiteral("o2 changed"))
        );

        assertChanged(o2ImportedBeforeChange, o2ImportedAfterChange, "o2Imported");

        // VERIFY scenario 2.2 - cached model o1 is reloaded if it is not changed directly when its import o2's file is
        // changed, and reloadFiles is true
        o1ModelBeforeChange = o1ModelAfterChange;
        o2ImportedBeforeChange = o2ImportedAfterChange;

        // change files, for scenarios 2.2
        ws.addUpdate(o2, triple(o2, RDFS.comment, "o2 changed again"));
        // call tested method
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));


        o1ModelAfterChange = ontoDocManager.getOntology(o1.getURI());
        o2ImportedAfterChange = o1ModelAfterChange.getImportedModel(o2.getURI());

        assertChanged(o2ImportedBeforeChange, o2ImportedAfterChange, "o2Imported");

        assertExtended(o1ModelBeforeChange, o1ModelAfterChange, "o1",
                triple(o2, RDFS.comment, o1ModelAfterChange.createLiteral("o2 changed again"))
        );
    }

    /**
     * This test verifies ontology cache in 2 scenarios calling OntoDocManager.registerDocuments:
     * <ol>
     * <li> Cached ontology files are NOT UPDATED when changed and OntoDocManager.reloadFiles = FALSE - files changed
     * the same way as in scenario 2.1
     *
     * <li> Cached ontology files are UPDATED when changed and OntoDocManager.reloadFiles = TRUE
     * <ol>
     * <li> Verify that if import clauses of ontology imported by main ontology, the main ontology and its imports are
     * reladed.
     * </ol>
     * </ol>
     * @throws Exception
     */
    @Test
    public void registerDocuments_CacheAndItsUpdateBasedOnReloadFilesValue_updatingIntermediateImportedModelsImports() throws Exception {
        // setup - create 4 ontologies where o1 is a script and imports o2 and o2 imports o3. o4 is not yet imported.
        Workspace ws = build(4, "updatingIntermediateImportedModelsImports", onts -> {
            onts.get(0).addImport(onts.get(1));
            onts.get(1).addImport(onts.get(2));
        });
        Ontology o1 = ws.getOnts().get(0);
        Ontology o2 = ws.getOnts().get(1);
        Ontology o3 = ws.getOnts().get(2);
        Ontology o4 = ws.getOnts().get(3);

        File rootDir = ws.getRootDir();

        // call tested method
        OntoDocManager ontoDocManager = (OntoDocManager)OntoDocManager.getInstance();
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));


        OntModel o1ModelBeforeChange = ontoDocManager.getOntology(o1.getURI());
        OntModel o2ImportedBeforeChange = o1ModelBeforeChange.getImportedModel(o2.getURI());
        OntModel o3IndirectlyImportedBeforeChange = o1ModelBeforeChange.getImportedModel(o3.getURI());
        OntModel o3ImportedBeforeChange = o2ImportedBeforeChange.getImportedModel(o3.getURI());

        // change files for scenario 1 and 2.1
        o2.removeProperty(OWL2.imports, o3);
        ws.addUpdate(o2, triple(o2, OWL2.imports, o4));

        OntModel o1ModelAfterChange = ontoDocManager.getOntology(o1.getURI());
        OntModel o2ImportedAfterChange = o1ModelAfterChange.getImportedModel(o2.getURI());
        OntModel o3IndirectlyImportedAfterChange = o1ModelAfterChange.getImportedModel(o3.getURI());
        OntModel o3ImportedAfterChange = o2ImportedAfterChange.getImportedModel(o3.getURI());

        // VERIFY scenario 1 - cache does not change for o1, importd o2, o3 imported indirectly from o1 or
        // imported directly from o2 when OntoDocManager.reloadFiles = FALSE
        assertNotChanged(o1ModelBeforeChange, o1ModelAfterChange, "o1");
        // TODO - inference layer of imported ont models are not cached. Implement caching of ont models of imports.
        assertNotChangedByBase(o2ImportedBeforeChange, o2ImportedAfterChange, "o2Imported");
        assertNotChangedByBase(o3IndirectlyImportedBeforeChange, o3IndirectlyImportedAfterChange, "o3IndirectlyImported");
        assertNotChangedByBase(o3ImportedBeforeChange, o3ImportedAfterChange, "o3Imported");
        assertTrue(o3IndirectlyImportedBeforeChange.isIsomorphicWith(o3ImportedAfterChange));

        // call tested method
        OntoDocManager.setReloadFiles(true);
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));

        o1ModelAfterChange = ontoDocManager.getOntology(o1.getURI());
        o2ImportedAfterChange = o1ModelAfterChange.getImportedModel(o2.getURI());
        o3IndirectlyImportedAfterChange = o1ModelAfterChange.getImportedModel(o3.getURI());
        o3ImportedAfterChange = o2ImportedAfterChange.getImportedModel(o3.getURI());
        OntModel o4IndirectlyImportedAfterChange = o1ModelAfterChange.getImportedModel(o4.getURI());
        OntModel o4ImportedAfterChange = o2ImportedAfterChange.getImportedModel(o4.getURI());

        // VERIFY scenario 2.1 - cache updated for o1, importd o2, o3 imported indirectly from o1 or
        // imported directly from o2 when OntoDocManager.reloadFiles = TRUE
        assertFalse(o1ModelBeforeChange.hashCode() == o1ModelAfterChange.hashCode()); // TODO - this fails, is this ok?
//        assertFalse(o1ModelBeforeChange.isIsomorphicWith(o1ModelAfterChange)); // TODO - fails, it should not be isomorphic

        assertChanged(o2ImportedBeforeChange, o2ImportedAfterChange,"o2Imported");

        assertNull(o3ImportedAfterChange);
        assertNull(o3IndirectlyImportedAfterChange);
        assertNotNull(o4IndirectlyImportedAfterChange);
        assertNotNull(o4ImportedAfterChange);
        assertTrue(o3IndirectlyImportedBeforeChange.isIsomorphicWith(o3ImportedBeforeChange)); // Redundant, already checked before
        assertTrue(o4IndirectlyImportedAfterChange.isIsomorphicWith(o4ImportedAfterChange));

        assertContains(o1ModelAfterChange, "o1",
                triple(o4, RDF.type, OWL2.Ontology), triple(o2, OWL2.imports, o4));

        assertTrue(o1ModelAfterChange.listStatements(o2, OWL2.imports, o3).toList().isEmpty());
        assertFalse(o1ModelAfterChange.contains(o2, OWL2.imports, o3));
    }


    @Test
    public void registerAllShaclFunctionsUpdatesShaclFunctionsCorrectly() throws Exception {
        String ontologyIRI = "http://onto.fel.cvut.cz/ontologies/lib/shacl-function";
        String ontology2IRI = ontologyIRI + "-new";

        String function1IRI = "http://onto.fel.cvut.cz/ontologies/lib/shacl-function/create-sparql-service-url";
        String function2IRI = "http://onto.fel.cvut.cz/ontologies/lib/shacl-function/construct-full-name";
        String function3IRI = "http://onto.fel.cvut.cz/ontologies/lib/shacl-function/construct-greeting-message";

        OntModel mFull= ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        final String funcDefIs = this.getClass().getResource("/shacl/shacl-function.shacl.ttl").toString();
        mFull.read(funcDefIs, null, FileUtils.langTurtle);
        Ontology onto = mFull.getOntology(ontologyIRI);

        OntModel mF1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        mF1.add(mFull.getBaseModel().listStatements());
        mF1.setNsPrefixes(mF1.getNsPrefixMap());
        mF1.remove(mFull.getResource(function2IRI).listProperties());
        mF1.remove(mFull.getResource(function3IRI).listProperties());
        ResourceUtils.renameResource(mF1.getOntology(ontologyIRI), ontology2IRI).toString();
        Ontology onto2 = mF1.getOntology(ontology2IRI);


        OntModel mF2 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        mF2.add(mFull.getBaseModel().listStatements());
        mF2.setNsPrefixes(mFull.getNsPrefixMap());
        mF2.remove(mFull.getResource(function1IRI).listProperties());



        File rootDir = makeDir("cache-registerDocumentsUpdatesShaclFunctionsCorrectly");

        OntoDocManager.setReloadFiles(true);
        OntoDocManager ontoDocManager = (OntoDocManager) OntoDocManager.getInstance();


        // VERIFY scenario 1 - no files in workspace, verify there are no non-system functions registered.
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));
        OntoDocManager.registerAllSPINModules();
        assertTrue(SPipesUtil.getNonSystemFunctions().isEmpty(), "There are registered non system functions \n %s".formatted(
                SPipesUtil.getNonSystemFunctions().stream().map("<%s>"::formatted).collect(Collectors.joining("\n"))
        ));


        // VERIFY scenario 2 - update ontology adding three functions, verify the three new functions are registered
        File f1 = file(rootDir, onto, "");
        f1.deleteOnExit();
        write(f1, mFull.getOntology(ontologyIRI));
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));
        OntoDocManager.registerAllSPINModules();
        assertEquals(
                Stream.of(function1IRI, function2IRI, function3IRI).collect(Collectors.toSet()),
                new HashSet<>(SPipesUtil.getNonSystemFunctions()),
                "Actually registered functions do no correspond to registered functions"
        );

        // VERIFY scenario 3 - remove function1 from the ontology, verify that deleted function is no longer registered
        write(f1, mF2.getOntology(ontologyIRI));
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));
        OntoDocManager.registerAllSPINModules();
        assertEquals(
                Stream.of(function2IRI, function3IRI).collect(Collectors.toSet()),
                new HashSet<>(SPipesUtil.getNonSystemFunctions()),
                "Actually registered functions not same as expected registered functions"
        );

        // VERIFY scenario 4 - add new ontology2 with function 1, verify that there are 3 registered functions
        File f2 = file(rootDir, onto2, "");
        write(f2, onto2);
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));
        OntoDocManager.registerAllSPINModules();
        assertEquals(
                Stream.of(function1IRI, function2IRI, function3IRI).collect(Collectors.toSet()),
                new HashSet<>(SPipesUtil.getNonSystemFunctions()),
                "Actually registered functions not same as expected registered functions"
        );

        // VERIFY scenario 5 - delete ontology2 file and move function1 to ontology, verify that there are three registered functions
        f2.delete();
        write(f1, mFull.getOntology(ontologyIRI));
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));
        OntoDocManager.registerAllSPINModules();
        assertEquals(
                Stream.of(function1IRI, function2IRI, function3IRI).collect(Collectors.toSet()),
                new HashSet<>(SPipesUtil.getNonSystemFunctions()),
                "Actually registered functions not same as expected registered functions"
        );

        // VERIFY scenario 6 - rename ontology 1 file and remove function 1, verify a that there are two registered functions
        File renamed_f1 = file(rootDir, onto, "-renamed");
        renamed_f1.deleteOnExit();
        boolean renamed = f1.renameTo(renamed_f1);
        assertTrue(renamed, "Could not rename file");
        write(f1, mF2.getOntology(ontologyIRI));
        ontoDocManager.registerDocuments(Arrays.asList(rootDir.toPath()));
        OntoDocManager.registerAllSPINModules();
        assertEquals(
                Stream.of(function2IRI, function3IRI).collect(Collectors.toSet()),
                new HashSet<>(SPipesUtil.getNonSystemFunctions()),
                "Actually registered functions not same as expected registered functions"
        );
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



    private static int getLocationMapperEntriesCount(OntologyDocumentManager ontoDocManager) {
        return getLocationMapperEntriesCount(ontoDocManager.getOntDocumentManager().getFileManager().getLocationMapper());
    }

    private static int getLocationMapperEntriesCount(LocationMapper locationMapper) {
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

    private static void assertNotChanged(Model mBeforeChange, Model mAfterChange, String modelName){
        assertEquals(mBeforeChange.hashCode(), mAfterChange.hashCode(), "Model \"%s\" has changed.".formatted(modelName));
        assertTrue(mBeforeChange.isIsomorphicWith(mAfterChange), () -> difference(mBeforeChange, mAfterChange, modelName));
    }

    private static void assertChanged(Model mBeforeChange, Model mAfterChange, String modelName){
        assertNotEquals(mBeforeChange.hashCode(), mAfterChange.hashCode(), "Model \"%s\" was not changed.".formatted(modelName));
        assertFalse(mBeforeChange.isIsomorphicWith(mAfterChange), () -> "Model \"%s\" was not changed ".formatted(modelName));
    }

    private static void assertChanged(OntModel mBeforeChange, OntModel mAfterChange, String modelName){
        assertNotEquals(mBeforeChange.hashCode(), mAfterChange.hashCode(), "Model \"%s\" was not changed.".formatted(modelName));
        assertNotEquals(mBeforeChange.getBaseModel().hashCode(), mAfterChange.getBaseModel().hashCode(), "Model \"%s\" has changed.".formatted(modelName));
        assertFalse(mBeforeChange.isIsomorphicWith(mAfterChange), () -> "%s was expected to be changed".formatted(modelName));
    }

    /**
     * Does not compare hasCode of input models <code>mBeforeChange</code> and <code>mAfterChange</code>. Used when
     * comparing models returned by OntModel.getImportedModel as jena OntDocumentManager might cache only the asserted
     * triples in the base model and not cache the inferred layer.
     *
     * @param mBeforeChange
     * @param mAfterChange
     * @param modelName
     */
    private static void assertNotChangedByBase(OntModel mBeforeChange, OntModel mAfterChange, String modelName){

//        assertNotEquals(mBeforeChange.hashCode(), mAfterChange.hashCode(), "Model \"%s\" was not changed.".formatted(modelName));
        assertEquals(mBeforeChange.getBaseModel().hashCode(), mAfterChange.getBaseModel().hashCode(), "Model \"%s\" has changed.".formatted(modelName));
        assertTrue(mBeforeChange.isIsomorphicWith(mAfterChange), () -> difference(mBeforeChange, mAfterChange, modelName));
    }

    private static void assertNotChanged(OntModel mBeforeChange, OntModel mAfterChange, String modelName){
        assertEquals(mBeforeChange.hashCode(), mAfterChange.hashCode(), "Model \"%s\" was not changed.".formatted(modelName));
        assertEquals(mBeforeChange.getBaseModel().hashCode(), mAfterChange.getBaseModel().hashCode(), "Model \"%s\" has changed.".formatted(modelName));
        assertTrue(mBeforeChange.isIsomorphicWith(mAfterChange), () -> difference(mBeforeChange, mAfterChange, modelName));
    }

    private static void assertExtended(Model m, Model mExtension, String modelName, Triple... extensionTriples){

        Model mDiffRemoved = m.difference(mExtension);

        assertTrue(mDiffRemoved.isEmpty(), () -> {
            StringWriter sw = new StringWriter();
            mDiffRemoved.write(sw, "TTL");
            return "There should not be removed triples from \"%s\" \n%s".formatted(modelName, sw.toString());
        });
        Model mExtensionAdded = mExtension.difference(m);

        Model extension = model(extensionTriples);

        List<Statement> missingTriples = difference(extension, mExtensionAdded);

        List<Statement> unexpectedAddedTriples = difference(mExtensionAdded, extension);

        assertTrue(missingTriples.isEmpty() && unexpectedAddedTriples.isEmpty(),
                """
                        Actual extension is different than expected:
                        triples missing in extension:
                        %s
                        
                        triples unexpected in extension:
                        %s""".formatted(toString(missingTriples), toString(unexpectedAddedTriples))
        );
    }

    private static void assertContains(Model m, String modelName, Triple ... triples){
        Model subSet = model(triples);
        List<Statement> notContained = difference(subSet, m);
        assertTrue(
                notContained.isEmpty(),
                () -> "Extension of \"%s\" does not contain expected triples :\n[%s]"
                        .formatted(modelName, toString(notContained))
        );
    }

    private static List<Statement> difference(Model m, Model m2){
        return m.difference(m2).listStatements().toList();
    }

    private static String toString(Collection<?> list){
        return list.stream()
                .map(t -> "(%s)".formatted(t.toString()))
                .collect(Collectors.joining("\n"));
    }

    private static String difference(Model mBeforeChange, Model mAfterChange, String modelName){
        StringWriter sw = new StringWriter();
        PrintWriter sp = new PrintWriter(sw);
        sp.println("Model \"%s\" has changed. Difference is: ".formatted(modelName));
        sp.println("added triples:");
        mAfterChange.difference(mBeforeChange).write(sp, "ttl");
        sp.println("removed triples:");
        mBeforeChange.difference(mAfterChange).write(sp, "ttl");
        return sw.toString();
    }

    private static File makeDir(String dirName) {
        File rootDir = new File(managerDir, dirName);
        rootDir.mkdir();
        rootDir.deleteOnExit();
        return rootDir;
    }

    private static File file(File dir, Ontology onto, String postFix){
        return new File(dir, onto.getLocalName() + postFix +".ttl");
    }

    private static void write(File dir, Ontology onto, String postFix ) throws IOException {
        File f = file(dir, onto, postFix);
        write(f, onto);
    }

    private static void write(File file, Ontology onto) throws IOException {
        try (Writer w = new FileWriter(file, false)) {
            onto.getModel().write(w, "ttl");
        }
        file.deleteOnExit();
    }

    private static Ontology createOntology(String id) {
        OntModel model = ModelFactory.createOntologyModel();
        Ontology onto = model.createOntology("http://onto.fel.cvut.cz/ontologies/test-" + id);

        return onto;
    }


    private static Workspace build(int size, String dirId, Consumer<List<Ontology>> customizer) throws IOException, URISyntaxException {
        List<Ontology> onts = new ArrayList<>();
        for(int i = 0; i < size; i ++){
            Ontology o = createOntology("o" + (i + 1));
            onts.add(o);
        }

        customizer.accept(onts);

        File rootDir = makeDir("cache-"+ dirId);

        Map<Ontology,File> ont2File = new HashMap<>();
        ont2File.put(onts.get(0), file(rootDir, onts.get(0), ".sms"));
        for(Ontology o : onts.subList(1, onts.size())){
            ont2File.put(o, file(rootDir, o, ""));
        }

        // create other ontologies
        for(Map.Entry<Ontology, File> entry : ont2File.entrySet())
            write(entry.getValue(), entry.getKey());

        return new Workspace(rootDir, onts, ont2File);
    }

    private static class Workspace {
        protected File rootDir;
        protected List<Ontology> onts;
        protected Map<Ontology, File> ont2File;

        public Workspace(File rootDir, List<Ontology> onts, Map<Ontology, File> ont2File) {
            this.rootDir = rootDir;
            this.onts = onts;
            this.ont2File = ont2File;
        }

        public File getRootDir() {
            return rootDir;
        }

        public List<Ontology> getOnts() {
            return onts;
        }

        public void addUpdate(Ontology o, Triple ... triplesToAdd) throws IOException {
            for(Triple t: triplesToAdd){
                o.getModel().getGraph().add(t);
            }
            write(ont2File.get(o), o);
        }
    }


    private static Triple triple(RDFNode s, RDFNode p, String o){
        return Triple.create(s.asNode(), p.asNode(), NodeFactory.createLiteralString(o));
    }

    private static Triple triple(RDFNode s, RDFNode p, RDFNode o){
        return Triple.create(s.asNode(), p.asNode(), o.asNode());
    }

    private static Model model(Triple ... triples){
        Model model = ModelFactory.createDefaultModel();
        Arrays.asList(triples).forEach(model.getGraph()::add);
        return model;
    }
}