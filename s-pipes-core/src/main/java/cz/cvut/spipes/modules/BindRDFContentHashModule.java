package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SM;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import cz.cvut.spipes.util.JenaUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindRDFContentHashModule extends AnnotatedAbstractModule {

    private static final Logger log = LoggerFactory.getLogger(BindRDFContentHashModule.class);
    @Parameter(iri =  SM.outputVariable)
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
        return KBSS_MODULE.uri + "bind-rdf-content-hash";
    }

}
