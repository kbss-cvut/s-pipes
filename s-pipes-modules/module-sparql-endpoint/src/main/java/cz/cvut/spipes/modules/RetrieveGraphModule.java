package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.impl.GraphChunkedDownload;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;


// TODO - lable = SpEP retrieve graph
@Slf4j
@Getter
@Setter
@SPipesModule(label = "sparql endpoint retrieve graph", comment = "Retrieves graph from sparql endpoint specified by " +
        "?endpointUrl and optionaly ?namedGraphId. If ?namedGraphId is not specified it retreaves the default graph.")
public class RetrieveGraphModule extends AnnotatedAbstractModule {

    protected static final String TYPE_URI = KBSS_MODULE.uri + "sparql-endpoint-retrieve-graph";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final int DEFAULT_PAGE_SIZE = 10000;

    @Parameter(iri = TYPE_PREFIX + "named-graph-id", comment = "Named graph id")
    private String namedGraphId;

    @Parameter(iri = TYPE_PREFIX + "endpoint-url", comment = "Endpoint url")
    private String endpointUrl;

    @Parameter(iri = TYPE_PREFIX + "page-size", comment = "Page size. Default is 10000.")
    private int pageSize = DEFAULT_PAGE_SIZE;

    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    ExecutionContext executeSelf() {
        Model outputModel = ModelFactory.createDefaultModel();
        GraphChunkedDownload downloader = new GraphChunkedDownload(endpointUrl, namedGraphId, pageSize) {
            @Override
            protected void processPartialModel(Model partialModel) {
                log.trace("persisting partial download, {} triples from (<{}>,<{}>)",
                        partialModel.size(), endpointUrl, namedGraphId);
                outputModel.add(partialModel);
            }
        };
        downloader.execute();
        return ExecutionContextFactory.createContext(outputModel);
    }
}
