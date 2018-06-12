package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractGraphChunckedDownload extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractGraphChunckedDownload.class);

    protected static final String TYPE_URI = KBSS_MODULE.uri + "sparql-endpoint-download-graph";
    protected static final String TYPE_PREFIX = TYPE_URI + "/";
    protected static final int DEFAULT_PAGE_SIZE = 10000;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "named-graph-id")
    private String namedGrapheId;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "endpoint-url")
    private String endpointUrl;
    
    @Parameter(urlPrefix = TYPE_PREFIX, name = "http://topbraid.org/sparqlmotion#outputVariable")
    private String outputVariable;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "page-size")
    private Integer pageSize = DEFAULT_PAGE_SIZE;


    public String getNamedGrapheId() {
        return namedGrapheId;
    }

    public void setNamedGrapheId(String namedGrapheId) {
        this.namedGrapheId = namedGrapheId;
    }

    @Override
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
    
}
