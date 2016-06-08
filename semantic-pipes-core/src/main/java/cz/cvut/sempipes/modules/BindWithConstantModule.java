package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.SM;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Created by Miroslav Blasko on 28.5.16.
 */
public class BindWithConstantModule extends AbstractModule  {

    String outputVariable;
    RDFNode value;

    @Override
    public ExecutionContext execute() {

        return ExecutionContextFactory.createContext(
                executionContext.getDefaultModel(),
                new VariablesBinding(outputVariable, value));
    }

    @Override
    public void loadConfiguration() {
        value = getEffectiveValue(SML.value);
        outputVariable = getStringPropertyValue(SM.outputVariable);
    }

    public String getOutputVariable() {
        return outputVariable;
    }

    public void setOutputVariable(String outputVariable) {
        this.outputVariable = outputVariable;
    }

    public RDFNode getValue() {
        return value;
    }

    public void setValue(RDFNode value) {
        this.value = value;
    }
}
