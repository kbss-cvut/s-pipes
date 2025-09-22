package cz.cvut.spipes.manager;

import cz.cvut.spipes.config.CompatibilityConfig;
import cz.cvut.spipes.util.JenaUtils;
import cz.cvut.spipes.util.SPipesUtil;
import cz.cvut.spipes.util.SparqlMotionUtils;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.models.ModelMaker;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;
import org.apache.jena.util.LocationMapper;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//  TODO
//        JenaUtil.addTransitiveSubjects()
//        JenaUtil.getAllInstances(cls)
//        JenaUtil.getImports(graph)
//        JenaUtil.initNamespaces(graph);
//        JenaUtil.invokeExpression();
//        JenaUtil.invokeFunction1();
//        SPINUtil.getFirstResult();
//        SPINUtil.getFirstResult()
//        SPINUtil.isLibraryOntology
//        SPLUtil.getArgument();
//        SPINImports
//            OntDocumentManager.getInstance().addModel(uri, model);

/**
 * manages mapping file --> ontology IRI
 * caches the files
 * manages prefixes
 *
 * This implementation of {@link OntologyDocumentManager} is based on jena's {@link OntDocumentManager} which supports
 * model caching and IRI to file mapping.
 * <p/>
 * {@link OntoDocManager#registerDocuments(Iterable)} resets the ontology IRI to file path mapping to ontology IRIs
 * discovered in file paths walking dirs and files in Iterable argument. <code>getOntology</code> and <code>getModel</code>
 * methods will return models read from filesystem only if the IRI was found as an ontology IRI in one of the files
 * found in one of the root paths/files in the iterable argument of last call to {@link OntoDocManager#registerDocuments(Iterable)}.
 *
 * <p/>
 * If {@link OntoDocManager#reloadFiles} is false
 * all file paths are processed, e.g., files are scanned for ontology IRIs and mapped to file paths.
 *
 * <p/>
 * If {@link OntoDocManager#reloadFiles} is true, <code>getOntology</code> and <code>getModel</code> caches returned
 * models. {@link OntoDocManager#registerDocuments(Iterable)} cache and IRI to file path is updated only for removed,
 * updated and new file.
 *
 * <p/>
 *
 * This implementation also maintains loaded SHACL functions.
 *
 *
 **/
public class OntoDocManager implements OntologyDocumentManager {

    private static final Logger log = LoggerFactory.getLogger(OntoDocManager.class);
    private static Instant lastTime = Instant.now();
    private static boolean reloadFiles = false;

    private static Set<Path> managedFiles;

    private static Set<Path> availableFiles;
    private static Set<Path> updatePaths;

    private static Set<String> dirtyModels;


    OntDocumentManager ontDocumentManager;
    static OntoDocManager sInstance;
    static String[] SUPPORTED_FILE_EXTENSIONS = {"n3", "nt", "ttl", "rdf", "owl"}; //TODO json-ld

    public static OntModelSpec ONT_MODEL_SPEC = OntModelSpec.OWL_MEM;

    private OntoDocManager() {
        this(new OntDocumentManager());
    }

    OntoDocManager(OntDocumentManager ontDocumentManager) {
        this.ontDocumentManager = ontDocumentManager;
        ontDocumentManager.setReadFailureHandler(new OntologyReadFailureHandler());
        ontDocumentManager.setFileManager(FileManager.get());
        if (! CompatibilityConfig.isLoadSparqlMotionFiles()) {
            SparqlMotionUtils.SM_ONTOLOGIES.forEach(
                ontDocumentManager::addIgnoreImport
            );
        }
    }

    public static OntologyDocumentManager getInstance() {
        if (sInstance == null) {
            sInstance = new OntoDocManager(OntDocumentManager.getInstance());
            OntDocumentManager.getInstance().getFileManager().setModelCaching(true);
        }
        return sInstance;
    }


    @Override
    public List<String> getSupportedFileExtensions() {
        return Arrays.asList(SUPPORTED_FILE_EXTENSIONS);
    }

    protected void registerDocuments(Path directoryOrFilePath) {

        if (Files.isDirectory(directoryOrFilePath) && Files.isSymbolicLink(directoryOrFilePath)) {
            log.error("Cannot register documents from directory {}. Directories that are symbolic links " +
                     "are not supported.", directoryOrFilePath );
            throw new IllegalArgumentException("Symbolic link directories in 'scripts.contextPaths' variable are not supported: " + directoryOrFilePath);
        }

        findAvailableAndUpdatedPaths(directoryOrFilePath);
    }

