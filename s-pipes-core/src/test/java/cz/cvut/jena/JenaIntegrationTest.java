package cz.cvut.jena;

import cz.cvut.spipes.engine.PipelineFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JenaIntegrationTest {

    @Test
    public void executeQueryWithCustomJavaFunction() {

        PipelineFactory.registerFunctionsOnClassPath();

        String queryString = """
                PREFIX kbss-timef: <http://onto.fel.cvut.cz/ontologies/lib/function/time/>
                PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                SELECT ?nextDay
                WHERE {
                  BIND(kbss-timef:add-days("2022-01-01"^^xsd:date, 1) AS ?nextDay)
                }
                """;
        Model model = ModelFactory.createDefaultModel();

        Query query = QueryFactory.create(queryString);

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();

            assertTrue(results.hasNext(), "No results found");

            QuerySolution soln = results.nextSolution();
            assertEquals("2022-01-02", soln.getLiteral("nextDay").getString());
        }
    }

}
