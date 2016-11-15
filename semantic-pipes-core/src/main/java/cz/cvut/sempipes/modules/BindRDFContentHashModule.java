package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.constants.SM;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import cz.cvut.sempipes.util.JenaUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Select;

/**
 * Created by Miroslav Blasko on 28.5.16.
 */
public class BindRDFContentHashModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(BindRDFContentHashModule.class);
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
