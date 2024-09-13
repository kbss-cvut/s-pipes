package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Getter
@Setter
@SPipesModule(label = "get dataset descriptors v1", comment = "Retrieve dataset descriptor for dataset" +
    " with dataset-iri in endpoint-url.")
public class GetDatasetDescriptorsModule extends AnnotatedAbstractModule {

    private static final String TYPE_URI = KBSS_MODULE.uri + "get-dataset-descriptors-v1";

    @Parameter(iri = TYPE_URI + "/" + "p-dataset-iri", comment = "IRI of the dataset.")// TODO - revise comment
    private String prpDatasetIri;

    @Parameter(iri = TYPE_URI + "/" + "endpoint-url", comment = "URL of the SPARQL endpoint. Default value" +
        " is 'http://onto.fel.cvut.cz/rdf4j-server/repositories/descriptors-metadata'")
    private String endpointUrl = "http://onto.fel.cvut.cz/rdf4j-server/repositories/descriptors-metadata";

    @Override
    ExecutionContext executeSelf() {
        prpDatasetIri = executionContext.getVariablesBinding().getNode("p-dataset-iri").toString();

        if (prpDatasetIri == null) {
            log.error("No dataset IRI supplied, terminating");
            return executionContext;
        } else {
            log.info("[DATASET] " + prpDatasetIri);
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

}
