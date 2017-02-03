package cz.cvut.sempipes.cli;



import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import cz.cvut.kbss.util.CmdLineUtils;
import cz.cvut.sempipes.config.ContextLoaderConfig;
import cz.cvut.sempipes.constants.AppConstants;
import cz.cvut.sempipes.constants.SM;
import cz.cvut.sempipes.engine.*;
import cz.cvut.sempipes.manager.OntoDocManager;
import cz.cvut.sempipes.manager.OntologyDocumentManager;
import cz.cvut.sempipes.manager.SempipesScriptManager;
import cz.cvut.sempipes.modules.Module;
import cz.cvut.sempipes.engine.PipelineFactory;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import org.apache.jena.util.LocationMapper;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author blcha
 */
public class ExecuteModuleCLI {

    // cat input-data.rdf | sem-pipes execute --instance "<http://url>"
    //                   --config-file "$PATH/config.ttl"
    //                   --input-binding-file "$PATH/input-binding.ttl" --output-binding-file "$PATH/output-binding.ttl"
    //                   --input-file --output-file
    // > output.data.rdf


    private static final Logger LOG = LoggerFactory.getLogger(ExecuteModuleCLI.class);
    private static final String DEFAULT_DELIMITER = ";";
    //@Option(name = "-d", aliases = "--delimiter", metaVar = "DELIMITER", usage = "Input variables data delimiter ('" + DEFAULT_DELIMITER + "' by default)")
    //private String delimiter = DEFAULT_DELIMITER;

    //   @Option(name = "-p", aliases = "--input-variables-pattern", metaVar = "PATTERN", usage = "Input variables pattern")
//    private String inputVariablesPattern;
    //@Option(name = "-i", aliases = "--input-variables-file", metaVar = "INPUT_FILE", usage = "Input variables file")
    //private File inputVariablesFile;

//    @Option(name = "-d", aliases = "--input-data-file", metaVar = "OUTPUT_FILE", usage = "Input data file")
//    private File inputRdfFile;
//    @Option(name = "-s", aliases = "--variables-pattern-strategy", metaVar = "VARIABLES_PATTERN_STRATEGY", usage = "Input variables pattern strategy i.e. exact-match|subset-match|superset-match")
//    private VariablesPatternStrategy variablesPatternStrategy; TODO


   //TURTLE, N-TRIPLES, JSON-LD, RDF/XML, RDF/XML-ABBREV, N3, RDF/JSON
    @Option(name = "-l", aliases = "--output-data-lang", metaVar = "OUTPUT_LANG", usage = "Output data lang (e.g. TURTLE, N-TRIPLES, JSON-LD, RDF/XML, RDF/XML-ABBREV, N3, RDF/JSON)")
    private String langRdfFile;

    @Option(name = "-o", aliases = "--output-data-file", metaVar = "OUTPUT_FILE", usage = "Output data file")
    private File outputRdfFile;
//    @Option(name = "-b", aliases = "--input-binding-file", metaVar = "INPUT_BINDING_FILE", usage = "Input binding file")
    private File inputBindingFile;
//    @Option(name = "-B", aliases = "--output-binding-file", metaVar = "OUTPUT_BINDING_FILE", usage = "Output binding file")
    private File outputBindingFile;
//    @Option(name = "-c", aliases = "--config-file", required = true, metaVar = "CONFIG_FILE", usage = "Module configuration file")
    private File configFile;
//    @Option(name = "-m", aliases = "--execute-module-only", usage = "Execute module only instead of whole pipeline")
    private boolean isExecuteModuleOnly;
    // TODO should be optional in case config file contains only one module
//    @Option(name = "-r", aliases = "--module-resource-uri", metaVar = "CONFIG_RESOURCE_URI", usage = "RDF configuration uri")
    private String configResourceUri;

    @Option(name = "-f", aliases = "--execute-only-one-function", usage = "Execute only one function (TEMPORARY)")
    private boolean isExecuteOnlyOneFunction;


    @Option(name = "-i", aliases = "--input-data-from-stdin", usage = "Input rdf is taken from also from std-in")
    private boolean isInputDataFromStdIn = false;
    @Option(name = "-I", aliases = "--input-data-file", usage = "Input rdf files", multiValued = true)
    private List<File> inputDataFiles;
    @Option(name = "-P", aliases = "--input-binding-parameter", usage = "Input binding parameter", multiValued = true)
    private Map<String, String> inputBindingParametersMap;

    @Argument(index = 0, metaVar = "EXECUTION_TARGET", usage = "Execution target id")
    private String executionTarget;
//    @Argument(index = 1, metaVar = "INPUT_RDF_FILES", usage = "Input rdf files")
//    private List<File> inputDataFiles;


