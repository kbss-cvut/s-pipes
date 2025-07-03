package cz.cvut.spipes.modules;

import cz.cvut.spipes.Vocabulary;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Slf4j
@SPipesModule(label = "dataset discovery v1", comment =
        "Discovers dataset based on keyword userInput in repository linked.opendata.cz-federated-descriptor-faceted-search " +
        "hosted at http://onto.fel.cvut.cz/rdf4j-server.")
public class DatasetDiscoveryModule extends AnnotatedAbstractModule {

    private static final String TYPE_URI = KBSS_MODULE.uri + "dataset-discovery-v1";

    @Parameter(iri = TYPE_URI + "/" + "prp-user-input", comment = "Keywords query. Keywords are separated by space.")
    private String userInput;

    private List<String> getDatasetsForQuery(final String s, final String endpoint) {
        final List<String> datasets = new ArrayList<>();

        org.apache.jena.query.Query query = QueryFactory.create();
        QueryFactory.parse(query, s, "", Syntax.syntaxSPARQL_11);
        try(QueryExecution qexec = QueryExecutionHTTP.service(endpoint, query)) {
            ResultSet r = qexec.execSelect();
            r.forEachRemaining(querySolution -> {
                if (querySolution.contains("g")) {
                    datasets.add(querySolution.get("g").asResource().getURI());
                }
            });
        }

        return datasets;
    }

    @Override
    ExecutionContext executeSelf() {
        // user input (no interpretation, currently list of keywords)
        userInput = executionContext.getVariablesBinding().getNode("prp-user-input").toString();

        if (userInput == null) {
            log.error("No userInput supplied, terminating");
            return executionContext;
        } else {
            log.info("[USER-QUERY] " + userInput);
        }

        final UserQuery q2 = UserQuery.parse(userInput);

        String endpoint = "http://onto.fel.cvut.cz/rdf4j-server/repositories/linked.opendata.cz-federated-descriptor-faceted-search";

        try {
            String query;
            if (q2.getDates().isEmpty()) {
                query = FileUtils.readWholeFileAsUTF8(
                    getClass().getResourceAsStream("/get-labels.rq"));
                query = query.replaceAll("\\?keywords", "\"" + q2.getKeywordRegex() + "\"");
            } else {
                query = FileUtils.readWholeFileAsUTF8(
                    getClass().getResourceAsStream("/get-datasets.rq"));
                query = query.replaceAll("\\?keywords", "\"" + q2.getKeywordRegex() + "\"");
                // String qTimeRange = FileUtils.readWholeFileAsUTF8(
                // getClass().getResourceAsStream("/get-time-range.rq"));
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Prague")));
                query = query.replaceAll("\\?date", "\""
                    + format.format(q2.getDates().iterator().next()) + "\"");
                // List<String> datasetIRIs2 = getDatasetsForQuery(qTimeRange, endpoint);
                // datasetIris1.retainAll(datasetIRIs2);
            }

            final Model model = executionContext.getDefaultModel();
            final Resource indSparqlEndpointDatasetSource =
                ResourceFactory.createResource(Vocabulary.s_c_dataset_source
                    + "/linked.opendata.cz");
            model.add(indSparqlEndpointDatasetSource,
                RDF.type, cls(Vocabulary.s_c_sparql_endpoint_dataset_source));
            model.add(indSparqlEndpointDatasetSource,
                prp(Vocabulary.s_p_has_endpoint_url), "http://linked.opendata.cz/sparql");

            final List<String> datasetIris1 = getDatasetsForQuery(query, endpoint);
            datasetIris1.forEach(datasetIRI -> {
                Resource indDescription = ResourceFactory.createResource(datasetIRI
                    + "-description");
                model.add(indDescription, RDF.type, cls(Vocabulary.s_c_description));

                Resource indDatasetSnapshot = ResourceFactory.createResource(datasetIRI);
                model.add(indDatasetSnapshot, RDF.type, cls(Vocabulary.s_c_dataset_snapshot));

                model.add(indDescription,
                    prp(Vocabulary.s_p_has_source), indSparqlEndpointDatasetSource);
                model.add(indDescription,
                    prp(Vocabulary.s_p_has_dataset_descriptor), indDatasetSnapshot);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return executionContext;
    }

    private Resource cls(final String uri) {
        return ResourceFactory.createProperty(uri);
    }

    private Property prp(final String uri) {
        return ResourceFactory.createProperty(uri);
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

}
