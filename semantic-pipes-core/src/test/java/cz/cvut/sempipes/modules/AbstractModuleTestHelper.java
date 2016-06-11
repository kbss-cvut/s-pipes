package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.PipelineFactory;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;

/**
 * Created by Miroslav Blasko on 8.6.16.
 */
public abstract class AbstractModuleTestHelper {

    private static final String MODULE_DIR_NAME = "module";
    private static final String CONFIG_FILE_NAME = "config.ttl";


    /**
     * @return Name of the directory within test resources/modules folder.
     */
    abstract String getModuleName();

    public OntModel getConfigOntModel() {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        // set external context
        //OntDocumentManager dm = OntDocumentManager.getInstance();
        //dm.setFileManager( FileManager.get() );
        //LocationMapper lm= FileManager.get().getLocationMapper();
        //dm.loadImports(ontModel);


        // load config
        ontModel.read(
                getClass().getResourceAsStream(getConfigFilePath()), null, FileUtils.langTurtle);

        return ontModel;
    }

    public Module getConfigRootModule() {
        OntModel configModel = getConfigOntModel();

        return PipelineFactory.loadPipelines(configModel).get(0);
    }


    private String getConfigFilePath() {
        return "/" + MODULE_DIR_NAME + "/" + getModuleName() + "/" + CONFIG_FILE_NAME;
    }
}