    /**
     * Replaces any previous ontology IRI to file mappings, cached models and loaded SHACL functions with the ones
     * discovered by recursively searching the path entries in the argument <code>fileOrDirectoryPath</code> for files
     * with supported file extensions, see {@link OntoDocManager#getSupportedFileExtensions()}. If {@link OntoDocManager#reloadFiles}
     * is true only removed, updated and new paths are processed, otherwise all paths are processed.
     *
     * @param fileOrDirectoryPath File or directory path to register. If directory, it is recursively crawled.
     */
    @Override
    public void registerDocuments(Iterable<Path> fileOrDirectoryPath) {
        availableFiles = new HashSet<>();
        updatePaths = new HashSet<>();
        dirtyModels = new HashSet<>();

        fileOrDirectoryPath.forEach(
                this::registerDocuments
        );

        resetCache(managedFiles, availableFiles, updatePaths);

        managedFiles = availableFiles;
        lastTime = Instant.now();
    }

    @Override
    public Set<String> getRegisteredOntologyUris() {
        Set<String> ontoUris = new HashSet<>();
        ontDocumentManager.listDocuments().forEachRemaining(ontoUris::add);
        return ontoUris;
    }

    @Override
    public OntModel getOntology(String uri) {
        return ontDocumentManager.getOntology(uri, ONT_MODEL_SPEC);
    }

    
    /**
     * Extracts file2iri map from LocationManager associated with the {@link OntDocumentManager}
     * @return
     */
    protected static Map<String,String> file2iri(){
        LocationMapper lm  = OntDocumentManager.getInstance().getFileManager().getLocationMapper();
        Map<String, String> file2iri = new HashMap<>();
        Iterator<String> altEntries = lm.listAltEntries();
        while(altEntries.hasNext()) {
            String uri = altEntries.next();
            String file = lm.getAltEntry(uri);
            if(file == null) {
                log.warn("Iri <{}> is without file mapping or mapped to null", uri);
                continue;
            }
            String oldUri = file2iri.put(file, uri);
            if(oldUri != null && !oldUri.equals(uri))  
                log.warn("Multiple URIs, e.g. <{}> and <{}>, are mapped to the same file\"{}\". Considering only mapping of uri <{}>", oldUri, uri, file, uri);
            
        }
        return file2iri;
    }

    /**
     * For each file path in the <code>filePath</code> argument, clears cached models and iri to file path mappings in the OntDocumentManager.
     * <p>
     * Cache is implemented by OntDocumentManager.getInstance().getFileManager(). Additionally, jena may store models in
     * OntModelSpec.OWL_MEM.getBaseModelMaker and OntModelSpec.OWL_MEM.getImportModelMaker. Specifically, the
     * ImportModelMaker stores imports and it is necessary to clear.
     * @param filePaths
     */
    protected static void clearCachedModel(Collection<Path> ... filePaths){
        Map<String, String> file2iri = file2iri();
        LocationMapper lm  = OntDocumentManager.getInstance().getFileManager().getLocationMapper();

        for(Path filePath : Arrays.stream(filePaths).sequential().flatMap(Collection::stream).toList()){
            String pathString = filePath.toString();
            String uriToClear = Optional.ofNullable(file2iri.get(pathString)).orElse(null);
            if(uriToClear != null) {
                lm.removeAltEntry(uriToClear);
                clearCachedModel(uriToClear);
            }
        }
    }

    protected static void clearOntModelsImportingDirtyModel(OntDocumentManager ontDocumentManager){
        Iterator<String> iriIter = ontDocumentManager.getFileManager().getLocationMapper().listAltEntries();
        Stack<String> iris = new Stack<>();
        iriIter.forEachRemaining(iris::push);

        while(!iris.isEmpty()) {
            String iri = iris.pop();

            if(ontDocumentManager.getFileManager().hasCachedModel(iri)){
                Model m = ontDocumentManager.getFileManager().getFromCache(iri);
                if(m instanceof OntModel) {
                     ((OntModel)m).listImportedOntologyURIs(true).stream()
                             .filter(dirtyModels::contains)
                             .findAny()
                             .ifPresent(i -> clearCachedModel(iri));
                } else {
                    // do nothing - assumes that non OntModel models do not have imports.
                }
            }
        }
    }

