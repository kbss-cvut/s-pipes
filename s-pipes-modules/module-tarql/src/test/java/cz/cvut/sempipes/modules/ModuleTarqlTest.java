package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Ignore;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ModuleTarqlTest {

    @Ignore
    @org.junit.Test
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

        Assert.assertEquals(2, module.getOutputContext().getDefaultModel().listStatements(null, RDF.type, ResourceFactory.createResource("http://onto.fel.cvut.cz/ontologies/example/model/person")).toList().size());
    }
}