package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.Constants;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextImpl;
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
        ApplyConstructModule module = new ApplyConstructModule();

        // set external context
        ExecutionContext context = new ExecutionContextImpl();
        Model model = ModelFactory.createDefaultModel();
        context.setDefaultModel(model);

        // load config
        model.read(getClass().getResourceAsStream("/apply-construct-module/standard-query-config.ttl"), null, FileUtils.langTurtle);

        Resource moduleRes = model.listResourcesWithProperty(
                RDF.type,
                model.createProperty(Constants.SML_APPLY_CONSTRUCT)
        ).nextResource();



        module.loadConfiguration(moduleRes);

        ExecutionContext newContext = module.execute(context);

        newContext.getDefaultModel().write(System.out, FileUtils.langTurtle);
        // check results
    }

}