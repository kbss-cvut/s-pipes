package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

public class ModuleGetDatasetDescriptors extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleGetDatasetDescriptors.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "get-dataset-descriptors-v1";

    /**
     * URL of the Sesame server
     */
    private static final Property P_DATASET_IRI = getParameter("p-dataset-iri");
    private String pDatasetIRI;

    /**
     * URL of the SPARQL endpoint
     */
    private String endpointURL = "http://onto.fel.cvut.cz/rdf4j-server/repositories/descriptors-metadata";

    private static Property getParameter(final String name) {
        return ResourceFactory.createProperty(TYPE_URI + "/" + name);
    }

    @Override
    ExecutionContext executeSelf() {
        pDatasetIRI = executionContext.getVariablesBinding().getNode("p-dataset-iri").toString();

        if ( pDatasetIRI == null ) {
            LOG.error("No dataset IRI supplied, terminating");
            return executionContext;
        } else {
            LOG.info("[DATASET] " + pDatasetIRI);
        }

        final String queryString;
        try {
            queryString = FileUtils.readWholeFileAsUTF8(getClass().getResourceAsStream("/get-dataset-descriptors.rq"));
            Query query = QueryFactory.create();
            QueryFactory.parse(query, queryString, "", Syntax.syntaxSPARQL_11);

            final Var g = Var.alloc("ds");
            final BindingMap bm = new BindingHashMap();
            bm.add(g,ResourceFactory.createPlainLiteral(pDatasetIRI).asNode());

            query.setValuesDataBlock(
                    Collections.singletonList(g),
                    Collections.singletonList(bm)
                    );

            QueryExecution qexec = QueryExecutionFactory.sparqlService(endpointURL, query);
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
        pDatasetIRI = this.getStringPropertyValue(P_DATASET_IRI);
    }
}
