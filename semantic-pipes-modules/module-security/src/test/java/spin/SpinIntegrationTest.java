package spin;

import cz.cvut.sempipes.function.EncodePassword;
import java.io.InputStream;
import static junit.framework.Assert.assertEquals;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.util.FileUtils;
import org.junit.Test;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.util.SPINExpressions;
import org.topbraid.spin.vocabulary.SP;

public class SpinIntegrationTest {

    @Test
    public void executeSPINExpressionWithCustomJavaFunction() {


        // load custom function definition
        Model funcDefModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        FunctionRegistry.get().put((new EncodePassword()).getTypeURI(), EncodePassword.class);

        // load custom function call
        Model funcCallModel = ModelFactory.createDefaultModel();

        final InputStream funcCallIs = this.getClass().getResourceAsStream("/spin/spin-function-call.ttl");

        funcCallModel.read(funcCallIs, null, FileUtils.langTurtle);

        Resource call = funcCallModel.listSubjectsWithProperty(SP.arg1).nextResource();

        Resource callExpr = (Resource) SPINFactory.asExpression(call);

        // evaluate SPIN expression
        QuerySolutionMap bindings = new QuerySolutionMap();
        String plainPassword = "plainPassword";
        bindings.add("plainPassword", ResourceFactory.createPlainLiteral(plainPassword));

        RDFNode node = SPINExpressions.evaluate(callExpr, callExpr.getModel(), bindings); //TODO resource.getModel() should be part o context

        assertEquals(node.toString(), plainPassword + "ahoj" );
    }
}
