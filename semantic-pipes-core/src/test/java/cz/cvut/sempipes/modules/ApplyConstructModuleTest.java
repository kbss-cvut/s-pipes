package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Miroslav Blasko on 12.5.16.
 */
public class ApplyConstructModuleTest {

    @Before
    public void setUp() {

    }


    @Test
    public void execute() throws Exception {

        // set external context
        Model model = ModelFactory.createDefaultModel();

        // load config
        model.read(getClass().getResourceAsStream("/apply-construct-module/standard-query-config.ttl"), null, FileUtils.langTurtle);

        Resource moduleRes = model.listResourcesWithProperty(
                RDF.type,
                SML.ApplyConstruct
        ).nextResource();

        Module module = PipelineFactory.loadModule(moduleRes);

        ExecutionContext newContext = module.execute(ExecutionContextFactory.createContext(model));

        newContext.getDefaultModel().write(System.out, FileUtils.langTurtle);
        // check results
    }

}