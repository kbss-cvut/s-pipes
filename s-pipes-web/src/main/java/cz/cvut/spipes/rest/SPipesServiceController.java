package cz.cvut.spipes.rest;

import cz.cvut.spipes.config.ExecutionConfig;
import cz.cvut.spipes.engine.*;
import cz.cvut.spipes.exception.SPipesServiceException;
import cz.cvut.spipes.manager.SPipesScriptManager;
import cz.cvut.spipes.modules.Module;
import cz.cvut.spipes.rest.util.*;
import cz.cvut.spipes.util.JenaUtils;
import cz.cvut.spipes.util.RDFMimeType;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@EnableWebMvc
public class SPipesServiceController {

    /**
     * Request parameter - 'id' of the module to be executed
     */
    public static final String P_ID = "_pId";
    /**
     * Request parameter - URL of the resource containing configuration
     */
    public static final String P_CONFIG_URL = "_pConfigURL";
    /**
     * Input graph - URL of the file where input graph is stored
     */
    public static final String P_INPUT_GRAPH_URL = "_pInputGraphURL";
    /**
     * Input binding - URL of the file where input bindings are stored
     */
    public static final String P_INPUT_BINDING_URL = "_pInputBindingURL";
    /**
     * Output binding - URL of the file where output bindings are stored
     */
    public static final String P_OUTPUT_BINDING_URL = "_pOutputBindingURL";
    private static final Logger LOG = LoggerFactory.getLogger(SPipesServiceController.class);
    private final ResourceRegisterHelper resourceRegisterHelper;
    private final SPipesScriptManager scriptManager;


    @Autowired
    public SPipesServiceController() {
        this.resourceRegisterHelper = new ResourceRegisterHelper();
        scriptManager = ScriptManagerFactory.getSingletonSPipesScriptManager();
    }

    @GetMapping(
        value = "/module",
        produces = {
            RDFMimeType.LD_JSON_STRING,
            RDFMimeType.N_TRIPLES_STRING,
            RDFMimeType.RDF_XML_STRING,
            RDFMimeType.TURTLE_STRING
        }
    )
    public Model processGetRequest(@RequestParam MultiValueMap<String, String> parameters) {
        LOG.info("Processing GET request.");
        return runModule(ModelFactory.createDefaultModel(), parameters);
    }

    @ApiOperation(
        value = "Run a module."
    )
    @PostMapping(
        value = "/module",
        consumes = {
            RDFMimeType.LD_JSON_STRING,
            RDFMimeType.N_TRIPLES_STRING,
            RDFMimeType.RDF_XML_STRING,
            RDFMimeType.TURTLE_STRING
        },
        produces = {
            RDFMimeType.LD_JSON_STRING,
            RDFMimeType.N_TRIPLES_STRING,
            RDFMimeType.RDF_XML_STRING,
            RDFMimeType.TURTLE_STRING
        }
    )
    public Model processPostRequest(
        @ApiParam(value = "Input RDF model that is fed to the module. Additional models can be specified using"
            + " parameter '" + P_INPUT_GRAPH_URL + "'. In case, more than one model is specified, they are merged"
            + " into one union model before fed to the module.")
        @RequestBody Model inputModel,
        @RequestParam(name = P_ID)
        @ApiParam(value = "Id of the module.")
            String pId,
        @RequestParam(name = P_CONFIG_URL, required = false)
        @ApiParam(value = "Url used to set configuration of the module and possibly a logging.")
            String pConfigURL,
        @RequestParam(value = P_INPUT_GRAPH_URL, required = false)
        @ApiParam(value = "Url used to retrieve input graph for the module. See 'inputModel' parameter for"
            + " additional info.")
            String pInputGraphURL,
        @RequestParam(name = P_INPUT_BINDING_URL, required = false)
        @ApiParam(value = "Url used to retrieve input binding of the module. Note that additional request parameters"
            + " can be used for same purpose.")
            String pInputBindingURL,
        @RequestParam(name = P_OUTPUT_BINDING_URL, required = false)
        @ApiParam(value = "Url used to retrieve output binding of the module.")
            String pOutputBindingURL,
        @RequestParam MultiValueMap<String, String> parameters
    ) {
        LOG.info("Processing POST request.");
        // TODO process internal params passed arguments not parameters map
        return runModule(inputModel, parameters);
    }

    @GetMapping(
        value = "/service",
        produces = {
            RDFMimeType.LD_JSON_STRING + ";charset=utf-8",
            RDFMimeType.N_TRIPLES_STRING,
            RDFMimeType.RDF_XML_STRING,
            RDFMimeType.TURTLE_STRING
        }
    )
    public Model processServiceGetRequest(@RequestParam MultiValueMap<String, String> parameters) {
        LOG.info("Processing service GET request.");
        return runService(ModelFactory.createDefaultModel(), parameters);
    }