    protected static void clearCachedModel(String uri){
        if(OntDocumentManager.getInstance().getFileManager().hasCachedModel(uri))
            OntDocumentManager.getInstance().getFileManager().removeCacheModel(uri);
        boolean cachedImportChanged = clearCache(uri, ONT_MODEL_SPEC);
        if(cachedImportChanged)
            dirtyModels.add(uri);
    }

    private static boolean clearCache(String uri, OntModelSpec ontModelSpec) {
        List<ModelMaker> modelMakers = Stream.of(ontModelSpec.getBaseModelMaker(), ontModelSpec.getImportModelMaker())
                .filter(m -> m.hasModel(uri)).toList();
        modelMakers.forEach(m -> m.removeModel(uri));
        return !modelMakers.isEmpty();
    }

    private static void clearCache(OntModelSpec ontModelSpec){
        Stream.of(ontModelSpec.getBaseModelMaker(), ontModelSpec.getImportModelMaker())
                .forEach(
                        m -> m.listModels().toList().forEach(m::removeModel)
                );
    }

    @Override
    public Model getModel(String uri) {
        return ontDocumentManager.getModel(uri);
    }

    /**
     * Resets state of this ontology document manager.
     */
    @Override
    public void reset() {
        getOntDocumentManager().reset();
        clearCache(ONT_MODEL_SPEC);
        resetIriToManagedFileMappings(getOntDocumentManager());
    }

    private static void resetIriToManagedFileMappings(OntDocumentManager ontDocumentManager){
        if(managedFiles == null)
            return;
        Set<String> managedPaths = managedFiles.stream().map(p -> p.toString()).collect(Collectors.toSet());
        Map<String, String> mappedPaths = file2iri();
        LocationMapper lm = ontDocumentManager.getFileManager().getLocationMapper();

        mappedPaths.entrySet().stream()
                .filter(e -> managedPaths.contains(e.getKey()))
                .forEach(e -> lm.removeAltEntry(e.getValue()));
    }

    public OntDocumentManager getOntDocumentManager() {
        return ontDocumentManager;
    }


    // -------------- PRIVATE METHODS -------------------

    private static boolean isFileNameSupported(String fileName) {
        return Arrays.stream(SUPPORTED_FILE_EXTENSIONS).anyMatch(ext -> fileName.endsWith("." + ext));
    }

    private static boolean wasModified(Path fileName){
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(fileName, BasicFileAttributes.class);
            return attr.lastModifiedTime().toInstant().isAfter(lastTime);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns mapping filePath --> model for all files recursively defined by <code>directoryOrFilePath</code>.
     * If <code>this.reloadFiles=true</code>, it ignores files that are older than <code>this.lastTime</code>.
     *
     * @param directoryOrFilePath File or directory to by searched recursively for models.
     * @return Mapping filePath to model.
     */
    synchronized static void findAvailableAndUpdatedPaths(Path directoryOrFilePath) {
        try (Stream<Path> stream = Files.walk(directoryOrFilePath)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(f -> {
                        String fileName = f.getFileName().toString();
                        return isFileNameSupported(fileName);
                    })                    .forEach(file -> {
                        availableFiles.add(file.toAbsolutePath()); // could contain renamed files which were not modified since the last cache update
                        if (reloadFiles && !wasModified(file)) {
                            log.debug("Skipping unmodified file: {}", file.toUri());
                            return;
                        }
                        updatePaths.add(file); // contains only updated files including renamed updated files
                    });

        } catch (IOException | DirectoryIteratorException e) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            log.error("Could not load ontologies from directory {} -- {} .", directoryOrFilePath, e);
        }
    }

    static void resetCache(Set<Path> managedFiles, Set<Path> availableFiles, Set<Path> updatePaths){
        OntDocumentManager ontDocumentManager = OntoDocManager.getInstance().getOntDocumentManager();
        Set<Path> removedFiles = new HashSet<>();
        // remove deleted files from cache
        if(managedFiles != null) {
            removedFiles.addAll(managedFiles);
            removedFiles.removeAll(availableFiles);
        }

        // add renamed but not updated files to updatedPaths
        // TODO - optimize processing renamed but not updated files, e.g. cache should not be cleared as the model did not change. Update path mappings, e.g. ontology iri to path, functions registered under path
        Set<Path> newOrRenamedFiles = new HashSet<>(availableFiles);
        newOrRenamedFiles.removeAll(Optional.ofNullable(managedFiles).orElse(Collections.emptySet()));


        Set<Path> updatedNewOrRenamedPaths = new HashSet<>(updatePaths);
        updatedNewOrRenamedPaths.addAll(newOrRenamedFiles);
        // reload new, updated and renamed files
        Map<String, Model> file2Model = loadModels(updatedNewOrRenamedPaths);

        clearCachedModel(removedFiles, updatePaths);
        clearOntModelsImportingDirtyModel(ontDocumentManager);

        // get all baseIRIs
        Map<String, String> file2baseIRI = getAllBaseIris(file2Model);


        // load it to document manager
        file2baseIRI.entrySet().forEach(e -> {
            ontDocumentManager.addAltEntry(e.getKey(), e.getValue());
            }
        );

        // TODO - move this method and call. For example, implement event listener SPipesUtils which will listen for
        //  reset events.
        OntoDocManager.updateSHACLFunctionsFromUpdatedWorkspace(file2Model, removedFiles);
    }

