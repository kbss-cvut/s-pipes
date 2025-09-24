package cz.cvut.spipes.rest;

import cz.cvut.spipes.config.ExecutionConfig;
import cz.cvut.spipes.engine.*;
import cz.cvut.spipes.exception.SPipesServiceException;
import cz.cvut.spipes.manager.SPipesScriptManager;
import cz.cvut.spipes.manager.factory.ContextLoaderHelper;
import cz.cvut.spipes.manager.factory.ScriptManagerFactory;
import cz.cvut.spipes.modules.Module;
import cz.cvut.spipes.rest.util.*;
import cz.cvut.spipes.util.JenaUtils;
import cz.cvut.spipes.util.RDFMimeType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.util.FileUtils;
import org.jetbrains.annotations.NotNull;
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
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static cz.cvut.spipes.manager.factory.ContextLoaderHelper.isKeepUpdated;
import static cz.cvut.spipes.util.VariableBindingUtils.extendBindingFromURL;

@Slf4j
@RestController
@EnableWebMvc
public class SPipesServiceController {

    private final ResourceRegisterHelper resourceRegisterHelper;
    private final SPipesScriptManager scriptManager;


    @Autowired
    public SPipesServiceController() {
        this.resourceRegisterHelper = new ResourceRegisterHelper();
        scriptManager = ScriptManagerFactory.getSingletonSPipesScriptManager();
    }

    @RdfApiResponse
    @GetMapping(
        value = "/module",
        produces = {
            RDFMimeType.LD_JSON_STRING,
            RDFMimeType.N_TRIPLES_STRING,
            RDFMimeType.RDF_XML_STRING,
            RDFMimeType.TURTLE_STRING
        }
    )
    public Model processGetRequest(
            @RequestParam @Schema(description = "Request Parameters",
            example = "{\"_pId\": \"value1\", \"param2\": \"value2\"}")
                                       MultiValueMap<String, String> parameters) {
        log.info("Processing GET request.");
        return runModule(ModelFactory.createDefaultModel(), parameters);
    }

    @RdfApiResponse
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
    public @Schema(hidden = true) Model processPostRequest(
        @Parameter(description = "Input RDF model that is fed to the module. Additional models can be specified using"
            + " parameter '" + ReservedParams.P_INPUT_GRAPH_URL + "'. In case, more than one model is specified, they are merged"
            + " into one union model before fed to the module.", hidden = true)
        @RequestBody Model inputModel,
        @RequestParam(name = ReservedParams.P_ID)
        @Parameter(description = "Id of the module.")
            String pId,
        @RequestParam(name = ReservedParams.P_CONFIG_URL, required = false)
        @Parameter(description = "Url used to set configuration of the module and possibly a logging.")
            String pConfigURL,
        @RequestParam(value = ReservedParams.P_INPUT_GRAPH_URL, required = false)
        @Parameter(description = "Url used to retrieve input graph for the module. See 'inputModel' parameter for"
            + " additional info.")
            String pInputGraphURL,
        @RequestParam(name = ReservedParams.P_INPUT_BINDING_URL, required = false)
        @Parameter(description = "Url used to retrieve input binding of the module. Note that additional request parameters"
            + " can be used for same purpose.")
            String pInputBindingURL,
        @RequestParam(name = ReservedParams.P_OUTPUT_BINDING_URL, required = false)
        @Parameter(description = "Url used to retrieve output binding of the module.")
            String pOutputBindingURL,
        @RequestParam MultiValueMap<String, String> parameters
    ) {
        log.info("Processing POST request.");
        // TODO process internal params passed arguments not parameters map
        return runModule(inputModel, parameters);
    }

    @RdfApiResponse
    @GetMapping(
        value = "/service",
        produces = {
            RDFMimeType.LD_JSON_STRING + ";charset=utf-8",
            RDFMimeType.N_TRIPLES_STRING,
            RDFMimeType.RDF_XML_STRING,
            RDFMimeType.TURTLE_STRING
        }
    )
    public Model processServiceGetRequest(
            @RequestParam @Schema(description = "Request Parameters",
                    example = "{\"_pId\": \"value1\", \"param2\": \"value2\"}")
            MultiValueMap<String, String> parameters) {
        log.info("Processing service GET request.");
        return runService(ModelFactory.createDefaultModel(), parameters);
    }

