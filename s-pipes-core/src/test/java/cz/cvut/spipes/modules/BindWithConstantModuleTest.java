package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.ExecutionEngine;
import cz.cvut.spipes.engine.ExecutionEngineFactory;
import cz.cvut.spipes.engine.PipelineFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import java.util.List;
import java.util.Optional;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

public class BindWithConstantModuleTest extends AbstractCoreModuleTestHelper {

    @Override
    public String getModuleName() {
        return "bind-with-constant";
    }

    @Test
    public void executeWithSimpleValue() {

        String outputVariable = "name";
        RDFNode variableValue = ResourceFactory.createStringLiteral("James");

        BindWithConstantModule module = new BindWithConstantModule();

        module.setInputContext(ExecutionContextFactory.createEmptyContext());
        module.setOutputVariable(outputVariable);
        module.setValue(variableValue);

        ExecutionContext context = module.executeSelf();

        assertFalse(context.getVariablesBinding().isEmpty(), "Output binding of the module is empty.");
        assertEquals(variableValue, context.getVariablesBinding().getNode(outputVariable), "Output binding does not contain correct value.");
    }

    @Test
    public void executeWithBindedValue() throws Exception {

        OntModel ontModel = getConfigOntModel();

        List<Module> moduleList = PipelineFactory.loadPipelines(ontModel);
        assertEquals(moduleList.size(), 1, "Bad number of output modules");

        Module module = moduleList.get(0);

        System.out.println("Root module of pipeline is " + module);

        ExecutionEngine e = ExecutionEngineFactory.createEngine();
        ExecutionContext context = ExecutionContextFactory.createContext(new VariablesBinding("name", ResourceFactory.createPlainLiteral("Miroslav")));

        ExecutionContext newContext = e.executePipeline(module,context);

        assertEquals(
                "Hello Miroslav",
                Optional.ofNullable(newContext.getVariablesBinding().getNode("greetingMessage")).map(RDFNode::toString).orElse(null), "Output variable binding of this module is not correct");
    }


}