    /**
     *
     * @param modelPaths
     * @return a file to model map, contains only entries where the model could be loaded from the file path.
     */
    private static Map<String, Model> loadModels(Set<Path> modelPaths){
        Map<String, Model> file2Model = new HashMap<>();
        for(Path path : modelPaths){
            String lang = FileUtils.guessLang(path.getFileName().toString());

            log.info("Loading model from {} ...", path.toUri());
            try{
                Model model = loadModel(path, lang);
                log.debug("Successfully loaded model from {}.", path.toUri());
                file2Model.put(path.toString(), model);
            }catch (Exception e){
                log.warn("Failed to load model from {}", path.toUri(), e);
            }
        }
        return file2Model;
    }


    /**
     * Returns mapping filePath --> baseUri for all (file, model) pairs in the <code>file2Model</code> input argument
     * BaseUri is uri of the ontology defined in the model loaded from the file.
     * If <code>this.reloadFiles=true</code>, it ignores files that are older than <code>this.lastTime</code>.
     *
     * @param file2Model File to model map
     * @return Mapping filePath to baseURI.
     */
    static Map<String, String> getAllBaseIris(Map<String, Model> file2Model) {
        Map<String, String> baseUri2file = new HashMap<>();
        file2Model.entrySet().forEach(e -> {
            String file = e.getKey();
            Model model = e.getValue();

            String baseURI = JenaUtils.getBaseUri(model);

            if (baseURI == null) {
                log.info("Ignoring file \"" + file + "\" as it does not contain baseURI.");
                return;
            }
            baseUri2file.put(baseURI, file);
        });

        return baseUri2file;
    }


    // ------------------------- TODO OLD -> REMOVE ------------------


    public static OntModel loadOntModel(String resourcePath) {
        InputStream is = OntoDocManager.class.getResourceAsStream(resourcePath);

        if (is == null) {
            throw new IllegalArgumentException("Resource " + resourcePath + " not found.");
        }
        return loadOntModel(is);
    }

    public static OntModel loadOntModel(InputStream inputStream) {
        OntModel ontModel = ModelFactory.createOntologyModel(ONT_MODEL_SPEC);

        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.setFileManager(FileManager.get());

        dm.addIgnoreImport("http://onto.fel.cvut.cz/ontologies/aviation/eccairs-form-static-0.2");
        //LocationMapper lm= FileManager.get().getLocationMapper();

        // load config
        ontModel.read(inputStream, null, FileUtils.langTurtle);

        dm.loadImports(ontModel);
        return ontModel;

    }

    public static OntModel loadOntModel(Path path) {
        try {
            return loadOntModel(new FileInputStream(path.toFile()));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not load file from path " + path, e);
        }
    }

    /**
     * Reads model directly from file bypassing cache.
     * @param path
     * @param lang
     * @return
     */
    public static Model loadModel(Path path, String lang) {
        try {
            return ModelFactory.createDefaultModel().read(new FileInputStream(path.toFile()), null, lang);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not load file from path " + path, e);
        }
    }

    public static Model loadModel(Path path) {
        return loadModel(path, FileUtils.langTurtle);
    }

//    public static Map<String, String> getAllBaseIris(String resourceDirPath, boolean recursive) {
//        OntologyManager.class.getResource(resourceDirPath).getPath()
//    }


    public static <T> List<T> toList(Iterable<T> directoryStream) {

        List<T> list = new ArrayList<>();
        directoryStream.forEach(list::add);
        return list;
    }


