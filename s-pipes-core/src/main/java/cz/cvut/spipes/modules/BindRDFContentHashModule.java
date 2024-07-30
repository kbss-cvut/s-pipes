package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SM;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import cz.cvut.spipes.util.JenaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class BindRDFContentHashModule extends AbstractModule {

    String outputVariable;

    @Override
    public ExecutionContext executeSelf() {

        Model model = executionContext.getDefaultModel();

        String computedHash = JenaUtils.computeHash(model);

        VariablesBinding variablesBinding = new VariablesBinding(
                outputVariable,
                ResourceFactory.createPlainLiteral(computedHash)
        );

        return ExecutionContextFactory.createContext(executionContext.getDefaultModel(), variablesBinding);
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.getURI() + "bind-rdf-content-hash";
    }

    @Override
    public void loadConfiguration() {
        outputVariable = getStringPropertyValue(SM.outputVariable);
    }
}