    /**
     * Processes the service POST request, can be used to upload multiple files for input binding.
     *
     * @param parameters url query parameters
     * @return a {@link Model} representing the processed RDF
     */
    @RdfApiResponse
    @PostMapping(
        value = "/service",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
        produces = {
            RDFMimeType.LD_JSON_STRING + ";charset=utf-8",
            RDFMimeType.N_TRIPLES_STRING,
            RDFMimeType.RDF_XML_STRING,
            RDFMimeType.TURTLE_STRING
        }
    )
    public Model processServicePostRequest(@RequestParam @Schema(description = "Request Parameters",
           example = "{\"_pId\": \"value1\", \"param2\": \"value2\"}")
           MultiValueMap<String, String> parameters,
           @RequestParam("files") MultipartFile[] files) {

        List<StreamResourceDTO> newStreamResources = new LinkedList<>();
        MultiValueMap<String, String> newParameters =
            new MultipartFileResourceResolver(resourceRegisterHelper).resolveResources(
                parameters,
                files,
                newStreamResources
            );

        log.info("Processing service POST request, with {} multipart file(s).", files.length);
        Model model =  runService(ModelFactory.createDefaultModel(), newParameters);
        if (! newStreamResources.isEmpty()) {
            log.info(
                "Loosing reference to stream resources: " +
                newStreamResources.stream().map(StreamResourceDTO::getId).collect(Collectors.toList())
            );
        }
        return model;
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
        log.info("- parameters={}", parameters);

        String id = extractId(parameters);

        File outputBindingPath = extractOutputBindingPath(parameters);
        Model configModel = extractConfigurationModel(parameters);
        ContextLoaderHelper.updateContextsIfNecessary(scriptManager);
        ExecutionContext inputExecutionContext = extractInputExecutionContext(
                inputDataModel, parameters, ExecutionContextFactory::createFunctionContext);

        ExecutionEngine engine = createExecutionEngine(configModel);

        // EXECUTE PIPELINE
        Module module = scriptManager.loadFunction(id);

        if (module == null) {
            throw new SPipesServiceException("Cannot load return module for a function with id=" + id);
        }
        ExecutionContext outputExecutionContext = engine.executePipeline(module, inputExecutionContext);

        if (outputBindingPath != null) {
            saveOutputBinding(outputBindingPath, outputExecutionContext.getVariablesBinding());
        }

        log.info("Processing successfully finished.");
        return outputExecutionContext.getDefaultModel();
    }

    private Model runModule(final Model inputDataModel, final MultiValueMap<String, String> parameters) {
        log.info("- parameters={}", parameters);

        String id = extractId(parameters);

        File outputBindingPath = extractOutputBindingPath(parameters);
        ContextLoaderHelper.updateContextsIfNecessary(scriptManager);
        ExecutionContext inputExecutionContext = extractInputExecutionContext(inputDataModel, parameters,
                (m, b) -> ExecutionContextFactory.createModuleContext(m, b, ReservedParams.P_CONFIG_URL));

        Model configModel = extractConfigurationModel(parameters);

        ExecutionEngine engine = createExecutionEngine(configModel);
        Module module = null;
        if (isKeepUpdated()) {
            module = scriptManager.loadModule(id, null, inputExecutionContext.getScriptUri());
        } else {
            module = PipelineFactory.loadModule(configModel.createResource(id));
        }
        if (module == null) {
            throw new SPipesServiceException("Cannot load module with id=" + id);
        }
        ExecutionContext outputExecutionContext = engine.executePipeline(module, inputExecutionContext);

        if (outputBindingPath != null) {
            saveOutputBinding(outputBindingPath, outputExecutionContext.getVariablesBinding());
        }

        log.info("Processing successfully finished.");
        return outputExecutionContext.getDefaultModel();
    }

