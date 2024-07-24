package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.impl.GraphChunkedDownload;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;


// TODO - lable = SpEP retrieve graph
@Slf4j
@SPipesModule(label = "sparql endpoint retrieve graph", comment = "Retrieves graph from sparql endpoint specified by " +
        "?endpointUrl and optionaly ?namedGraphId. If ?namedGraphId is not specified it retreaves the default graph.")
public class RetrieveGraphModule extends AnnotatedAbstractModule {

    protected static final String TYPE_URI = KBSS_MODULE.uri + "sparql-endpoint-retrieve-graph";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final int DEFAULT_PAGE_SIZE = 10000;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "named-graph-id", comment = "Named graph id")
    private String namedGraphId;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "endpoint-url", comment = "Endpoint url")
    private String endpointUrl;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "page-size", comment = "Page size. Default is 10000.")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    public String getNamedGraphId() {
        return namedGraphId;
    }

    public void setNamedGraphId(String namedGraphId) {
        this.namedGraphId = namedGraphId;
    }

    public String getTypeURI() {
        return TYPE_URI;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public int getPageSize() {
        return pageSize;
    }


    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
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
