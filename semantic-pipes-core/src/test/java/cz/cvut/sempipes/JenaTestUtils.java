package cz.cvut.sempipes;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;

/**
 * Created by Miroslav Blasko on 7.6.16.
 */
public class JenaTestUtils {

    public static OntModel loadOntologyResource(String path) {
        // set external context
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.setFileManager( FileManager.get() );
        //LocationMapper lm= FileManager.get().getLocationMapper();

        // load config
        ontModel.read(JenaTestUtils.class.getResourceAsStream(path), null, FileUtils.langTurtle);

        dm.loadImports(ontModel);
        return ontModel;
    }
}
