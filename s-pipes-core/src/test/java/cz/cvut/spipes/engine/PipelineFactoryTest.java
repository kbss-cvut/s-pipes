package cz.cvut.spipes.engine;

import cz.cvut.spipes.modules.Module;
import cz.cvut.spipes.test.JenaTestUtils;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PipelineFactoryTest {

    @Test
    public void loadPipelines() {

        JenaTestUtils.mapLocalSPipesDefinitionFiles();
        OntModel ontModel = JenaTestUtils.loadOntologyClosureFromResources("/pipeline/config.ttl");

        List<Module> moduleList = PipelineFactory.loadPipelines(ontModel);
        assertEquals(2, moduleList.size(),"Number of output modules of pipeline does not match");

//        Module module = moduleList.get(0);
//        System.out.println("Root module of pipeline is " + module);
//        ExecutionContext newContext = module.execute(ExecutionContextFactory.createContext(ontModel));
//        newContext.getDefaultModel().write(System.out, FileUtils.langTurtle);
    }

}