    public static void main(String[] args) throws IOException {

        ExecuteModuleCLI asArgs = new ExecuteModuleCLI();

        // ---------- arguments parsing ------------
        CmdLineParser argParser = new CmdLineParser(asArgs);
        CmdLineUtils.parseCommandLine(args, argParser);

        String output = Arrays.stream(args).collect(Collectors.joining(" "));
        LOG.info("Executing external module/function ... " + output);

        // ----- load input model
        Model inputDataModel = ModelFactory.createDefaultModel();

        if (asArgs.inputDataFiles != null) {
            for (File idFile : asArgs.inputDataFiles) {
                LOG.debug("Loading input data from file {} ...", idFile);
                inputDataModel.read(new FileInputStream(idFile), null, FileUtils.langTurtle);
            }
        }
        if (asArgs.isInputDataFromStdIn) {
            LOG.info("Loading input data from std-in ...");
            inputDataModel.read(System.in, null, FileUtils.langTurtle);
        }

        // ----- load modules and functions
        LOG.debug("Loading  scripts ...");
        SempipesScriptManager scriptManager = scriptManager = createSempipesScriptManager();
        OntoDocManager.registerAllSPINModules();

        // ----- load input bindings
        VariablesBinding inputVariablesBinding = new VariablesBinding();
        if (asArgs.inputBindingParametersMap != null) {
            inputVariablesBinding = new VariablesBinding(transform(asArgs.inputBindingParametersMap));
            LOG.info("Loaded input variable binding ={}", inputVariablesBinding);
        }

        // ----- create execution context
        ExecutionContext inputExecutionContext = ExecutionContextFactory.createContext(inputDataModel, inputVariablesBinding);

        // ----- execute pipeline
        ExecutionEngine engine = ExecutionEngineFactory.createEngine();
        Module module = scriptManager.loadFunction(asArgs.executionTarget);
//          module =  PipelineFactory.loadModulePipeline(inputDataModel.listObjectsOfProperty(SM.returnModule).next().asResource());

        if ( module == null ) {
            throw new RuntimeException("Cannot load module/function with id=" + asArgs.executionTarget);
        }
        ExecutionContext outputExecutionContext = engine.executePipeline(module, inputExecutionContext);

        LOG.info("Processing successfully finished.");
       // outputExecutionContext.getDefaultModel().write(System.out);

//        Model inputBindingModel = null;
//
//        // load input binding
//        if (asArgs.inputBindingFile != null) {
//            inputBindingModel = ModelFactory.createDefaultModel();
//
//            inputBindingModel.read(new FileInputStream(asArgs.inputBindingFile), null);
//            throw new RuntimeException("Not implemented exception.");
//        }
//
//        // load config file
//        Model configModel = ModelFactory.createDefaultModel();
//
//        configModel.read(new FileInputStream(asArgs.configFile), null, FileUtils.langTurtle);
//
//        //TODO set up input binding
//        ExecutionContext inputExecutionContext = ExecutionContextFactory.createContext(inputDataModel);
//
//        ExecutionEngine engine = ExecutionEngineFactory.createEngine();
//        ExecutionContext outputExecutionContext = null;
//        Module module = null;
//        // should execute module only
//        if (asArgs.isExecuteModuleOnly) {
//            module = PipelineFactory.loadModule(configModel.createResource(asArgs.configResourceUri));
//            outputExecutionContext = engine.executeModule(module, inputExecutionContext);
//        } else {
//            module = PipelineFactory.loadPipeline(configModel.createResource(asArgs.configResourceUri));
//            outputExecutionContext = engine.executePipeline(module, inputExecutionContext);
//
//        }
//
//        //TODO return output binding
//
        // return output data
        if (asArgs.outputRdfFile != null) {
            outputExecutionContext.getDefaultModel().write(new FileOutputStream(asArgs.outputRdfFile), FileUtils.langTurtle);
        } else {
            outputExecutionContext.getDefaultModel().write(System.out, FileUtils.langTurtle);
        }

        return;
    }


//    /**
//     * @param args the command line arguments
//     */
//    public static void mainOld(String[] args) throws IOException {
//
//        ExecuteModuleCLI asArgs = new ExecuteModuleCLI();
//
//        // ---------- arguments parsing ------------
//        CmdLineParser argParser = new CmdLineParser(asArgs);
//        CmdLineUtils.parseCommandLine(args, argParser);
//
//        // TODO remove
//        String output = "execute " + Arrays.asList(args).stream().collect(Collectors.joining(" "));
//
//        //List<List<String>> rowsOfInputData = new LinkedList();
//        System.err.println("Executing external module ... ");
//
//        // load input model
//        Model inputDataModel = ModelFactory.createDefaultModel();
//
//        if (asArgs.inputDataFiles != null) {
//            for (File idFile : asArgs.inputDataFiles) {
//                inputDataModel.read(new FileInputStream(idFile), null);
//            }
//
//        }
//        if (asArgs.isInputDataFromStdIn) {
//            inputDataModel.read(System.in, null);
//        }
//
//        Model inputBindingModel = null;
//
//        // load input binding
//        if (asArgs.inputBindingFile != null) {
//            inputBindingModel = ModelFactory.createDefaultModel();
//
//            inputBindingModel.read(new FileInputStream(asArgs.inputBindingFile), null);
//            throw new RuntimeException("Not implemented exception.");
//        }
//
//        // load config file
//        Model configModel = ModelFactory.createDefaultModel();
//
//        configModel.read(new FileInputStream(asArgs.configFile), null, FileUtils.langTurtle);
//
//        //TODO set up input binding
//        ExecutionContext inputExecutionContext = ExecutionContextFactory.createContext(inputDataModel);
//
//        ExecutionEngine engine = ExecutionEngineFactory.createEngine();
//        ExecutionContext outputExecutionContext = null;
//        Module module = null;
//        // should execute module only
//        if (asArgs.isExecuteModuleOnly) {
//            module = PipelineFactory.loadModule(configModel.createResource(asArgs.configResourceUri));
//            outputExecutionContext = engine.executeModule(module, inputExecutionContext);
//        } else {
//            module = PipelineFactory.loadPipeline(configModel.createResource(asArgs.configResourceUri));
//            outputExecutionContext = engine.executePipeline(module, inputExecutionContext);
//
//        }
//
//        //TODO return output binding
//
//        // return output data
//        if (asArgs.outputRdfFile != null) {
//            outputExecutionContext.getDefaultModel().write(new FileOutputStream(asArgs.outputRdfFile), FileUtils.langTurtle);
//        } else {
//            outputExecutionContext.getDefaultModel().write(System.out, FileUtils.langTurtle);
//        }
//
//        return;
//    }


