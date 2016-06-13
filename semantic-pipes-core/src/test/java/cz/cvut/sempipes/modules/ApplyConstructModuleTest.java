package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.PipelineFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
    public void executeSimple()  {

        ApplyConstructModule module = (ApplyConstructModule) getConfigRootModule();

        module.setExecutionContext(ExecutionContextFactory.createContext(createSimpleModel()));
        module.loadConfiguration();

        ExecutionContext newContext = null;

        // isReplace = true
        module.setReplace(true);
        newContext = module.executeSelf();
        Assert.assertEquals(newContext.getDefaultModel().listStatements().toList().size(), 2);

        // isReplace = true
        module.setReplace(false);
        newContext = module.executeSelf();
        Assert.assertEquals(newContext.getDefaultModel().listStatements().toList().size(), 3);

        //newContext.getDefaultModel().write(System.out, FileUtils.langTurtle);
    }

    @Ignore
    @Test
    public void executeConstructQueryWithVariable() {
        ApplyConstructModule module = (ApplyConstructModule) getRootModule("remote-query.ttl");


        ExecutionContext newContext = null;

        VariablesBinding variablesBinding = new VariablesBinding(
                "sampleServiceUri",
                ResourceFactory.createResource("http://martin.inbas.cz/openrdf-sesame/repositories/form-generator?default-graph-uri=http://www.inbas.cz/ontologies/reporting-tool/formGen-977414103")
        );

        module.setExecutionContext(ExecutionContextFactory.createContext(createSimpleModel(), variablesBinding));
        module.loadConfiguration();

        // isReplace = true
        module.setReplace(true);
        newContext = module.executeSelf();
        Assert.assertEquals(newContext.getDefaultModel().listStatements().toList().size(), 54);
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