package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ModuleTarqlTest {

    @Disabled
    @Test
    public void testTarqlSimple() throws Exception {
        final ModuleTarql module = new ModuleTarql();

        final Model inputModel = ModelFactory.createDefaultModel();
        final ExecutionContext executionContext = ExecutionContextFactory.createContext(inputModel);

        final Model model = ModelFactory.createDefaultModel();
        final Resource root = model.createResource();
        model.add(root, ModuleTarql.P_INPUT_FILE, new File("src/test/resources/test-1.csv").getAbsolutePath());
//        model.add(root, ModuleTarql.P_NO_HEADER, "false" );
        model.add(root, ModuleTarql.P_ONTOLOGY_IRI, "http://onto.fel.cvut.cz/ontologies/test-1");
        String contents = new String(Files.readAllBytes(Paths.get("src/test/resources/test-1.tarql")));
        model.add(root, ModuleTarql.P_TARQL_STRING, contents);

        module.setConfigurationResource(root);

        module.setInputContext(executionContext);

        module.execute();

//        module.getOutputContext().getDefaultModel().write(System.out);

        assertEquals(2, module.getOutputContext().getDefaultModel().listStatements(null, RDF.type, ResourceFactory.createResource("http://onto.fel.cvut.cz/ontologies/example/model/person")).toList().size());
    }
}