    /**
     * Processes the service POST request, can be used to upload multiple files for input binding.
     *
     * @param parameters url query parameters
     * @return a {@link Model} representing the processed RDF
     */
    @PostMapping(
        value = "/service",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
        produces = {
            RDFMimeType.LD_JSON_STRING + ";chaset=utf-8",
            RDFMimeType.N_TRIPLES_STRING,
            RDFMimeType.RDF_XML_STRING,
            RDFMimeType.TURTLE_STRING
        }
    )
    public Model processServicePostRequest(@RequestParam MultiValueMap<String, String> parameters,
                                           @RequestParam("files") MultipartFile[] files) {

        MultiValueMap<String, String> newParameters =
            new MultipartFileResourceResolver(resourceRegisterHelper).resolveResources(parameters, files);

        LOG.info("Processing service POST request, with {} multipart file(s).", files.length);
        return runService(ModelFactory.createDefaultModel(), newParameters);
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> notFoundHandler(SPipesServiceException e) {
        return Collections.singletonMap("message", e.getMessage());
    }

    private QuerySolution transform(final MultiValueMap<String, String> parameters) {
        final QuerySolutionMap querySolution = new QuerySolutionMap();

        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            // TODO types of RDFNode
            String value = entry.getValue().get(0);
            querySolution.add(entry.getKey(), ResourceFactory.createPlainLiteral(value));
        }

        return querySolution;
    }


    // TODO merge it with implementation in /module
    private Model runService(final Model inputDataModel, final MultiValueMap<String, String> parameters) {
        LOG.info("- parameters={}", parameters);

        String id = extractId(parameters);

        File outputBindingPath = extractOutputBindingPath(parameters);
        Model configModel = extractConfigurationModel(parameters);
        ExecutionContext inputExecutionContext = extractInputExecutionContext(inputDataModel, parameters);

        ExecutionEngine engine = createExecutionEngine(configModel);

        // EXECUTE PIPELINE
        ContextLoaderHelper.updateContextsIfNecessary(scriptManager);
        Module module = scriptManager.loadFunction(id);

        if (module == null) {
            throw new SPipesServiceException("Cannot load return module for a function with id=" + id);
        }
        ExecutionContext outputExecutionContext = engine.executePipeline(module, inputExecutionContext);

        if (outputBindingPath != null) {
            saveOutputBinding(outputBindingPath, outputExecutionContext.getVariablesBinding());
        }

        LOG.info("Processing successfully finished.");
        return outputExecutionContext.getDefaultModel();
    }

    private Model runModule(final Model inputDataModel, final MultiValueMap<String, String> parameters) {
        LOG.info("- parameters={}", parameters);

        String id = extractId(parameters);

        File outputBindingPath = extractOutputBindingPath(parameters);
        Model configModel = extractConfigurationModel(parameters);
        ExecutionContext inputExecutionContext = extractInputExecutionContext(inputDataModel, parameters);

        ExecutionEngine engine = createExecutionEngine(configModel);
        ContextLoaderHelper.updateContextsIfNecessary(scriptManager);
        Module module = PipelineFactory.loadModule(configModel.createResource(id));
        if (module == null) {
            throw new SPipesServiceException("Cannot load module with id=" + id);
        }
        ExecutionContext outputExecutionContext = engine.executePipeline(module, inputExecutionContext);

        if (outputBindingPath != null) {
            saveOutputBinding(outputBindingPath, outputExecutionContext.getVariablesBinding());
        }

        LOG.info("Processing successfully finished.");
        return outputExecutionContext.getDefaultModel();
    }

    private ExecutionContext extractInputExecutionContext(final Model inputDataModel, final MultiValueMap<String, String> parameters) {
        ServiceParametersHelper paramHelper = new ServiceParametersHelper(parameters);

        // FILE WHERE TO GET INPUT GRAPH
        URL inputGraphURL = null;
        if (paramHelper.hasParameterValue(P_INPUT_GRAPH_URL)) {
            inputGraphURL = paramHelper.parseParameterValueAsUrl(P_INPUT_GRAPH_URL);
            logParam(P_INPUT_GRAPH_URL, inputGraphURL.toString());
        }

        // FILE WHERE TO GET INPUT BINDING
        URL inputBindingURL = null;
        if (paramHelper.hasParameterValue(P_INPUT_BINDING_URL)) {
            inputBindingURL = paramHelper.parseParameterValueAsUrl(P_INPUT_BINDING_URL);
            logParam(P_INPUT_BINDING_URL, inputBindingURL.toString());
        }

        parameters.remove(P_INPUT_GRAPH_URL);
        parameters.remove(P_INPUT_BINDING_URL);

        final VariablesBinding inputVariablesBinding = new VariablesBinding(transform(parameters));
        if (inputBindingURL != null) {
            extendBindingFromURL(inputVariablesBinding, inputBindingURL);
        }
        LOG.info("- input variable binding ={}", inputVariablesBinding);

        Model unionModel = Optional.ofNullable(inputGraphURL)
            .map(url -> JenaUtils.createUnion(inputDataModel, loadModelFromUrl(url.toString())))
            .orElse(inputDataModel);

        return ExecutionContextFactory.createContext(unionModel, inputVariablesBinding);
    }

