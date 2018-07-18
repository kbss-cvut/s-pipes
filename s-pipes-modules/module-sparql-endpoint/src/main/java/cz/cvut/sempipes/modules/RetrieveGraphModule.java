package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.impl.GraphChunkedDownload;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrieveGraphModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(RetrieveGraphModule.class);
    protected static final String TYPE_URI = KBSS_MODULE.uri + "sparql-endpoint-retrieve-graph";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final int DEFAULT_PAGE_SIZE = 10000;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "named-graph-id")
    private String namedGraphId;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "endpoint-url")
    private String endpointUrl;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "page-size")
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
                LOG.trace("persisting partial download, {} triples from (<{}>,<{}>)",
                        partialModel.size(), endpointUrl, namedGraphId);
                outputModel.add(partialModel);
            }
        };
        downloader.execute();
        return ExecutionContextFactory.createContext(outputModel);
    }
}