    public static Path getPathFromResource(String resourcePath) {
        //TODO toto nefunguje
        try {
            return Paths.get(OntoDocManager.class.getResource(resourcePath).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not resolve path " + resourcePath + " in resources : ", e);
        }
    }

    public static FileManager getFileManager() {
        return OntDocumentManager.getInstance().getFileManager();
    }

    public static void ignoreImport(String uri) {
        OntDocumentManager.getInstance().addIgnoreImport(uri);
    }

    public static String getBaseUri(Model model) {
        return model.listResourcesWithProperty(RDF.type, OWL.Ontology).nextResource().toString();
    }

    /**
     * Updates registered SHACL functions (i.e., functions which can be used in sparql queries SPipes modules and shacl
     * functions) based updated workspace (i.e. updated added and removed files in the workspace).
     * <p>
     * This method at the end of the processing of {@link OntoDocManager#registerDocuments(Iterable)} when updated, added
     * and deleted files in the workspace are identified.
     */
    static void updateSHACLFunctionsFromUpdatedWorkspace(Map<String, Model> file2Model, Set<Path> removedFiles) {
        log.warn("WORKAROUND -- Applying a workaround to register all SHACL modules ...");

        Map ontModleMap = new HashMap();

        file2Model.forEach((p,m) -> ontModleMap.put(
                p,
                !(m instanceof OntModel)
                        ? ModelFactory.createOntologyModel(ONT_MODEL_SPEC, m)
                        : m
        ));
        SPipesUtil.resetFunctions(ontModleMap, removedFiles);
    }

    class OntologyReadFailureHandler implements OntDocumentManager.ReadFailureHandler {
        @Override
        public void handleFailedRead(String url, Model model, Exception e) {

            if (e instanceof HttpException) {
                int responseCode = ((HttpException) e).getStatusCode();
                if (responseCode == 404) {
                    log.warn("Attempt to read ontology from {} returned HTTP code '404 - Not Found'.", url);
                    return;
                }
            }
            log.warn("Attempt to read ontology from {} failed. Msg was {}. {}", url, e.getMessage(), e);
        }
    }

    public static void setReloadFiles(boolean reloadFiles) {
        OntoDocManager.reloadFiles = reloadFiles;
    }

    /**
     * Returns a list of files within one or more locations with prefix in scriptPaths corresponding to the <code>uriStr</code>.
     * Searching for files associated with <code>uriStr</code> is done in the following order:
     *
     * <ol>
     * <li/> <code>uriStr</code> is in LocationMapper of OntDocumentManager - found mapped file is returned if exists in the file system.
     * <li/> <code>uriStr</code> is an absolute file uri - return if it exists in the file system.
     * <li/> <code>uriStr</code> is a relative path - return files with <code>path = root + uriStr</code>
     * where <code>root</code> is in <code>scriptPaths</code> and the file exists in the file system.
     * </ol>
     *
     * @param uriStr ontology uri, file uri or relative path
     * @param scriptPaths root paths to search for relative paths in. This is typically ContextsConfig.getScriptPaths()
     * @return List of files mapped to the uriStr
     */
    public static List<File> getScriptFiles(String uriStr, List<String> scriptPaths) {
        //1. Handle uriStr is an ontology iri
        // TODO - make it work with prefixed uris

        String path = OntDocumentManager.getInstance().doAltURLMapping(uriStr);
        Optional<File> f = Optional.ofNullable(path).map(File::new).filter(File::exists);
        if (f.isPresent())
            return Arrays.asList(f.get());

        //2. handle case where uriStr is a file uri
        URI uri = URI.create(path);

        if(uri.getScheme() != null && !uri.getScheme().equalsIgnoreCase("file") )
            return Collections.EMPTY_LIST;


        if(uri.getScheme() == null) {
            if(!uriStr.startsWith("[^/]") && !uriStr.matches("^[a-zA-Z]+:.*$")) {
                // search if relative uriStr is in any of the scriptPaths,
                // relative uri is one without a scheme starting with "./", "../" or path component.
                return scriptPaths.stream()
                        .distinct()
                        .map(p -> cannonicalFile(p, uriStr))
                        .distinct()
                        .filter(File::exists).toList();
            }

            // absolute path
            return Stream.of(new File(uriStr)).filter(File::exists).toList();
        }
        return Stream.of(new File(uri)).filter(File::exists).toList();
    }

    private static File cannonicalFile(String root, String relPath){
        String normalizedRoot = root.replaceFirst("([^/\\\\])$", "$1/");
        try{
            return new File(normalizedRoot, relPath).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
