package cz.cvut.spin;

import cz.cvut.spipes.engine.PipelineFactory;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.SPINExpressions;
import org.topbraid.spin.vocabulary.SP;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpinIntegrationTest {

    @Test
    public void executeCustomSPINRDFFunctionWithinQuery() {
        // load custom function definition from RDF
        Model funcDefModel = getCustomSPINRDFFunctionModel();

        // register custom function
        //SPINModuleRegistry.get().init();
        SPINModuleRegistry.get().registerAll(funcDefModel, null);

        String repositoryUrl = "http://repository.org";
        String graphId = "http://graphid.org";

        String queryString = String.format("""
            PREFIX kbss-spif: <http://onto.fel.cvut.cz/ontologies/lib/spin-function/>
            SELECT ?sparqlServiceUrl 
            WHERE {
            BIND(kbss-spif:create-sparql-service-url(
                "%s", 
                "%s"
            ) AS ?sparqlServiceUrl)
            }
        """, repositoryUrl, graphId);

        Model model = ModelFactory.createDefaultModel();

        Query query = QueryFactory.create(queryString);

        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet results = qexec.execSelect();

        assertTrue(results.hasNext(), "No results found");

        QuerySolution soln = results.nextSolution();
        assertEquals(
            soln.getResource("sparqlServiceUrl").getURI(),
            constructServiceUrl(repositoryUrl, graphId)
        );

    }

    @Test
    public void executeSPINExpressionWithCustomSPINRDFFunction()  {
        // load custom function definition from RDF
        Model funcDefModel = getCustomSPINRDFFunctionModel();

        // register custom function
        //SPINModuleRegistry.get().init();
        SPINModuleRegistry.get().registerAll(funcDefModel, null);

        // load custom function call
        Model funcCallModel = ModelFactory.createDefaultModel();

        final InputStream funcCallIs = this.getClass().getResourceAsStream("/spin/spin-function-call.ttl");

        funcCallModel.read(funcCallIs, null, FileUtils.langTurtle);

        Resource call = funcCallModel.listSubjectsWithProperty(SP.arg1).nextResource();

        Resource callExpr = (Resource) SPINFactory.asExpression(call);

        // evaluate SPIN expression
        QuerySolutionMap bindings = new QuerySolutionMap();
        String repositoryUrl = "http://repository.org";
        String graphId = "http://graphid.org";
        bindings.add("sparqlEndpoint", ResourceFactory.createPlainLiteral(repositoryUrl));
        bindings.add("defaultGraphUri", ResourceFactory.createPlainLiteral(graphId));

        RDFNode node = SPINExpressions.evaluate(callExpr, callExpr.getModel(), bindings); //TODO resource.getModel() should be part o context

        assertEquals(node.toString(), constructServiceUrl(repositoryUrl, graphId));
    }

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

        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet results = qexec.execSelect();

        assertTrue(results.hasNext(), "No results found");

        QuerySolution soln = results.nextSolution();
        assertEquals(soln.getLiteral("nextDay").getString(), "2022-01-02");
    }

    @NotNull
    private Model getCustomSPINRDFFunctionModel() {
        // load custom function definition
        Model funcDefModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        // Model funcDefModel = ModelFactory.createDefaultModel(); // TODO this does not work

        final InputStream funcDefIs = this.getClass().getResourceAsStream("/spin/spin-function.spin.ttl");

        funcDefModel.read(funcDefIs, null, FileUtils.langTurtle);

        return funcDefModel;
    }

    @NotNull
    private String constructServiceUrl(String repositoryUrl, String graphId) {
        return String.format("%s?default-graph-uri=%s", repositoryUrl, URLEncoder.encode(graphId, StandardCharsets.UTF_8));
    }
}
