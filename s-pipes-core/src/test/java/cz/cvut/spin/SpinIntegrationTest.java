 package cz.cvut.spin;

import cz.cvut.spipes.engine.PipelineFactory;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.SPINExpressions;
import org.topbraid.spin.vocabulary.SP;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpinIntegrationTest {


    @Test
    public void executeSPINExpressionWithCustomSpinFunction() throws UnsupportedEncodingException {

        // load custom function definition
        Model funcDefModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        // Model funcDefModel = ModelFactory.createDefaultModel(); // TODO this does not work

        final InputStream funcDefIs = this.getClass().getResourceAsStream("/spin/spin-function.spin.ttl");

        funcDefModel.read(funcDefIs, null, FileUtils.langTurtle);

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
        String reportGraphId = "http://graphid.org";
        bindings.add("repositoryUrl", ResourceFactory.createPlainLiteral(repositoryUrl));
        bindings.add("reportGraphId", ResourceFactory.createPlainLiteral(reportGraphId));


        RDFNode node = SPINExpressions.evaluate(callExpr, callExpr.getModel(), bindings); //TODO resource.getModel() should be part o context


        assertEquals(node.toString(), repositoryUrl + "?default-graph-uri=" + URLEncoder.encode(reportGraphId, "UTF-8") );
    }

    @Test
    public void executeSPINQueryWithCustomJavaFunction() {
        PipelineFactory pipelineFactory = new PipelineFactory();

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

        if (results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            assertEquals(soln.getLiteral("nextDay").getString(), "2022-01-02");
        }
    }
}
