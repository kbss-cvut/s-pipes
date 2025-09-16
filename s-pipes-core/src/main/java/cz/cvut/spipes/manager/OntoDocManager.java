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
import java.util.stream.Stream;

//import static cz.cvut.spipes.manager.OntologyDocumentManagerImpl.isFileNameSupported;

/**
 */
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
 **/
public class OntoDocManager implements OntologyDocumentManager {

    private static final Logger log = LoggerFactory.getLogger(OntoDocManager.class);
    private static Instant lastTime = Instant.now();
    private static boolean reloadFiles = false;

    private static Map<String, Model> allChangedFiles = new HashMap<>();
    private static Set<Path> removedFiles = new HashSet<>();
    private static Set<Path> managedFiles;


    OntDocumentManager ontDocumentManager;
    static OntoDocManager sInstance;
    static String[] SUPPORTED_FILE_EXTENSIONS = {"n3", "nt", "ttl", "rdf", "owl"}; //TODO json-ld

    public static OntModelSpec ONT_MODEL_SPEC = OntModelSpec.OWL_MEM;
    public static Set<String> dirtyModels = new HashSet<>();

    private OntoDocManager() {
        this(new OntDocumentManager());
        clearShaclRelevantModel();
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

    @Override
    public void registerDocuments(Path directoryOrFilePath) {

        if (Files.isDirectory(directoryOrFilePath) && Files.isSymbolicLink(directoryOrFilePath)) {
            log.error("Cannot register documents from directory {}. Directories that are symbolic links " +
                     "are not supported.", directoryOrFilePath );
            throw new IllegalArgumentException("Symbolic link directories in 'scripts.contextPaths' variable are not supported: " + directoryOrFilePath);
        }

        // get all baseIRIs
        Map<String, String> file2baseIRI = getAllBaseIris(directoryOrFilePath);

        // load it to document manager
        file2baseIRI.entrySet().forEach(e -> {
                    ontDocumentManager.addAltEntry(e.getKey(), e.getValue());
                }
        );
    }

    @Override
    public void registerDocuments(Iterable<Path> fileOrDirectoryPath) {
        fileOrDirectoryPath.forEach(
                this::registerDocuments
        );
        clearOntModelsImportingDirtyModel();
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
     * Cache is implemented by OntDocumentManager.getInstance().getFileManager(). Additionally, jena may store models in
     * OntModelSpec.OWL_MEM.getBaseModelMaker and OntModelSpec.OWL_MEM.getImportModelMaker. Specifically, the
     * ImportModelMaker stores imports and it is necessary to clear.
     * @param filePath
     */
    protected static void clearCachedModel(Path filePath){
        LocationMapper lm  = OntDocumentManager.getInstance().getFileManager().getLocationMapper();
        Iterator<String> altEntries = lm.listAltEntries();
        String pathString = filePath.toString();
        Set<String> urisToClear =  new HashSet<>();
        while(altEntries.hasNext()){
            String uri = altEntries.next();
            if(!Optional.ofNullable(lm.getAltEntry(uri)).map(s -> s.equals(pathString)).orElse(false))
                continue;
            urisToClear.add(uri);
        }
        for(String uri : urisToClear) {
            lm.removeAltEntry(uri);
            clearCachedModel(uri);
        }
    }

    protected void clearOntModelsImportingDirtyModel(){
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
                             .ifPresent(i -> ontDocumentManager.getFileManager().removeCacheModel(iri));
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
    public synchronized static Map<String, Model> getAllFile2Model(Path directoryOrFilePath) {
        Map<String, Model> file2Model = new HashMap<>();
        Set<Path> availableFiles = new HashSet<>();

        try (Stream<Path> stream = Files.walk(directoryOrFilePath)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(f -> {
                        String fileName = f.getFileName().toString();
                        return isFileNameSupported(fileName);
                    })
                    .forEach(file -> {
                        availableFiles.add(file.toAbsolutePath());
                        if (reloadFiles && !wasModified(file)) {
                            log.debug("Skipping unmodified file: {}", file.toUri());
                            return;
                        }

                        clearCachedModel(file);
                        String lang = FileUtils.guessLang(file.getFileName().toString());

                        log.info("Loading model from {} ...", file.toUri());
                        Model model = loadModel(file, lang);

                        if (model != null) {
                            OntoDocManager.addSHACLRelevantModel(file.toAbsolutePath().toString(), model);
                            file2Model.put(file.toString(), model);
                            log.debug("Successfully loaded model from {}.", file.toUri());
                        } else {
                            log.warn("Failed to load model from {}", file.toUri());
                        }
                    });
        } catch (IOException | DirectoryIteratorException e) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            log.error("Could not load ontologies from directory {} -- {} .", directoryOrFilePath, e);
        }

        // remove deleted files from cache
        if(reloadFiles){
            removedFiles = new HashSet<>();
            if(managedFiles != null) {
                removedFiles.addAll(managedFiles);
                removedFiles.removeAll(availableFiles);
            }

            for(Path removedFile : removedFiles)
                clearCachedModel(removedFile);
            managedFiles = availableFiles;
        }
        return file2Model;
    }

    // TODO remove this method !!!
    private static void addSHACLRelevantModel(String fileName, Model model) {
        String baseURI = JenaUtils.getBaseUri(model);

        if (baseURI != null) {
//            if (baseURI.contains("spin")
//                    || baseURI.contains("w3.org")
//                    || baseURI.contains("topbraid")
//                    || baseURI.contains("ontologies.lib")
//                    || baseURI.contains("function")
//                    || baseURI.contains("lib")
//                    ) {
            //LOG.debug("Adding library ... " + baseURI);
//                if (fileName.endsWith("spin-function.spin.ttl")) {
            allChangedFiles.put(fileName, model);
//                }
//            }
        }
    }

    private static void clearShaclRelevantModel() {
        allChangedFiles.clear();
        removedFiles.clear();
    }

    /**
     * Returns mapping filePath --> baseUri for all files recursively defined by <code>directoryOrFilePath</code>.
     * BaseUri is uri of the ontology defined in the model loaded from the file.
     * If <code>this.reloadFiles=true</code>, it ignores files that are older than <code>this.lastTime</code>.
     *
     * @param directoryOrFilePath File or directory to by searched recursively for models.
     * @return Mapping filePath to baseURI.
     */
    static Map<String, String> getAllBaseIris(Path directoryOrFilePath) {

        Map<String, String> baseUri2file = new HashMap<>();

        getAllFile2Model(directoryOrFilePath).entrySet().forEach(e -> {
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


    public static void loadAllBaseIrisFromResourceDir(String resourceDirPath) {
        loadAllBaseIrisFromDir(getPathFromResource(resourceDirPath));
    }


    public static void loadAllBaseIrisFromDir(Path directoryPath) {

        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.setFileManager(FileManager.get());
        LocationMapper lm = FileManager.get().getLocationMapper();

        getAllFile2Model(directoryPath).entrySet().forEach(e -> {
            Model model = e.getValue();
            String file = e.getKey();

            String baseURI = model.listResourcesWithProperty(RDF.type, OWL.Ontology).nextResource().toString();

            dm.getFileManager().addCacheModel(baseURI, model);

        });

//        getAllBaseIris(directoryPath).entrySet().stream().forEach(
//                e -> {
//                    String baseUri = e.getKey();
//                    String filePath = e.getValue();
//
//                    LOG.info("Loading mapping {} -> {}.", baseUri, filePath);
//                    lm.addAltPrefix(baseUri, filePath);
//                }
//        );
//
//        dm.getFileManager().setLocationMapper(lm);
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
     * Updates registered shacl functions (i.e., functions which can be used in sparql queries spipes modules and shacl
     * functions) based updated workspace (i.e. updated added and removed files in the workspace).
     * <p>
     * This method is designed to be called after {@link OntoDocManager#registerDocuments(Iterable)} ( or more specifically
     * {@link OntoDocManager#getAllFile2Model(Path)}) which identifies updated, added and deleted files in the workspace.
     */
    public static void updateSHACLFunctionsFromUpdatedWorkspace() {
        log.warn("WORKAROUND -- Applying a workaround to register all SHACL modules ...");

        Map ontModleMap = new HashMap();

        allChangedFiles.forEach((p,m) -> ontModleMap.put(
                p,
                !(m instanceof OntModel)
                        ? ModelFactory.createOntologyModel(ONT_MODEL_SPEC, m)
                        : m
        ));
        SPipesUtil.resetFunctions(ontModleMap, removedFiles);
        clearShaclRelevantModel();
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