    private File extractOutputBindingPath(final MultiValueMap<String, String> parameters) {
        ServiceParametersHelper paramHelper = new ServiceParametersHelper(parameters);

        // FILE WHERE TO SAVE OUTPUT BINDING
        File outputBindingPath = null;
        if (paramHelper.hasParameterValue(P_OUTPUT_BINDING_URL)) {
            outputBindingPath = paramHelper.parseParameterValueAsFilePath(P_OUTPUT_BINDING_URL).toFile();
            logParam(P_OUTPUT_BINDING_URL, outputBindingPath.toString());
        }

        parameters.remove(P_OUTPUT_BINDING_URL);

        return outputBindingPath;
    }

    private void saveOutputBinding(File outputBindingPath, VariablesBinding outputVariablesBinding) {
        try {
            outputVariablesBinding.save(new FileOutputStream(outputBindingPath), FileUtils.langTurtle);
        } catch (IOException e) {
            throw new SPipesServiceException("Cannot save output binding.", e);
        }
    }

    private String extractId(final MultiValueMap<String, String> parameters) {
        ServiceParametersHelper paramHelper = new ServiceParametersHelper(parameters);

        // LOAD MODULE ID
        final String id = getId(paramHelper);
        logParam(P_ID, id);

        return id;
    }

    private Model extractConfigurationModel(final MultiValueMap<String, String> parameters) {
        ServiceParametersHelper paramHelper = new ServiceParametersHelper(parameters);

        // LOAD CONFIGURATION
        String configURL = null;
        if (paramHelper.hasParameterValue(P_CONFIG_URL)) {
            configURL = paramHelper.getRequiredParameterValue(P_CONFIG_URL);
            logParam(P_CONFIG_URL, configURL);
            parameters.remove(P_CONFIG_URL);
        }

        String cUrl = Optional.ofNullable(configURL)
            .orElse(ExecutionConfig.getConfigUrl());

        if (cUrl.isEmpty()) {
            return ModelFactory.createDefaultModel();
        }

        return loadModelFromUrl(cUrl);
    }

    private ExecutionEngine createExecutionEngine(Model configModel) {
        // CONFIGURE ENGINE
        ExecutionEngine engine = ExecutionEngineFactory.createEngine();
        ProgressListenerLoader.createListeners(configModel).forEach(
            engine::addProgressListener
        );
        return engine;
    }

    private void logParam(String parameterKey, String parameterValue) {
        LOG.info("- {}={}", parameterKey, parameterValue);
    }

    private @NotNull
    String getId(@NotNull final ServiceParametersHelper paramHelper) {

        if (paramHelper.hasParameterValue(P_ID)) {
            return paramHelper.getParameterValue(P_ID);
        }
        throw new SPipesServiceException("Invalid/no module id supplied.");
    }

    private @NotNull
    Model loadModelFromUrl(@NotNull String modelUrl) {
        final Model outputModel = ModelFactory.createDefaultModel();
        try {
            outputModel.read(modelUrl, FileUtils.langTurtle);
        } catch (Exception e) {
            throw new SPipesServiceException("Could not load model from URL " + modelUrl + ".");
        }
        return outputModel;
    }

    private void extendBindingFromURL(VariablesBinding inputVariablesBinding, URL inputBindingURL) {
        try {
            final VariablesBinding vb2 = new VariablesBinding();
            vb2.load(inputBindingURL.openStream(), FileUtils.langTurtle);
            VariablesBinding vb3 = inputVariablesBinding.extendConsistently(vb2);
            if (vb3.isEmpty()) {
                LOG.debug("- no conflict between bindings loaded from '{}' and those provided in query string.",
                    P_INPUT_BINDING_URL
                );
            } else {
                LOG.info("- conflicts found between bindings loaded from '{}' and those provided in query string: {}",
                    P_INPUT_BINDING_URL, vb3
                );
            }
        } catch (IOException e) {
            LOG.warn("Could not read data from parameter {}={}, caused by: {}", P_INPUT_BINDING_URL, inputBindingURL, e);
        }

    }

}
