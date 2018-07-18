package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import java.io.IOException;
import java.util.Collections;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetDatasetDescriptorsModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(GetDatasetDescriptorsModule.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "get-dataset-descriptors-v1";

    /**
     * URL of the Sesame server.
     */
    private static final Property P_DATASET_IRI = getParameter("p-dataset-iri");
    private String prpDatasetIri;

    /**
     * URL of the SPARQL endpoint.
     */
    private String endpointUrl = "http://onto.fel.cvut.cz/rdf4j-server/repositories/descriptors-metadata";

    private static Property getParameter(final String name) {
        return ResourceFactory.createProperty(TYPE_URI + "/" + name);
    }

    public String getPrpDatasetIri() {
        return prpDatasetIri;
    }

    public void setPrpDatasetIri(String prpDatasetIri) {
        this.prpDatasetIri = prpDatasetIri;
    }

    @Override
    ExecutionContext executeSelf() {
        prpDatasetIri = executionContext.getVariablesBinding().getNode("p-dataset-iri").toString();

        if (prpDatasetIri == null) {
            LOG.error("No dataset IRI supplied, terminating");
            return executionContext;
        } else {
            LOG.info("[DATASET] " + prpDatasetIri);
        }

        final String queryString;
        try {
            queryString = FileUtils.readWholeFileAsUTF8(
                getClass().getResourceAsStream("/get-dataset-descriptors.rq"));
            Query query = QueryFactory.create();
            QueryFactory.parse(query, queryString, "", Syntax.syntaxSPARQL_11);

            final Var g = Var.alloc("ds");
            final BindingMap bm = new BindingHashMap();
            bm.add(g, ResourceFactory.createPlainLiteral(prpDatasetIri).asNode());

            query.setValuesDataBlock(
                Collections.singletonList(g),
                Collections.singletonList(bm)
            );

            QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointUrl, query);
            Model m = qexec.execConstruct();

            executionContext.getDefaultModel().add(m);

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
        prpDatasetIri = this.getStringPropertyValue(P_DATASET_IRI);
    }
}
