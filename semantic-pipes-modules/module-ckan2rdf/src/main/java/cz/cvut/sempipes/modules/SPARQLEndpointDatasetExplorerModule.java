package cz.cvut.sempipes.modules;

import com.sun.javafx.binding.StringFormatter;
import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import org.apache.jena.query.*;
import org.apache.jena.shared.PrefixMapping;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPARQLEndpointDatasetExplorerModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(SPARQLEndpointDatasetExplorerModule.class);

    public static final String TYPE_URI = KBSS_MODULE.uri + "sparqlEndpointDatasetExplorer-v1";

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    /**
     * URL of the SPARQL endpoint
     */
    @Parameter(urlPrefix = TYPE_URI+"/", name = "p-sparql-endpoint-url")
    private String pSPARQLEndpointURL;

    @Override
    ExecutionContext executeSelf() {
        final String q = StringFormatter.format(
                "CONSTRUCT {?dcatDS a <"+DCAT.DATASET.toString()+">} {?dcatDS a <"+DCAT.DATASET.toString()+">}"
        ).getValue();
        final Query query = QueryFactory.create();
        query.setPrefixMapping(PrefixMapping.Standard);
        QueryFactory.parse(query, q, "", Syntax.syntaxSPARQL_11);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(pSPARQLEndpointURL, query);
        executionContext.getDefaultModel().add(qexec.execConstruct());
        return executionContext;
    }
}