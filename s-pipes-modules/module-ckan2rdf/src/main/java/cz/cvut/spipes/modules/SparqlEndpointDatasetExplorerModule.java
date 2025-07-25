package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;
import org.apache.jena.vocabulary.RDF;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Calendar;

@SPipesModule(label = "sparqlEndpointDatasetExplorer-v1", comment = "TODO")
public class SparqlEndpointDatasetExplorerModule extends AnnotatedAbstractModule {

    public static final String TYPE_URI = KBSS_MODULE.uri + "sparqlEndpointDatasetExplorer-v1";
    private final String nsHttp = "http://onto.fel.cvut.cz/ontologies/http/";

    @Parameter(iri = TYPE_URI + "/" + "p-sparql-endpoint-url", comment = "URL of the SPARQL endpoint.")
    private String propSparqlEndpointUrl;

    @Parameter(iri = TYPE_URI + "/" + "p-connection-timeout", comment = "Connection Timeout in ms. Default 3000.")
    private long propConnectionTimeout = 3000;

    @Parameter(iri = TYPE_URI + "/" + "p-query-timeout", comment = "Query Timeout. Default 60000")
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
                getClass().getResourceAsStream("/find-datasets.rq"), StandardCharsets.UTF_8);
            final ParameterizedSparqlString strSparql = new ParameterizedSparqlString(queryString);
            strSparql.setIri("event", iAccess.getURI());
            final Resource cAccess = ResourceFactory.createResource(clsHttpAccessIri);
            final Property pHasUrl = ResourceFactory.createProperty(nsHttp + "has-url");
            final Property pHasTime = ResourceFactory.createProperty(nsHttp + "has-time");
            model.add(iAccess, RDF.type, cAccess);
            model.add(iAccess, pHasUrl, ResourceFactory.createResource(propSparqlEndpointUrl));
            model.add(iAccess, pHasTime, ResourceFactory.createTypedLiteral(
                Calendar.getInstance()));


            // TODO - check in git history that timeout(propConnectionTimeout) is proper replacement for previous call of setTimeout(propConnectionTimeout, propQueryTimeout)?
            QueryExecutionHTTPBuilder builder = QueryExecutionHTTP.service(propSparqlEndpointUrl);

            builder.query(strSparql.toString()).timeout(propConnectionTimeout);
            try (final QueryExecution qexec = builder.build()) {
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