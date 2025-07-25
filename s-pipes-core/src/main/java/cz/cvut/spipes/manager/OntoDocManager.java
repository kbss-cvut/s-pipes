package cz.cvut.spipes.manager;

import cz.cvut.spipes.config.CompatibilityConfig;
import cz.cvut.spipes.util.JenaUtils;
import cz.cvut.spipes.util.SPipesUtil;
import cz.cvut.spipes.util.SparqlMotionUtils;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;
import org.apache.jena.util.LocationMapper;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    // TODO remove !!!!!!! this is workaround for registering SPIN related things.
    private static Model allLoadedFilesModel = ModelFactory.createDefaultModel();


    OntDocumentManager ontDocumentManager;
    static OntoDocManager sInstance;
    static String[] SUPPORTED_FILE_EXTENSIONS = {"n3", "nt", "ttl", "rdf", "owl"}; //TODO json-ld


    private OntoDocManager() {
        this(new OntDocumentManager());
        clearSPINRelevantModel();
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
        return ontDocumentManager.getOntology(uri, OntModelSpec.OWL_MEM);
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
    public static Map<String, Model> getAllFile2Model(Path directoryOrFilePath) {
        Map<String, Model> file2Model = new HashMap<>();

        try (Stream<Path> stream = Files.walk(directoryOrFilePath)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(f -> {
                        String fileName = f.getFileName().toString();
                        return isFileNameSupported(fileName);
                    })
                    .forEach(file -> {
                        if (reloadFiles && !wasModified(file)) {
                            log.debug("Skipping unmodified file: {}", file.toUri());
                            return;
                        }
                        String lang = FileUtils.guessLang(file.getFileName().toString());

                        log.info("Loading model from {} ...", file.toUri());
                        Model model = loadModel(file, lang);

                        if (model != null) {
                            OntoDocManager.addSPINRelevantModel(file.toAbsolutePath().toString(), model);
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
        return file2Model;
    }

    // TODO remove this method !!!
    private static void addSPINRelevantModel(String fileName, Model model) {
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
            allLoadedFilesModel.add(model);
//                }
//            }
        }
    }

    private static void clearSPINRelevantModel() {
        allLoadedFilesModel = ModelFactory.createDefaultModel();
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
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

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

    public static void registerAllSPINModules() {
        log.warn("WORKAROUND -- Applying a workaround to register all SPIN modules ..."); // TODO remove this workaround
        Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.add(OntoDocManager.allLoadedFilesModel);
        SPipesUtil.resetFunctions(model);
        clearSPINRelevantModel();
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
}
