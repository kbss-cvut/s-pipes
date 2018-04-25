package cz.cvut.sempipes.rest;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.ExecutionEngine;
import cz.cvut.sempipes.engine.ExecutionEngineFactory;
import cz.cvut.sempipes.engine.PipelineFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import cz.cvut.sempipes.exception.SempipesServiceException;
import cz.cvut.sempipes.manager.SempipesScriptManager;
import cz.cvut.sempipes.modules.Module;
import cz.cvut.sempipes.rest.util.ContextLoaderHelper;
import cz.cvut.sempipes.rest.util.ScriptManagerFactory;
import cz.cvut.sempipes.rest.util.ServiceParametersHelper;
import cz.cvut.sempipes.util.RDFMimeType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RestController
@EnableWebMvc
public class SempipesServiceController {

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
    private static final Logger LOG = LoggerFactory.getLogger(SempipesServiceController.class);
    private SempipesScriptManager scriptManager;

    public SempipesServiceController() {
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

    @RequestMapping(
        value = "/service-new",
        method = RequestMethod.GET,
        produces = {
            RDFMimeType.LD_JSON_STRING,
            RDFMimeType.N_TRIPLES_STRING,
            RDFMimeType.RDF_XML_STRING,
            RDFMimeType.TURTLE_STRING
        }
    )
    @Deprecated //TODO remove -- only to support compatibility with older version (used by RLP)
    public Model processServiceGetRequestCompat(@RequestParam MultiValueMap<String, String> parameters) {
        LOG.info("Processing service GET request.");
        return runService(ModelFactory.createDefaultModel(), parameters);
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, String> notFoundHandler(SempipesServiceException e) {
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


        // FILE WHERE TO GET INPUT BINDING
        URL inputBindingURL = null;
        if (parameters.containsKey(P_INPUT_BINDING_URL)) {
            inputBindingURL = paramHelper.parseParameterValueAsUrl(P_INPUT_BINDING_URL);
            logParam(P_INPUT_BINDING_URL, inputBindingURL.toString());
        }

        if (!paramHelper.hasParameterValue(P_ID)) {
            parameters.remove(P_ID_ALTERNATIVE);
        }
        parameters.remove(P_ID);
        parameters.remove(P_CONFIG_URL);
        parameters.remove(P_INPUT_BINDING_URL);
        parameters.remove(P_OUTPUT_BINDING_URL);

        // END OF PARAMETER PROCESSING
        final VariablesBinding inputVariablesBinding = new VariablesBinding(transform(parameters));
        if (inputBindingURL != null) {
            extendBindingFromURL(inputVariablesBinding, inputBindingURL);
        }
        LOG.info("- input variable binding ={}", inputVariablesBinding);

        // LOAD INPUT DATA
        ExecutionContext inputExecutionContext = ExecutionContextFactory.createContext(inputDataModel, inputVariablesBinding);

        // EXECUTE PIPELINE
        ExecutionEngine engine = ExecutionEngineFactory.createEngine();

        ContextLoaderHelper.updateContextsIfNecessary(scriptManager);
        Module module = scriptManager.loadFunction(id);


        if (module == null) {
            throw new SempipesServiceException("Cannot load module with id=" + id);
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
        final Model configModel = loadModelFromUrl(configURL);

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

        if (!paramHelper.hasParameterValue(P_ID)) {
            parameters.remove(P_ID_ALTERNATIVE);
        }
        parameters.remove(P_ID);
        parameters.remove(P_CONFIG_URL);
        parameters.remove(P_INPUT_BINDING_URL);
        parameters.remove(P_OUTPUT_BINDING_URL);

        // END OF PARAMETER PROCESSING
        final VariablesBinding inputVariablesBinding = new VariablesBinding(transform(parameters));
        if (inputBindingURL != null) {
            extendBindingFromURL(inputVariablesBinding, inputBindingURL);
        }
        LOG.info("- input variable binding ={}", inputVariablesBinding);

        // LOAD INPUT DATA
        ExecutionContext inputExecutionContext = ExecutionContextFactory.createContext(inputDataModel, inputVariablesBinding);

        ExecutionEngine engine = ExecutionEngineFactory.createEngine();
        ExecutionContext outputExecutionContext;
        Module module;
        // should execute module only
//        if (asArgs.isExecuteModuleOnly) {
        module = PipelineFactory.loadModule(configModel.createResource(id));

        if (module == null) {
            throw new SempipesServiceException("Cannot load module with id=" + id);
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
                throw new SempipesServiceException("Cannot save output binding.", e);
            }
        }

        LOG.info("Processing successfully finished.");
        return outputExecutionContext.getDefaultModel();
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

        throw new SempipesServiceException("Invalid/no module id supplied.");
    }

    private @NotNull
    Model loadModelFromUrl(@NotNull String modelUrl) {
        final Model outputModel = ModelFactory.createDefaultModel();
        try {
            outputModel.read(modelUrl, FileUtils.langTurtle);
        } catch (Exception e) {
            throw new SempipesServiceException("Could not load model from URL " + modelUrl + ".");
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