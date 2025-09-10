package cz.cvut.shacl;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.util.SPINUtils;
import cz.cvut.spipes.util.SPipesUtil;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.topbraid.shacl.arq.SHACLFunctions;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SHACLIntegrationTest {

    @BeforeAll
    public static void init(){
        SPipesUtil.init();
    }

    @Test
    public void executeCustomSHACLRDFFunctionWithinQuery() {
        // load custom function definition from RDF
        Model funcDefModel = getCustomSHACLRDFFunctionModel();

        // register custom function
        SHACLFunctions.registerFunctions(funcDefModel);

        String repositoryUrl = "http://repository.org";
        String graphId = "http://graphid.org";

        String queryString = String.format("""
            PREFIX kbss-shaclf: <http://onto.fel.cvut.cz/ontologies/lib/shacl-function/>
            SELECT ?sparqlServiceUrl
            WHERE {
            BIND(kbss-shaclf:create-sparql-service-url(
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
    public void loadAndExecuteShaclFunctionWithPrefix(){
        // load custom function definition from RDF
        Model funcDefModel = getCustomSHACLRDFFunctionModel();

        // register custom function
        SHACLFunctions.registerFunctions(funcDefModel);

        String firstName = "John";
        String lastName = "Smith";

        String queryString = String.format("""
            PREFIX kbss-shaclf: <http://onto.fel.cvut.cz/ontologies/lib/shacl-function/>
            SELECT ?greetingMessage
            WHERE {
            BIND(kbss-shaclf:construct-greeting-message(
                "%s",
                "%s"
            ) AS ?greetingMessage)
            }
        """, firstName, lastName);

        Model model = ModelFactory.createDefaultModel();

        Query query = QueryFactory.create(queryString);

        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet results = qexec.execSelect();

        assertTrue(results.hasNext(), "No results found");

        QuerySolution soln = results.nextSolution();
        assertEquals(
                soln.getLiteral("greetingMessage").getString(),
                constructGreetingMessage(firstName, lastName)
        );
    }

    @Test
    public void executeExpressionWithCustomSHACLRDFFunction()  {
        // load custom function definition from RDF
        Model funcDefModel = getCustomSHACLRDFFunctionModel();

        // register custom function
        SPipesUtil.resetFunctions(funcDefModel);

        // load custom function call
        Model funcCallModel = ModelFactory.createDefaultModel();

        final InputStream funcCallIs = this.getClass().getResourceAsStream("/shacl/shacl-function-call.ttl");

        funcCallModel.read(funcCallIs, null, FileUtils.langTurtle);

        RDFNode call = funcCallModel
                .listStatements(null, funcCallModel.getProperty(SML.value),(RDFNode)null)
                .mapWith(Statement::getObject)
                .filterKeep(SPINUtils::isExpression)
                .next();

        // evaluate expression
        BindingBuilder bb = BindingBuilder.create();
        String repositoryUrl = "http://repository.org";
        String graphId = "http://graphid.org";
        bb.add(Var.alloc("sparqlEndpoint"), NodeFactory.createLiteralString(repositoryUrl));
        bb.add(Var.alloc("defaultGraphUri"), NodeFactory.createLiteralString(graphId));

        RDFNode nodeValue = SPINUtils.evaluate(call, bb.build());

        assertEquals(nodeValue.asNode().toString(), constructServiceUrl(repositoryUrl, graphId));
    }

    @NotNull
    private Model getCustomSHACLRDFFunctionModel() {
        // load custom function definition
        Model funcDefModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        // Model funcDefModel = ModelFactory.createDefaultModel(); // TODO this does not work

        final InputStream funcDefIs = this.getClass().getResourceAsStream("/shacl/shacl-function.shacl.ttl");

        funcDefModel.read(funcDefIs, null, FileUtils.langTurtle);

        return funcDefModel;
    }

    @NotNull
    private String constructServiceUrl(String repositoryUrl, String graphId) {
        return String.format("%s?default-graph-uri=%s", repositoryUrl, URLEncoder.encode(graphId, StandardCharsets.UTF_8));
    }

    @NotNull
    private String constructGreetingMessage(String firstName, String lastName) {
        return String.format("Hello %s %s!", firstName, lastName);
    }
}
