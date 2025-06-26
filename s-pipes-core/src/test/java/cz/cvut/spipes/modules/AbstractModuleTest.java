package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.PipelineFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractModuleTest extends AbstractCoreModuleTestHelper {

    @Override
    String getModuleName() {
        return "abstract";
    }

    @Test
    public void getEffectiveValueReturnsComputedValue() throws Exception {
        Module module = PipelineFactory.loadPipelines(getConfigOntModel()).get(0);

        assertEquals(BindWithConstantModule.class, module.getClass(), "Incorrect module loaded.");

        VariablesBinding variablesBinding = new VariablesBinding();
        variablesBinding.add("name", ResourceFactory.createPlainLiteral("James"));

        module.setInputContext(
                ExecutionContextFactory.createContext(ModelFactory.createDefaultModel(), variablesBinding));

        RDFNode node = ((AbstractModule) module).getEffectiveValue(SML.JENA.value);

        assertEquals(node, ResourceFactory.createPlainLiteral("Hello James"), "Effective value computed incorrectly.");

    }

    @Disabled
    @Test
    public void throwValidationExceptionIfValidationConstrainFailsAndExitOnErrorIsTrue() {
//        AbstractModule m = createModuleWithFailingValidationConstraint();
//
//        m.setInputContext(ExecutionContextFactory.createEmptyContext());
//        m.checkInputConstraints();
    }

    @Disabled
    @Test
    public void throwNoValidationExceptionIfValidationConstrainFailsAndExitOnErrorIsFalse() {

    }


}