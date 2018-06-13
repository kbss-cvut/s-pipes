package cz.cvut.sempipes.impl;

import cz.cvut.sempipes.modules.DownloadGraph;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GraphChunkedDownload {
    
    private static final Logger LOG = LoggerFactory.getLogger(DownloadGraph.class);

    private static final int DEFAULT_PAGE_SIZE = 10000;

    private String namedGrapheId;

    private String endpointUrl;
    
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    public GraphChunkedDownload(String namedGrapheId, String endpointUrl) {
        this(namedGrapheId, endpointUrl, DEFAULT_PAGE_SIZE);
    }
    
    public GraphChunkedDownload(String namedGrapheId, String endpointUrl, int pageSize) {
        this.namedGrapheId = namedGrapheId;
        this.endpointUrl = endpointUrl;
        this.pageSize = pageSize;
                 
    }

    public void execute() {
        long offset = 0;
        while (true) {
            LOG.debug("Executing query for offset: {}", offset);
            Model model = executeQuery(offset);
            if (model.isEmpty()) {
                break;
            } else {
                processPartialModel(model);
            }
            offset += getPageSize();
        }
    }
    
    /**
     * Implement this method to 
     * @param partialModel 
     */
    protected abstract void processPartialModel(Model partialModel);

    private Model executeQuery(long offset) {
        String query = prepareQuery(offset);
        return QueryExecutionFactory.sparqlService(endpointUrl, query).execConstruct();
    }

    public String getNamedGrapheId() {
        return namedGrapheId;
    }

    public void setNamedGrapheId(String namedGrapheId) {
        this.namedGrapheId = namedGrapheId;
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

    private String getInnerSelect() {
        if(this.namedGrapheId == null){
            return "SELECT ?s ?p ?o WHERE { GRAPH <" + this.namedGrapheId + "> { ?s ?p ?o } }";
        }else{
            return "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";
        }
    }

    private String getOuterConstruct() {
        return "?s ?p ?o";
    }

    private String prepareQuery(long offset) {
        return "CONSTRUCT {\n" +
            getOuterConstruct() + "\n } WHERE { {" +
            getInnerSelect() +
            "\n} }" +
            "\nLIMIT " + Integer.toString(this.getPageSize()) +
            "\nOFFSET " + Long.toString(offset);
    }
}
