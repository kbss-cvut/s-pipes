package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SM;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindWithConstantModule extends AnnotatedAbstractModule  {

    private static final Logger log = LoggerFactory.getLogger(BindWithConstantModule.class);
    @Parameter(iri = SM.outputVariable)
    String outputVariable;

    @Parameter(iri = SML.value)
    RDFNode value;

    @Parameter(iri = SML.replace)
    private final boolean isReplace = false;

    @Override
    public ExecutionContext executeSelf() {

        VariablesBinding bindings = new VariablesBinding(outputVariable, value);

        log.debug("\tBinding {} --> {}", outputVariable, value);


        return ExecutionContextFactory.createContext(
            this.createOutputModel(isReplace, ModelFactory.createDefaultModel()),
            bindings
        );
    }

    @Override
    public String getTypeURI() {
        return SML.BindWithConstant;
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