    private ExecutionContext extractInputExecutionContext(final Model inputDataModel,
                                                          final MultiValueMap<String, String> parameters,
                                                          BiFunction<Model, VariablesBinding, ExecutionContext> executionContextFactory) {
        ServiceParametersHelper paramHelper = new ServiceParametersHelper(parameters);

        // FILE WHERE TO GET INPUT GRAPH
        URL inputGraphURL = null;
        if (paramHelper.hasParameterValue(ReservedParams.P_INPUT_GRAPH_URL)) {
            inputGraphURL = paramHelper.parseParameterValueAsUrl(ReservedParams.P_INPUT_GRAPH_URL);
            logParam(ReservedParams.P_INPUT_GRAPH_URL, inputGraphURL.toString());
        }

        // FILE WHERE TO GET INPUT BINDING
        URL inputBindingURL = null;
        if (paramHelper.hasParameterValue(ReservedParams.P_INPUT_BINDING_URL)) {
            inputBindingURL = paramHelper.parseParameterValueAsUrl(ReservedParams.P_INPUT_BINDING_URL);
            logParam(ReservedParams.P_INPUT_BINDING_URL, inputBindingURL.toString());
        }

        parameters.remove(ReservedParams.P_INPUT_GRAPH_URL);
        parameters.remove(ReservedParams.P_INPUT_BINDING_URL);

        final VariablesBinding inputVariablesBinding = new VariablesBinding(transform(parameters));
        if (inputBindingURL != null) {
            try {
                extendBindingFromURL(inputVariablesBinding, inputBindingURL);
            }
            catch (IOException e){
                log.warn("Could not read data from parameter {}={}, caused by: {}", ReservedParams.P_INPUT_BINDING_URL, inputBindingURL, e.getMessage());
            }
        }
        log.info("- input variable binding ={}", inputVariablesBinding);

        Model unionModel = Optional.ofNullable(inputGraphURL)
            .map(url -> JenaUtils.createUnion(inputDataModel, loadModelFromUrl(url.toString())))
            .orElse(inputDataModel);

        return executionContextFactory.apply(unionModel, inputVariablesBinding);
    }

    private File extractOutputBindingPath(final MultiValueMap<String, String> parameters) {
        ServiceParametersHelper paramHelper = new ServiceParametersHelper(parameters);

        // FILE WHERE TO SAVE OUTPUT BINDING
        File outputBindingPath = null;
        if (paramHelper.hasParameterValue(ReservedParams.P_OUTPUT_BINDING_URL)) {
            outputBindingPath = paramHelper.parseParameterValueAsFilePath(ReservedParams.P_OUTPUT_BINDING_URL).toFile();
            logParam(ReservedParams.P_OUTPUT_BINDING_URL, outputBindingPath.toString());
        }

        parameters.remove(ReservedParams.P_OUTPUT_BINDING_URL);

        return outputBindingPath;
    }

    private void saveOutputBinding(File outputBindingPath, VariablesBinding outputVariablesBinding) {
        try {
            outputVariablesBinding.save(new FileOutputStream(outputBindingPath), Lang.TTL);
        } catch (IOException e) {
            throw new SPipesServiceException("Cannot save output binding.", e);
        }
    }

    private String extractId(final MultiValueMap<String, String> parameters) {
        ServiceParametersHelper paramHelper = new ServiceParametersHelper(parameters);

        // LOAD MODULE ID
        final String id = getId(paramHelper);
        logParam(ReservedParams.P_ID, id);

        return id;
    }

    private Model extractConfigurationModel(final MultiValueMap<String, String> parameters) {
        ServiceParametersHelper paramHelper = new ServiceParametersHelper(parameters);

        // LOAD CONFIGURATION
        String configURL = null;
        if (paramHelper.hasParameterValue(ReservedParams.P_CONFIG_URL)) {
            configURL = paramHelper.getRequiredParameterValue(ReservedParams.P_CONFIG_URL);
            logParam(ReservedParams.P_CONFIG_URL, configURL);
            parameters.remove(ReservedParams.P_CONFIG_URL);
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
        log.info("- {}={}", parameterKey, parameterValue);
    }

    private @NotNull
    String getId(@NotNull final ServiceParametersHelper paramHelper) {

        if (paramHelper.hasParameterValue(ReservedParams.P_ID)) {
            return paramHelper.getParameterValue(ReservedParams.P_ID);
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

}
