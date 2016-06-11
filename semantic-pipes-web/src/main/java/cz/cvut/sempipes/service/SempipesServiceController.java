package cz.cvut.sempipes.service;

import cz.cvut.sempipes.eccairs.EccairsService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

@RestController
@EnableWebMvc
public class SempipesServiceController {

    private static final Logger LOG = LoggerFactory.getLogger(SempipesServiceController.class);

    /**
     * Request parameter - 'id' of the module to be executed
     */
    public static final String P_ID = "id";

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

        final Map moduleParams = parameters.toSingleValueMap();
        moduleParams.remove(P_ID);

        final QuerySolution querySolution = transform(moduleParams);
        LOG.info("- parameters as query solution ={}", querySolution);
        contentType = contentType == null || contentType.isEmpty() ? "application/n-triples" : contentType;

        // TODO find in module registry ?!?
        String result="";
        if ( id.equals("http://onto.fel.cvut.cz/ontologies/sempipes/identity-transformer") ) {
            Model m = ModelFactory.createDefaultModel();
            m.read(rdfData,"", RDFLanguages.contentTypeToLang(contentType).getLabel());
            final StringWriter writer = new StringWriter();
//            m.write(writer);
            RDFDataMgr.write(writer, m, Lang.JSONLD);
            result = writer.toString();
        } else {
            throw new SempipesServiceInvalidModuleIdException();
        }

        LOG.info("Processing successfully finished.");
        return new RawJson(result);
    }
}