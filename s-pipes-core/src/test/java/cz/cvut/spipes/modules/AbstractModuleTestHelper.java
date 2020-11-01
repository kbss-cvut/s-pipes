package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.PipelineFactory;
import cz.cvut.spipes.test.JenaTestUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;

public abstract class AbstractModuleTestHelper {

    private static final String MODULE_DIR_NAME = "module";
    private static final String CONFIG_FILE_NAME = "config.ttl";


    /**
     * @return Name of the directory within test resources/modules folder.
     */
    abstract String getModuleName();

    public OntModel getConfigOntModel() {
        JenaTestUtils.mapLocalSPipesDefinitionFiles();
        return getOntModel(CONFIG_FILE_NAME);
    }

    public OntModel getOntModel(String fileName) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        // set external context
        //OntDocumentManager dm = OntDocumentManager.getInstance();
        //dm.setFileManager( FileManager.get() );
        //LocationMapper lm= FileManager.get().getLocationMapper();
        //dm.loadImports(ontModel);


        // load config
        ontModel.read(
                getClass().getResourceAsStream(getFilePath(fileName)), null, FileUtils.langTurtle);

        return ontModel;
    }

    public Model getModel(String fileName) {
        Model model = ModelFactory.createDefaultModel();

        model.read(
            getClass().getResourceAsStream(getFilePath(fileName)), null, FileUtils.langTurtle);

        return model;
    }


    public Module getConfigRootModule() {
        return  getRootModule(CONFIG_FILE_NAME);
    }

    public Module getRootModule(String fileName) {
        OntModel configModel = getOntModel(fileName);

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        ontModel.read(
                getClass().getResourceAsStream(getConfigFilePath()), null, FileUtils.langTurtle);

        return PipelineFactory.loadPipelines(configModel).get(0);
    }

    private String getFilePath(String fileName) {
        return "/" + MODULE_DIR_NAME + "/" + getModuleName() + "/" + fileName;
    }

    private String getConfigFilePath() {
        return "/" + MODULE_DIR_NAME + "/" + getModuleName() + "/" + CONFIG_FILE_NAME;
    }
}
