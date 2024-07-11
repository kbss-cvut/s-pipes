package cz.cvut.spipes.manager;

import com.google.common.collect.HashMultimap;
import cz.cvut.spipes.config.CompatibilityConfig;
import cz.cvut.spipes.util.JenaUtils;
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
import org.topbraid.spin.system.SPINModuleRegistry;

import java.io.*;
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

    private static final Logger LOG = LoggerFactory.getLogger(OntoDocManager.class);
    private static final HashMap<Path, Instant> filesModificationTime = new HashMap<>();
    private static final HashMultimap<Path, Path> loadedFiles = HashMultimap.create(); // {directory: filesInDirectory}
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
            LOG.warn("Ignoring to register documents from directory {}. Directories that are symbolic links " +
                     "are not supported.", directoryOrFilePath );
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

    private static boolean wasModifiedOrNewlyAdded(Path file){
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(file, BasicFileAttributes.class);
            return  !filesModificationTime.containsKey(file) || attr.lastModifiedTime().toInstant().isAfter(filesModificationTime.get(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Map<String, Model> getAllFile2Model(Path directoryOrFilePath) {
        Map<String, Model> file2Model = new HashMap<>();

        if(reloadFiles){
            checkFileExistence(directoryOrFilePath);
        }

        try (Stream<Path> stream = Files.walk(directoryOrFilePath)) {
            stream
                    .filter(f -> !Files.isDirectory(f))
                    .filter(f -> {
                        String fileName = f.getFileName().toString();
                        return isFileNameSupported(fileName);
                    })
                    .filter(f -> !reloadFiles || wasModifiedOrNewlyAdded(f))
                    .forEach(file -> {
                        String lang = FileUtils.guessLang(file.getFileName().toString());
                        loadedFiles.put(directoryOrFilePath, file);

                        LOG.debug("Loading model from {} ...", file.toUri().toString());
                        Model model = loadModel(file, lang);

                        if (model != null) {
                            OntoDocManager.addSPINRelevantModel(file.toAbsolutePath().toString(), model);
                        }
                        if(reloadFiles){
                            BasicFileAttributes attr;
                            try {
                                attr = Files.readAttributes(file, BasicFileAttributes.class);
                                filesModificationTime.put(file, attr.lastModifiedTime().toInstant());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        file2Model.put(file.toString(), model);

                    });
        } catch (IOException | DirectoryIteratorException e) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            LOG.error("Could not load ontologies from directory {} -- {} .", directoryOrFilePath, e);
        }
        return file2Model;
    }

    private static void checkFileExistence(Path directoryOrFilePath) {
        for (Path p: loadedFiles.get(directoryOrFilePath)){
            String file = p.toString();
            try{
                File f = new File(file);
                if (!f.exists()) {
                    throw new FileNotFoundException(file);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
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

    static Map<String, String> getAllBaseIris(Path directoryorFilePath) {

        Map<String, String> baseUri2file = new HashMap<>();

        getAllFile2Model(directoryorFilePath).entrySet().forEach(e -> {
            String file = e.getKey();
            Model model = e.getValue();

            String baseURI = JenaUtils.getBaseUri(model);

            if (baseURI == null) {
                LOG.info("Ignoring file \"" + file + "\" as it does not contain baseURI.");
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
        LOG.warn("WORKAROUND -- Applying a workaround to register all SPIN modules ..."); // TODO remove this workaround
        Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.add(OntoDocManager.allLoadedFilesModel);
        SPINModuleRegistry.get().reset();
//        if (!SPINModuleRegistry.get().getFunctions().isEmpty()) {
//            LOG.error("SPIN registry was not cleared.");
//        }
        SPINModuleRegistry.get().init();
        SPINModuleRegistry.get().registerAll(model, null);
        clearSPINRelevantModel();
//        OntModel model = ModelFactory.createOntologyModel();
//        model.add(OntoDocManager.allLoadedFilesModel);
//        SPINModuleRegistry.get().registerAll(model, null);

    }
    class OntologyReadFailureHandler implements OntDocumentManager.ReadFailureHandler {
        @Override
        public void handleFailedRead(String url, Model model, Exception e) {

            if (e instanceof HttpException) {
                int responseCode = ((HttpException) e).getResponseCode();
                if (responseCode == 404) {
                    LOG.warn("Attempt to read ontology from {} returned HTTP code '404 - Not Found'.", url);
                    return;
                }
            }
            LOG.warn("Attempt to read ontology from {} failed. Msg was {}. {}", url, e.getMessage(), e);
        }
    }

    public static void setReloadFiles(boolean reloadFiles) {
        OntoDocManager.reloadFiles = reloadFiles;
    }
}
