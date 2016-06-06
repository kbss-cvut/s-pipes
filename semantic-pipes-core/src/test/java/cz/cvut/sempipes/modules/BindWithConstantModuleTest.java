package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.system.stream.LocationMapper;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.SPINUtil;
import org.topbraid.spin.util.SPLUtil;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by Miroslav Blasko on 1.6.16.
 */
public class BindWithConstantModuleTest {
    @Test
    public void execute() throws Exception {

        // set external context
        OntModel ontModel = ModelFactory.createOntologyModel();

        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.setFileManager( FileManager.get() );
        //LocationMapper lm= FileManager.get().getLocationMapper();

        // load config
        ontModel.read(getClass().getResourceAsStream("/bind-with-constant-module/config.ttl"), null, FileUtils.langTurtle);

        dm.loadImports(ontModel);

        List<Module> moduleList = PipelineFactory.loadPipelines(ontModel);
        assertTrue(moduleList.size() == 1);

        Module module = moduleList.get(0);

        System.out.println("Root module of pipeline is " + module);


        ExecutionContext newContext = module.execute(ExecutionContextFactory.createContext(ontModel));

        newContext.getDefaultModel().write(System.out, FileUtils.langTurtle);






        }

};