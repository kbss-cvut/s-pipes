package cz.cvut.spipes.test;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;

import java.io.InputStream;

public class JenaTestUtils {


    public static OntModel loadOntologyClosureFromResources(String path) {
        // set external context
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.setFileManager( FileManager.get() );
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
}
