package cz.cvut.spipes.utils;

import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;

public class EndpointTestUtils {


    public static long getNumberOfTriples(String sparqlEndpointUrl, String namedGraphUri) {
        String query = "SELECT (COUNT(*) as ?count) WHERE { GRAPH <" + namedGraphUri + "> { ?s ?p ?o }}";
        try(QueryExecutionHTTP qe = QueryExecutionHTTP.service(sparqlEndpointUrl, query)) {
            ResultSet rs = qe.execSelect();
            return Long.parseLong(rs.next().get("count").asLiteral().getString());
        }
    }

}
