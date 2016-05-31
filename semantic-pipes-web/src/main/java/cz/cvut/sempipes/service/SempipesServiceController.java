package cz.cvut.sempipes.service;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

@RestController
public class SempipesServiceController {

    private static final Logger LOG = LoggerFactory.getLogger(SempipesServiceController.class);

    /**
     * Request parameter - 'id' of the module to be executed
     */
    public static final String P_ID = "id";

    @RequestMapping(
            value = "/serviceGet",
            method = RequestMethod.GET,
            produces = {RDFMimeType.JSONLD}
    )
    public String processGetRequest(@RequestParam MultiValueMap parameters) {
        LOG.info("Processing GET request.");
        return run(new ByteArrayInputStream(new byte[]{}),parameters);
    }

    @RequestMapping(
            value = "/servicePost",
            method = RequestMethod.POST
//            ,
//            consumes = {
//                    RDFMimeType.OWL_XML,
//                    RDFMimeType.JSONLD,
//                    RDFMimeType.N_TRIPLES,
//                    RDFMimeType.RDF_XML,
//                    RDFMimeType.TURTLE},
//            produces = {RDFMimeType.JSONLD}
    )
    public String processPostRequest(@RequestBody InputStream rdfData, @RequestParam MultiValueMap parameters) {
        LOG.info("Processing POST request.");
        return run(rdfData, parameters);
    }

    private QuerySolution transform(final Map parameters) {
        final QuerySolutionMap querySolution = new QuerySolutionMap();

        for(Object key : parameters.keySet()) {
            // TODO types of RDFNode
            querySolution.add(key.toString(), ResourceFactory.createPlainLiteral(parameters.get(key).toString()));
        }

        return querySolution;
    }

    private String run(final InputStream rdfData, final MultiValueMap parameters) {
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

        // TODO find in module registry ?!?
        String result="";
        if ( id.equals("cz.cvut.sempipes.modules.IdentityModule") ) {
            System.out.println("I AM HERE");
            Model m = ModelFactory.createDefaultModel();
            m.read(rdfData,"");
            final StringWriter writer = new StringWriter();
//            m.write(writer);
            RDFDataMgr.write(writer, m, Lang.JSONLD);
            result = writer.toString();
        } else {
            System.out.println("I AM THERE");
            throw new SempipesServiceInvalidModuleIdException();
        }

        LOG.info("Processing successfully finished.");
        return result;
    }
}