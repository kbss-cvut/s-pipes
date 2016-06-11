package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.PipelineFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Created by Miroslav Blasko on 12.5.16.
 */
public class ApplyConstructModuleTest extends AbstractModuleTestHelper {

    @Before
    public void setUp() {

    }

    @Override
    String getModuleName() {
        return "apply-construct";
    }

    @Test
    public void execute()  {

        ApplyConstructModule module = (ApplyConstructModule) getConfigRootModule();

        module.loadConfiguration();

        module.setExecutionContext(ExecutionContextFactory.createContext(createSimpleModel()));

        ExecutionContext newContext = null;

        // isReplace = true
        module.setReplace(true);
        newContext = module.execute();
        Assert.assertEquals(newContext.getDefaultModel().listStatements().toList().size(), 2);

        // isReplace = true
        module.setReplace(false);
        newContext = module.execute();
        Assert.assertEquals(newContext.getDefaultModel().listStatements().toList().size(), 3);

        //newContext.getDefaultModel().write(System.out, FileUtils.langTurtle);
    }

    private Model createSimpleModel() {
        Model model = ModelFactory.createDefaultModel();
        model.add(
                model.getResource("http://example.org"),
                RDFS.label,
                ResourceFactory.createPlainLiteral("illustration")
        );
        return model;
    }

}