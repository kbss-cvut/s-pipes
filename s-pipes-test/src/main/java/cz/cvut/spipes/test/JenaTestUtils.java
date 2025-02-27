package cz.cvut.spipes.test;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RIOT;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

public class JenaTestUtils {

    private static final Logger log = LoggerFactory.getLogger(JenaTestUtils.class);

    public static void mapLocalSPipesDefinitionFiles() {
        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.setFileManager(FileManager.getInternal());
        dm.getFileManager().getLocationMapper().addAltEntry("http://onto.fel.cvut.cz/ontologies/s-pipes", "s-pipes.ttl");
        dm.getFileManager().getLocationMapper().addAltEntry("http://onto.fel.cvut.cz/ontologies/s-pipes-lib", "s-pipes-lib.ttl");
    }

    public static OntModel loadOntologyClosureFromResources(String path) {
        // set external context
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.setFileManager(FileManager.getInternal());
        mapLocalSPipesDefinitionFiles();

        //LocationMapper lm= FileManager.get().getLocationMapper();

        // load config
        InputStream inputStream = JenaTestUtils.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalArgumentException("Cannot find resource with path \"" + path + "\".");
        }
        ontModel.read(inputStream, null, FileUtils.langTurtle);

        dm.loadImports(ontModel);
        return ontModel;
    }

    public static Model laodModelFromResource(String path) {
        InputStream inputStream = JenaTestUtils.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalArgumentException("Cannot find resource with path \"" + path + "\".");
        }

        Model model = ModelFactory.createDefaultModel();

        model.read(inputStream, null, FileUtils.langTurtle);

        return model;
    }

    public static void assertIsomorphic(Model actualModel, Model expectedModel) {
        if (!actualModel.isIsomorphicWith(expectedModel)) {
            log.debug("Saving actual model ... ");
            saveModelToTemporaryFile(actualModel);
            log.debug("Saving expected model ... ");
            saveModelToTemporaryFile(expectedModel);
            fail("Actual model is not isomorphic with expected model (see additional information above).");
        }
    }

    private static void saveModelToTemporaryFile(Model model) {
        try {
            Path file = Files.createTempFile("model-output-", ".ttl");
            log.debug("Saving model to temporary file " + file.toString() + " ...");
            RDFWriter.create()
                    .source(model)
                    .lang(Lang.TTL)
                    .set(RIOT.multilineLiterals, true)
                    .output(Files.newOutputStream(file.toFile().toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
