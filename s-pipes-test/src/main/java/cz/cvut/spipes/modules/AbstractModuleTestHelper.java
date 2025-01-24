package cz.cvut.spipes.modules;

import cz.cvut.spipes.test.JenaTestUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This is helper class to write tests that load configuration of modules from ttl file
 * that is organized in directory `test/resources/module/${moduleName}/`.
 *
 * The `${moduleName}` is taken from abstract method <code>getModuleName()</code>.
 * The configuration file should be located at `test/resources/module/${moduleName}/${fileName}`
 * and to load the module one can call method  <code>getRootModule(fileName)</code>.
 *
 * ${fileName} (e.g. config.ttl) must contain only one module and must include declaration of the module type.
 * Example `./test/module/apply-construct/config.ttl`:
 *
 * <pre><code>
 *
 * @prefix apply-construct: <http://onto.fel.cvut.cz/ontologies/test/apply-construct#> .
 * @prefix owl: <http://www.w3.org/2002/07/owl#> .
 * @prefix sp: <http://spinrdf.org/sp#> .
 * @prefix sml: <http://topbraid.org/sparqlmotionlib#> .
 *
 * # Declare module type (alternatively you can import it using owl:imports)
 * sml:ApplyConstruct a sm:Module .
 *
 * # configure the module
 * apply-construct:create-sample-triples
 *   a sml:ApplyConstruct ;
 *   sml:constructQuery [
 *       a sp:Construct ;
 *       sp:text """CONSTRUCT {
 *     <http://example.org> rdfs:label "label 1" .
 *     <http://example.org> rdfs:label "label 2" .
 * }
 * WHERE {
 * }""" ;
 *   ] ;
 * .
 *
 * <http://onto.fel.cvut.cz/ontologies/test/apply-construct/config>
 *   a owl:Ontology ;
 *   owl:imports <http://onto.fel.cvut.cz/ontologies/s-pipes> ;
 * .
 *
 * </code></pre>
 *
 */
public abstract class AbstractModuleTestHelper {

    private static final String MODULE_DIR_NAME = "module";
    private static final String CONFIG_FILE_NAME = "config.ttl";


    /**
     * @return Name of the directory within test resources/modules folder.
     */
    abstract String getModuleName();

    abstract Object getSingleModule(OntModel configModel);

    public OntModel getConfigOntModel() {
        return getOntModel(CONFIG_FILE_NAME);
    }

    public OntModel getOntModel(String fileName) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        // set external context
        //OntDocumentManager dm = OntDocumentManager.getInstance();
        //dm.setFileManager( FileManager.get() );
        //LocationMapper lm= FileManager.get().getLocationMapper();
        //dm.loadImports(ontModel);

        JenaTestUtils.mapLocalSPipesDefinitionFiles();

        // load config
        ontModel.read(
                getClass().getResourceAsStream(getRelativeFilePath(fileName)), null, FileUtils.langTurtle);

        return ontModel;
    }

    public Model getModel(String fileName) {
        Model model = ModelFactory.createDefaultModel();

        model.read(
            getClass().getResourceAsStream(getRelativeFilePath(fileName)), null, FileUtils.langTurtle);

        return model;
    }

    /**
     * Returns module from a default configuration file located are `test/resources/module/${moduleName}/config.ttl`,
     * where ${moduleName} is returned by <code>getModuleName()</code>.
     *
     * @return Returns loaded module.
     */
    public Object getConfigRootModule() {
        return getRootModule(CONFIG_FILE_NAME);
    }

    /**
     * Returns module from a provided configuration file.
     *
     * @param fileName of ttl file (e.g. config.ttl) to load module configuration.
     *                 It should be located at <code>test/resources/module/${fileName}</code>.
     * @return Returns loaded module from the given file.
     */
    public Object getRootModule(String fileName) {
        OntModel configModel = getOntModel(fileName);

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        ontModel.read(
                getClass().getResourceAsStream(getConfigFilePath()), null, FileUtils.langTurtle);

        return getSingleModule(configModel);
    }

    public Path getFilePath(String fileName) throws URISyntaxException {
        return Paths.get(getClass().getResource(getRelativeFilePath(fileName)).toURI());
    }

    private String getRelativeFilePath(String fileName) {
        return "/" + MODULE_DIR_NAME + "/" + getModuleName() + "/" + fileName;
    }

    private String getConfigFilePath() {
        return "/" + MODULE_DIR_NAME + "/" + getModuleName() + "/" + CONFIG_FILE_NAME;
    }
}
