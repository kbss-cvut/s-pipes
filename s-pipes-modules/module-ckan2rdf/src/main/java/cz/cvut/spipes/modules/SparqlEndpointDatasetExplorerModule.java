package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import java.time.Instant;
import java.util.Calendar;

import cz.cvut.spipes.modules.annotations.SPipesModule;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SPipesModule(label = "sparqlEndpointDatasetExplorer-v1", comment = "TODO")
public class SparqlEndpointDatasetExplorerModule extends AnnotatedAbstractModule {

    public static final String TYPE_URI = KBSS_MODULE.uri + "sparqlEndpointDatasetExplorer-v1";
    private static final Logger LOG =
        LoggerFactory.getLogger(SparqlEndpointDatasetExplorerModule.class);
    private final String nsHttp = "http://onto.fel.cvut.cz/ontologies/http/";

    /**
     * URL of the SPARQL endpoint.
     */
    @Parameter(urlPrefix = TYPE_URI + "/", name = "p-sparql-endpoint-url")
    private String propSparqlEndpointUrl;

    /**
     * Connection Timeout.
     */
    @Parameter(urlPrefix = TYPE_URI + "/", name = "p-connection-timeout")
    private long propConnectionTimeout = 3000;

    /**
     * Query Timeout.
     */
    @Parameter(urlPrefix = TYPE_URI + "/", name = "p-query-timeout")
    private long propQueryTimeout = 60000;

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    ExecutionContext executeSelf() {
        outputContext = ExecutionContextFactory.createEmptyContext();
        final Model model = outputContext.getDefaultModel();
        try {
            final String clsHttpAccessIri = nsHttp + "http-access";
            final Resource iAccess = ResourceFactory.createResource(
                clsHttpAccessIri + Instant.now().toString());
            final String queryString = IOUtils.toString(
                getClass().getResourceAsStream("/find-datasets.rq"), "UTF-8");
            final ParameterizedSparqlString strSparql = new ParameterizedSparqlString(queryString);
            strSparql.setIri("event", iAccess.getURI());
            final Resource cAccess = ResourceFactory.createResource(clsHttpAccessIri);
            final Property pHasUrl = ResourceFactory.createProperty(nsHttp + "has-url");
            final Property pHasTime = ResourceFactory.createProperty(nsHttp + "has-time");
            model.add(iAccess, RDF.type, cAccess);
            model.add(iAccess, pHasUrl, ResourceFactory.createResource(propSparqlEndpointUrl));
            model.add(iAccess, pHasTime, ResourceFactory.createTypedLiteral(
                Calendar.getInstance()));
            try {
                final Query query = strSparql.asQuery();
                final QueryExecution qexec =
                    QueryExecutionFactory.sparqlService(propSparqlEndpointUrl, query);
                qexec.setTimeout(propConnectionTimeout, propQueryTimeout);
                model.add(qexec.execConstruct());
            } catch (Exception e) {
                model.add(iAccess,
                    ResourceFactory.createProperty(nsHttp + "has-error"),
                    ResourceFactory.createPlainLiteral(e.getMessage()));
            }
        } catch (Exception e) {
            model.add(
                ResourceFactory.createResource(propSparqlEndpointUrl),
                RDF.type,
                ResourceFactory.createResource(nsHttp + "url"));
        }
        return outputContext;
    }
}