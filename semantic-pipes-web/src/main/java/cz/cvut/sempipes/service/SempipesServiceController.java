package cz.cvut.sempipes.service;

import cz.cvut.sempipes.eccairs.EccairsService;
import cz.cvut.sempipes.engine.*;
import cz.cvut.sempipes.modules.AbstractModule;
import cz.cvut.sempipes.modules.Module;
import cz.cvut.sempipes.modules.ModuleIdentity;
import cz.cvut.sempipes.util.RDFMimeType;
import cz.cvut.sempipes.util.RawJson;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.Map;

@RestController
@EnableWebMvc
public class SempipesServiceController {

    private static final Logger LOG = LoggerFactory.getLogger(SempipesServiceController.class);

    /**
     * Request parameter - 'id' of the module to be executed
     */
    public static final String P_ID = "id";

    /**
     * Request parameter - URL of the resource containing configuration
     */
    public static final String P_CONFIG_URL = "configURL";

    @RequestMapping(
            value = "/module",
            method = RequestMethod.GET,
            produces = {RDFMimeType.LD_JSON_STRING}
    )
    public RawJson processGetRequest(@RequestParam MultiValueMap parameters) {
        LOG.info("Processing GET request.");
        return run(new ByteArrayInputStream(new byte[]{}), "", parameters);
    }

    @RequestMapping(
            value = "/module",
            method = RequestMethod.POST
            ,
            consumes = {
                    RDFMimeType.LD_JSON_STRING,
                    RDFMimeType.N_TRIPLES_STRING,
                    RDFMimeType.RDF_XML_STRING,
                    RDFMimeType.TURTLE_STRING},
            produces = {RDFMimeType.LD_JSON_STRING}
    )
    public RawJson processPostRequest(@RequestBody InputStream rdfData, @RequestParam MultiValueMap parameters, @RequestHeader(value="Content-type") String contentType) {
        LOG.info("Processing POST request.");
        return run(rdfData, contentType, parameters);
    }

    @RequestMapping(
            value = "/service",
            method = RequestMethod.GET,
            produces = {RDFMimeType.LD_JSON_STRING}
    ) // @ResponseBody
    public RawJson processServiceGetRequest(@RequestParam MultiValueMap parameters, HttpServletResponse response) {
        LOG.info("Processing service GET request.");
        return new RawJson(new EccairsService().run(new ByteArrayInputStream(new byte[]{}), "", parameters));
    }

    private QuerySolution transform(final Map parameters) {
        final QuerySolutionMap querySolution = new QuerySolutionMap();

        for(Object key : parameters.keySet()) {
            // TODO types of RDFNode
            querySolution.add(key.toString(), ResourceFactory.createPlainLiteral(parameters.get(key).toString()));
        }

        return querySolution;
    }

    private RawJson run(final InputStream rdfData, String contentType, final MultiValueMap parameters) {
        LOG.info("- parameters={}", parameters);

        if (!parameters.containsKey(P_ID)) {
            throw new SempipesServiceNoModuleIdException();
        }

        final String id = parameters.getFirst(P_ID).toString();
        LOG.info("- id={}", id);

        // LOAD MODULE CONFIGURATION
        if (!parameters.containsKey(P_CONFIG_URL)) {
            throw new SempipesServiceNoModuleIdException();
        }
        final String configURL = parameters.getFirst(P_CONFIG_URL).toString();
        LOG.info("- config URL={}", configURL);
        final Model configModel = ModelFactory.createDefaultModel();
        configModel.read(configURL,"TURTLE");

        final Map moduleParams = parameters.toSingleValueMap();
        moduleParams.remove(P_ID);

        final QuerySolution querySolution = transform(moduleParams);
        LOG.info("- parameters as query solution ={}", querySolution);
        contentType = contentType == null || contentType.isEmpty() ? "application/n-triples" : contentType;

        // LOAD INPUT DATA
        Model inputDataModel = ModelFactory.createDefaultModel();
        inputDataModel.read(rdfData,"", RDFLanguages.contentTypeToLang(contentType).getLabel());

        ExecutionContext inputExecutionContext = ExecutionContextFactory.createContext(inputDataModel, new VariablesBinding(querySolution));

        ExecutionEngine engine = ExecutionEngineFactory.createEngine();
        ExecutionContext outputExecutionContext = null;
        Module module = null;
        // should execute module only
//        if (asArgs.isExecuteModuleOnly) {
        module = loadModule(configModel,id);

        if ( module == null ) {
            throw new SempipesServiceInvalidModuleIdException();
        }

        outputExecutionContext = engine.executeModule(module, inputExecutionContext);
//        } else {
//            module = PipelineFactory.loadPipeline(configModel.createResource(asArgs.configResourceUri));
//            outputExecutionContext = engine.executePipeline(module, inputExecutionContext);
//        }

        // TODO output binding

        final StringWriter writer = new StringWriter();
        RDFDataMgr.write(writer, outputExecutionContext.getDefaultModel(), Lang.JSONLD);
        String result = writer.toString();

        LOG.info("Processing successfully finished.");
        return new RawJson(result);
    }

    private Module loadModule(Model configModel, String id) {
        // TODO find in module registry ?!?
        if ( "http://onto.fel.cvut.cz/ontologies/sempipes/identity-transformer".equals(id)) {
            return new ModuleIdentity();
        } else {
            return PipelineFactory.loadModule(configModel.createResource(id));
        }
    }
}