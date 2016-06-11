package cz.cvut.sempipes.manager;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;
import org.apache.jena.util.LocationMapper;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by Miroslav Blasko on 10.6.16.
 */
public class OntologyManagerTest {


    @Test
    public void getAllBaseIris() throws Exception {
        Path dirPath = OntologyManager.getPathFromResource("/etc");

        Map<String, String> map = OntologyManager.getAllBaseIris(dirPath);

        assertEquals(map.size(), 6);
    }

    //@Test
    public void loadAll() throws Exception {
        String resourcePath = "/etc/spin.rdf";

        InputStream inputStream = OntologyManager.class.getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IllegalArgumentException("Resource " + resourcePath + " not found.");
        }
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.setFileManager(FileManager.get());
        //LocationMapper lm= FileManager.get().getLocationMapper();

        Model spModel = loadSPModel();
        String x =  getOntologyURI();

        dm.getFileManager().addCacheModel("http://spinrdf.org/sp", spModel);
        dm.addModel("http://spinrdf.org/sp", spModel);
        dm.setCacheModels(true);
        LocationMapper lm = new LocationMapper();
        lm.addAltPrefix("http://spinrdf.org/sp", "/home/blcha/projects/semantic-pipelines/semantic-pipes/semantic-pipes-core/src/test/resources" + "/etc/sp.rdf");
        dm.getFileManager().setLocationMapper(lm);


        // load config
        Model model = ontModel.read(inputStream, null, FileUtils.langXML);




       // OntModel model2 = null;
       // model2.getSpecification().getBaseModelMaker()


        //dm.loadImports(ontModel);

    }

    Model loadSPModel() {
        String resourcePath = "/etc/sp.rdf";
        InputStream inputStream = OntologyManager.class.getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IllegalArgumentException("Resource " + resourcePath + " not found.");
        }
        Model model = ModelFactory.createDefaultModel();

        model.read(inputStream, null, FileUtils.langXML);
        return model;
    }

    String getOntologyURI() {
        Resource res = loadSPModel().listResourcesWithProperty(RDF.type, OWL.Ontology).nextResource();
        return res.toString();
    }


}