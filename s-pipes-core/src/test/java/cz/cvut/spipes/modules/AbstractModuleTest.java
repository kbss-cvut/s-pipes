package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.PipelineFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by Miroslav Blasko on 8.6.16.
 */
public class AbstractModuleTest extends AbstractModuleTestHelper {

    @Override
    String getModuleName() {
        return "abstract";
    }

    @Test
    public void getEffectiveValue() throws Exception {
        Module module = PipelineFactory.loadPipelines(getConfigOntModel()).get(0);

        assertEquals("Incorrect module loaded.", BindWithConstantModule.class, module.getClass());

        VariablesBinding variablesBinding = new VariablesBinding();
        variablesBinding.add("name", ResourceFactory.createPlainLiteral("James"));

        module.setInputContext(
                ExecutionContextFactory.createContext(ModelFactory.createDefaultModel(), variablesBinding));

        RDFNode node = ((AbstractModule) module).getEffectiveValue(SML.value);

        assertEquals("Effective value computed incorrectly.", node, ResourceFactory.createPlainLiteral("Hello James"));

    }

    @Ignore
    @Test
    public void throwValidationExceptionIfValidationConstrainFailsAndExitOnErrorIsTrue() {
//        AbstractModule m = createModuleWithFailingValidationConstraint();
//
//        m.setInputContext(ExecutionContextFactory.createEmptyContext());
//        m.checkInputConstraints();
    }

    @Ignore
    @Test
    public void throwNoValidationExceptionIfValidationConstrainFailsAndExitOnErrorIsFalse() {

    }


}