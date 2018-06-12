package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrieveGraphModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(RetrieveGraphModule.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "sparql-endpoint-retrieve-graph";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final int DEFAULT_PAGE_SIZE = 10000;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "named-graph-id")
    private String namedGrapheId;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "endpoint-url")
    private String endpointUrl;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "page-size")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    @Override
    ExecutionContext executeSelf() {

        Model outputModel = ModelFactory.createDefaultModel();

        int offset = 0;
        while (true) {
            LOG.debug("Executing query for offset: {}", offset);
            Model model = executeQuery(offset);
            if (model.isEmpty()) {
                break;
            } else {
                outputModel.add(model);
            }
            offset += getPageSize();
        }

        return ExecutionContextFactory.createContext(outputModel);
    }

    private Model executeQuery(int offset) {
        String query = prepareQuery(offset);
        return QueryExecutionFactory.sparqlService(endpointUrl, query).execConstruct();
    }

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

    private String getInnerSelect() {
        // TODO do we needed ordering ? ORDER BY ASC(?s) ASC(?p) ASC(?o)
        if(this.namedGrapheId == null){
            return "SELECT ?s ?p ?o WHERE { GRAPH <" + this.namedGrapheId + "> { ?s ?p ?o } }";
        }else{
            return "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";
        }
    }

    private String getOuterConstruct() {
        return "?s ?p ?o";
    }

    private String prepareQuery(int offset) {
        return "CONSTRUCT {\n" +
            getOuterConstruct() + "\n } WHERE { {" +
            getInnerSelect() +
            "\n} }" +
            "\nLIMIT " + Integer.toString(this.getPageSize()) +
            "\nOFFSET " + Integer.toString(offset);
    }
}
