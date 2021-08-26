package cz.cvut.spipes.rest;

import cz.cvut.spipes.config.ExecutionConfig;
import cz.cvut.spipes.engine.*;
import cz.cvut.spipes.exception.SPipesServiceException;
import cz.cvut.spipes.manager.SPipesScriptManager;
import cz.cvut.spipes.modules.Module;
import cz.cvut.spipes.rest.util.ContextLoaderHelper;
import cz.cvut.spipes.rest.util.ProgressListenerLoader;
import cz.cvut.spipes.rest.util.ScriptManagerFactory;
import cz.cvut.spipes.rest.util.ServiceParametersHelper;
import cz.cvut.spipes.util.RDFMimeType;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
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
     * Request parameter - 'id' of the module to be executed, in case when '_pId' is not specified. Otherwise it is regular service parameter.
     */
    public static final String P_ID_ALTERNATIVE = "id";
    /**
     * Request parameter - URL of the resource containing configuration
     */
    public static final String P_CONFIG_URL = "_pConfigURL";
    /**
     * Input binding - URL of the file where input bindings are stored
     */
    public static final String P_INPUT_BINDING_URL = "_pInputBindingURL";
    /**
     * Output binding - URL of the file where output bindings are stored
     */
    public static final String P_OUTPUT_BINDING_URL = "_pOutputBindingURL";
    private static final Logger LOG = LoggerFactory.getLogger(SPipesServiceController.class);
    private SPipesScriptManager scriptManager;

    public SPipesServiceController() {
        scriptManager = ScriptManagerFactory.getSingletonSPipesScriptManager();
    }

    @PostConstruct
    void init() {
    }

    @RequestMapping(
        value = "/module",
        method = RequestMethod.GET,
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

    @RequestMapping(
        value = "/module",
        method = RequestMethod.POST
        ,
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
    public Model processPostRequest(@RequestBody Model inputModel,
                                    @RequestParam MultiValueMap<String, String> parameters
    ) {
        LOG.info("Processing POST request.");
        return runModule(inputModel, parameters);
    }

    @RequestMapping(
        value = "/service",
        method = RequestMethod.POST,
        produces = {
            RDFMimeType.LD_JSON_STRING + ";chaset=utf-8",
            RDFMimeType.N_TRIPLES_STRING,
            RDFMimeType.RDF_XML_STRING,
            RDFMimeType.TURTLE_STRING
        }
    )
    public Model processServicePostRequest(@RequestParam MultiValueMap<String, String> parameters,
                                           @RequestParam("files") MultipartFile[] files)  {
        LOG.info("Processing service POST request, with {} multipart files.", files.length);
        return runService(ModelFactory.createDefaultModel(), parameters);
    }

    @RequestMapping(
        value = "/service",
        method = RequestMethod.GET,
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

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> notFoundHandler(SPipesServiceException e) {
        return Collections.singletonMap("message", e.getMessage());
    }

    private QuerySolution transform(final Map parameters) {
        final QuerySolutionMap querySolution = new QuerySolutionMap();

        for (Object key : parameters.keySet()) {
            // TODO types of RDFNode
            String value = (String) ((List) parameters.get(key)).get(0);
            querySolution.add(key.toString(), ResourceFactory.createPlainLiteral(value));
        }

        return querySolution;
    }


    // TODO merge it with implementation in /module
    private Model runService(final Model inputDataModel, final MultiValueMap<String, String> parameters) {
        LOG.info("- parameters={}", parameters);

        ServiceParametersHelper paramHelper = new ServiceParametersHelper(parameters);

        // LOAD MODULE ID
        final String id = getId(paramHelper);
        logParam(P_ID, id);

        // LOAD CONFIGURATION
        String configURL = null;
        if (paramHelper.hasParameterValue(P_CONFIG_URL)) {
            configURL = paramHelper.getRequiredParameterValue(P_CONFIG_URL);
            logParam(P_CONFIG_URL, configURL);
        }

        // FILE WHERE TO GET INPUT BINDING
        URL inputBindingURL = null;
        if (parameters.containsKey(P_INPUT_BINDING_URL)) {
            inputBindingURL = paramHelper.parseParameterValueAsUrl(P_INPUT_BINDING_URL);
            logParam(P_INPUT_BINDING_URL, inputBindingURL.toString());
        }

        // TODO included P_ID
        //      -- commented out to be available to semantic logging listener (engine should provide it instead)
        if (!paramHelper.hasParameterValue(P_ID)) {
            parameters.add(P_ID, paramHelper.getParameterValue(P_ID_ALTERNATIVE));
            parameters.remove(P_ID_ALTERNATIVE);
        }

        // parameters.remove(P_ID);
        parameters.remove(P_CONFIG_URL);
        parameters.remove(P_INPUT_BINDING_URL);
        parameters.remove(P_OUTPUT_BINDING_URL);

        // END OF PARAMETER PROCESSING
        final VariablesBinding inputVariablesBinding = new VariablesBinding(transform(parameters));
        if (inputBindingURL != null) {
            extendBindingFromURL(inputVariablesBinding, inputBindingURL);
        }
        LOG.info("- input variable binding ={}", inputVariablesBinding);

        // CONFIGURE ENGINE
        ExecutionEngine engine = createExecutionEngine(configURL);

        // LOAD INPUT DATA
        ExecutionContext inputExecutionContext = ExecutionContextFactory.createContext(inputDataModel, inputVariablesBinding);

        // EXECUTE PIPELINE
        ContextLoaderHelper.updateContextsIfNecessary(scriptManager);
        Module module = scriptManager.loadFunction(id);


        if (module == null) {
            throw new SPipesServiceException("Cannot load return module for a function with id=" + id);
        }
        ExecutionContext outputExecutionContext = engine.executePipeline(module, inputExecutionContext);

        LOG.info("Processing successfully finished.");
        return outputExecutionContext.getDefaultModel();
    }

    private Model runModule(final Model inputDataModel, final MultiValueMap<String, String> parameters) {
        LOG.info("- parameters={}", parameters);

        ServiceParametersHelper paramHelper = new ServiceParametersHelper(parameters);

        // LOAD MODULE ID
        final String id = getId(paramHelper);
        logParam(P_ID, id);

        // LOAD MODULE CONFIGURATION
        final String configURL = paramHelper.getRequiredParameterValue(P_CONFIG_URL);
        logParam(P_CONFIG_URL, configURL);

        // FILE WHERE TO GET INPUT BINDING
        URL inputBindingURL = null;
        if (paramHelper.hasParameterValue(P_INPUT_BINDING_URL)) {
            inputBindingURL = paramHelper.parseParameterValueAsUrl(P_INPUT_BINDING_URL);
            logParam(P_INPUT_BINDING_URL, inputBindingURL.toString());
        }

        // FILE WHERE TO SAVE OUTPUT BINDING
        File outputBindingPath = null;
        if (paramHelper.hasParameterValue(P_OUTPUT_BINDING_URL)) {
            outputBindingPath = paramHelper.parseParameterValueAsFilePath(P_OUTPUT_BINDING_URL).toFile();
            logParam(P_OUTPUT_BINDING_URL, outputBindingPath.toString());
        }

        // TODO included P_ID
        //      -- commented out to be available to semantic logging listener (engine should provide it instead)
        if (!paramHelper.hasParameterValue(P_ID)) {
            parameters.add(P_ID, paramHelper.getParameterValue(P_ID_ALTERNATIVE));
            parameters.remove(P_ID_ALTERNATIVE);
        }

        // parameters.remove(P_ID);
        parameters.remove(P_CONFIG_URL);
        parameters.remove(P_INPUT_BINDING_URL);
        parameters.remove(P_OUTPUT_BINDING_URL);

        // END OF PARAMETER PROCESSING
        final VariablesBinding inputVariablesBinding = new VariablesBinding(transform(parameters));
        if (inputBindingURL != null) {
            extendBindingFromURL(inputVariablesBinding, inputBindingURL);
        }
        LOG.info("- input variable binding ={}", inputVariablesBinding);

        // CONFIGURE ENGINE
        final Model configModel = loadModelFromUrl(configURL);
        ExecutionEngine engine = ExecutionEngineFactory.createEngine();
        ProgressListenerLoader.createListeners(configModel).forEach(
            engine::addProgressListener
        );

        // LOAD INPUT DATA
        ExecutionContext inputExecutionContext = ExecutionContextFactory.createContext(inputDataModel, inputVariablesBinding);


        ExecutionContext outputExecutionContext;
        Module module;
        // should execute module only
//        if (asArgs.isExecuteModuleOnly) {
        module = PipelineFactory.loadModule(configModel.createResource(id));

        if (module == null) {
            throw new SPipesServiceException("Cannot load module with id=" + id);
        }

        outputExecutionContext = engine.executePipeline(module, inputExecutionContext);
//        } else {
//            module = PipelineFactory.loadPipeline(configModel.createResource(asArgs.configResourceUri));
//            outputExecutionContext = engine.executePipeline(module, inputExecutionContext);
//        }

        if (outputBindingPath != null) {
            try {
                outputExecutionContext.getVariablesBinding().save(new FileOutputStream(outputBindingPath), FileUtils.langTurtle);
            } catch (IOException e) {
                throw new SPipesServiceException("Cannot save output binding.", e);
            }
        }

        LOG.info("Processing successfully finished.");
        return outputExecutionContext.getDefaultModel();
    }

    private ExecutionEngine createExecutionEngine(@Nullable final String configUrl) {
        ExecutionEngine engine = ExecutionEngineFactory.createEngine();

        String cUrl = Optional.ofNullable(configUrl)
            .orElse(ExecutionConfig.getConfigUrl());

        final Model configModel = loadModelFromUrl(cUrl);
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
        } else if (paramHelper.hasParameterValue(P_ID_ALTERNATIVE)) {
            LOG.debug("Parameter '{}' is used instead of parameter '{}', which is missing.", P_ID_ALTERNATIVE, P_ID);
            return paramHelper.getParameterValue(P_ID_ALTERNATIVE);
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
                LOG.debug("- no conflict between bindings loaded from '" + P_INPUT_BINDING_URL + "' and those provided in query string.");
            } else {
                LOG.info("- conflicts found between bindings loaded from '" + P_INPUT_BINDING_URL + "' and those provided in query string: " + vb3.toString());
            }
        } catch (IOException e) {
            LOG.warn("Could not read data from parameter {}={}, caused by: {}", P_INPUT_BINDING_URL, inputBindingURL, e);
        }

    }

}