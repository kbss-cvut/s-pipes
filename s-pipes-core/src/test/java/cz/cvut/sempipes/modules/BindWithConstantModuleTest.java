package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.ExecutionEngine;
import cz.cvut.sempipes.engine.ExecutionEngineFactory;
import cz.cvut.sempipes.engine.PipelineFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import java.util.List;
import java.util.Optional;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 * Created by Miroslav Blasko on 1.6.16.
 */
public class BindWithConstantModuleTest extends AbstractModuleTestHelper {

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

        assertFalse("Output binding of the module is empty.", context.getVariablesBinding().isEmpty());
        assertEquals("Output binding does not contain correct value.", variableValue, context.getVariablesBinding().getNode(outputVariable));
    }

    @Test
    public void executeWithBindedValue() throws Exception {

        OntModel ontModel = getConfigOntModel();

        List<Module> moduleList = PipelineFactory.loadPipelines(ontModel);
        assertEquals("Bad number of output modules", moduleList.size(), 1);

        Module module = moduleList.get(0);

        System.out.println("Root module of pipeline is " + module);

        ExecutionEngine e = ExecutionEngineFactory.createEngine();
        ExecutionContext context = ExecutionContextFactory.createContext(new VariablesBinding("name", ResourceFactory.createPlainLiteral("Miroslav")));

        ExecutionContext newContext = e.executePipeline(module,context);

        assertEquals("Output variable binding of this module is not correct",
                "Hello Miroslav",
                Optional.ofNullable(newContext.getVariablesBinding().getNode("greetingMessage")).map(RDFNode::toString).orElse(null));
    }


};