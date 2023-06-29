package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class Rdf4jUpdateModuleTest {

    private static String query = "DELETE {\n" +
        "\t<http://example.org/people/john>  <http://example.org/people/age> ?oldAge .\n" +
        "}\n" +
        "INSERT {\n" +
        "\t<http://example.org/people/john>  <http://example.org/people/age> ?newAge .\n" +
        "} WHERE {\n" +
        "   OPTIONAL {\n" +
        "      <http://example.org/people/john>  <http://example.org/people/age> ?oldAge .\n" +
        "   }\n" +
        "   BIND(COALESCE(?oldAge+1, 1) as ?newAge)\n" +
        "}";

    @Test
    @Disabled
    public void testUpdateEmpty() {
        final ExecutionContext inputExecutionContext = ExecutionContextFactory.createEmptyContext();
        Model configModel = ModelFactory.createDefaultModel();

        final Rdf4jUpdateModule moduleRdf4j = new Rdf4jUpdateModule();
        final Resource root = configModel.createResource();
        configModel.add(root, Rdf4jDeployModule.P_RDF4J_SERVER_URL, "http://localhost:8080/rdf4j-server/");
        configModel.add(root, Rdf4jDeployModule.P_RDF4J_REPOSITORY_NAME, "mb-test");
        configModel.add(root, SML.updateQuery, Rdf4jUpdateModule.createUpdateQueryResource(configModel, query));
        configModel.write(System.out, FileUtils.langTurtle, null);
        moduleRdf4j.setConfigurationResource(root);
        moduleRdf4j.setInputContext(inputExecutionContext);
        moduleRdf4j.execute();
    }
}