package cz.cvut.shacl;

import cz.cvut.spipes.constants.SML;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionEnvBase;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.topbraid.shacl.arq.SHACLFunctions;
import cz.cvut.spipes.spin.vocabulary.SP;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SHACLIntegrationTest {

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
    public void executeExpressionWithCustomSHACLRDFFunction()  {
        // load custom function definition from RDF
        Model funcDefModel = getCustomSHACLRDFFunctionModel();

        // register custom function
        SHACLFunctions.registerFunctions(funcDefModel);

        // load custom function call
        Model funcCallModel = ModelFactory.createDefaultModel();

        final InputStream funcCallIs = this.getClass().getResourceAsStream("/shacl/shacl-function-call.ttl");

        funcCallModel.read(funcCallIs, null, FileUtils.langTurtle);

        RDFNode call = funcCallModel
                .listStatements(null, funcCallModel.getProperty(SML.value),(RDFNode)null).nextStatement()
                .getObject().asResource().listProperties(SP.expression).nextStatement().getObject();

        PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefixes(funcCallModel.getNsPrefixMap());
//        NodeExpression callExpr = NodeExpressionFactory.get().create(call); // TODO - potential alternative
//        Expr callExpr = SSE.parseExpr("kbss-spif:create-sparql-service-url(?sparqlEndpoint, ?defaultGraphUri)", pm);
        Expr callExpr = ExprUtils.parse(call.toString(), pm);


        // evaluate expression
        BindingMap bindings = BindingFactory.create();
        String repositoryUrl = "http://repository.org";
        String graphId = "http://graphid.org";
        bindings.add(Var.alloc("sparqlEndpoint"), NodeFactory.createLiteral(repositoryUrl));
        bindings.add(Var.alloc("defaultGraphUri"), NodeFactory.createLiteral(graphId));

        FunctionEnv fe = new FunctionEnvBase(null, null, DatasetFactory.create().asDatasetGraph());

        NodeValue nodeValue = callExpr.eval(bindings, fe);

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
}
