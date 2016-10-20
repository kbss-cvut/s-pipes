package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.modules.datasetdiscovery.model.generated.Vocabulary;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ModuleDatasetDiscovery extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleDatasetDiscovery.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "dataset-discovery-v1";

    /**
     * URL of the Sesame server
     */
    private static final Property P_USER_INPUT = getParameter("p-user-input");
    private String userInput;

    private static Property getParameter(final String name) {
        return ResourceFactory.createProperty(TYPE_URI + "/" + name);
    }

    private List<String> getDatasetsForQuery(final String s, final String endpoint) {
        final List<String> datasets = new ArrayList<>();

        org.apache.jena.query.Query query = QueryFactory.create();
        QueryFactory.parse(query, s, "", Syntax.syntaxSPARQL_11);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet r = qexec.execSelect();
        r.forEachRemaining(querySolution -> { if (querySolution.contains("g")) datasets.add(querySolution.get("g").asResource().getURI()); });

        return datasets;
    }

    @Override
    ExecutionContext executeSelf() {
        // user input (no interpretation, currently list of keywords)
        userInput = executionContext.getVariablesBinding().getNode("p-user-input").toString();

        if ( userInput == null ) {
            LOG.error("No userInput supplied, terminating");
            return executionContext;
        } else {
            LOG.info("[USER-QUERY] " + userInput);
        }

        final UserQuery q2 = UserQuery.parse(userInput);

        String endpoint = "http://onto.fel.cvut.cz/rdf4j-server/repositories/linked.opendata.cz-federated-descriptor-faceted-search";

        try {
            String query;
            if ( q2.getDates().isEmpty() ) {
                query = FileUtils.readWholeFileAsUTF8(getClass().getResourceAsStream("/get-labels.rq"));
                query = query.replaceAll("\\?keywords", "\""+q2.getKeywordRegex()+"\"");
            } else {
                query = FileUtils.readWholeFileAsUTF8(getClass().getResourceAsStream("/get-datasets.rq"));
                query = query.replaceAll("\\?keywords", "\""+q2.getKeywordRegex()+"\"");
//                String qTimeRange = FileUtils.readWholeFileAsUTF8(getClass().getResourceAsStream("/get-time-range.rq"));
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Prague")));
                    query = query.replaceAll("\\?date", "\""+ format.format(q2.getDates().iterator().next())+"\"");
//                List<String> datasetIRIs2 = getDatasetsForQuery(qTimeRange, endpoint);
//                datasetIRIs1.retainAll(datasetIRIs2);
            }

            List<String> datasetIRIs1 = getDatasetsForQuery(query, endpoint);

            datasetIRIs1.forEach(datasetIRI -> {
                Resource dataset = ResourceFactory.createResource(datasetIRI);
                Model model = executionContext.getDefaultModel();
                Resource cDatasetSnapshot = ResourceFactory.createResource(Vocabulary.s_c_dataset_snapshot);
                Property pWasObtainedFrom = ResourceFactory.createProperty(Vocabulary.s_p_was_obtained_from);
                Resource iSPARQLEndpoint = ResourceFactory.createResource("http://onto.fel.cvut.cz/ontologies/dataset-descriptor/dataset-source/linked.opendata.cz");
                Property pHasURL = ResourceFactory.createProperty("http://onto.fel.cvut.cz/ontologies/dataset-descriptor/has-url");
                Literal iSPARQLEndpointURL = ResourceFactory.createStringLiteral("http://linked.opendata.cz/sparql");
                Resource cSPARQLEndpoint = ResourceFactory.createResource(Vocabulary.s_c_sparql_endpoint);

                model.add(dataset, RDF.type, cDatasetSnapshot);
                model.add(dataset, pWasObtainedFrom, iSPARQLEndpoint);
                model.add(iSPARQLEndpoint, RDF.type, cSPARQLEndpoint);
                model.add(iSPARQLEndpoint, pHasURL, iSPARQLEndpointURL);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return executionContext;
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {
        userInput = this.getStringPropertyValue(P_USER_INPUT);
    }
}
