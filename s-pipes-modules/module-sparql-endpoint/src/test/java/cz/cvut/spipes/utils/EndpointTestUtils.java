package cz.cvut.spipes.utils;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;

public class EndpointTestUtils {


    public static long getNumberOfTriples(String sparqlEndpointUrl, String namedGraphUri) {
        String query = "SELECT (COUNT(*) as ?count) WHERE { GRAPH <" + namedGraphUri + "> { ?s ?p ?o }}";
        ResultSet rs = QueryExecutionFactory.sparqlService(sparqlEndpointUrl, query).execSelect();
        return Long.parseLong(rs.next().get("count").asLiteral().getString());
    }

}
