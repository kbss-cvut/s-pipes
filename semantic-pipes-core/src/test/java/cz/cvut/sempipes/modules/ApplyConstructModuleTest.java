package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.topbraid.spin.arq.ARQ2SPIN;

import java.util.ArrayList;
import java.util.List;


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

        module.setInputContext(ExecutionContextFactory.createContext(createSimpleModel()));
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

    @Test
    public void executeIteration()  {

        ApplyConstructModule module = (ApplyConstructModule) getConfigRootModule();

        module.setInputContext(ExecutionContextFactory.createContext(createSimpleModel()));
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

        module.setInputContext(ExecutionContextFactory.createContext(createSimpleModel(), variablesBinding));
        module.loadConfiguration();

        // isReplace = true
        module.setReplace(true);
        newContext = module.executeSelf();
        Assert.assertEquals(newContext.getDefaultModel().listStatements().toList().size(), 54);
    }

    @Test
    public void executeConstructOneIteration() {
        executeConstructIterations(1,1);

    }

    @Test
    public void executeConstructTwoIterations() {
        executeConstructIterations(2,2);

    }

    @Test
    public void executeConstructThreeIterations() {
        executeConstructIterations(3,2);
    }

    public void executeConstructIterations(int n, int expectedNumberOfResults) {
        final ApplyConstructModule m = (ApplyConstructModule) getRootModule("iteration-config.ttl");
        m.setInputContext( ExecutionContextFactory.createContext(ModelFactory.createDefaultModel()));
        m.loadConfiguration();

        m.setIterationCount(n);

        final ExecutionContext eo = m.executeSelf();

        Assert.assertEquals(expectedNumberOfResults, eo.getDefaultModel().size());
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