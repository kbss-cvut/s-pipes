package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
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
    public void executeConstructOneIterationWithoutInputModel() {
        executeConstructIterations(1,1);

    }

    @Test
    public void executeConstructTwoIterationsWithoutInputModel() {
        executeConstructIterations(2,2);

    }

    @Test
    public void executeConstructThreeIterationsWithoutInputModel() {
        executeConstructIterations(3,2);
    }

    @Test
    public void executeConstructOneIterationWithModelReplace() {
        executeConstructIterations(1,1, createSimpleModel(), true);
    }

    @Test
    public void executeConstructTwoIterationWithModelReplace() {
        executeConstructIterations(2,2, createSimpleModel(), true);
    }

    @Test
    public void executeConstructOneIterationWithoutModelReplace() {
        executeConstructIterations(1,2, createSimpleModel(),false);
    }

    @Test
    public void executeConstructTwoIterationWithoutModelReplace() {
        executeConstructIterations(2,3, createSimpleModel(), false);
    }

    @Test
    @Ignore
    public void executeConstructStopsIfIterationDoesNotProduceNewTriples() {
        executeConstructIterations(100,3, createSimpleModel(), false);
        // TODO verify number of iterations with mockito
    }

    @Test
    @Ignore
    public void executeConstructWithBadSyntaxServiceCall() {
        // TODO verify 400 exception and found diagnoses
    }

    @Test
    @Ignore
    public void executeConstructWithServiceNotFoundCall() {
        // TODO verify exception and found diagnoses with simple query to check availability of the service
    }


    private void executeConstructIterations(int n, int expectedNumberOfResults) {
        executeConstructIterations(n, expectedNumberOfResults, ModelFactory.createDefaultModel(), false);
    }

    private void executeConstructIterations(int iterationCount, int expectedNumberOfResults, Model inputModel, boolean isReplace) {
        final ApplyConstructModule m = (ApplyConstructModule) getRootModule("iteration-config.ttl");
        m.setInputContext( ExecutionContextFactory.createContext(inputModel));
        m.loadConfiguration();
        m.setReplace(isReplace);

        m.setIterationCount(iterationCount);

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