    private static SempipesScriptManager createSempipesScriptManager() {
        List<Path> scriptPaths = new LinkedList<>();

        // load from environment variables
        String sempipesOntologiesPathsStr = System.getenv(AppConstants.SYSVAR_SEMPIPES_ONTOLOGIES_PATH);
        LOG.debug("Loading scripts from system variable {} = {}", AppConstants.SYSVAR_SEMPIPES_ONTOLOGIES_PATH, sempipesOntologiesPathsStr);
        if (sempipesOntologiesPathsStr != null) {
            scriptPaths.addAll(
                    Arrays.stream(sempipesOntologiesPathsStr.split(";"))
                    .map(path -> Paths.get(path))
                    .collect(Collectors.toList())
            );
        }

        // load ontologies from config file

        // load ontologies from parameters

        // load ontologies from current working directory

            OntologyDocumentManager ontoDocManager = OntoDocManager.getInstance();
            List<String> globalScripts = registerGlobalScripts(ontoDocManager, scriptPaths);
            return new SempipesScriptManager(
                    ontoDocManager,
                    globalScripts
            );
    }


    // TODO merge with same method from ContextLoaderHelper !!!!!!!
    public static List<String> registerGlobalScripts(OntologyDocumentManager ontDocManager, List<Path> scriptPaths) {
        scriptPaths.forEach(
                ontDocManager::registerDocuments
        );

        LocationMapper locMapper = ontDocManager.getOntDocumentManager().getFileManager().getLocationMapper();

        List<String> _globalScripts = new LinkedList<>();

        locMapper.listAltEntries().forEachRemaining(
                ontoUri -> {
                    String loc = locMapper.getAltEntry(ontoUri);
                    if (loc.endsWith(".sms.ttl")) {
                        LOG.info("Registering script from file " + loc + ".");
                        _globalScripts.add(ontoUri);
                    }
                }
        );
        return _globalScripts;
    }



    // TODO move
    private static QuerySolution transform(final Map<String,String> parameters) {
        final QuerySolutionMap querySolution = new QuerySolutionMap();

        parameters.entrySet().forEach(
                e -> {
                    querySolution.add(e.getKey(), ResourceFactory.createPlainLiteral(e.getValue()));
                }
        );
        return querySolution;
    }

    private static Model loadModel(File modelFile) throws FileNotFoundException {
        Model m = ModelFactory.createDefaultModel();
        m.read(new FileInputStream(modelFile), null);

        return  m;
    }
}
