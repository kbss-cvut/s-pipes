package cz.cvut.spipes.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;


@Slf4j
public abstract class GraphChunkedDownload {

    private static final int DEFAULT_PAGE_SIZE = 10000;

    private String endpointUrl;

    private String namedGraphId;

    private Integer pageSize = DEFAULT_PAGE_SIZE;

    public GraphChunkedDownload() {
    }

    public GraphChunkedDownload(String endpointUrl, String namedGrapheId) {
        this(endpointUrl, namedGrapheId, DEFAULT_PAGE_SIZE);
    }

    public GraphChunkedDownload(String endpointUrl, String namedGrapheId, int pageSize) {
        this.endpointUrl = endpointUrl;
        this.namedGraphId = namedGrapheId;
        this.pageSize = pageSize;
                 
    }

    public void execute() {
        long offset = 0;
        while (true) {
            log.debug("Executing query for offset: {}", offset);
            String query = prepareQuery(offset);
            Model model = executeQuery(query);
            if (model.isEmpty()) {
                break;
            } else {
                processPartialModel(model);
            }
            offset += getPageSize();
        }
    }
    
    /**
     * Method called whenever partial model is constructed.
     * @param partialModel 
     */
    protected abstract void processPartialModel(Model partialModel);

    /**
     * Implementation specific.
     * @param query
     * @return
     */
    protected Model executeQuery(String query) {
        // According to docs QueryExecutionHTTP is AutoCloseable and models from returned construct are valid after close
        try(QueryExecutionHTTP qe = QueryExecutionHTTP.service(endpointUrl, query)) {
            return qe.execConstruct();
        }
    }

    public String getNamedGraphId() {
        return namedGraphId;
    }

    public void setNamedGraphId(String namedGraphId) {
        this.namedGraphId = namedGraphId;
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

    /**
     * Implementation specific.
     * @return
     */
    protected String getInnerSelect() {
        if(this.namedGraphId != null){
            return "SELECT ?s ?p ?o WHERE { GRAPH <" + this.namedGraphId + "> { ?s ?p ?o } }";
        }else{
            return "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";
        }
    }

    private String getOuterConstruct() {
        return "?s ?p ?o";
    }

    /**
     * Implementation specific.
     * @param offset
     * @return
     */
    protected String prepareQuery(long offset) {
        return "CONSTRUCT {\n" +
            getOuterConstruct() + "\n } WHERE { {" +
            getInnerSelect() +
            "\n} }" +
            "\nLIMIT " + this.getPageSize() +
            "\nOFFSET " + offset;
    }
}
