package cz.cvut.sempipes.modules;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * Created by Miroslav Blasko on 6.6.17.
 */
class SparqlServiceHelper {

    Model getModel(String serviceUrl) {
        Query query = QueryFactory.create("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
        QueryEngineHTTP qe = QueryExecutionFactory.createServiceRequest(serviceUrl, query);
        return qe.execConstruct();
    }
}
