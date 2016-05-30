package cz.cvut.sempipes.cli;



import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.istack.internal.NotNull;
import cz.cvut.kbss.util.CmdLineUtils;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextImpl;
import cz.cvut.sempipes.engine.ExecutionEngine;
import cz.cvut.sempipes.engine.ExecutionEngineImpl;
import cz.cvut.sempipes.modules.Module;
import cz.cvut.sempipes.modules.ModuleFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.util.JenaUtil;


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

    @Option(name = "-i", aliases = "--input-data-from-stdin", usage = "Input rdf is taken from also from std-in")
    private boolean isInputDataFromStdIn = false;
    @Option(name = "-o", aliases = "--output-data-file", metaVar = "OUTPUT_FILE", usage = "Output data file")
    private File outputRdfFile;
    @Option(name = "-b", aliases = "--input-binding-file", metaVar = "INPUT_BINDING_FILE", usage = "Input binding file")
    private File inputBindingFile;
    @Option(name = "-B", aliases = "--output-binding-file", metaVar = "OUTPUT_BINDING_FILE", usage = "Output binding file")
    private File outputBindingFile;
    @Option(name = "-c", aliases = "--config-file", required = true, metaVar = "CONFIG_FILE", usage = "Module configuration file")
    private File configFile;
    @Option(name = "-m", aliases = "--execute-module-only", usage = "Execute module only instead of whole pipeline")
    private boolean isExecuteModuleOnly;
    // TODO should be optional in case config file contains only one module
    @Option(name = "-r", aliases = "--module-resource-uri", metaVar = "CONFIG_RESOURCE_URI", usage = "RDF configuration uri")
    private String configResourceUri;
     @Argument(index = 0, metaVar = "INPUT_RDF_FILES", usage = "Input rdf files")
     private List<File> inputDataFiles;

    // TODO remove
    private static boolean WAIT_FOR_DEBUGGER = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        ExecuteModuleCLI asArgs = new ExecuteModuleCLI();

        // ---------- arguments parsing ------------
        CmdLineParser argParser = new CmdLineParser(asArgs);
        CmdLineUtils.parseCommandLine(args, argParser);

        // TODO remove
        String output = "execute " + Arrays.asList(args).stream().collect(Collectors.joining(" "));
        Path log = null;
        if (WAIT_FOR_DEBUGGER) {
            log = Paths.get("/home/blcha/sempipes-logpipe.txt");
            Files.write(log, output.getBytes());
        }
        log = Paths.get("/home/blcha/sempipes-log.txt");
        Files.write(log, output.getBytes());

        //List<List<String>> rowsOfInputData = new LinkedList();
        System.err.println("Executing external module ... ");

        ExecutionEngine e = new ExecutionEngineImpl();

        // load input model
        Model inputDataModel = ModelFactory.createDefaultModel();

        if (asArgs.inputDataFiles != null) {
            for (File idFile : asArgs.inputDataFiles) {
                inputDataModel.read(new FileInputStream(idFile), null);
            }

        }
        if (asArgs.isInputDataFromStdIn) {
            inputDataModel.read(System.in, null);
        }

        Model inputBindingModel = null;

        // load input binding
        if (asArgs.inputBindingFile != null) {
            inputBindingModel = ModelFactory.createDefaultModel();

            inputBindingModel.read(new FileInputStream(asArgs.inputBindingFile), null);
            throw new RuntimeException("Not implemented exception.");
        }

        // load config file
        Model configModel = ModelFactory.createDefaultModel();

        configModel.read(new FileInputStream(asArgs.configFile), null, FileUtils.langTurtle);


        ExecutionContext outputExecutionContext = new ExecutionContextImpl();

        // should execute module only
        if (asArgs.isExecuteModuleOnly) {

            Module module = ModuleFactory.loadModule(configModel.createResource(asArgs.configResourceUri));


            ExecutionContext inputExecutionContext = new ExecutionContextImpl();

            // set up input data
            inputExecutionContext.setDefaultModel(inputDataModel);

            //TODO set up input binding
            outputExecutionContext = module.execute(inputExecutionContext);
        }

        //TODO return output binding
        QueryExecutionFactory fac;


        // return output data
        if (asArgs.outputRdfFile != null) {
            outputExecutionContext.getDefaultModel().write(new FileOutputStream(asArgs.outputRdfFile), FileUtils.langTurtle);
        } else {
            outputExecutionContext.getDefaultModel().write(System.out, FileUtils.langTurtle);
        }

        return;
    }

    private static Model loadModel(File modelFile) throws FileNotFoundException {
        Model m = ModelFactory.createDefaultModel();
        m.read(new FileInputStream(modelFile), null);

        return  m;
    }
}
