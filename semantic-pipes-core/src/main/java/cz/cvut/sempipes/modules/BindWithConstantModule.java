package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.SM;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * Created by Miroslav Blasko on 28.5.16.
 */
public class BindWithConstantModule extends AbstractModule  {

    String outputVariable;
    RDFNode valueNode;

    @Override
    public ExecutionContext execute(ExecutionContext context) {

        RDFNode value = getEffectiveValue(valueNode, context);

        return ExecutionContextFactory.createContext(
                context.getDefaultModel(),
                new VariablesBinding(outputVariable, value));
    }


    @Override
    public void loadConfiguration(Resource moduleRes) {
        valueNode = getPropertyValue(SML.value);
        outputVariable = getStringPropertyValue(SM.outputVariable);
    }



//    public void setOutputVariable(String outputVariable) {
//        this.outputVariable = outputVariable;
//    }
//
//
//    public RDFNode getValue() {
//        return valueNode;
//    }
//
//    public void setValue(RDFNode spExpression) {
//        this.valueNode = value;
//    }
//
//    public void setValue(String value) {
//        valueNode = resource.getModel().createLiteral(value);
//    }
}
